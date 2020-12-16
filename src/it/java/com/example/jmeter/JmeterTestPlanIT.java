package com.example.jmeter;

import org.apache.jorphan.collections.HashTree;
import org.junit.Test;

import java.io.IOException;

public class JmeterTestPlanIT {
    String domainName = "google.com";
    String path = "/search?q=" + "${" + JmeterTestPlan.QUERY_PARAMETER_VAR_NAME + "}";
    String inputCSVFile = "src/it/resources/names.csv";

    @Test
    public void sendQueriesToGoogle() throws IOException {

        System.out.println("hello world");

        JmeterTestPlan testPlanClient = new JmeterTestPlan();

        HashTree createdTree = testPlanClient.createTestPLan(
                inputCSVFile,
                domainName,
                path,
                "GET",
                1);

        testPlanClient.engineRunner(createdTree);

    }
}
