package qatch.analysis;

import java.nio.file.Path;
import java.util.Map;

/**
 * Interface definition for static analysis tools.
 *
 * All tools must define how they run, how to parse the results file,
 * and how to transform the parsed data, if needed.
 */
// TODO: need to rethink .yaml interaction. Once a QM is made via a yaml config, should only the QM xml be used?
public interface ITool {

    /**
     * Run the external static analysis tool (often a binary or .exe)
     *
     * @param projectLocation
     *      Root directory location needed by the tool to perform its analysis
     *      on the given project
     * @return
     *      The location of the analysis results: often a .xml or .json file.
     *      Ideally this file should be a temporary file stored on disk only
     *      as long as is necessary.
     */
    Path analyze(Path projectLocation);

    /**
     * Update the finding objects in a (property name, measure object) mapping with the findings in a
     * (diagnostic name, diagnostic) mapping.
     *
     * @param measures
     *      An object representation of the yaml config
     * @param diagnosticFindings
     *      The resulting object representation from parsing the tool's analysis results
     * @return
     *      An object representation of the yaml config (Key: property name, Value: measure object) with all
     *      Measure.Diagnostic.Finding objects updated with the tool's findings.
     */
    // TODO: this likely belongs in a different class
    Map<String, Measure> applyFindings(Map<String, Measure> measures, Map<String, Diagnostic> diagnosticFindings);

    /**
     * Read a .yaml config file that relates properties to their associated measure, tool, and diagnostics.
     * The .yaml file should have the form:
     *      Property01Name:
     *        Tool: Tool_Name
     *        Measure: Measure_Name
     *        Diagnostics:
     *          - list
     *          - of
     *          - relevant
     *          - diagnostic names
     *      Injection:
     *        Tool: Roslynator
     *        Measure: Injection Findings
     *        Diagnostics:
     *          - SCS0001
     *          - SCS0002
     *
     * @param toolConfig
     *      Path location of the .yaml configuration
     * @return
     *      A map (Key: property name, Value: the measure object of the property) object representation of
     *      the config file.
     */
    Map<String, Measure> parseConfig(Path toolConfig);

    /**
     * Parse the analysis file generated by the tool and transform the data
     * into Diagnostic objects.
     *
     * @param toolResults
     *      The location of the output file generated by running the static analysis tool
     * @return
     *      A mapping (Key: diagnostic name, Value: diagnostic object) of the parsed diagnostics
     */
    Map<String, Diagnostic> parseAnalysis(Path toolResults);

    /**
     * Each tool should have an associated config file (likely .yaml or .xml) describing the properties, measures,
     * and diagnostics associations.
     *
     * @return
     *      The path to the config file resource on the file system
     */
    Path getConfig();

    /**
     * @return
     *      The name of the tool
     */
    String getName();
}
