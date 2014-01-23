/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package com.intel.pig.udf.eval.mappings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.pig.backend.executionengine.ExecException;

/**
 * <p>
 * Represents an edge mapping used in generating property graphs
 * </p>
 * <h3>Mapping Format</h3>
 * <p>
 * Mappings are expressed as Pig maps, the mapping contains at least a
 * {@code source}, {@code target} and {@code label} keys and potentially the
 * optional {@code properties} and {@code bidirectional} keys e.g.
 * </p>
 * 
 * <pre>
 * [ 'source' # 'ssn', 'target' # 'mother', 
 *   'label' # 'mother', 
 *   'inverseLabel' # 'child', 
 *   'properties' # ( 'dob' ),
 *   'inverseProperties' # ( 'dob' ),
 *   'bidirectional' # 'false' ]
 * </pre>
 * 
 * <h4>source</h4>
 * <p>
 * The {@code source} key provides a field name which is used to extract a
 * vertex ID for the source vertex.
 * </p>
 * <h4>target</h4>
 * <p>
 * The {@code target} key provides a field name which is used to extract a
 * vertex ID for the target vertex.
 * </p>
 * <h4>label</h4>
 * <p>
 * The {@code label} key provides a label to the generated edges.
 * </p>
 * <h4>inverseLabel</h4>
 * <p>
 * The {@code inverseLabel} key provides a label to generate an inverse edge, if
 * this key is used then {@code bidirectional} is set to assumed to be false
 * unless otherwise set.
 * </p>
 * <h4>properties</h4>
 * <p>
 * The {@code properties} key provides a tuple consisting of field names which
 * should be used to add properties to the generated edge. Field names are used
 * as the resulting property names with property values taking from the actual
 * input data being mapped.
 * </p>
 * <h4>inverseProperties</h4>
 * <p>
 * The {@code inverseProperties} key provides a tuple consisting of field names
 * which should be used to add properties to the generated inverse edge. Field
 * names are used as the resulting property names with property values taking
 * from the actual input data being mapped.
 * </p>
 * <h4>bidirectional</h4>
 * <p>
 * The {@code bidirectional} key provides a {@code true} or {@code false} value
 * indicating whether the edge is bidrectional or directed. Edges default to
 * being bidirectional if this key is not used.
 * </p>
 */
public class EdgeMapping extends AbstractMapping {

    protected static final String SOURCE_FIELD = "source";
    protected static final String TARGET_FIELD = "target";
    protected static final String LABEL = "label";
    protected static final String INVERSE_LABEL = "inverseLabel";
    protected static final String BIDIRECTIONAL = "bidirectional";
    protected static final String INVERSE_PROPERTIES = "inverseProperties";

    private String sourceField, targetField, label, inverseLabel;
    private List<String> properties = new ArrayList<String>();
    private List<String> inverseProperties = new ArrayList<String>();
    private boolean bidirectional;

    /**
     * Creates a new edge mapping
     * 
     * @param source
     *            Source field
     * @param target
     *            Target field
     * @param label
     *            Label
     * @param inverseLabel
     *            Inverse Label
     * @param properties
     *            Properties
     * @param inverseProperties
     *            Inverse properties
     * @param bidirectional
     *            Whether the edge is bi-directional
     */
    public EdgeMapping(String source, String target, String label, String inverseLabel, Collection<String> properties,
            Collection<String> inverseProperties, boolean bidirectional) {
        if (source == null)
            throw new NullPointerException("Source Field for an edge mapping cannot be null");
        if (target == null)
            throw new NullPointerException("Target Field for an edge mapping cannot be null");
        if (label == null)
            throw new NullPointerException("Label for an edge mapping cannot be null");
        this.sourceField = source;
        this.targetField = target;
        this.label = label;
        this.inverseLabel = inverseLabel;
        if (properties != null)
            this.properties.addAll(properties);
        if (inverseProperties != null)
            this.inverseProperties.addAll(inverseProperties);
        this.bidirectional = bidirectional;
    }

    /**
     * Creates a new edge mapping assuming a directed edge
     * 
     * @param source
     *            Source field
     * @param target
     *            Target field
     * @param label
     *            Label
     * @param inverseLabel
     *            Inverse label
     */
    public EdgeMapping(String source, String target, String label, String inverseLabel) {
        this(source, target, label, null, null, null, false);
    }

