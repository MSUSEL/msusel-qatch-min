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
package pique.evaluation;

import pique.model.Measure;
import pique.model.ModelNode;

// TODO (1.0) Documentation
public class DefaultMeasureEvaluator extends Evaluator {

    /**
     * By default, a measure should evaluate as the sum of its diagnostics normalized by its normalizer.
     * Note: for measure metrics, this will likely be different functionality and should be overridden as so.
     */
    @Override
    public double evaluate(ModelNode inNode) {
        Measure node = (Measure)inNode;

        // Sum values of child diagnostics
        double value = node.getChildren().values().stream()
                .mapToDouble(ModelNode::getValue)
                .sum();

        // Normalize
        value = node.getNormalizerObject().normalize(value);

        // Apply utility function
        return node.getUtilityFunctionObject().utilityFunction(value, node.getThresholds(), node.isPositive());
    }
}
