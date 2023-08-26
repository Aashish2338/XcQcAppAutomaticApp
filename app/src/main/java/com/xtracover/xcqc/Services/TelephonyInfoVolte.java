package com.xtracover.xcqc.Services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TelephonyInfoVolte {

    private static TelephonyInfoVolte telephonyInfo;
    private String imsiSIM1;
    private String imsiSIM2;
    private boolean isSIM1Ready;
    private boolean isSIM2Ready;

    public static TelephonyInfoVolte getTelephonyInfo() {
        return telephonyInfo;
    }

    public static void setTelephonyInfo(TelephonyInfoVolte telephonyInfo) {
        TelephonyInfoVolte.telephonyInfo = telephonyInfo;
    }

    public String getImsiSIM1() {
        return imsiSIM1;
    }

    public void setImsiSIM1(String imsiSIM1) {
        this.imsiSIM1 = imsiSIM1;
    }

    public String getImsiSIM2() {
        return imsiSIM2;
    }

    public void setImsiSIM2(String imsiSIM2) {
        this.imsiSIM2 = imsiSIM2;
    }

    public boolean isSIM1Ready() {
        return isSIM1Ready;
    }

    public void setSIM1Ready(boolean SIM1Ready) {
        isSIM1Ready = SIM1Ready;
    }

    public boolean isSIM2Ready() {
        return isSIM2Ready;
    }

    public void setSIM2Ready(boolean SIM2Ready) {
        isSIM2Ready = SIM2Ready;
    }

    @SuppressLint("MissingPermission")
    public static TelephonyInfoVolte getInstance(Context context) {
        try {
            if (telephonyInfo == null) {
                telephonyInfo = new TelephonyInfoVolte();
                TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

                telephonyInfo.imsiSIM1 = telephonyManager.getDeviceId();
                telephonyInfo.imsiSIM2 = null;

                List<String> list = printTelephonyManagerMethodNamesForThisDeviceList(context);
                for (int i = 0; i < list.size(); i++) {
                    try {
                        System.out.println("1 -> " + list.get(i));
                        telephonyInfo.imsiSIM1 = getDeviceIdBySlot(context, list.get(i), 0);
                        telephonyInfo.imsiSIM2 = getDeviceIdBySlot(context, list.get(i), 1);
                        break;
                    } catch (GeminiMethodNotFoundException e2) {
                        e2.printStackTrace();
                    }
                }

                telephonyInfo.isSIM1Ready = telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
                telephonyInfo.isSIM2Ready = false;

                try {
                    telephonyInfo.isSIM1Ready = getSIMStateBySlot(context, "getSimStateGemini", 0);
                    telephonyInfo.isSIM2Ready = getSIMStateBySlot(context, "getSimStateGemini", 1);
                } catch (GeminiMethodNotFoundException e) {
                    e.printStackTrace();
                    try {
                        telephonyInfo.isSIM1Ready = getSIMStateBySlot(context, "getSimState", 0);
                        telephonyInfo.isSIM2Ready = getSIMStateBySlot(context, "getSimState", 1);
                    } catch (GeminiMethodNotFoundException e1) {
                        List<String> list1 = printTelephonyManagerMethodNamesForThisDeviceList(context);
                        for (int i = 0; i < list1.size(); i++) {
                            try {
                                telephonyInfo.isSIM1Ready = getSIMStateBySlot(context, list1.get(i), 0);
                                telephonyInfo.isSIM2Ready = getSIMStateBySlot(context, list1.get(i), 1);
                                break;
                            } catch (GeminiMethodNotFoundException e2) {
                                e2.printStackTrace();
                            }
                        }
                        e1.printStackTrace();
                    }
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        return telephonyInfo;
    }

    private static String getDeviceIdBySlot(Context context, String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {
        String imsi = null;
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);
            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimID.invoke(telephony, obParameter);

            if (ob_phone != null) {
                imsi = ob_phone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return imsi;
    }

    private static boolean getSIMStateBySlot(Context context, String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {
        boolean isReady = false;
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimStateGemini = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimStateGemini.invoke(telephony, obParameter);

            if (ob_phone != null) {
                int simState = Integer.parseInt(ob_phone.toString());
                if (simState == TelephonyManager.SIM_STATE_READY) {
                    isReady = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return isReady;
    }

    private static class GeminiMethodNotFoundException extends Exception {
        private static final long serialVersionUID = -996812356902545308L;
        public GeminiMethodNotFoundException(String info) {
            super(info);
        }
    }

    public static List<String> printTelephonyManagerMethodNamesForThisDeviceList(Context context) {
        List<String> list = new ArrayList<String>();
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class<?> telephonyClass;
        try {
            telephonyClass = Class.forName(telephony.getClass().getName());
            Method[] methods = telephonyClass.getMethods();
            for (int idx = 0; idx < methods.length; idx++) {
                String mth = methods[idx].getName();
                list.add(mth);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return list;
    }
}