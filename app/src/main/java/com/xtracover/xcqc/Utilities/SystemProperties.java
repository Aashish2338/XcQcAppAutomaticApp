package com.xtracover.xcqc.Utilities;

import java.lang.reflect.InvocationTargetException;

public class SystemProperties {

    private static final Class<?> systemProperty = getSystemPropertiesClass();

    public static String get(String key) {
        try {
            return (String) systemProperty.getMethod("get", String.class).invoke(null, key);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String get(String key, String usual) {
        try {
            return (String) systemProperty.getMethod("get", String.class, String.class).invoke(null, key, usual);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean set(String key, String value) {
        try {
            systemProperty.getMethod("set", String.class, String.class).invoke(null, key, value);
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Class<?> getSystemPropertiesClass() {
        try {
            return Class.forName("android.os.SystemProperties");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private SystemProperties() {
        throw new AssertionError();
    }
}