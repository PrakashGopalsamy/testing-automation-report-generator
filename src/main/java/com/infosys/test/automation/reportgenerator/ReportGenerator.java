package com.infosys.test.automation.reportgenerator;

import com.infosys.test.automation.reportgenerator.exceptions.FolderCreationException;
import com.infosys.test.automation.reportgenerator.exceptions.ReportCreationException;
import com.infosys.test.automation.reportgenerator.exceptions.ReportWriteException;
import com.infosys.test.automation.reportgenerator.exceptions.TestResultsParseException;

public interface ReportGenerator {
    public String reportFormat();
    public boolean generateReport(String testName,String reportFolder,String testResult) throws FolderCreationException, TestResultsParseException, ReportWriteException, ReportCreationException;
}
