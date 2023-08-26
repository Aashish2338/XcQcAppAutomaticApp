package com.xtracover.xcqc.Utilities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestTestPojo {
    @SerializedName("TestNameSend")
    @Expose
    private String TestNameSend;
    @SerializedName("resultfailed")
    @Expose
    private String resultfailed;

    public String getTestNameSend() {
        return TestNameSend;
    }

    public void setTestNameSend(String testNameSend) {
        TestNameSend = testNameSend;
    }

    public String getResultfailed() {
        return resultfailed;
    }

    public void setResultfailed(String resultfailed) {
        this.resultfailed = resultfailed;
    }
}
