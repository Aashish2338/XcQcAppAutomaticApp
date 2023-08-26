package com.xtracover.xcqc.Utilities;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.concurrent.TimeUnit;

import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiNetworkClient {

    // https://store.xtracover.com/api/StoreApi/warrantybazzarLogindeatil
    // https://store.xtracover.com/api/StoreApi/GetwarrantybazzarOrderdeatil
    // https://store.xtracover.com/api/StoreApi/NewCheckBatteryStatus
    // https://store.xtracover.com/api/StoreApi/GetServiceKeyOnIMEI?IMEI=
    // https://store.xtracover.com/api/StoreApi/AddNewWarrantyBazzarAppResultWithGrade
    // http://api.qutrust.com/clientdata/setsubmit
    // https://channel.xtracover.com/api/StoreApi/warrantybazzarLogindeatilpin
    // http://storetemp.xtracover.com/api/StoreApi/Getservice_key
    // kde kre tu kde mai kru nadaniyan

    //        private static final String Base_Url = "https://storetemp.xtracover.com/api/StoreApi/";
    // https://store.xtracover.com/api/StoreApi/Getservice_key
    private static final String Base_Url = "https://store.xtracover.com/api/StoreApi/";
    private static final String Base_UrlQutrust = "http://api.qutrust.com/clientdata/";
    private static final String Base_UrlWithPinCode = "https://channel.xtracover.com/api/StoreApi/";
    private static final String Base_UrlNewCertificate = "http://storetemp.xtracover.com/api/StoreApi/";
    private static Retrofit retrofit = null;
    private static Retrofit retrofitQutrust = null;
    private static Retrofit retrofitWithPinCode = null;
    private static Retrofit retrofitNewCertificate = null;

    public static Retrofit getStoreApiRetrofit() {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptorLogin = new HttpLoggingInterceptor();
            interceptorLogin.setLevel(HttpLoggingInterceptor.Level.BODY);

            CookieHandler cookieHandlerLogin = new CookieManager();
            OkHttpClient clientLogin = new OkHttpClient.Builder().addNetworkInterceptor(interceptorLogin)
                    .cookieJar(new JavaNetCookieJar(cookieHandlerLogin))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Base_Url)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(clientLogin)
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getRetrofitQutrust() {
        if (retrofitQutrust == null) {
            HttpLoggingInterceptor interceptorLogin = new HttpLoggingInterceptor();
            interceptorLogin.setLevel(HttpLoggingInterceptor.Level.BODY);

            CookieHandler cookieHandlerLogin = new CookieManager();
            OkHttpClient clientLogin = new OkHttpClient.Builder().addNetworkInterceptor(interceptorLogin)
                    .cookieJar(new JavaNetCookieJar(cookieHandlerLogin))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofitQutrust = new Retrofit.Builder()
                    .baseUrl(Base_UrlQutrust)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(clientLogin)
                    .build();
        }
        return retrofitQutrust;
    }

    public static Retrofit getRetrofitWithPinCode() {
        if (retrofitWithPinCode == null) {
            HttpLoggingInterceptor interceptorLogin = new HttpLoggingInterceptor();
            interceptorLogin.setLevel(HttpLoggingInterceptor.Level.BODY);

            CookieHandler cookieHandlerLogin = new CookieManager();
            OkHttpClient clientLogin = new OkHttpClient.Builder().addNetworkInterceptor(interceptorLogin)
                    .cookieJar(new JavaNetCookieJar(cookieHandlerLogin))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofitWithPinCode = new Retrofit.Builder()
                    .baseUrl(Base_UrlWithPinCode)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(clientLogin)
                    .build();
        }
        return retrofitWithPinCode;
    }

    public static Retrofit getStoreApiRetrofitNewCertificate() {
        if (retrofitNewCertificate == null) {
            HttpLoggingInterceptor interceptorLogin = new HttpLoggingInterceptor();
            interceptorLogin.setLevel(HttpLoggingInterceptor.Level.BODY);

            CookieHandler cookieHandlerLogin = new CookieManager();
            OkHttpClient clientLogin = new OkHttpClient.Builder().addNetworkInterceptor(interceptorLogin)
                    .cookieJar(new JavaNetCookieJar(cookieHandlerLogin))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofitNewCertificate = new Retrofit.Builder()
                    .baseUrl(Base_UrlNewCertificate)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(clientLogin)
                    .build();
        }
        return retrofitNewCertificate;
    }
}