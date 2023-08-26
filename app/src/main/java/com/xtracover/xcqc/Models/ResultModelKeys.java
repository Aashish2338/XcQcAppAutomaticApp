package com.xtracover.xcqc.Models;

public class ResultModelKeys {
    private String TestName;
    private String TestNameSend;
    private String resultfailed;

    public String getTestName() {
        return TestName;
    }

    public void setTestName(String testName) {
        TestName = testName;
    }

    public String getResultfailed() {
        return resultfailed;
    }

    public void setResultfailed(String resultfailed) {
        this.resultfailed = resultfailed;
    }

    public String getTestNameSend() {
        return TestNameSend;
    }

    public void setTestNameSend(String testNameSend) {
        TestNameSend = testNameSend;
    }
}
