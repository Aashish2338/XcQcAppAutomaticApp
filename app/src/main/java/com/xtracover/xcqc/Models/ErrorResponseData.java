package com.xtracover.xcqc.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ErrorResponseData {

    @SerializedName("mstChildid")
    @Expose
    private Integer mstChildid;
    @SerializedName("mstid")
    @Expose
    private Integer mstid;
    @SerializedName("certificate_number")
    @Expose
    private String certificateNumber;
    @SerializedName("error_details")
    @Expose
    private String errorDetails;
    @SerializedName("test_date_time")
    @Expose
    private String testDateTime;
    @SerializedName("application_version")
    @Expose
    private String applicationVersion;

    public Integer getMstChildid() {
        return mstChildid;
    }

    public void setMstChildid(Integer mstChildid) {
        this.mstChildid = mstChildid;
    }

    public Integer getMstid() {
        return mstid;
    }

    public void setMstid(Integer mstid) {
        this.mstid = mstid;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getTestDateTime() {
        return testDateTime;
    }

    public void setTestDateTime(String testDateTime) {
        this.testDateTime = testDateTime;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }
}
