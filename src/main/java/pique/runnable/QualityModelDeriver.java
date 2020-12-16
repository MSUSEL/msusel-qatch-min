package pique.runnable;

import pique.analysis.ITool;
import pique.calibration.IBenchmarker;
import pique.calibration.IWeighter;
import pique.calibration.WeightResult;
import pique.model.Measure;
import pique.model.ModelNode;
import pique.model.QualityModel;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Utility driver class responible for running the calibration module's procedure.
 * This uses a benchmark repository, quality model description, directory of comparison matrices,
 * instances of language-specific analysis tools, and the RInvoker as input to perform a 3 step process of
 *     (1) Derive thresholds
 *     (2) Elicitate weights
 *     (3) Apply these results to the quality model to generate a fully derived quality model
 */
public class QualityModelDeriver {

    public static QualityModel deriveModel(QualityModel qmDesign, Set<ITool> tools,
                                           Path benchmarkRepository, String projectRootFlag) {

        // (1) Derive thresholds
        IBenchmarker benchmarker = qmDesign.getBenchmarker();
        Map<String, Double[]> measureNameThresholdMappings = benchmarker.deriveThresholds(
                benchmarkRepository, qmDesign, tools, projectRootFlag);

        // (2) Elicitate weights
        IWeighter weighter = qmDesign.getWeighter();
        // TODO (1.0): Consider, instead of weighting all nodes in one sweep here, dynamically assigning IWeighter
        //  ojbects to each node to have them weight using JIT evaluation functions.
        Set<WeightResult> weights = weighter.elicitateWeights(qmDesign);
        // TODO: assert WeightResult names match expected TQI, QualityAspect, and ProductFactor names from quality model description

        // (3) Apply results to nodes in quality model by matching names
        // Thresholds (ProductFactor nodes)
        // TODO (1.0): Support now in place to apply thresholds to all nodes (if they exist), not just measures. Just
        //  need to implement.
        measureNameThresholdMappings.forEach((measureName, thresholds) -> {
            Measure measure = (Measure)qmDesign.getMeasure(measureName);
            measure.setThresholds(thresholds);
        });

        // Weights (TQI and QualityAspect nodes)
        weights.forEach(weightResult -> {
            Map<String, ModelNode> allNodes = qmDesign.getAllQualityModelNodes();
            allNodes.get(weightResult.name).setWeights(weightResult.weights);
        });

        return qmDesign;
    }
}
