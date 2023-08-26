package com.xtracover.xcqc.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class QCTestListData {
    @SerializedName("mstid")
    @Expose
    private Integer mstid;
    @SerializedName("act")
    @Expose
    private String act;
    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("imei_1")
    @Expose
    private String imei1;
    @SerializedName("imei_2")
    @Expose
    private String imei2;
    @SerializedName("MacAddress")
    @Expose
    private String macAddress;
    @SerializedName("device_category")
    @Expose
    private String deviceCategory;
    @SerializedName("brand_name")
    @Expose
    private String brandName;
    @SerializedName("model_name")
    @Expose
    private String modelName;
    @SerializedName("device_id")
    @Expose
    private String deviceId;
    @SerializedName("screen_size")
    @Expose
    private String screenSize;
    @SerializedName("storage")
    @Expose
    private String storage;
    @SerializedName("front_camera_mp")
    @Expose
    private String frontCameraMp;
    @SerializedName("rear_camera_mp")
    @Expose
    private String rearCameraMp;
    @SerializedName("processor_core")
    @Expose
    private String processorCore;
    @SerializedName("battery_capacity")
    @Expose
    private String batteryCapacity;
    @SerializedName("os")
    @Expose
    private String os;
    @SerializedName("os_versio")
    @Expose
    private String osVersio;
    @SerializedName("serial_number")
    @Expose
    private String serialNumber;
    @SerializedName("score")
    @Expose
    private String score;
    @SerializedName("merchant_id")
    @Expose
    private String merchantId;
    @SerializedName("physical_condition_category")
    @Expose
    private String physicalConditionCategory;
    @SerializedName("workorderid")
    @Expose
    private String workorderid;
    @SerializedName("createdBy")
    @Expose
    private String createdBy;
    @SerializedName("QCResult")
    @Expose
    private String qCResult;
    @SerializedName("ServiceKey")
    @Expose
    private String serviceKey;
    @SerializedName("CreatedOn")
    @Expose
    private String createdOn;
    @SerializedName("deviceType")
    @Expose
    private Object deviceType;
    @SerializedName("PlanType")
    @Expose
    private Object planType;
    @SerializedName("partnercode")
    @Expose
    private Object partnercode;
    @SerializedName("IsUpload")
    @Expose
    private Boolean isUpload;
    @SerializedName("mstChildid")
    @Expose
    private Integer mstChildid;
    @SerializedName("Call_SIM_1")
    @Expose
    private String callSIM1;
    @SerializedName("Call_SIM_2")
    @Expose
    private String callSIM2;
    @SerializedName("WiFi")
    @Expose
    private String wiFi;
    @SerializedName("Internet")
    @Expose
    private String internet;
    @SerializedName("IMEI_VALIDATION")
    @Expose
    private String imeiValidation;
    @SerializedName("Vibrate")
    @Expose
    private String vibrate;
    @SerializedName("Back_Camera")
    @Expose
    private String backCamera;
    @SerializedName("Front_Camera")
    @Expose
    private String frontCamera;
    @SerializedName("Battery")
    @Expose
    private String battery;
    @SerializedName("DEAD_PIXEL_CHECK")
    @Expose
    private String deadPixelCheck;
    @SerializedName("Internal_Storage")
    @Expose
    private String internalStorage;
    @SerializedName("External_Storage")
    @Expose
    private String externalStorage;
    @SerializedName("Proximity")
    @Expose
    private String proximity;
    @SerializedName("Bluetooth")
    @Expose
    private String bluetooth;
    @SerializedName("Earphone_Jack")
    @Expose
    private String earphoneJack;
    @SerializedName("LoudSpeaker")
    @Expose
    private String loudSpeaker;
    @SerializedName("Front_speaker")
    @Expose
    private String frontSpeaker;
    @SerializedName("Camera_Auto_Focus")
    @Expose
    private String cameraAutoFocus;
    @SerializedName("GPS")
    @Expose
    private String gps;
    @SerializedName("Display_Dead_Pixel")
    @Expose
    private String displayDeadPixel;
    @SerializedName("Display_Touch_Screen")
    @Expose
    private String displayTouchScreen;
    @SerializedName("Volume_Up_Button")
    @Expose
    private String volumeUpButton;
    @SerializedName("Volume_Down_Button")
    @Expose
    private String volumeDownButton;
    @SerializedName("Home_Key")
    @Expose
    private String homeKey;
    @SerializedName("Back_Key")
    @Expose
    private String backKey;
    @SerializedName("Menu_Key")
    @Expose
    private String menuKey;
    @SerializedName("Back_Video_Recording")
    @Expose
    private String backVideoRecording;
    @SerializedName("Front_Video_Recording")
    @Expose
    private String frontVideoRecording;
    @SerializedName("USB")
    @Expose
    private String usb;
    @SerializedName("Microphone")
    @Expose
    private String microphone;
    @SerializedName("Biometric")
    @Expose
    private String biometric;
    @SerializedName("Gyroscope")
    @Expose
    private String gyroscope;
    @SerializedName("Screen_Lock")
    @Expose
    private String screenLock;
    @SerializedName("battarystatus")
    @Expose
    private Object battarystatus;
    @SerializedName("Network_Signal_sim1")
    @Expose
    private String networkSignalSim1;
    @SerializedName("Network_Signal_sim2")
    @Expose
    private String networkSignalSim2;
    @SerializedName("Flash")
    @Expose
    private String flash;
    @SerializedName("Earphone")
    @Expose
    private String earphone;
    @SerializedName("Display_brightness")
    @Expose
    private String displayBrightness;
    @SerializedName("Light")
    @Expose
    private String light;
    @SerializedName("AllKeys")
    @Expose
    private String allKeys;
    @SerializedName("Background")
    @Expose
    private String background;
    @SerializedName("DeviceTemperature")
    @Expose
    private String deviceTemperature;
    @SerializedName("Tourch")
    @Expose
    private String tourch;
    @SerializedName("NFC")
    @Expose
    private String nfc;
    @SerializedName("Power_Key")
    @Expose
    private String powerKey;
    @SerializedName("Air_Pressure")
    @Expose
    private String airPressure;
    @SerializedName("Air_Temperature")
    @Expose
    private String airTemperature;
    @SerializedName("Gravity")
    @Expose
    private String gravity;
    @SerializedName("Infrared")
    @Expose
    private String infrared;
    @SerializedName("GyroscopeGaming")
    @Expose
    private String gyroscopeGaming;
    @SerializedName("Orientation")
    @Expose
    private String orientation;
    @SerializedName("Humidity")
    @Expose
    private String humidity;
    @SerializedName("HRM")
    @Expose
    private String hrm;
    @SerializedName("SPO_2")
    @Expose
    private String spo2;
    @SerializedName("Notification_LED")
    @Expose
    private String notificationLED;
    @SerializedName("Swing")
    @Expose
    private String swing;
    @SerializedName("Motion_Detector")
    @Expose
    private String motionDetector;
    @SerializedName("Step_Detector")
    @Expose
    private String stepDetector;
    @SerializedName("Step_Counter")
    @Expose
    private String stepCounter;
    @SerializedName("UV_Sensor")
    @Expose
    private String uVSensor;
    @SerializedName("Flip_Action")
    @Expose
    private String flipAction;
    @SerializedName("Stylus")
    @Expose
    private String stylus;
    @SerializedName("Totaltest")
    @Expose
    private String totaltest;
    @SerializedName("Testpassed")
    @Expose
    private String testpassed;
    @SerializedName("Testfailed")
    @Expose
    private String testfailed;
    @SerializedName("Testnotperformed")
    @Expose
    private String testnotperformed;
    @SerializedName("TestnotApplicable")
    @Expose
    private String testnotApplicable;
    @SerializedName("boxCondition")
    @Expose
    private String boxCondition;
    @SerializedName("TouchGlassBroken")
    @Expose
    private String touchGlassBroken;
    @SerializedName("DisplayOG")
    @Expose
    private String displayOG;
    @SerializedName("Displayspot")
    @Expose
    private String displayspot;
    @SerializedName("ChargingTest")
    @Expose
    private String chargingTest;
    @SerializedName("Multifinger_test")
    @Expose
    private String multifingerTest;
    @SerializedName("OtgTest")
    @Expose
    private String otgTest;
    @SerializedName("screenDefect")
    @Expose
    private String screenDefect;
    @SerializedName("handset_mic")
    @Expose
    private String handsetMic;
    @SerializedName("handset_mic_keys")
    @Expose
    private String handsetMicKeys;
    @SerializedName("Fm_radio")
    @Expose
    private String fmRadio;
    @SerializedName("Front_camera_flash")
    @Expose
    private String frontCameraFlash;
    @SerializedName("audioPlakbackTest")
    @Expose
    private String audioPlakbackTest;
    @SerializedName("hallSensor")
    @Expose
    private String hallSensor;
    @SerializedName("deviceTempTest")
    @Expose
    private Object deviceTempTest;
    @SerializedName("NoiseCancellationTest")
    @Expose
    private String noiseCancellationTest;
    @SerializedName("volteCallingTest")
    @Expose
    private String volteCallingTest;
    @SerializedName("batteryStorageCapacity")
    @Expose
    private String batteryStorageCapacity;
    @SerializedName("cpuPerformance")
    @Expose
    private String cpuPerformance;
    @SerializedName("Grade")
    @Expose
    private Object grade;
    @SerializedName("AppExt")
    @Expose
    private Object appExt;
    @SerializedName("Charger_Adapter")
    @Expose
    private Object chargerAdapter;
    @SerializedName("USB_Cable")
    @Expose
    private Object uSBCable;
    @SerializedName("Sim_Remover")
    @Expose
    private Object simRemover;
    @SerializedName("Box_damage")
    @Expose
    private Object boxDamage;
    @SerializedName("Data_Wipe")
    @Expose
    private Object dataWipe;
    @SerializedName("Sim_Tray")
    @Expose
    private Object simTray;
    @SerializedName("Box_Imei_number")
    @Expose
    private Object boxImeiNumber;
    @SerializedName("OEM_Warranty_greater_6_Month")
    @Expose
    private Object oEMWarrantyGreater6Month;

    public Integer getMstid() {
        return mstid;
    }

    public void setMstid(Integer mstid) {
        this.mstid = mstid;
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImei1() {
        return imei1;
    }

    public void setImei1(String imei1) {
        this.imei1 = imei1;
    }

    public String getImei2() {
        return imei2;
    }

    public void setImei2(String imei2) {
        this.imei2 = imei2;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(String deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getFrontCameraMp() {
        return frontCameraMp;
    }

    public void setFrontCameraMp(String frontCameraMp) {
        this.frontCameraMp = frontCameraMp;
    }

    public String getRearCameraMp() {
        return rearCameraMp;
    }

    public void setRearCameraMp(String rearCameraMp) {
        this.rearCameraMp = rearCameraMp;
    }

    public String getProcessorCore() {
        return processorCore;
    }

    public void setProcessorCore(String processorCore) {
        this.processorCore = processorCore;
    }

    public String getBatteryCapacity() {
        return batteryCapacity;
    }

    public void setBatteryCapacity(String batteryCapacity) {
        this.batteryCapacity = batteryCapacity;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOsVersio() {
        return osVersio;
    }

    public void setOsVersio(String osVersio) {
        this.osVersio = osVersio;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getPhysicalConditionCategory() {
        return physicalConditionCategory;
    }

    public void setPhysicalConditionCategory(String physicalConditionCategory) {
        this.physicalConditionCategory = physicalConditionCategory;
    }

    public String getWorkorderid() {
        return workorderid;
    }

    public void setWorkorderid(String workorderid) {
        this.workorderid = workorderid;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getQCResult() {
        return qCResult;
    }

    public void setQCResult(String qCResult) {
        this.qCResult = qCResult;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public Object getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Object deviceType) {
        this.deviceType = deviceType;
    }

    public Object getPlanType() {
        return planType;
    }

    public void setPlanType(Object planType) {
        this.planType = planType;
    }

    public Object getPartnercode() {
        return partnercode;
    }

    public void setPartnercode(Object partnercode) {
        this.partnercode = partnercode;
    }

    public Boolean getIsUpload() {
        return isUpload;
    }

    public void setIsUpload(Boolean isUpload) {
        this.isUpload = isUpload;
    }

    public Integer getMstChildid() {
        return mstChildid;
    }

    public void setMstChildid(Integer mstChildid) {
        this.mstChildid = mstChildid;
    }

    public String getCallSIM1() {
        return callSIM1;
    }

    public void setCallSIM1(String callSIM1) {
        this.callSIM1 = callSIM1;
    }

    public String getCallSIM2() {
        return callSIM2;
    }

    public void setCallSIM2(String callSIM2) {
        this.callSIM2 = callSIM2;
    }

    public String getWiFi() {
        return wiFi;
    }

    public void setWiFi(String wiFi) {
        this.wiFi = wiFi;
    }

    public String getInternet() {
        return internet;
    }

    public void setInternet(String internet) {
        this.internet = internet;
    }

    public String getImeiValidation() {
        return imeiValidation;
    }

    public void setImeiValidation(String imeiValidation) {
        this.imeiValidation = imeiValidation;
    }

    public String getVibrate() {
        return vibrate;
    }

    public void setVibrate(String vibrate) {
        this.vibrate = vibrate;
    }

    public String getBackCamera() {
        return backCamera;
    }

    public void setBackCamera(String backCamera) {
        this.backCamera = backCamera;
    }

    public String getFrontCamera() {
        return frontCamera;
    }

    public void setFrontCamera(String frontCamera) {
        this.frontCamera = frontCamera;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getDeadPixelCheck() {
        return deadPixelCheck;
    }

    public void setDeadPixelCheck(String deadPixelCheck) {
        this.deadPixelCheck = deadPixelCheck;
    }

    public String getInternalStorage() {
        return internalStorage;
    }

    public void setInternalStorage(String internalStorage) {
        this.internalStorage = internalStorage;
    }

    public String getExternalStorage() {
        return externalStorage;
    }

    public void setExternalStorage(String externalStorage) {
        this.externalStorage = externalStorage;
    }

    public String getProximity() {
        return proximity;
    }

    public void setProximity(String proximity) {
        this.proximity = proximity;
    }

    public String getBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(String bluetooth) {
        this.bluetooth = bluetooth;
    }

    public String getEarphoneJack() {
        return earphoneJack;
    }

    public void setEarphoneJack(String earphoneJack) {
        this.earphoneJack = earphoneJack;
    }

    public String getLoudSpeaker() {
        return loudSpeaker;
    }

    public void setLoudSpeaker(String loudSpeaker) {
        this.loudSpeaker = loudSpeaker;
    }

    public String getFrontSpeaker() {
        return frontSpeaker;
    }

    public void setFrontSpeaker(String frontSpeaker) {
        this.frontSpeaker = frontSpeaker;
    }

    public String getCameraAutoFocus() {
        return cameraAutoFocus;
    }

    public void setCameraAutoFocus(String cameraAutoFocus) {
        this.cameraAutoFocus = cameraAutoFocus;
    }

    public String getGps() {
        return gps;
    }

    public void setGps(String gps) {
        this.gps = gps;
    }

    public String getDisplayDeadPixel() {
        return displayDeadPixel;
    }

    public void setDisplayDeadPixel(String displayDeadPixel) {
        this.displayDeadPixel = displayDeadPixel;
    }

    public String getDisplayTouchScreen() {
        return displayTouchScreen;
    }

    public void setDisplayTouchScreen(String displayTouchScreen) {
        this.displayTouchScreen = displayTouchScreen;
    }

    public String getVolumeUpButton() {
        return volumeUpButton;
    }

    public void setVolumeUpButton(String volumeUpButton) {
        this.volumeUpButton = volumeUpButton;
    }

    public String getVolumeDownButton() {
        return volumeDownButton;
    }

    public void setVolumeDownButton(String volumeDownButton) {
        this.volumeDownButton = volumeDownButton;
    }

    public String getHomeKey() {
        return homeKey;
    }

    public void setHomeKey(String homeKey) {
        this.homeKey = homeKey;
    }

    public String getBackKey() {
        return backKey;
    }

    public void setBackKey(String backKey) {
        this.backKey = backKey;
    }

    public String getMenuKey() {
        return menuKey;
    }

    public void setMenuKey(String menuKey) {
        this.menuKey = menuKey;
    }

    public String getBackVideoRecording() {
        return backVideoRecording;
    }

    public void setBackVideoRecording(String backVideoRecording) {
        this.backVideoRecording = backVideoRecording;
    }

    public String getFrontVideoRecording() {
        return frontVideoRecording;
    }

    public void setFrontVideoRecording(String frontVideoRecording) {
        this.frontVideoRecording = frontVideoRecording;
    }

    public String getUsb() {
        return usb;
    }

    public void setUsb(String usb) {
        this.usb = usb;
    }

    public String getMicrophone() {
        return microphone;
    }

    public void setMicrophone(String microphone) {
        this.microphone = microphone;
    }

    public String getBiometric() {
        return biometric;
    }

    public void setBiometric(String biometric) {
        this.biometric = biometric;
    }

    public String getGyroscope() {
        return gyroscope;
    }

    public void setGyroscope(String gyroscope) {
        this.gyroscope = gyroscope;
    }

    public String getScreenLock() {
        return screenLock;
    }

    public void setScreenLock(String screenLock) {
        this.screenLock = screenLock;
    }

    public Object getBattarystatus() {
        return battarystatus;
    }

    public void setBattarystatus(Object battarystatus) {
        this.battarystatus = battarystatus;
    }

    public String getNetworkSignalSim1() {
        return networkSignalSim1;
    }

    public void setNetworkSignalSim1(String networkSignalSim1) {
        this.networkSignalSim1 = networkSignalSim1;
    }

    public String getNetworkSignalSim2() {
        return networkSignalSim2;
    }

    public void setNetworkSignalSim2(String networkSignalSim2) {
        this.networkSignalSim2 = networkSignalSim2;
    }

    public String getFlash() {
        return flash;
    }

    public void setFlash(String flash) {
        this.flash = flash;
    }

    public String getEarphone() {
        return earphone;
    }

    public void setEarphone(String earphone) {
        this.earphone = earphone;
    }

    public String getDisplayBrightness() {
        return displayBrightness;
    }

    public void setDisplayBrightness(String displayBrightness) {
        this.displayBrightness = displayBrightness;
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }

    public String getAllKeys() {
        return allKeys;
    }

    public void setAllKeys(String allKeys) {
        this.allKeys = allKeys;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getDeviceTemperature() {
        return deviceTemperature;
    }

    public void setDeviceTemperature(String deviceTemperature) {
        this.deviceTemperature = deviceTemperature;
    }

    public String getTourch() {
        return tourch;
    }

    public void setTourch(String tourch) {
        this.tourch = tourch;
    }

    public String getNfc() {
        return nfc;
    }

    public void setNfc(String nfc) {
        this.nfc = nfc;
    }

    public String getPowerKey() {
        return powerKey;
    }

    public void setPowerKey(String powerKey) {
        this.powerKey = powerKey;
    }

    public String getAirPressure() {
        return airPressure;
    }

    public void setAirPressure(String airPressure) {
        this.airPressure = airPressure;
    }

    public String getAirTemperature() {
        return airTemperature;
    }

    public void setAirTemperature(String airTemperature) {
        this.airTemperature = airTemperature;
    }

    public String getGravity() {
        return gravity;
    }

    public void setGravity(String gravity) {
        this.gravity = gravity;
    }

    public String getInfrared() {
        return infrared;
    }

    public void setInfrared(String infrared) {
        this.infrared = infrared;
    }

    public String getGyroscopeGaming() {
        return gyroscopeGaming;
    }

    public void setGyroscopeGaming(String gyroscopeGaming) {
        this.gyroscopeGaming = gyroscopeGaming;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getHrm() {
        return hrm;
    }

    public void setHrm(String hrm) {
        this.hrm = hrm;
    }

    public String getSpo2() {
        return spo2;
    }

    public void setSpo2(String spo2) {
        this.spo2 = spo2;
    }

    public String getNotificationLED() {
        return notificationLED;
    }

    public void setNotificationLED(String notificationLED) {
        this.notificationLED = notificationLED;
    }

    public String getSwing() {
        return swing;
    }

    public void setSwing(String swing) {
        this.swing = swing;
    }

    public String getMotionDetector() {
        return motionDetector;
    }

    public void setMotionDetector(String motionDetector) {
        this.motionDetector = motionDetector;
    }

    public String getStepDetector() {
        return stepDetector;
    }

    public void setStepDetector(String stepDetector) {
        this.stepDetector = stepDetector;
    }

    public String getStepCounter() {
        return stepCounter;
    }

    public void setStepCounter(String stepCounter) {
        this.stepCounter = stepCounter;
    }

    public String getUVSensor() {
        return uVSensor;
    }

    public void setUVSensor(String uVSensor) {
        this.uVSensor = uVSensor;
    }

    public String getFlipAction() {
        return flipAction;
    }

    public void setFlipAction(String flipAction) {
        this.flipAction = flipAction;
    }

    public String getStylus() {
        return stylus;
    }

    public void setStylus(String stylus) {
        this.stylus = stylus;
    }

    public String getTotaltest() {
        return totaltest;
    }

    public void setTotaltest(String totaltest) {
        this.totaltest = totaltest;
    }

    public String getTestpassed() {
        return testpassed;
    }

    public void setTestpassed(String testpassed) {
        this.testpassed = testpassed;
    }

    public String getTestfailed() {
        return testfailed;
    }

    public void setTestfailed(String testfailed) {
        this.testfailed = testfailed;
    }

    public String getTestnotperformed() {
        return testnotperformed;
    }

    public void setTestnotperformed(String testnotperformed) {
        this.testnotperformed = testnotperformed;
    }

    public String getTestnotApplicable() {
        return testnotApplicable;
    }

    public void setTestnotApplicable(String testnotApplicable) {
        this.testnotApplicable = testnotApplicable;
    }

    public String getBoxCondition() {
        return boxCondition;
    }

    public void setBoxCondition(String boxCondition) {
        this.boxCondition = boxCondition;
    }

    public String getTouchGlassBroken() {
        return touchGlassBroken;
    }

    public void setTouchGlassBroken(String touchGlassBroken) {
        this.touchGlassBroken = touchGlassBroken;
    }

    public String getDisplayOG() {
        return displayOG;
    }

    public void setDisplayOG(String displayOG) {
        this.displayOG = displayOG;
    }

    public String getDisplayspot() {
        return displayspot;
    }

    public void setDisplayspot(String displayspot) {
        this.displayspot = displayspot;
    }

    public String getChargingTest() {
        return chargingTest;
    }

    public void setChargingTest(String chargingTest) {
        this.chargingTest = chargingTest;
    }

    public String getMultifingerTest() {
        return multifingerTest;
    }

    public void setMultifingerTest(String multifingerTest) {
        this.multifingerTest = multifingerTest;
    }

    public String getOtgTest() {
        return otgTest;
    }

    public void setOtgTest(String otgTest) {
        this.otgTest = otgTest;
    }

    public String getScreenDefect() {
        return screenDefect;
    }

    public void setScreenDefect(String screenDefect) {
        this.screenDefect = screenDefect;
    }

    public String getHandsetMic() {
        return handsetMic;
    }

    public void setHandsetMic(String handsetMic) {
        this.handsetMic = handsetMic;
    }

    public String getHandsetMicKeys() {
        return handsetMicKeys;
    }

    public void setHandsetMicKeys(String handsetMicKeys) {
        this.handsetMicKeys = handsetMicKeys;
    }

    public String getFmRadio() {
        return fmRadio;
    }

    public void setFmRadio(String fmRadio) {
        this.fmRadio = fmRadio;
    }

    public String getFrontCameraFlash() {
        return frontCameraFlash;
    }

    public void setFrontCameraFlash(String frontCameraFlash) {
        this.frontCameraFlash = frontCameraFlash;
    }

    public String getAudioPlakbackTest() {
        return audioPlakbackTest;
    }

    public void setAudioPlakbackTest(String audioPlakbackTest) {
        this.audioPlakbackTest = audioPlakbackTest;
    }

    public String getHallSensor() {
        return hallSensor;
    }

    public void setHallSensor(String hallSensor) {
        this.hallSensor = hallSensor;
    }

    public Object getDeviceTempTest() {
        return deviceTempTest;
    }

    public void setDeviceTempTest(Object deviceTempTest) {
        this.deviceTempTest = deviceTempTest;
    }

    public String getNoiseCancellationTest() {
        return noiseCancellationTest;
    }

    public void setNoiseCancellationTest(String noiseCancellationTest) {
        this.noiseCancellationTest = noiseCancellationTest;
    }

    public String getVolteCallingTest() {
        return volteCallingTest;
    }

    public void setVolteCallingTest(String volteCallingTest) {
        this.volteCallingTest = volteCallingTest;
    }

    public String getBatteryStorageCapacity() {
        return batteryStorageCapacity;
    }

    public void setBatteryStorageCapacity(String batteryStorageCapacity) {
        this.batteryStorageCapacity = batteryStorageCapacity;
    }

    public String getCpuPerformance() {
        return cpuPerformance;
    }

    public void setCpuPerformance(String cpuPerformance) {
        this.cpuPerformance = cpuPerformance;
    }

    public Object getGrade() {
        return grade;
    }

    public void setGrade(Object grade) {
        this.grade = grade;
    }

    public Object getAppExt() {
        return appExt;
    }

    public void setAppExt(Object appExt) {
        this.appExt = appExt;
    }

    public Object getChargerAdapter() {
        return chargerAdapter;
    }

    public void setChargerAdapter(Object chargerAdapter) {
        this.chargerAdapter = chargerAdapter;
    }

    public Object getUSBCable() {
        return uSBCable;
    }

    public void setUSBCable(Object uSBCable) {
        this.uSBCable = uSBCable;
    }

    public Object getSimRemover() {
        return simRemover;
    }

    public void setSimRemover(Object simRemover) {
        this.simRemover = simRemover;
    }

    public Object getBoxDamage() {
        return boxDamage;
    }

    public void setBoxDamage(Object boxDamage) {
        this.boxDamage = boxDamage;
    }

    public Object getDataWipe() {
        return dataWipe;
    }

    public void setDataWipe(Object dataWipe) {
        this.dataWipe = dataWipe;
    }

    public Object getSimTray() {
        return simTray;
    }

    public void setSimTray(Object simTray) {
        this.simTray = simTray;
    }

    public Object getBoxImeiNumber() {
        return boxImeiNumber;
    }

    public void setBoxImeiNumber(Object boxImeiNumber) {
        this.boxImeiNumber = boxImeiNumber;
    }

    public Object getOEMWarrantyGreater6Month() {
        return oEMWarrantyGreater6Month;
    }

    public void setOEMWarrantyGreater6Month(Object oEMWarrantyGreater6Month) {
        this.oEMWarrantyGreater6Month = oEMWarrantyGreater6Month;
    }
}