package com.infosys.test.automation.reportgenerator;

import com.infosys.test.automation.constants.TestResultConstants;

import com.infosys.test.automation.reportgenerator.exceptions.FolderCreationException;
import com.infosys.test.automation.reportgenerator.exceptions.ReportCreationException;
import com.infosys.test.automation.reportgenerator.exceptions.ReportWriteException;
import com.infosys.test.automation.reportgenerator.exceptions.TestResultsParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class CSVReportGenerator implements ReportGenerator{
    private final String REPORT_FORMAT = "csv";

    @Override
    public String reportFormat() {
        return REPORT_FORMAT;
    }

    @Override
    public boolean generateReport(String testName, String reportFolder, String testResult) throws FolderCreationException, TestResultsParseException, ReportWriteException, ReportCreationException {
        File reportFile = createReportFile(testName,reportFolder);
        writeReport(reportFile,testResult);
        return false;
    }

    private boolean writeReport(File reportFile,String testResult) throws TestResultsParseException, ReportCreationException, ReportWriteException {
        FileWriter reportWriter = null;
        try {
            reportWriter = new FileWriter(reportFile);
        } catch (IOException e){
            throw new ReportCreationException("Not able to create test report due to the exception : "+e.getMessage());
        }
        JSONParser jsonParser = new JSONParser();
        JSONObject testResObj = null;
        try {
            testResObj = (JSONObject)jsonParser.parse(testResult);
        } catch (ParseException e) {
            throw new TestResultsParseException("Not able to parse test results due to the exception : "+e.getMessage());
        }
        try {
            writeHeader(reportWriter);
            writeReport(testResObj,reportWriter);
            reportWriter.flush();
            reportWriter.close();
        } catch (IOException e) {
            throw new ReportWriteException("Not able to write test result to test report due to the exception : "+e.getMessage());
        }
        return true;
    }

    private void writeReport(JSONObject testResObj, FileWriter reportWriter) {
        JSONArray testResults = (JSONArray) testResObj.get(TestResultConstants.TESTRESULTS);
        AtomicInteger testDataPos = new AtomicInteger(0);
        testResults.forEach(testResult-> {
            JSONObject resultObject = (JSONObject) testResult;
            try {
                String testData = ((JSONObject)resultObject.get(TestResultConstants.TESTDATA)).toString().replaceAll(",","-");
                reportWriter.write(TestResultConstants.TESTDATA+"_"+testDataPos.addAndGet(1)+" : "+testData+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONArray testExecResults = (JSONArray) resultObject.get(TestResultConstants.TESTEXECRESULT);
            int finalTestDataPos = testDataPos.get();
            testExecResults.forEach(testExecResult -> {
                JSONObject execResObj = (JSONObject) testExecResult;
                String testCaseName = ((String)execResObj.get(TestResultConstants.TESTCASENAME)).replaceAll("\\ +","_");
                AtomicInteger testCasePos = new AtomicInteger(0);
                JSONArray validationResults = (JSONArray) execResObj.get(TestResultConstants.VALIDATIONRESULTS);
                validationResults.forEach(validationResult -> {
                    JSONObject validationResObj = (JSONObject)validationResult;
                    String fullTestCaseName = testCaseName+"_"+ finalTestDataPos +"_"+testCasePos.addAndGet(1);
                    String testSource = (String)validationResObj.get(TestResultConstants.SOURCECOLUMN)+":"+(String)validationResObj.get(TestResultConstants.SOURCEVALUE);
                    String testTarget = (String)validationResObj.get(TestResultConstants.TARGETCOLUMN)+":"+(String)validationResObj.get(TestResultConstants.TARGETVALUE);
                    String testValResult = (String)validationResObj.get(TestResultConstants.TESTRESULT);
                    try {
                        reportWriter.write(fullTestCaseName+","+testSource+","+testTarget+","+testValResult+"\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ;
                });
            });
        });
    }

    private void writeHeader(FileWriter reportWriter) throws IOException {
        reportWriter.write("TestCaseName,TestSource,TestTarget,TestResult\n");
        reportWriter.flush();
    }

    private File createReportFile(String testName,String reportFolder) throws FolderCreationException {
        String reportFileName = createFileName(testName);
        if (!checkFolder(reportFolder)){
            try {
                createFolder(reportFolder);
            } catch (SecurityException e){
                 throw new FolderCreationException("Not able to create the report folder due to the exception : "+e.getMessage());
            }
        }
        if (checkFolder(reportFolder)){
            String reportQualifiedPath = reportFolder+"\\"+reportFileName;
            return new File(reportQualifiedPath);
        } else{
            throw new FolderCreationException("Not able to create report folder : "+reportFolder);
        }
    }

    private boolean checkFolder(String reportFolder){
        File folder = new File(reportFolder);
        return folder.exists();
    }

    private boolean createFolder(String reportFolder) throws SecurityException{
        File folder = new File(reportFolder);
        return folder.mkdirs();
    }

    private String createFileName(String testName){
        String cleansedTestName = testName.replaceAll("\\ +","_");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd_HH_mm_ss");
        Date currentTime = Calendar.getInstance().getTime();
        String date = dateFormat.format(currentTime);
        return cleansedTestName+"_"+date+".csv";
    }
}
