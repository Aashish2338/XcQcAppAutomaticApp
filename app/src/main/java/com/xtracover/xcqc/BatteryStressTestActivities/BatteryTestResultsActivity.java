package com.xtracover.xcqc.BatteryStressTestActivities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xtracover.xcqc.Activities.GetInTouchActivity;
import com.xtracover.xcqc.Interfaces.ApiClient;
import com.xtracover.xcqc.Models.UpdateTestResultResponse;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.ApiNetworkClient;
import com.xtracover.xcqc.Utilities.ErrorTestReportShow;
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class BatteryTestResultsActivity extends AppCompatActivity implements SensorEventListener {

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String imei11, str_ServiceKey = "", str_battery, batteryStatus, str_volt, str_temp, str_dischargeRate, str_batteryHealth = "";
    private String str_deadTest, str_battery_healthPer, str_estimatedCapacity, str_battery_designCapacity, str_battery_maxCurrent;
    private String initialBatteryCurrent, finalBatteryCurrent, str_Over_heating_test, str_finalVolt, str_final_Temp, str_DeviceID;
    private float str_initialVoltage, str_finalVoltage, str_initialTemp, str_finalTemp, str_batteryConsume, finalVolt, finalTemp;
    private LinearLayout batryStats, Status;
    private ProgressBar login_progress;
    private TextView battery_txt, batte_voltage, batte_voltage1, battery_temp, battery_temp1, battery_consume, battery_health,
            estimatedCapacity, battery_healthPer, battery_designCapacity, dischargeRate, battery_maxCurrent, battery_overheating,
            batteryinHr, battery_DeadTest, battery_initialCurrent, battery_finalCurrent, battery_initialVoltage, battery_finalVoltage,
            battery_initialTemp, battery_finalTemp, ambient_Temp, testDuration, service_keytxt;
    private Button home;
    private IntentFilter intentfilter;
    private int deviceHealth;
    private double safeVolt = 3.4, SafeVoltDropLimit, expectedDropVoltage, dischargeRateperHr;
    private SharedPreferences sharedPreferences;
    private SensorManager mSensorManager;
    private String ambientTemperature;
    private Sensor mAirPressure;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Battery Test Results Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_test_results);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);
        getLauoutUiId();

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        imei11 = sharedPreferences.getString("imei_1", "");
        str_DeviceID = sharedPreferences.getString("DeviceID", "");
        str_ServiceKey = userSession.getServiceKey();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        getAmbientTemperature();

        Bundle bundel = getIntent().getExtras();
        intentfilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        BatteryTestResultsActivity.this.registerReceiver(broadcastreceiver, intentfilter);

        if (bundel != null) {
            str_battery = bundel.getString("batteryString");
            batteryStatus = bundel.getString("batteryStatus");
            str_volt = bundel.getString("batteryVoltage");
            str_initialVoltage = bundel.getFloat("initialBatteryVoltage");
            str_finalVoltage = bundel.getFloat("finalBatteryVoltage");

            str_estimatedCapacity = bundel.getString("batteryEstimatedCapacity");
            str_battery_healthPer = bundel.getString("batteryHealth%");
            str_battery_designCapacity = bundel.getString("batteryCapacity_Mah");
            str_dischargeRate = bundel.getString("battery_life");
            str_battery_maxCurrent = bundel.getString("maximumCurrent");
            initialBatteryCurrent = bundel.getString("initialBatteryCurrent");
            finalBatteryCurrent = bundel.getString("finalBatteryCurrent");
            battery_initialCurrent.setText(initialBatteryCurrent + " mA");
            battery_finalCurrent.setText(finalBatteryCurrent + " mA");
            str_batteryConsume = bundel.getFloat("totalBatteryDec");
            dischargeRateperHr = str_batteryConsume * 3; // battery discharge in hr  // 20* 3 = 60 min
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            batteryinHr.setText(decimalFormat.format(dischargeRateperHr) + " hr");
            SafeVoltDropLimit = (str_initialVoltage - safeVolt) * 80;
            expectedDropVoltage = ((str_initialVoltage - str_finalVoltage) / 20) * 60 * (100 / dischargeRateperHr);

            if (SafeVoltDropLimit >= expectedDropVoltage) {
                battery_DeadTest.setText("Pass");
                str_deadTest = "Pass";
                System.out.println("Battery Dead Test :- " + str_deadTest);
            } else {
                battery_DeadTest.setText("Fail");
                str_deadTest = "Fail";
                System.out.println("Battery Dead Test :- " + str_deadTest);
            }

            battery_healthPer.setText(str_battery_healthPer + "%");
            estimatedCapacity.setText(str_estimatedCapacity + " mAh");
            battery_designCapacity.setText(str_battery_designCapacity + " mAh");
            dischargeRate.setText(str_dischargeRate + "%");
            battery_maxCurrent.setText(str_battery_maxCurrent + " mA");
            testDuration.setText("05 Min");

            if (str_initialVoltage > str_finalVoltage) {
                finalVolt = (str_initialVoltage - str_finalVoltage);
                str_finalVolt = "-" + (String.format(Locale.US, "%.2f", finalVolt));
                batte_voltage1.setTextColor(Color.parseColor("#00AE00"));
            } else {
                batte_voltage1.setTextColor(Color.parseColor("#FF0000"));
                finalVolt = (str_finalVoltage - str_initialVoltage);
                str_finalVolt = (String.format(Locale.US, "%.2f", finalVolt));
            }
            str_initialTemp = bundel.getFloat("initialBatteryTemp");
            str_finalTemp = bundel.getFloat("finalBatteryTemp");
            str_temp = bundel.getString("maxBatteryTemp");

            battery_finalVoltage.setText("" + (String.format(Locale.US, "%.2f", str_finalVoltage)) + "V");
            battery_initialVoltage.setText("" + (String.format(Locale.US, "%.2f", str_initialVoltage)) + "V");
            battery_initialTemp.setText("" + (String.format(Locale.US, "%.2f", str_initialTemp)) + "\u2103");
            battery_finalTemp.setText("" + (String.format(Locale.US, "%.2f", str_finalTemp)) + "\u2103");

            if (str_battery.equals("Battery_Status")) {
                Status.setVisibility(View.GONE);
                batryStats.setVisibility(View.VISIBLE);
                battery_txt.setText(batteryStatus);

                batte_voltage.setText(str_volt + "V, ");
                batte_voltage1.setText(str_finalVolt + "V");
                if (str_initialTemp > str_finalTemp) {
                    finalTemp = str_initialTemp - str_finalTemp;
                    str_final_Temp = "-" + (String.format(Locale.US, "%.2f", finalTemp));
                    battery_temp1.setTextColor(Color.parseColor("#00AE00"));
                    battery_temp.setText(str_temp + "\u2103" + ", ");
                    battery_temp1.setText(str_final_Temp + "\u2103");
                    if (str_finalTemp < 60) {
                        battery_overheating.setText("Pass");
                        str_Over_heating_test = "Pass";
                        System.out.println("Battery Over Heating :- " + str_Over_heating_test);
                    } else {
                        battery_overheating.setText("Fail");
                        str_Over_heating_test = "Fail";
                        System.out.println("Battery Over Heating :- " + str_Over_heating_test);
                    }
                } else {
                    finalTemp = str_initialTemp - str_finalTemp;
                    str_final_Temp = String.format(Locale.US, "%.2f", finalTemp);
                    battery_temp1.setTextColor(Color.parseColor("#FF0000"));
                    battery_temp.setText(str_temp + "\u2103" + ", ");
                    battery_temp1.setText(str_final_Temp + "\u2103");
                    if (str_finalTemp < 60) {
                        battery_overheating.setText("Pass");
                        str_Over_heating_test = "Pass";
                        System.out.println("Battery Over Heating :- " + "Pass");
                    } else {
                        battery_overheating.setText("Fail");
                        str_Over_heating_test = "Fail";
                        System.out.println("Battery Over Heating :- " + "Fail");
                    }
                }

                DecimalFormat decimalFormat1 = new DecimalFormat("0.00");
                battery_consume.setText("" + decimalFormat1.format(Math.abs(str_batteryConsume)) + "%");
            } else {
                service_keytxt.setText("" + str_ServiceKey);
                Status.setVisibility(View.VISIBLE);
                batryStats.setVisibility(View.GONE);
            }
        }

        if (!(batteryStatus == null) && !(str_deadTest == null) && !(str_Over_heating_test == null)) {
            storeBatteryStatus(batteryStatus, str_batteryHealth, str_deadTest, str_Over_heating_test);
        } else {
            System.out.println("Data Already Successfully Uploaded!");
        }

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getLauoutUiId() {
        try {
            home = (Button) findViewById(R.id.home);
            batryStats = (LinearLayout) findViewById(R.id.batryStats);
            Status = (LinearLayout) findViewById(R.id.Status);
            battery_txt = (TextView) findViewById(R.id.battery_txt);
            batte_voltage = (TextView) findViewById(R.id.batte_voltage);
            batte_voltage1 = (TextView) findViewById(R.id.batte_voltage1);
            battery_temp = (TextView) findViewById(R.id.battery_temp);
            battery_temp1 = (TextView) findViewById(R.id.battery_temp1);
            battery_consume = (TextView) findViewById(R.id.battery_consume);
            battery_overheating = (TextView) findViewById(R.id.battery_overheating);
            batteryinHr = (TextView) findViewById(R.id.batteryinHr);
            battery_DeadTest = (TextView) findViewById(R.id.battery_DeadTest);
            ambient_Temp = (TextView) findViewById(R.id.ambient_Temp);
            testDuration = (TextView) findViewById(R.id.testDuration);

            battery_initialCurrent = (TextView) findViewById(R.id.battery_initialCurrent);
            battery_finalCurrent = (TextView) findViewById(R.id.battery_finalCurrent);
            battery_initialVoltage = (TextView) findViewById(R.id.battery_initialVoltage);
            battery_finalVoltage = (TextView) findViewById(R.id.battery_finalVoltage);
            battery_initialTemp = (TextView) findViewById(R.id.battery_initialTemp);
            battery_finalTemp = (TextView) findViewById(R.id.battery_finalTemp);

            battery_healthPer = (TextView) findViewById(R.id.battery_healthPer);
            estimatedCapacity = (TextView) findViewById(R.id.estimatedCapacity);
            battery_designCapacity = (TextView) findViewById(R.id.battery_designCapacity);
            dischargeRate = (TextView) findViewById(R.id.dischargeRate);
            battery_maxCurrent = (TextView) findViewById(R.id.battery_maxCurrent);
            login_progress = (ProgressBar) findViewById(R.id.login_progress);

            battery_health = (TextView) findViewById(R.id.battery_health);
            service_keytxt = (TextView) findViewById(R.id.service_key);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BatteryTestResultsActivity Exception :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private BroadcastReceiver broadcastreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                deviceHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
                switch (deviceHealth) {
                    case BatteryManager.BATTERY_HEALTH_COLD:
                        battery_health.setText("Cold");
                        str_batteryHealth = "Cold";
                        System.out.println("Battery Health :- " + str_batteryHealth);
                        break;

                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        battery_health.setText("Dead");
                        str_batteryHealth = "Dead";
                        System.out.println("Battery Health :- " + str_batteryHealth);
                        break;

                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        battery_health.setText("Good");
                        str_batteryHealth = "Good";
                        System.out.println("Battery Health :- " + str_batteryHealth);
                        break;

                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        battery_health.setText("OverHeat");
                        str_batteryHealth = "OverHeat";
                        System.out.println("Battery Health :- " + str_batteryHealth);
                        break;

                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        battery_health.setText("Over voltage");
                        str_batteryHealth = "Over voltage";
                        System.out.println("Battery Health :- " + str_batteryHealth);
                        break;

                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        battery_health.setText("Unspecified Failure");
                        str_batteryHealth = "Unspecified Failure";
                        System.out.println("Battery Health :- " + str_batteryHealth);
                        break;

                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        battery_health.setText("Unspecified Error");
                        str_batteryHealth = "Unspecified Error";
                        System.out.println("Battery Health :- " + str_batteryHealth);
                        break;

                    default:
                        break;
                }
            } catch (Exception exp) {
                exp.getStackTrace();
                activity_Error = "BatteryTestResultsActivity Exception :- " + exp.getMessage() + ", " + exp.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        }
    };

    private void storeBatteryStatus(String statusOfBattery, String batteryHealth, String batteryDeadTest, String overHeatingTest) {

      /*  JSONObject jsonObj;
        HashMap<String, String> param = new HashMap<String, String>();
        HashMap<String, JSONObject> param1 = new HashMap<String, JSONObject>();
        param.put("IMEI", str_ServiceKey);
//        param.put("IMEI", str_DeviceID);
        param.put("BatterytestStatus", statusOfBattery);
        param.put("Health", batteryHealth);
        param.put("Health_Parcent", str_battery_healthPer);
        param.put("Battery_Design_Capacity", str_battery_designCapacity);
//        param.put("TestDuration", "20 min");
        param.put("TestDuration", "5 min");
        param.put("MaxCurrent", str_battery_maxCurrent + "mA");
        param.put("BatteryVoltage", str_volt + "V");
        param.put("BatteryUsage", "" + Math.abs(str_batteryConsume));
        param.put("Discharge_Rate_per_Hours", str_dischargeRate + "");
        param.put("Estimated_Capacity", str_estimatedCapacity + " mAh");
        param.put("Battery_temperature", str_temp + "\u2103");
        param.put("Fully_charged_battery_can_run_for", dischargeRateperHr + " hr");
        param.put("Dead_test_result", batteryDeadTest);
        param.put("Over_heating_test", overHeatingTest);
        jsonObj = new JSONObject(param);
        System.out.println("Battery Check Status :- " + jsonObj);*/
        try {
            String jsonData = ApiJsonUpdateTestResult(statusOfBattery, batteryHealth, batteryDeadTest, overHeatingTest).toString();
            Log.d("Battery Json Data :- ", jsonData);

            ApiClient apiClient = ApiNetworkClient.getStoreApiRetrofit().create(ApiClient.class);
            compositeDisposable.add(apiClient.updateNewCheckBatteryStatusReport(ApiJsonUpdateTestResult(statusOfBattery, batteryHealth, batteryDeadTest, overHeatingTest)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<UpdateTestResultResponse>() {
                        @Override
                        public void onSuccess(@NonNull UpdateTestResultResponse updateTestResultResponse) {
                            System.out.println("Update Status :- " + updateTestResultResponse.toString());
                            if (updateTestResultResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                System.out.println("Battery Status :- " + batteryStatus + ", Battery Health :- " + str_batteryHealth + ", Battery Dead :- "
                                        + str_deadTest + ", Battery Over Heating :- " + str_Over_heating_test);
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, "Error to save Data!", Toast.LENGTH_SHORT).show();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private JsonObject ApiJsonUpdateTestResult(String statusOfBattery, String batteryHealth, String batteryDeadTest, String overHeatingTest) {
        JsonObject gsonObjectUpdateTestResult = new JsonObject();
        try {
            JSONObject paramAbTestResult = new JSONObject();
            paramAbTestResult.put("IMEI", str_ServiceKey);
            paramAbTestResult.put("BatterytestStatus", statusOfBattery);
            paramAbTestResult.put("Health", batteryHealth);
            paramAbTestResult.put("Health_Parcent", str_battery_healthPer);
            paramAbTestResult.put("Battery_Design_Capacity", str_battery_designCapacity);
            paramAbTestResult.put("TestDuration", "5 Min");
            paramAbTestResult.put("MaxCurrent", str_battery_maxCurrent + "mA");
            paramAbTestResult.put("BatteryVoltage", str_volt + "V");
            paramAbTestResult.put("BatteryUsage", "" + Math.abs(str_batteryConsume));
            paramAbTestResult.put("Discharge_Rate_per_Hours", str_dischargeRate + "");
            paramAbTestResult.put("Estimated_Capacity", str_estimatedCapacity + " mAh");
            paramAbTestResult.put("Battery_temperature", str_estimatedCapacity + " mAh");
            paramAbTestResult.put("Fully_charged_battery_can_run_for", dischargeRateperHr + " hr");
            paramAbTestResult.put("Dead_test_result", batteryDeadTest);
            paramAbTestResult.put("Over_heating_test", overHeatingTest);

            JsonParser jsonParser = new JsonParser();
            gsonObjectUpdateTestResult = (JsonObject) jsonParser.parse(paramAbTestResult.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return gsonObjectUpdateTestResult;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            Intent intent = new Intent(mContext, GetInTouchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);
            finish();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BatteryTestResultsActivity Exception :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getAmbientTemperature() {
        try {
            mAirPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            if (mAirPressure == null) {
                ambientTemperature = "skip";
            } else {
                ambientTemperature = "Pass";
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BatteryTestResultsActivity Exception :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                if (ambientTemperature.equals("Pass")) {
                    ambient_Temp.setText("" + event.values[0]);
                } else {
                    ambient_Temp.setText("NA");
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BatteryTestResultsActivity Exception :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        mContext.registerReceiver(broadcastreceiver, intentfilter);
    }
}