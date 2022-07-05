package com.infosys.test.automation.reportgenerator;

public interface ReportGenerator {
    public String reportFormat();
    public boolean generateReport(String testName,String reportFolder,String testResult) throws Exception;
}
