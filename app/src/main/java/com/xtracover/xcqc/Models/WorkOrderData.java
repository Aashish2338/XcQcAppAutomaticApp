package com.xtracover.xcqc.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WorkOrderData {
    @SerializedName("MstRWid")
    @Expose
    private Integer mstRWid;
    @SerializedName("Work_Order_no")
    @Expose
    private String workOrderNo;
    @SerializedName("PartnerID")
    @Expose
    private Integer partnerID;
    @SerializedName("TotalValue")
    @Expose
    private Integer totalValue;
    @SerializedName("TotalTaxValue")
    @Expose
    private Integer totalTaxValue;
    @SerializedName("NetValue")
    @Expose
    private Integer netValue;
    @SerializedName("Preferred_date")
    @Expose
    private String preferredDate;
    @SerializedName("QC_Location_name")
    @Expose
    private Object qCLocationName;
    @SerializedName("Address")
    @Expose
    private Object address;
    @SerializedName("Landmark")
    @Expose
    private Object landmark;
    @SerializedName("QC_SPOC_name")
    @Expose
    private String qCSPOCName;
    @SerializedName("QC_SPOC_ContactNo")
    @Expose
    private String qCSPOCContactNo;
    @SerializedName("Warranty_tenure")
    @Expose
    private Integer warrantyTenure;
    @SerializedName("CreatedBy")
    @Expose
    private Integer createdBy;
    @SerializedName("IsApproved")
    @Expose
    private Boolean isApproved;
    @SerializedName("status")
    @Expose
    private Integer status;
    //    @SerializedName("estimated_Work_Order_value")
//    @Expose
//    private Float estimatedWorkOrderValue;
    @SerializedName("PartnerName")
    @Expose
    private String partnerName;
    @SerializedName("BRNTotal")
    @Expose
    private Integer bRNTotal;
    @SerializedName("BRNGST")
    @Expose
    private Integer brngst;
    @SerializedName("BRNNetValue")
    @Expose
    private Integer bRNNetValue;
    @SerializedName("PartnerCode")
    @Expose
    private String partnerCode;
    @SerializedName("MstID")
    @Expose
    private Integer mstID;
    @SerializedName("ChildMstRWid")
    @Expose
    private Integer childMstRWid;
    @SerializedName("childWork_Order_no")
    @Expose
    private String childWorkOrderNo;
    @SerializedName("Device_TypeID")
    @Expose
    private Integer deviceTypeID;
    @SerializedName("Product_TypeID")
    @Expose
    private Integer productTypeID;
    @SerializedName("Device_Qty")
    @Expose
    private Integer deviceQty;
    @SerializedName("CreatedOn")
    @Expose
    private String createdOn;
    @SerializedName("UpdatedBy")
    @Expose
    private Integer updatedBy;
    @SerializedName("IsActive")
    @Expose
    private Boolean isActive;
    @SerializedName("MobileType")
    @Expose
    private String mobileType;
    @SerializedName("Childestimated_Work_Order_value")
    @Expose
    private Integer childestimatedWorkOrderValue;
    @SerializedName("DeviceType")
    @Expose
    private String deviceType;
    @SerializedName("PlanType")
    @Expose
    private String planType;
    @SerializedName("UplodedQty")
    @Expose
    private Integer uplodedQty;
    @SerializedName("PendingQty")
    @Expose
    private Integer pendingQty;
    @SerializedName("QCStatusCount")
    @Expose
    private Integer qCStatusCount;
    @SerializedName("PassQty")
    @Expose
    private Integer passQty;
    @SerializedName("FailQty")
    @Expose
    private Integer failQty;
    @SerializedName("DeviceVal")
    @Expose
    private Integer deviceVal;
    @SerializedName("QCStatus")
    @Expose
    private Object qCStatus;
    @SerializedName("IMEI_Or_serialNo")
    @Expose
    private Object iMEIOrSerialNo;
    @SerializedName("QC_Charges")
    @Expose
    private Integer qCCharges;
    @SerializedName("TotalQC_Charges")
    @Expose
    private Integer totalQCCharges;
    @SerializedName("TotalEstimatedVal")
    @Expose
    private Integer totalEstimatedVal;

    public Integer getMstRWid() {
        return mstRWid;
    }

    public void setMstRWid(Integer mstRWid) {
        this.mstRWid = mstRWid;
    }

    public String getWorkOrderNo() {
        return workOrderNo;
    }

    public void setWorkOrderNo(String workOrderNo) {
        this.workOrderNo = workOrderNo;
    }

    public Integer getPartnerID() {
        return partnerID;
    }

    public void setPartnerID(Integer partnerID) {
        this.partnerID = partnerID;
    }

    public Integer getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Integer totalValue) {
        this.totalValue = totalValue;
    }

    public Integer getTotalTaxValue() {
        return totalTaxValue;
    }

    public void setTotalTaxValue(Integer totalTaxValue) {
        this.totalTaxValue = totalTaxValue;
    }

    public Integer getNetValue() {
        return netValue;
    }

    public void setNetValue(Integer netValue) {
        this.netValue = netValue;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    public Object getQCLocationName() {
        return qCLocationName;
    }

    public void setQCLocationName(Object qCLocationName) {
        this.qCLocationName = qCLocationName;
    }

    public Object getAddress() {
        return address;
    }

    public void setAddress(Object address) {
        this.address = address;
    }

    public Object getLandmark() {
        return landmark;
    }

    public void setLandmark(Object landmark) {
        this.landmark = landmark;
    }

    public String getQCSPOCName() {
        return qCSPOCName;
    }

    public void setQCSPOCName(String qCSPOCName) {
        this.qCSPOCName = qCSPOCName;
    }

    public String getQCSPOCContactNo() {
        return qCSPOCContactNo;
    }

    public void setQCSPOCContactNo(String qCSPOCContactNo) {
        this.qCSPOCContactNo = qCSPOCContactNo;
    }

    public Integer getWarrantyTenure() {
        return warrantyTenure;
    }

    public void setWarrantyTenure(Integer warrantyTenure) {
        this.warrantyTenure = warrantyTenure;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

//    public Float getEstimatedWorkOrderValue() {
//        return estimatedWorkOrderValue;
//    }
//
//    public void setEstimatedWorkOrderValue(Float estimatedWorkOrderValue) {
//        this.estimatedWorkOrderValue = estimatedWorkOrderValue;
//    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public Integer getBRNTotal() {
        return bRNTotal;
    }

    public void setBRNTotal(Integer bRNTotal) {
        this.bRNTotal = bRNTotal;
    }

    public Integer getBrngst() {
        return brngst;
    }

    public void setBrngst(Integer brngst) {
        this.brngst = brngst;
    }

    public Integer getBRNNetValue() {
        return bRNNetValue;
    }

    public void setBRNNetValue(Integer bRNNetValue) {
        this.bRNNetValue = bRNNetValue;
    }

    public String getPartnerCode() {
        return partnerCode;
    }

    public void setPartnerCode(String partnerCode) {
        this.partnerCode = partnerCode;
    }

    public Integer getMstID() {
        return mstID;
    }

    public void setMstID(Integer mstID) {
        this.mstID = mstID;
    }

    public Integer getChildMstRWid() {
        return childMstRWid;
    }

    public void setChildMstRWid(Integer childMstRWid) {
        this.childMstRWid = childMstRWid;
    }

    public String getChildWorkOrderNo() {
        return childWorkOrderNo;
    }

    public void setChildWorkOrderNo(String childWorkOrderNo) {
        this.childWorkOrderNo = childWorkOrderNo;
    }

    public Integer getDeviceTypeID() {
        return deviceTypeID;
    }

    public void setDeviceTypeID(Integer deviceTypeID) {
        this.deviceTypeID = deviceTypeID;
    }

    public Integer getProductTypeID() {
        return productTypeID;
    }

    public void setProductTypeID(Integer productTypeID) {
        this.productTypeID = productTypeID;
    }

    public Integer getDeviceQty() {
        return deviceQty;
    }

    public void setDeviceQty(Integer deviceQty) {
        this.deviceQty = deviceQty;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getMobileType() {
        return mobileType;
    }

    public void setMobileType(String mobileType) {
        this.mobileType = mobileType;
    }

    public Integer getChildestimatedWorkOrderValue() {
        return childestimatedWorkOrderValue;
    }

    public void setChildestimatedWorkOrderValue(Integer childestimatedWorkOrderValue) {
        this.childestimatedWorkOrderValue = childestimatedWorkOrderValue;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public Integer getUplodedQty() {
        return uplodedQty;
    }

    public void setUplodedQty(Integer uplodedQty) {
        this.uplodedQty = uplodedQty;
    }

    public Integer getPendingQty() {
        return pendingQty;
    }

    public void setPendingQty(Integer pendingQty) {
        this.pendingQty = pendingQty;
    }

    public Integer getQCStatusCount() {
        return qCStatusCount;
    }

    public void setQCStatusCount(Integer qCStatusCount) {
        this.qCStatusCount = qCStatusCount;
    }

    public Integer getPassQty() {
        return passQty;
    }

    public void setPassQty(Integer passQty) {
        this.passQty = passQty;
    }

    public Integer getFailQty() {
        return failQty;
    }

    public void setFailQty(Integer failQty) {
        this.failQty = failQty;
    }

    public Integer getDeviceVal() {
        return deviceVal;
    }

    public void setDeviceVal(Integer deviceVal) {
        this.deviceVal = deviceVal;
    }

    public Object getQCStatus() {
        return qCStatus;
    }

    public void setQCStatus(Object qCStatus) {
        this.qCStatus = qCStatus;
    }

    public Object getIMEIOrSerialNo() {
        return iMEIOrSerialNo;
    }

    public void setIMEIOrSerialNo(Object iMEIOrSerialNo) {
        this.iMEIOrSerialNo = iMEIOrSerialNo;
    }

    public Integer getQCCharges() {
        return qCCharges;
    }

    public void setQCCharges(Integer qCCharges) {
        this.qCCharges = qCCharges;
    }

    public Integer getTotalQCCharges() {
        return totalQCCharges;
    }

    public void setTotalQCCharges(Integer totalQCCharges) {
        this.totalQCCharges = totalQCCharges;
    }

    public Integer getTotalEstimatedVal() {
        return totalEstimatedVal;
    }

    public void setTotalEstimatedVal(Integer totalEstimatedVal) {
        this.totalEstimatedVal = totalEstimatedVal;
    }
}