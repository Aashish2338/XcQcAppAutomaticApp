package com.xtracover.xcqc.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServiceKeyData {
    @SerializedName("servicekey")
    @Expose
    private String servicekey;

    public String getServicekey() {
        return servicekey;
    }

    public void setServicekey(String servicekey) {
        this.servicekey = servicekey;
    }
}