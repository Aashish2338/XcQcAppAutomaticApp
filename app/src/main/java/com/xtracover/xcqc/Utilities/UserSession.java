package com.xtracover.xcqc.Utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.xtracover.xcqc.Activities.LoginActivity;

import java.util.HashMap;

public class UserSession {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;
    private int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "Xtracover";
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";

    public UserSession(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String name, String password) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, password);
        editor.commit();
    }

    public void checkLogin() {
        if (!this.isLoggedIn()) {
            Intent i = new Intent(_context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        return user;
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
        Intent i = new Intent(_context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }

    public void setUserPassword(String userPassword) {
        editor.putString("userPassword", userPassword);
        editor.commit();
    }

    public String getUserPassword() {
        return pref.getString("userPassword", "");
    }

    public void addError(String error) {
        editor.putString("error", error);
        editor.commit();
    }

    public String getError() {
        return pref.getString("error", "");
    }

    public void setSoftwareVersion(String softwareVersion) {
        editor.putString("softwareVersion", softwareVersion);
        editor.commit();
    }

    public String getSoftwareVersion() {
        return pref.getString("softwareVersion", "");
    }

    public void setphyCond(String phyCond) {
        editor.putString("box_Condition", phyCond);
        editor.commit();
    }

    public String getphyCond() {
        return pref.getString("box_Condition", " ");
    }

    public void setEmpCode(String EmpCode) {
        editor.putString("EmpCode", EmpCode);
        editor.commit();
    }

    public String getEmpCode() {
        return pref.getString("EmpCode", " ");
    }

    public void setUserName(String UserId) {
        editor.putString("UserId", UserId);
        editor.commit();
    }

    public String getUserName() {
        return pref.getString("UserId", " ");
    }

    public void setRefreshInstruction(String RefreshInstruction) { //RefreshInstruction
        editor.putString("RefreshInstruction", RefreshInstruction);
        editor.commit();
    }

    public String getRefreshInstruction() {
        return pref.getString("RefreshInstruction", " ");
    }

    public void setDeviceBrandName(String deviceBrandName) {
        editor.putString("DeviceBrandName", deviceBrandName);
        editor.commit();
    }

    public String getDeviceBrandName() {
        return pref.getString("DeviceBrandName", " ");
    }

    public void setDeviceModelNumber(String deviceModelNumber) {
        editor.putString("deviceModelNumber", deviceModelNumber);
        editor.commit();
    }

    public String getDeviceModelNumber() {
        return pref.getString("deviceModelNumber", " ");
    }

    public void setServiceKey(String serviceKey) {
        editor.putString("serviceKey", serviceKey);
        editor.commit();
    }

    public String getServiceKey() {
        return pref.getString("serviceKey", " ");
    }

    public void setTestAll(String allTest) {
        editor.putString("fullTest", allTest);
        editor.commit();
    }

    public String getTestAll() {
        return pref.getString("fullTest", "");
    }

    public void setIsRetest(String isRetest) {
        editor.putString("IsRetest", isRetest);
        editor.commit();
    }

    public String getIsRetest() {
        return pref.getString("IsRetest", " ");
    }

    public void setTestKeyName(String testKeyName) {
        editor.putString("testKeyName", testKeyName);
        editor.commit();
    }

    public String getTestKeyName() {
        return pref.getString("testKeyName", "");
    }

    public void setRetestKeyName(String testName) {
        editor.putString("testName", testName);
        editor.commit();
    }

    public String getRetestKeyName() {
        return pref.getString("testName", "");
    }

    public void setDeadPixelCheck(String deadPixelCheck) {
        editor.putString("DEAD_PIXEL_CHECK", deadPixelCheck);
        editor.commit();
    }

    public String getDeadPixelCheck() {
        return pref.getString("DEAD_PIXEL_CHECK", "");
    }

    public void setDisplayTouchScreen(String displayTouchScreen) {
        editor.putString("Display_Touch_Screen", displayTouchScreen);
        editor.commit();
    }

    public String getDisplayTouchScreen() {
        return pref.getString("Display_Touch_Screen", "");
    }

    public void setMultifingerTest(String multifingerTest) {
        editor.putString("Multifinger_test", multifingerTest);
        editor.commit();
    }

    public String getMultifingerTest() {
        return pref.getString("Multifinger_test", "");
    }

    public void setDisplayBrightnessTest(String displayBrightnessTest) {
        editor.putString("Display_Brightness", displayBrightnessTest);
        editor.commit();
    }

    public String getDisplayBrightnessTest() {
        return pref.getString("Display_Brightness", "");
    }

    public void setEarphoneTest(String Earphone) {
        editor.putString("Earphone", Earphone);
        editor.commit();
    }

    public String getEarphoneTest() {
        return pref.getString("Earphone", "");
    }

    public void setEarphoneJackTest(String EarphoneJack) {
        editor.putString("Earphone_Jack", EarphoneJack);
        editor.commit();
    }

    public String getEarphoneJackTest() {
        return pref.getString("Earphone_Jack", "");
    }

    public void setEarphoneKeysTest(String handset_mic_keys) {
        editor.putString("handset_mic_keys", handset_mic_keys);
        editor.commit();
    }

    public String getEarphoneKeysTest() {
        return pref.getString("handset_mic_keys", "");
    }

    public void setEarphoneMicTest(String handset_mic) {
        editor.putString("handset_mic", handset_mic);
        editor.commit();
    }

    public String getEarphoneMicTest() {
        return pref.getString("handset_mic", "");
    }

    public void setMicrophoneTest(String Microphone) {
        editor.putString("Microphone", Microphone);
        editor.commit();
    }

    public String getMicrophoneTest() {
        return pref.getString("Microphone", "");
    }

    public void setNoiseCancellationTest(String NoiseCancellationTest) {
        editor.putString("NoiseCancellationTest", NoiseCancellationTest);
        editor.commit();
    }

    public String getNoiseCancellationTest() {
        return pref.getString("NoiseCancellationTest", "");
    }

    public void setLoudSpeakerTest(String LoudSpeaker) {
        editor.putString("LoudSpeaker", LoudSpeaker);
        editor.commit();
    }

    public String getLoudSpeakerTest() {
        return pref.getString("LoudSpeaker", "");
    }

    public void setFrontSpeakerTest(String Front_speaker) {
        editor.putString("Front_speaker", Front_speaker);
        editor.commit();
    }

    public String getFrontSpeakerTest() {
        return pref.getString("Front_speaker", "");
    }

    public void setBackCameraFlashTest(String Flash) {
        editor.putString("Flash", Flash);
        editor.commit();
    }

    public String getBackCameraFlashTest() {
        return pref.getString("Flash", "");
    }

    public void setFrontCameraFlashTest(String Front_camera_flash) {
        editor.putString("Front_camera_flash", Front_camera_flash);
        editor.commit();
    }

    public String getFrontCameraFlashTest() {
        return pref.getString("Front_camera_flash", "");
    }

    public void setVibrationTest(String Vibrate) {
        editor.putString("Vibrate", Vibrate);
        editor.commit();
    }

    public String getVibrationTest() {
        return pref.getString("Vibrate", "");
    }

    public void setMenuKeyTest(String Menu_Key) {
        editor.putString("Menu_Key", Menu_Key);
        editor.commit();
    }

    public String getMenuKeyTest() {
        return pref.getString("Menu_Key", "");
    }

    public void setHomeKeyTest(String Home_Key) {
        editor.putString("Home_Key", Home_Key);
        editor.commit();
    }

    public String getHomeKeyTest() {
        return pref.getString("Home_Key", "");
    }

    public void setBackKeyTest(String Back_Key) {
        editor.putString("Back_Key", Back_Key);
        editor.commit();
    }

    public String getBackKeyTest() {
        return pref.getString("Back_Key", "");
    }

    public void setVolumeUpButtonTest(String Volume_Up_Button) {
        editor.putString("Volume_Up_Button", Volume_Up_Button);
        editor.commit();
    }

    public String getVolumeUpButtonTest() {
        return pref.getString("Volume_Up_Button", "");
    }

    public void setVolumeDownButtonTest(String Volume_Down_Button) {
        editor.putString("Volume_Down_Button", Volume_Down_Button);
        editor.commit();
    }

    public String getVolumeDownButtonTest() {
        return pref.getString("Volume_Down_Button", "");
    }

    public void setPowerButtonTest(String Power_Key) {
        editor.putString("Power_Key", Power_Key);
        editor.commit();
    }

    public String getPowerButtonTest() {
        return pref.getString("Power_Key", "");
    }

    public void setScreenLockTest(String Screen_Lock) {
        editor.putString("Screen_Lock", Screen_Lock);
        editor.commit();
    }

    public String getScreenLockTest() {
        return pref.getString("Screen_Lock", "");
    }

    public void setUSBTest(String USB) {
        editor.putString("USB", USB);
        editor.commit();
    }

    public String getUSBTest() {
        return pref.getString("USB", "");
    }

    public void setChargingTest(String ChargingTest) {
        editor.putString("ChargingTest", ChargingTest);
        editor.commit();
    }

    public String getChargingTest() {
        return pref.getString("ChargingTest", "");
    }

    public void setOtgTest(String OtgTest) {
        editor.putString("OtgTest", OtgTest);
        editor.commit();
    }

    public String getOtgTest() {
        return pref.getString("OtgTest", "");
    }

    public void setBiometricTest(String Biometric) {
        editor.putString("Biometric", Biometric);
        editor.commit();
    }

    public String getBiometricTest() {
        return pref.getString("Biometric", "");
    }

    public void setNFCTest(String NFC) {
        editor.putString("NFC", NFC);
        editor.commit();
    }

    public String getNFCTest() {
        return pref.getString("NFC", "");
    }

    public void setOrientationTest(String Orientation) {
        editor.putString("Orientation", Orientation);
        editor.commit();
    }

    public String getOrientationTest() {
        return pref.getString("Orientation", "");
    }

    public void setCallSIM1Test(String Call_SIM_1) {
        editor.putString("Call_SIM_1", Call_SIM_1);
        editor.commit();
    }

    public String getCallSIM1Test() {
        return pref.getString("Call_SIM_1", "");
    }

    public void setCallSIM2Test(String Call_SIM_2) {
        editor.putString("Call_SIM_2", Call_SIM_2);
        editor.commit();
    }

    public String getCallSIM2Test() {
        return pref.getString("Call_SIM_2", "");
    }

    public void setVolteCallingTest(String volteCallingTest) {
        editor.putString("volteCallingTest", volteCallingTest);
        editor.commit();
    }

    public String getVolteCallingTest() {
        return pref.getString("volteCallingTest", "");
    }


    public void setBackCameraTest(String backCameraTest) {
        editor.putString("Back_Camera", backCameraTest);
        editor.commit();
    }

    public String getBackCameraTest() {
        return pref.getString("Back_Camera", "");
    }

    public void setFrontCameraTest(String frontCameraTest) {
        editor.putString("Front_Camera", frontCameraTest);
        editor.commit();
    }

    public String getFrontCameraTest() {
        return pref.getString("Front_Camera", "");
    }

    public void setCameraAutoFocusTest(String frontCameraTest) {
        editor.putString("Camera_Auto_Focus", frontCameraTest);
        editor.commit();
    }

    public String getCameraAutoFocusTest() {
        return pref.getString("Camera_Auto_Focus", "");
    }

    public void setBackVideoRecordingTest(String frontCameraTest) {
        editor.putString("Back_Video_Recording", frontCameraTest);
        editor.commit();
    }

    public String getBackVideoRecordingTest() {
        return pref.getString("Back_Video_Recording", "");
    }

    public void setFrontVideoRecordingTest(String frontCameraTest) {
        editor.putString("Front_Video_Recording", frontCameraTest);
        editor.commit();
    }

    public String getFrontVideoRecordingTest() {
        return pref.getString("Front_Video_Recording", "");
    }

    public void setNetworkSignalSimOneTest(String networkSignalSim1) {
        editor.putString("Network_Signal_sim1", networkSignalSim1);
        editor.commit();
    }

    public String getNetworkSignalSimOneTest() {
        return pref.getString("Network_Signal_sim1", "");
    }

    public void setNetworkSignalSimTwoTest(String networkSignalSim2) {
        editor.putString("Network_Signal_sim2", networkSignalSim2);
        editor.commit();
    }

    public String getNetworkSignalSimTwoTest() {
        return pref.getString("Network_Signal_sim2", "");
    }

    public void setWiFiTest(String wiFi) {
        editor.putString("WiFi", wiFi); //WiFi
        editor.commit();
    }

    public String getWiFiTest() {
        return pref.getString("WiFi", "");
    }

    public void setInternetTest(String internet) {
        editor.putString("Internet", internet);//Internet
        editor.commit();
    }

    public String getInternetTest() {
        return pref.getString("Internet", "");
    }

    public void setGPSTest(String GPS) {
        editor.putString("GPS", GPS);//GPS
        editor.commit();
    }

    public String getGPSTest() {
        return pref.getString("GPS", "");
    }

    public void setBluetoothTest(String Bluetooth) {
        editor.putString("Bluetooth", Bluetooth);
        editor.commit();
    }

    public String getBluetoothTest() {
        return pref.getString("Bluetooth", "");
    }

    public void setAudioPlaybackTest(String Bluetooth) {
        editor.putString("audioPlakbackTest", Bluetooth);
        editor.commit();
    }

    public String getAudioPlaybackTest() {
        return pref.getString("audioPlakbackTest", "");
    }

    public void setImeiValidationTest(String ImeiValidation) {
        editor.putString("IMEI_VALIDATION", ImeiValidation);
        editor.commit();
    }

    public String getImeiValidationTest() {
        return pref.getString("IMEI_VALIDATION", "");
    }

    public void setBatteryTest(String Battery) {
        editor.putString("Battery", Battery);
        editor.commit();
    }

    public String getBatteryTest() {
        return pref.getString("Battery", "");
    }

    public void setInternalStorageTest(String internalStorage) {
        editor.putString("Internal_Storage", internalStorage);
        editor.commit();
    }

    public String getInternalStorageTest() {
        return pref.getString("Internal_Storage", "");
    }

    public void setExternalStorageTest(String externalStorage) {
        editor.putString("External_Storage", externalStorage);
        editor.commit();
    }

    public String getExternalStorageTest() {
        return pref.getString("External_Storage", "");
    }

    public void setProximityTest(String proximity) {
        editor.putString("Proximity", proximity);
        editor.commit();
    }

    public String getProximityTest() {
        return pref.getString("Proximity", "");
    }

    public void setGyroscopeTest(String Gyroscope) {
        editor.putString("Gyroscope", Gyroscope);
        editor.commit();
    }

    public String getGyroscopeTest() {
        return pref.getString("Gyroscope", "");
    }

    public void setGyroscopeGamingTest(String GyroscopeGaming) {
        editor.putString("GyroscopeGaming", GyroscopeGaming);
        editor.commit();
    }

    public String getGyroscopeGamingTest() {
        return pref.getString("GyroscopeGaming", "");
    }

    public void setGravityTest(String Gravity) {
        editor.putString("Gravity", Gravity);
        editor.commit();
    }

    public String getGravityTest() {
        return pref.getString("Gravity", "");
    }

    public void setHumidityTest(String Humidity) {
        editor.putString("Humidity", Humidity);
        editor.commit();
    }

    public String getHumidityTest() {
        return pref.getString("Humidity", "");
    }

    public void setMotionDetectorTest(String MotionDetector) {
        editor.putString("Motion_Detector", MotionDetector);
        editor.commit();
    }

    public String getMotionDetectorTest() {
        return pref.getString("Motion_Detector", "");
    }

    public void setStepDetectorTest(String StepDetector) {
        editor.putString("Step_Detector", StepDetector);
        editor.commit();
    }

    public String getStepDetectorTest() {
        return pref.getString("Step_Detector", "");
    }

    public void setStepCounterTest(String StepCounter) {
        editor.putString("Step_Counter", StepCounter);
        editor.commit();
    }

    public String getStepCounterTest() {
        return pref.getString("Step_Counter", "");
    }

    public void setUVSensorTest(String UVSensor) {
        editor.putString("UV_Sensor", UVSensor);
        editor.commit();
    }

    public String getUVSensorTest() {
        return pref.getString("UV_Sensor", "");
    }

    public void setLightTest(String Light) {
        editor.putString("Light", Light);
        editor.commit();
    }

    public String getLightTest() {
        return pref.getString("Light", "");
    }

    public void setInfraredTest(String Infrared) {
        editor.putString("Infrared", Infrared);
        editor.commit();
    }

    public String getInfraredTest() {
        return pref.getString("Infrared", "");
    }

    public void sethallSensorTest(String hallSensor) {
        editor.putString("hallSensor", hallSensor);
        editor.commit();
    }

    public String gethallSensorTest() {
        return pref.getString("hallSensor", "");
    }

    public void setCpuPerformanceTest(String hallSensor) {
        editor.putString("cpuPerformance", hallSensor);
        editor.commit();
    }

    public String getCpuPerformanceTest() {
        return pref.getString("cpuPerformance", "");
    }

    public void setDeviceTemperatureTest(String DeviceTemperature) {
        editor.putString("DeviceTemperature", DeviceTemperature);
        editor.commit();
    }

    public String getDeviceTemperatureTest() {
        return pref.getString("DeviceTemperature", "");
    }

    public String getFmRadioTest() {
        return pref.getString("Fm_radio", "");
    }

    public void setFmRadioTest(String Fm_radio) {
        editor.putString("Fm_radio", Fm_radio);
        editor.commit();
    }

    // Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}