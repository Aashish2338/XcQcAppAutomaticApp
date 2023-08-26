package com.xtracover.xcqc.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public class SimManagement {

    Context context = null;
    public String TAG = "SimManagement";
    String method = null;

    public boolean isDualSim(Context context) {
        String deviceIdSlot0 = getDeviceId(0);
        String deviceIdSlot1 = getDeviceId(1);

        if ((deviceIdSlot1 == null) || deviceIdSlot1.equals("")) {
            System.out.println("System is dual sim or not  1   ");
            return false;
        } else {
            if (deviceIdSlot0.equals(deviceIdSlot1)) {
                System.out.println("System is dual sim or not  2  ");
                return false;
            } else if (deviceIdSlot0 != null && deviceIdSlot0.length() > 0 && deviceIdSlot1 != null && deviceIdSlot1.length() > 0) {
                return true;
            } else {
                System.out.println("System is dual sim or not  3 ");
                return false;
            }
        }
    }

    public boolean isSim1Ready(Context context, boolean isDualSimPhone) {
        this.context = context;
        boolean isSimReady = false;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String simState = getSimStateBySlotId(0, isDualSimPhone);
        if (simState != null && simState.length() > 0) {
            try {
                if (Integer.parseInt(simState) == TelephonyManager.SIM_STATE_READY) {
                    isSimReady = true;
                }
            } catch (Exception e) {
            }
        }
        return isSimReady;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @SuppressLint("MissingPermission")
    public boolean isSim1ReadylolipopPlus(Context context) {
        boolean status = false;
        String carrierName = null;
        String displayname = null;
        List<SubscriptionInfo> sil = null;
        try {
            SubscriptionManager subscriptionmanager = SubscriptionManager.from(context);
            if (subscriptionmanager != null) {
                carrierName = subscriptionmanager.getActiveSubscriptionInfoForSimSlotIndex(0).getCarrierName().toString();
                displayname = subscriptionmanager.getActiveSubscriptionInfoForSimSlotIndex(0).getDisplayName().toString();

                sil = subscriptionmanager.getActiveSubscriptionInfoList();
            }

            if (sil != null) {
                for (SubscriptionInfo subInfo : sil) {
                    String sID = (String) subInfo.getCarrierName();
                }

                if (carrierName != null && carrierName.length() > 0) {
                    if (carrierName.contains("No service") || carrierName.contains("Emergency calls")) {
                        status = false;
                    } else {
                        status = true;
                    }
                } else if (displayname != null && displayname.length() > 0) {
                    if (displayname.contains("No service") || displayname.contains("Emergency calls")) {
                        status = false;
                    } else {
                        status = true;
                    }
                }
            }
        } catch (Exception e) {
            status = false;
        }
        return status;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public boolean isSim2ReadylolipopPlus(Context context) {
        boolean status = false;
        try {
            SubscriptionManager subscriptionmanager = SubscriptionManager.from(context);
            @SuppressLint("MissingPermission") String carrierName = subscriptionmanager.getActiveSubscriptionInfoForSimSlotIndex(1).getCarrierName().toString();
            @SuppressLint("MissingPermission") String displayname = subscriptionmanager.getActiveSubscriptionInfoForSimSlotIndex(1).getDisplayName().toString();
            @SuppressLint("MissingPermission") List<SubscriptionInfo> sil = subscriptionmanager.getActiveSubscriptionInfoList();

            if (sil != null) {
                for (SubscriptionInfo subInfo : sil) {
                    String sID = (String) subInfo.getCarrierName();
                }
                if (carrierName != null && carrierName.length() > 0) {
                    if (carrierName.contains("No service") || carrierName.contains("Emergency calls")) {
                        status = false;
                    } else {
                        status = true;
                    }
                } else if (displayname != null && displayname.length() > 0) {
                    if (displayname.contains("No service") || displayname.contains("Emergency calls")) {
                        status = false;
                    } else {
                        status = true;
                    }
                }
            }
        } catch (Exception e) {
            status = false;
        }
        return status;
    }

    public boolean isSim2Ready(Context context, boolean isDualSimPhone) {
        this.context = context;
        boolean isSimReady = false;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String simState = getSimStateBySlotId(1, isDualSimPhone);
        if (simState != null && simState.length() > 0) {
            try {
                if (Integer.parseInt(simState) == TelephonyManager.SIM_STATE_READY) {
                    isSimReady = true;
                }
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
        System.out.println("System is dual sim or not   simstate   " + isSimReady);
        return isSimReady;
    }

    public String getDeviceId(int slotId) {
        String imei = getImeiBySlotId(slotId);
        return imei;
    }

    public String getSimStateBySlotId(int slotId, boolean isDualSimPhone) {
        String simState = null;
        for (int index = 0; index < CommonConfig.ListOfClasses.length; index++) {
            if (isTelephonyClassExists(CommonConfig.ListOfClasses[index])) {
                for (int index1 = 0; index1 < CommonConfig.ListOfSimStateMethods.length; index1++) {
                    if (isSimStateMethodExists(CommonConfig.ListOfClasses[index], CommonConfig.ListOfSimStateMethods[index1], slotId, isDualSimPhone)) {
                        System.out.println("Vikash after issimstate");
                        simState = invokeMethod(CommonConfig.ListOfClasses[index], slotId, CommonConfig.ListOfSimStateMethods[index1], method);
                        System.out.println("Vikash after issimstate11" + simState);
                        if (simState != null) {
                            return simState;
                        }
                    }
                }
            }
        }
        return simState;
    }

    public String getImeiBySlotId(int slotId) {
        String imei = null;
        for (int index = 0; index < CommonConfig.ListOfClasses.length; index++) {
            if (isTelephonyClassExists(CommonConfig.ListOfClasses[index])) {
                for (int index1 = 0; index1 < CommonConfig.ListofDeviceIdMethods.length; index1++) {
                    if (isMethodExists(CommonConfig.ListOfClasses[index], CommonConfig.ListofDeviceIdMethods[index1])) {
                        imei = invokeMethod(CommonConfig.ListOfClasses[index], slotId, "getDeviceId", method);
                        if (imei != null && (!imei.equals(""))) {
                            return imei;
                        }
                    }
                }
            }
        }
        return imei;
    }

    public boolean isTelephonyClassExists(String className) {
        boolean isClassExists = false;
        try {
            Class<?> telephonyClass = Class.forName(className);
            isClassExists = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isClassExists;
    }

    public boolean isMethodExists(String className, String compairMethod) {
        boolean isExists = false;
        try {
            Class<?> telephonyClass = Class.forName(className);
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            StringBuffer sbf = new StringBuffer();
            Method[] methodList = telephonyClass.getDeclaredMethods();
            for (int index = methodList.length - 1; index >= 0; index--) {
                sbf.append("\n\n" + methodList[index].getName());
                if (methodList[index].getReturnType().equals(String.class)) {
                    String methodName = methodList[index].getName();
                    if (methodName.contains(compairMethod)) {
                        Class<?>[] param = methodList[index].getParameterTypes();
                        if (param.length > 0) {
                            if (param[0].equals(int.class)) {
                                try {
                                    method = methodName.substring(compairMethod.length(), methodName.length());
                                    CommonConfig.TelephonyClassName = className;
                                    isExists = true;
                                    break;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                CommonConfig.TelephonyClassName = className;
                                isExists = true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isExists;
    }

    public boolean isSimStateMethodExists(String className, String compairMethod, int slotId, boolean isDualSimPhone) {
        boolean isExists = false;
        try {
            Class<?> telephonyClass = Class.forName(className);
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            StringBuffer sbf = new StringBuffer();
            Method[] methodList = telephonyClass.getDeclaredMethods();
            for (int index = methodList.length - 1; index >= 0; index--) {
                sbf.append("\n\n" + methodList[index].getName());
                if (methodList[index].getReturnType().equals(int.class)) {
                    String methodName = methodList[index].getName();
                    System.out.println("Sim method=" + methodName);
                    if (methodName.contains(compairMethod)) {
                        System.out.println("Sim method1=");
                        Class<?>[] param = methodList[index].getParameterTypes();
                        System.out.println("Sim method2=" + param.length);
                        System.out.println("Sim method3=" + param.toString());
                        if (param.length > 0) {
                            System.out.println("Sim method4=" + param.toString());
                            if (param[0].equals(int.class)) {
                                System.out.println("Sim method5=" + param.toString());
                                try {
                                    System.out.println("Sim method6=" + param.toString());
                                    method = methodName.substring(compairMethod.length(), methodName.length());
                                    System.out.println("Sim method7=" + method.toString());
                                    CommonConfig.TelephonyClassName = className;
                                    System.out.println("Sim method8=" + className);
                                    isExists = true;
                                    break;
                                } catch (Exception e) {
                                    System.out.println("Sim method9=" + className);
                                    e.printStackTrace();
                                }
                            } else {
                                CommonConfig.TelephonyClassName = className;
                                System.out.println("Sim method10=" + className);
                                isExists = true;
                            }
                        } else if (isDualSimPhone == false && slotId == 0) {
                            System.out.println("Sim method11=" + className);
                            CommonConfig.TelephonyClassName = className;
                            method = "";
                            isExists = true;
                            System.out.println("Sim method12=" + isExists);
                            break;
                        } else {
                            CommonConfig.TelephonyClassName = className;
                            method = "";
                            isExists = true;
                            System.out.println("Sim method13=" + isExists);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Sim method13=" + className);
            e.printStackTrace();
        }
        return isExists;
    }

    private String invokeMethod(String className, int slotNumber, String methodName, String SIM_variant) {
        System.out.println("Vikash after issimstate2" + className + "slot number:" + slotNumber + "method name:" + methodName + "simvariant:" + SIM_variant);
        System.out.println(className + "Method name=" + methodName + "SIM variants=" + SIM_variant);
        String value = "";
        try {
            Class<?> telephonyClass = Class.forName("com.android.internal.telephony.IPhoneSubInfo$Stub");
            Constructor[] cons = telephonyClass.getDeclaredConstructors();
            Class<?>[] parameter = null;
            Object ob_phone = null;
            Object obj = null;

            System.out.println("Vikash after issimstate150=" + SIM_variant + "Sim variants=" + SIM_variant.length() + "slot number=" + slotNumber);
            cons[0].getName();
            System.out.println("Vikash after issimstate151=" + cons[0].getName());
            cons[0].setAccessible(true);
            System.out.println("Vikash after issimstate152=" + SIM_variant + "Sim variants=" + SIM_variant.length() + "slot number=" + slotNumber);
            obj = cons[0].newInstance();
            try {
                System.out.println("Vikash after issimstate153=" + SIM_variant + "Sim variants=" + SIM_variant.length() + "slot number=" + slotNumber);
                System.out.println("Vikash after issimstate154=" + SIM_variant + "Sim variants=" + SIM_variant.length() + "slot number=" + slotNumber);
                if (SIM_variant != null && SIM_variant.length() > 0 || slotNumber > 0) {
                    System.out.println("Vikash after issimstate16=");
                    parameter = new Class[1];
                    parameter[0] = int.class;
                    System.out.println("Vikash after issimstate17=" + parameter[0]);
                }
                // Object ob_phone = null;
                Method getSimID = telephonyClass.getMethod(methodName + SIM_variant, parameter);
                System.out.println("Vikash after issimstate18=" + getSimID);
                Object[] obParameter = null;
                if ((SIM_variant != null && SIM_variant.length() > 0) || slotNumber > 0) {
                    System.out.println("Vikash after issimstate19=");
                    obParameter = new Object[1];
                    obParameter[0] = slotNumber;
                    System.out.println("Vikash after issimstate20=" + obParameter[0]);
                }
                ob_phone = getSimID.invoke(obj, obParameter);
                System.out.println("Vikash after issimstate21=" + ob_phone);
            } catch (Exception e) {
                System.out.println("Vikash after issimstate22=+" + e);
                if (slotNumber == 0) {
                    System.out.println("Vikash after issimstate23=+");
                    Method getSimID = telephonyClass.getMethod(methodName + SIM_variant, parameter);
                    System.out.println("Vikash after issimstate24=+" + getSimID);
                    Object[] obParameter = null;
                    if ((SIM_variant != null && SIM_variant.length() > 0) || slotNumber > 0) {
                        System.out.println("Vikash after issimstate25=+");
                        obParameter = new Object[1];
                        obParameter[0] = slotNumber;
                        System.out.println("Vikash after issimstate26=+" + obParameter[0]);
                    }
                    ob_phone = getSimID.invoke(obj);
                    System.out.println("Vikash inside simmgmt ob_phone=" + ob_phone);
                }
            }

            if (ob_phone != null) {
                value = ob_phone.toString();
                System.out.println("Vikash inside simmgmt values=" + value);
            }
        } catch (Exception e) {
            System.out.println("Vikash inside exception0=" + e);

            TelephonyManager mgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (mgr != null) {
                System.out.println("Vikash inside exception1=");
                try {
                    System.out.println("Vikash inside exception2=");
                    Method simStateM = mgr.getClass().getDeclaredMethod("getSimState", int.class);
                    System.out.println("Vikash inside exception3=");
                    if (simStateM != null) {
                        System.out.println("Vikash inside exception4=");
                        Object[] args = new Object[1];
                        args[0] = slotNumber;
                        System.out.println("Vikash inside exception5=" + args[0]);

                        Object data = simStateM.invoke(mgr, args);
                        System.out.println("Vikash inside exception6= " + data.getClass());
                        if (data != null && data.getClass() == Integer.class) {
                            System.out.println("Data : " + data.getClass() + " , " + data + "value=" + value);
                            return (Integer) data + "";
                        }
                    }
                } catch (Exception e1) {
                    System.out.println("Vikash inside exception7=" + e1);
                    e1.printStackTrace();

                    value = invokeOldMethod(className, slotNumber, methodName, SIM_variant);
                    System.out.println("Vikash inside exception8=" + value);
                }
            } else {
                value = invokeOldMethod(className, slotNumber, methodName, SIM_variant);
                System.out.println("Vikash inside exception9=" + value);
            }
            System.out.println("Vikash inside exception10=" + value);
        }
        return value;
    }

    public String invokeOldMethod(String className, int slotNumber, String methodName, String SIM_variant) {
        String val = "";
        try {
            Class<?> telephonyClass = Class.forName(CommonConfig.TelephonyClassName);
            Constructor[] cons = telephonyClass.getDeclaredConstructors();
            cons[0].getName();
            cons[0].setAccessible(true);
            Object obj = cons[0].newInstance();
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Object ob_phone = null;
            try {
                Method getSimID = telephonyClass.getMethod(methodName + SIM_variant, parameter);
                Object[] obParameter = new Object[1];
                obParameter[0] = slotNumber;
                ob_phone = getSimID.invoke(obj, obParameter);
            } catch (Exception e) {
                if (slotNumber == 0) {
                    Method getSimID = telephonyClass.getMethod(methodName + SIM_variant, parameter);
                    Object[] obParameter = new Object[1];
                    obParameter[0] = slotNumber;
                    ob_phone = getSimID.invoke(obj);
                }
            }

            if (ob_phone != null) {
                val = ob_phone.toString();
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
        return val;
    }
}