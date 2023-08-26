package com.xtracover.xcqc.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServiceResponse {
    @SerializedName("result")
    @Expose
    private String result;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("certificatecode")
    @Expose
    private String certificatecode;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("price")
    @Expose
    private String price;
    @SerializedName("testPass")
    @Expose
    private String testPass;
    @SerializedName("priceMessage")
    @Expose
    private String priceMessage;
    @SerializedName("issynced")
    @Expose
    private String issynced;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCertificatecode() {
        return certificatecode;
    }

    public void setCertificatecode(String certificatecode) {
        this.certificatecode = certificatecode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTestPass() {
        return testPass;
    }

    public void setTestPass(String testPass) {
        this.testPass = testPass;
    }

    public String getPriceMessage() {
        return priceMessage;
    }

    public void setPriceMessage(String priceMessage) {
        this.priceMessage = priceMessage;
    }

    public String getIssynced() {
        return issynced;
    }

    public void setIssynced(String issynced) {
        this.issynced = issynced;
    }
}