    /**
     * Creates a new edge mapping assuming an undirected edge
     * 
     * @param source
     *            Source field
     * @param target
     *            Target field
     * @param label
     *            Label
     */
    public EdgeMapping(String source, String target, String label) {
        this(source, target, label, null, null, null, true);
    }

    /**
     * Creates a new edge mapping directly from an Object
     * <p>
     * This constructor assumes that the passed object comes from the processing
     * of a Pig script and thus will be a Map generated from Pig. See the
     * documentation for {@link EdgeMapping} for details of the map format
     * expected.
     * <p>
     * 
     * @param object
     * @throws ExecException
     */
    @SuppressWarnings("unchecked")
    public EdgeMapping(Object object) throws ExecException {
        if (object == null)
            throw new NullPointerException("Cannot create an edge mapping from a null object");
        if (!(object instanceof Map<?, ?>))
            throw new IllegalArgumentException("Cannot create an edge mapping from a non-map object");

        Map<String, Object> edgeMapping = (Map<String, Object>) object;
        this.sourceField = this.getStringValue(edgeMapping, SOURCE_FIELD, true);
        this.targetField = this.getStringValue(edgeMapping, TARGET_FIELD, true);
        this.label = this.getStringValue(edgeMapping, LABEL, true);
        this.inverseLabel = this.getStringValue(edgeMapping, INVERSE_LABEL, false);

        // When loading bidirectional from map default to true if no inverse
        // label and false if there is an inverse label
        this.bidirectional = this.getBooleanValue(edgeMapping, BIDIRECTIONAL, this.inverseLabel == null);
        List<String> ps = this.getListValue(edgeMapping, PROPERTIES, false);
        if (ps != null)
            this.properties.addAll(ps);
        ps = this.getListValue(edgeMapping, INVERSE_PROPERTIES, false);
        if (ps != null)
            this.inverseProperties.addAll(ps);
    }

    /**
     * Gets the source field
     * 
     * @return Source field
     */
    public String getSourceField() {
        return this.sourceField;
    }

    /**
     * Gets the target field
     * 
     * @return Target field
     */
    public String getTargetField() {
        return this.targetField;
    }

    /**
     * Gets the edge label
     * 
     * @return Edge label
     */
    public String getEdgeLabel() {
        return this.label;
    }

    /**
     * Gets the inverse edge label
     * 
     * @return Inverse edge label
     */
    public String getInverseEdgeLabel() {
        return this.inverseLabel;
    }

    /**
     * Gets the iterator of edge property names
     * 
     * @return Edge property names iterator
     */
    public Iterator<String> getProperties() {
        return this.properties.iterator();
    }

    /**
     * Gets the iterator of inverse edge property names
     * 
     * @return Inverse edge property names iterator
     */
    public Iterator<String> getInverseProperties() {
        return this.inverseProperties.iterator();
    }

    /**
     * Gets whether the edge is bi-directional
     * 
     * @return True if bi-directional, false otherwise
     */
    public boolean isBidirectional() {
        return this.bidirectional;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        builder.append(" '");
        builder.append(SOURCE_FIELD);
        builder.append("' # '");
        builder.append(this.sourceField);
        builder.append("', '");
        builder.append(TARGET_FIELD);
        builder.append("' # '");
        builder.append(this.targetField);
        builder.append("', '");
        builder.append(LABEL);
        builder.append("' # '");
        builder.append(this.label);
        if (this.inverseLabel != null) {
            builder.append("', '");
            builder.append(INVERSE_LABEL);
            builder.append("' # '");
            builder.append(this.inverseLabel);
        }
        builder.append("', '");
        builder.append(BIDIRECTIONAL);
        builder.append("' # '");
        builder.append(Boolean.toString(this.bidirectional).toLowerCase());
        builder.append('\'');
        if (this.properties.size() > 0) {
            builder.append(", ");
            builder.append(this.tupleToMapKeyValueString(this.properties, PROPERTIES));
        }
        if (this.inverseProperties.size() > 0) {
            builder.append(", ");
            builder.append(this.tupleToMapKeyValueString(this.inverseProperties, INVERSE_PROPERTIES));
        }
        builder.append(" ]");
        return builder.toString();
    }
}
