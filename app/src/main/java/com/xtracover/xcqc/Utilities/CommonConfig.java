package com.xtracover.xcqc.Utilities;

public class CommonConfig {

    String SDKVersion = "V2.6";
    public static String[] ListOfClasses = new String[]{
            "com.mediatek.telephony.TelephonyManagerEx",
            "android.telephony.MSimTelephonyManager",
            "android.telephony.TelephonyManager",
            "android.telephony.TelephonyManager"};

    public static String[] ListofDeviceIdMethods = new String[]{
            "getSimSerialNumber", "getDeviceIdDs",
            "getDeviceId", "getDeviceIdGemini", "getSubscriptionId",
            "getSimSlotIndex", "getSimSerialNumberGemini"
    };

    public static String[] ListOfSimStateMethods = new String[]{
            "getSimState", "getSimStateGemini"
    };

    public static String SimStateMethodName = "getSimState";
    public static String certificationqutrust = "http://devices.moonglabs.com/Certificates/submitreport";
    public static String partnerId = "10";
    public static String TelephonyClassName = "android.telephony.TelephonyManager";

}