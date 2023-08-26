package com.xtracover.xcqc.Interfaces;

import com.google.gson.JsonObject;
import com.xtracover.xcqc.Models.ErrorResponse;
import com.xtracover.xcqc.Models.ErrorTestResponse;
import com.xtracover.xcqc.Models.LoginResponse;
import com.xtracover.xcqc.Models.QCTestListByserviceKeyResponse;
import com.xtracover.xcqc.Models.ServiceKeyResponse;
import com.xtracover.xcqc.Models.ServiceKeysResponse;
import com.xtracover.xcqc.Models.ServiceResponse;
import com.xtracover.xcqc.Models.UpdateTestResultResponse;
import com.xtracover.xcqc.Models.WorkOrderResponse;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiClient {

    @FormUrlEncoded
    @POST("warrantybazzarLogindeatil")
    Single<LoginResponse> getUsersAccountLogin(@Field("EmpCode") String uerName, @Field("password") String password);

    @FormUrlEncoded
    @POST("warrantybazzarLogindeatilpin")
    Single<LoginResponse> getLoginDetailsByPinCode(@Field("EmpCode") String uerName, @Field("Password") String password);

    @GET("GetwarrantybazzarOrderdeatil")
    Single<WorkOrderResponse> getWorkOrders(@Query("EmpCode") String empCode);

    @GET("GetServiceKeyOnIMEI")
    Single<ServiceKeysResponse> getServiceKeyOnIMEI(@Query("IMEI") String iMEIKey);

    @POST("setsubmit")
    Single<ServiceResponse> getServiceKey(@Body JsonObject jsonBodyKey);

    @GET("GetQCTestListByserviceKey")
    Single<QCTestListByserviceKeyResponse> GetQCTestListByserviceKey(@Query("servicekey") String servicekey);

    @POST("AddNewWarrantyBazzarAppResultWithGrade")
        // AddNewwarrantybazzarMobiletestApiWithGrade
    Single<ServiceKeysResponse> getAddNewWarrantyBazzarAppResultWithGrade(@Body JsonObject jsonBodyKey);

    @POST("UpdateAppResultKeys")
        // This is use for single result test update
    Single<UpdateTestResultResponse> updateAppResultStatus(@Body JsonObject jsonBodyKey);

    @POST("UpdateMasterAppResultKeys")
        // This is use for single result test update
        // it is used for USB test, Charging storage test, Multitouch test, and OTG test only
    Single<UpdateTestResultResponse> updateMasterAppResultStatus(@Body JsonObject jsonBodyKey);

    @POST("NewCheckBatteryStatus")
    Single<UpdateTestResultResponse> updateNewCheckBatteryStatusReport(@Body JsonObject jsonBodyKey);

    @POST("Error_testlp")
    Single<ErrorTestResponse> updateErrorTestReport(@Body JsonObject jsonBodyKey);

    @GET("Geterror_log")
    Single<ErrorResponse> getErrorLog(@Query("servicekey") String servicekey);

    @POST("Getservice_key")
    Single<ServiceKeyResponse> getNewServiceKey();

}