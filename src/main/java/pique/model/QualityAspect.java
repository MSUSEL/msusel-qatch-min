/**
 * MIT License
 * Copyright (c) 2019 Montana State University Software Engineering Labs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package pique.model;

import pique.evaluation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a quality characteristic of the
 * Quality Model that is used in order to evaluate a
 * project or a set of projects.
 */
public class QualityAspect extends ModelNode {

    //region Constructors

    public QualityAspect(String name, String description) {

        super(name, description, new DefaultFactorEvaluator(), new DefaultNormalizer());
    }

    public QualityAspect(String name, String description, IEvaluator evaluator, INormalizer normalizer,
                         IUtilityFunction utilityFunction, Map<String, Double> weights, Double[] thresholds) {
        super(name, description, evaluator, normalizer, utilityFunction, weights, thresholds);
    }

    public QualityAspect(String name, String description, Map<String, Double> weights) {
        super(name, description, new DefaultFactorEvaluator(), new DefaultNormalizer());
        this.weights = weights;
    }

    public QualityAspect(String name, String description, Map<String, Double> weights,
                         Map<String, ModelNode> productFactors) {
        super(name, description, new DefaultFactorEvaluator(), new DefaultNormalizer());
        this.weights = weights;
        this.children = productFactors;
    }

    // Used for cloning
    public QualityAspect(double value, String name, String description, IEvaluator evaluator, INormalizer normalizer,
               IUtilityFunction utilityFunction, Map<String, Double> weights, Double[] thresholds, Map<String,
            ModelNode> children) {
        super(value, name, description, evaluator, normalizer, utilityFunction, weights, thresholds, children);
    }

    //endregion


    //region Methods

    @Override
    public ModelNode clone() {

        Map<String, ModelNode> clonedChildren = new HashMap<>();
        getChildren().forEach((k, v) -> clonedChildren.put(k, v.clone()));

        return new QualityAspect(getValue(), getName(), getDescription(), getEvaluatorObject(), getNormalizerObject(),
                getUtilityFunctionObject(), getWeights(), getThresholds(), clonedChildren);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof QualityAspect)) {
            return false;
        }
        QualityAspect otherQualityAspect = (QualityAspect) other;

        return getName().equals(otherQualityAspect.getName())
                && getChildren().size() == otherQualityAspect.getChildren().size();
    }

    //endregion
}
