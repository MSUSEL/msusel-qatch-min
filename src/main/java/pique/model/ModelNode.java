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

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import pique.evaluation.DefaultUtility;
import pique.evaluation.IEvaluator;
import pique.evaluation.INormalizer;
import pique.evaluation.IUtilityFunction;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract representation of a node belonging to the Quality Model
 */
public abstract class ModelNode {

    //region Fields
    @Getter @Setter @Expose
    protected String name;
    @Setter @Expose
    protected double value;  // the value this node evaluates to
    @Getter @Setter @Expose
    protected String description;
    // TODO: eventually consider new tree object that combines properties and their weight instead of relying on
    //  String name matching (not enough time for me to solve currently)
    @Getter @Expose
    protected Map<String, ModelNode> children = new HashMap<>();
    @Getter @Setter @Expose
    protected Map<String, Double> weights = new HashMap<>();
    @Getter @Setter @Expose
    protected Double[] thresholds;
    @Expose
    protected String eval_strategy;
    @Expose
    protected String normalizer;
    @Expose
    protected String utility_function;

    @Getter @Setter
    protected IEvaluator evaluatorObject;
    @Getter @Setter
    protected INormalizer normalizerObject;
    @Getter @Setter
    protected IUtilityFunction utilityFunctionObject;

    @Getter @Setter
    protected boolean visited = false;      // Use for BFS traversal

    // Constructor

    public ModelNode(String name, String description, IEvaluator evaluatorObject, INormalizer normalizerObject) {
        this.name = name;
        this.description = description;
        this.evaluatorObject = evaluatorObject;
        this.normalizerObject = normalizerObject;
        this.utilityFunctionObject = new DefaultUtility();

        this.eval_strategy    = evaluatorObject.getClass().getCanonicalName();
        this.normalizer       = normalizerObject.getClass().getCanonicalName();
        this.utility_function = utilityFunctionObject.getClass().getCanonicalName();
    }

    public ModelNode(String name, String description, IEvaluator evaluatorObject, INormalizer normalizerObject,
                     IUtilityFunction utilityFunctionObject, Map<String, Double> weights, Double[] thresholds) {
        this.name = name;
        this.description = description;
        this.evaluatorObject = evaluatorObject;
        this.normalizerObject = normalizerObject;
        this.utilityFunctionObject = utilityFunctionObject;
        if (weights != null) this.weights = weights;
        if (thresholds != null) this.thresholds = thresholds;

        this.eval_strategy    = evaluatorObject.getClass().getCanonicalName();
        this.normalizer       = normalizerObject.getClass().getCanonicalName();
        this.utility_function = utilityFunctionObject.getClass().getCanonicalName();
    }

    /**
     * Constructor for cloning.
     */
    public ModelNode(double value, String name, String description, IEvaluator evaluatorObject, INormalizer normalizerObject,
                     IUtilityFunction utilityFunctionObject, Map<String, Double> weights, Double[] thresholds, Map<String,
            ModelNode> children) {
        this.value = value;
        this.name = name;
        this.description = description;
        this.evaluatorObject = evaluatorObject;
        this.normalizerObject = normalizerObject;
        this.utilityFunctionObject = utilityFunctionObject;
        this.weights = weights;
        this.thresholds = thresholds;
        this.children = children;

        this.eval_strategy    = evaluatorObject.getClass().getCanonicalName();
        this.normalizer       = normalizerObject.getClass().getCanonicalName();
        this.utility_function = utilityFunctionObject.getClass().getCanonicalName();
    }

    //endregion


    //region Getters and setters

    public ModelNode getAnyChild() {
        ModelNode anyModelNode = getChildren().values().stream().findAny().orElse(null);
        assert anyModelNode != null;
        return anyModelNode;
    }

    public ModelNode getChild(String name) {
        return getChildren().get(name);
    }

    public void setChild(ModelNode child) {
        getChildren().put(child.getName(), child);
    }

    public void setChildren(Map<String, ModelNode> children) {
        this.children = children;
    }

    public void setChildren(Collection<ModelNode> children) {
        children.forEach(element -> getChildren().putIfAbsent(element.getName(), element));
    }

    public void setNormalizerValue(double value) {
        getNormalizerObject().setNormalizerValue(value);
    }

    public int getNumChildren() { return getChildren().size(); }

    public double getValue() {
        evaluate();
        return this.value;
    }

    public double getWeight(String modelNodeName) {
        try {
            return weights.get(modelNodeName);
        }
        // TODO (1.0): Some redesign needed to better handle quality model description where there are not yet weights,
        //  values, etc...
        catch (NullPointerException e) { return 0.0; }
    }

    public void setWeight(String modelNodeName, double value) {
        this.weights.put(modelNodeName, value);
    }

    //endregion


    //region Methods

    /**
     * A loose way of implementing the Prototype design pattern.
     * Each quality model node must describe how to clone itself.
     *
     * @return A deep clone of the current node object.
     */
    public abstract ModelNode clone();

    /**
     * TODO (1.0): Documentation
     */
    protected void evaluate() {
        setValue(getEvaluatorObject().evaluate(this));
    }

    //endregion
}
