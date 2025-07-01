import core.GraphViz;
import org.apache.commons.math3.distribution.NormalDistribution;
import test.*;

public class Main {
    public static void main(String[] args) {
        // Test
        System.out.println("===TestUkrainePowerGrid===");
        TestUkrainePowerGrid test1 = new TestUkrainePowerGrid();
        //test1.testSystemShutdown("Only Zero");
        //test1.testSystemShutdown("Only Probability");
        //test1.testSystemShutdown("Sample TTC");
        //test1.drawAttackGraph();

        System.out.println("===TestRansomwareImpacting===");
        TestRansomwareImpacting test2 = new TestRansomwareImpacting();
        test2.testPipelineOperationsWithRansomware();
        //test2.testPipelineAssert();
        //test2.drawAttackGraph();

        System.out.println("===TestBruteForce===");
        TestBruteForce test3 = new TestBruteForce();
        //test3.testBruteForceAttacksWithCyberActors();
        //test3.drawAttackGraph();

        //System.out.println("===TestDistributionSample===");
        // NormalDistribution normal = new NormalDistribution();
        // System.out.println(normal.sample());

        //System.out.println("===UnitTests1===");
        //test.ExampleLangTest.createLineChart();
        //CoreTest.unitTest("ALL");

        //System.out.println("===UnitTests2===");
        //test.ExampleLangTest.createLineChart();
        //CoreTest.unitTest("ALL");

        //System.out.println("===UnitTests3===");
        //test.ExampleLangTest.createLineChart();
        //CoreTest.unitTest("ALL");

        //System.out.println("===UnitTests4===");
        //test.ExampleLangTest.createLineChart();
        //CoreTest.unitTest("ALL");

        //System.out.println("===UnitTests5===");
        //test.ExampleLangTest.createLineChart();
        //CoreTest.unitTest("ALL");

        //System.out.println("===UnitTests6===");
        //test.ExampleLangTest.createLineChart();
        //CoreTest.unitTest("ALL");
    }

}
