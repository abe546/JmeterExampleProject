package com.example.jmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;

public class JmeterTestPlan {
    public static final String QUERY_PARAMETER_VAR_NAME = "queryParams";

    /**
     * Used to create Jmeter test plan, also saves testplan as a .jmx file
     * in resource folder
     */
    public HashTree createTestPLan(String inputCSVFile, String domainName, String path, String httpMethod, int threadCount) throws IOException {
        JMeterUtils.setJMeterHome("target/jmeter");

        //import the jmeter properties, as is provided
        JMeterUtils.loadJMeterProperties("target/jmeter/bin/jmeter.properties");
        //Set locale
        JMeterUtils.initLocale();

        //Will be used to compose the testPlan, acts as container
        HashTree hashTree = new HashTree();

        //Going to use google.com/search?q='queryParam', load in params from CSV
        CSVDataSet csvConfig = new CSVDataSet();
        csvConfig.setProperty("filename", inputCSVFile);
        csvConfig.setComment("List of query params");
        csvConfig.setName("MarshalQueryParams");

        csvConfig.setProperty("delimiter", "\\n");

        csvConfig.setProperty("variableNames", QUERY_PARAMETER_VAR_NAME);
        csvConfig.setProperty("recycle", "false");//Recycle input on end of file (set to false)
        csvConfig.setProperty("ignoreFirstLine", "false");//Ignore first line of file
        csvConfig.setProperty("stopThread", true);//Stops thread on EOF
        csvConfig.setProperty("shareMode", "shareMode.thread");

        csvConfig.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
        csvConfig.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());

        //HTTPSampler acts as the container for the HTTP request to the site.
        HTTPSampler httpHandler = new HTTPSampler();
        httpHandler.setDomain(domainName);
        httpHandler.setProtocol("https");
        httpHandler.setPath(path);
        httpHandler.setMethod(httpMethod);
        httpHandler.setName("GoogleSearch");

        //Adding pieces to enable this to be exported to a .jmx and loaded
        //into Jmeter
        httpHandler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
        httpHandler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());

        //LoopController, handles iteration settings
        LoopController loopController = new LoopController();
        loopController.setLoops(LoopController.INFINITE_LOOP_COUNT);
        loopController.setFirst(true);
        loopController.initialize();

        //Thread groups/user count
        SetupThreadGroup setupThreadGroup = new SetupThreadGroup();
        setupThreadGroup.setName("GoogleTG");
        setupThreadGroup.setNumThreads(threadCount);
        setupThreadGroup.setRampUp(1);
        setupThreadGroup.setSamplerController(loopController);

        //Adding GUI pieces for Jmeter
        setupThreadGroup.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
        setupThreadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());

        //Create the tesPlan item
        TestPlan testPlan = new TestPlan("GoogleQueryTestPlan");
        //Adding GUI pieces for Jmeter gui
        testPlan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());

        hashTree.add(testPlan);

        HashTree groupTree = hashTree.add(testPlan, setupThreadGroup);
        groupTree.add(httpHandler);
        groupTree.add(csvConfig);

        //Save this tes plan as a .jmx for future reference
        SaveService.saveTree(hashTree, new FileOutputStream("src/main/resources/jmxFile.jmx"));

        //Added summarizer for logging meta info
        Summariser summariser = new Summariser("summaryOfResults");

        //Collect results

        ResultCollector resultCollector = new ResultCollector(summariser);

        resultCollector.setFilename("src/main/resources/Results.csv");

        hashTree.add(hashTree.getArray()[0], resultCollector);

        return hashTree;
    }

    public void engineRunner(HashTree hashTree) {
        //Create the Jmeter engine to be used (Similar to Android's GUI engine)
        StandardJMeterEngine jEngine = new StandardJMeterEngine();

        jEngine.configure(hashTree);

        jEngine.run();
    }
}
