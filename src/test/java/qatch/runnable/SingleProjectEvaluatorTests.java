package qatch.runnable;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import qatch.TestHelper;
import qatch.analysis.*;
import qatch.evaluation.Project;
import qatch.model.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

public class SingleProjectEvaluatorTests {

    private SingleProjectEvaluator spe;
    private Path PROJECT_DIR = Paths.get("src/test/resources/TestCsharpProject");
    private Path RESULTS_DIR = Paths.get("src/test/output/SingleProjEval");
    private Path QM_LOCATION = Paths.get("src/test/resources/qualityModel_test.xml");
    private Path TOOL_RESULTS = Paths.get("src/test/resources/tool_results");
    private Path TEST_OUT = Paths.get("src/test/output");

    private IAnalyzer metricsAnalyzer = new IAnalyzer() {
        @Override
        public void analyze(Path src, Path dest, PropertySet properties)  {
            try {
                Path p = Files.createTempFile(dest, "metrics", ".txt");
                p.toFile().deleteOnExit();
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        @Override
        public Path getToolsDirectory() {
            return Paths.get("src/test/resources/fake_tools/FakeTool1.metrics");
        }

        @Override
        public String getResultFileName() {
            return "Metric";
        }
    };
    private IAnalyzer findingsAnalyzer = new IAnalyzer() {
        @Override
        public void analyze(Path src, Path dest, PropertySet properties)  {
            try {
                Path p = Files.createTempFile(dest, "findings", ".txt");
                p.toFile().deleteOnExit();
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        @Override
        public Path getToolsDirectory() {
            return Paths.get("src/test/resources/fake_tools/FakeTool2.findings");
        }

        @Override
        public String getResultFileName() {
            return "Finding";
        }
    };
    private IMetricsResultsImporter mri = path -> new MetricSet();
    private IFindingsResultsImporter fri = new IFindingsResultsImporter() {
        @Override
        public IssueSet parse(Path path) throws ParserConfigurationException, IOException, SAXException {
            return TestHelper.makeIssueSet("Issue Set", TestHelper.makeIssue("Some Rule 01"));
        }
    };
    private IMetricsAggregator metricsAgg = new IMetricsAggregator() {
        @Override
        public void aggregate(Project project) {
            // do nothing
        }
    };
    private IFindingsAggregator findingsAgg = new IFindingsAggregator() {
        @Override
        public void aggregate(Project project) {
            // do nothing
        }
    };

    @Before
    public void clean() throws IOException {
        cleanTestOutput();
    }

    @Before
    public void initClass() {
        spe = new SingleProjectEvaluator();
    }

    @After
    public void afterClean() throws IOException {
        cleanTestOutput();
    }

    @Test
    public void testAggregateNormalize() {
        // TODO: test might not be worth writing due to interface call
        Assert.assertTrue(true);
    }

    @Test
    public void testEvaluate() {
        Project p = TestHelper.makeProject("Test Project");
        // temp fix to avoid QM clone problem
        p.getCharacteristics().removeCharacteristic(0);
        p.getCharacteristics().removeCharacteristic(0);

        QualityModelLoader qmImporter = new QualityModelLoader(QM_LOCATION.toString());
        QualityModel qm = qmImporter.importQualityModel();

        // TODO: add more edge cases
        p.getProperties().get(0).getMeasure().setNormValue(0.90);
        p.getProperties().get(1).getMeasure().setNormValue(0.10);

        spe.evaluate(p, qm);

        Assert.assertEquals(0.099999, p.getProperties().get(0).getEval(),0.00001);
        Assert.assertEquals(0.9, p.getProperties().get(1).getEval(),0.00001);

        Assert.assertEquals(0.42, p.getCharacteristics().get(0).getEval(), 0.00001);
        Assert.assertEquals(0.50, p.getCharacteristics().get(1).getEval(), 0.00001);

        Assert.assertEquals(0.436, p.getTqi().getEval(),0.00001);

        System.out.println("...");
    }

    @Test
    public void testExport() {
        Project proj = TestHelper.makeProject("Test Project");
        Path parentDir = Paths.get("src/test/output");
        Path p = spe.export(proj, parentDir);

        Assert.assertTrue(p.toFile().exists());
        Assert.assertTrue(p.toFile().isFile());
        Assert.assertEquals("Test Project_evalResults.json",p.getFileName().toString());
        Assert.assertEquals(1692, p.toFile().length(), 300);
    }

    @Test
    public void testGetFindingsFromImporter() throws FileNotFoundException {
        String findingFileMatch = findingsAnalyzer.getResultFileName();
        Vector<IssueSet> findings = spe.getFindingsFromImporter(TOOL_RESULTS, fri, findingFileMatch);
        Assert.assertEquals("Some Rule 01", findings.firstElement().get(0).getRuleName());
    }

    @Test
    public void testGetMetricsFromImporter() throws FileNotFoundException {
        String metricFileMatch = metricsAnalyzer.getResultFileName();
        MetricSet ms = spe.getMetricsFromImporter(TOOL_RESULTS, mri, metricFileMatch);
        Assert.assertNotNull(ms);
    }

    @Test
    public void testInitialize() {
        try {
            spe.initialize(
                    PROJECT_DIR,
                    RESULTS_DIR,
                    QM_LOCATION,
                    metricsAnalyzer,
                    findingsAnalyzer
            );
        } catch (IllegalArgumentException e) { Assert.fail(e.getMessage()); }

        try {
            spe.initialize(
                    Paths.get("src/test/resources/IDONTEXIST"),
                    Paths.get("src/test/output/SingleProjEval"),
                    Paths.get("src/test/resources/qualityModel_iso25k_csharp.xml"),
                    metricsAnalyzer,
                    findingsAnalyzer
            );
        } catch (IllegalArgumentException ignored) {  }
    }

    @Test
    public void testMakeNewQM() {
        QualityModel qm = spe.makeNewQM(QM_LOCATION);
        Assert.assertEquals("Test QM", qm.getName());
        Assert.assertNotNull(qm.getProperties());
        Assert.assertNotNull(qm.getCharacteristics());
    }

    @Test
    public void testMakeProject() {
        Project p = spe.makeProject(PROJECT_DIR);
        Assert.assertEquals("TestCsharpProject", p.getName());
        Assert.assertTrue(p.getPath().contains(PROJECT_DIR.toString()));
    }

    @Test
    public void testRunFindingsTools() {
        QualityModel qm = spe.makeNewQM(QM_LOCATION);
        Path p = spe.runFindingsTools(PROJECT_DIR, RESULTS_DIR, qm, findingsAnalyzer);
        Assert.assertTrue(p.toFile().exists());
    }

    @Test
    public void testRunMetricsTools() {
        QualityModel qm = spe.makeNewQM(QM_LOCATION);
        Path p = spe.runMetricsTools(PROJECT_DIR, RESULTS_DIR, qm, metricsAnalyzer);
        Assert.assertTrue(p.toFile().exists());
    }

    public void cleanTestOutput() throws IOException {
        FileUtils.deleteDirectory(TEST_OUT.toFile());
    }
}
