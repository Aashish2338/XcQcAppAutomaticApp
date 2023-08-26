package com.xtracover.xcqc.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.UserSession;

import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;

public class GradingActivity extends AppCompatActivity {

    private Context mContext;
    private TextView tv_grade, application_Version;
    private SwitchMaterial aSwitch1;
    private TextToSpeech textToSpeech;
    private Button single_Test;
    private RadioGroup body_damages_rg, lcd_glass_damage_rg, lcd_damage_rg, pasting_issue_rg, yellow_border_rg, part_missing_rg,
            body_dents_rg, paint_peel_off_rg, scratches_on_back_cover_body_rg, camera_glass_rg, scratches_on_screen_rg, display_patch_dot_shade_rg;
    private String str_grade = "", str_body_damages = "", str_lcd_glass_damage = "", str_lcd_damage = "", str_pasting_issue = "", str_yellow_border = "",
            str_part_missing = "", str_icloud_lock = "", str_ios_upgrade = "", str_country_lock = "", str_body_dents = "", str_paint_peel_off = "",
            str_scratches_on_back_cover_body = "", str_camera_glass = "", str_scratches_on_screen = "", str_display_patch_dot_shade = "",
            str_switch_status = "OFF", str_speak = "";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private UserSession userSession;
    private CompositeDisposable disposable;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grading);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        disposable = new CompositeDisposable();
        getLayoutUiIdfinds();
        getSoftwareVersion();

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
//        body_damages_rg.clearCheck();
        str_switch_status = sharedPreferences.getString("Voice_Assistant", "");
        aSwitch1.setChecked(str_switch_status.equalsIgnoreCase("ON"));
        userSession.setIsRetest("No");

        if (aSwitch1.isChecked()) {
            str_speak = "Select appropriate option";
        } else {
            str_speak = "";
        }
        // create an object textToSpeech and adding features into it
        try {
            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    textToSpeech.setPitch(0.3f / 50);
                    textToSpeech.setSpeechRate(0.1f / 50);
                    // if No error is found then only it will run
                    if (i != TextToSpeech.ERROR) {
                        // To Choose language of speech
                        Locale locale = new Locale("en", "hi_IN");
                        textToSpeech.setLanguage(locale);
//                        if (str_switch_status.equalsIgnoreCase("ON")) {
//                            str_speak = "Select appropriate option";
//                        } else {
//                            str_speak = "";
//                        }
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                textToSpeech.speak(str_speak, TextToSpeech.QUEUE_FLUSH, null, null);
                            }
                        }
                    }
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        single_Test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (aSwitch1.isChecked())
                    str_switch_status = "ON";
                else
                    str_switch_status = "OFF";
                if (textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                editor.putString("Voice_Assistant", str_switch_status);
                editor.apply();
                editor.commit();

                if (!str_grade.equals("")
                        || !str_body_damages.equals("")
                        || !str_lcd_glass_damage.equals("")
                        || !str_lcd_damage.equals("")
                        || !str_pasting_issue.equals("")
                        || !str_yellow_border.equals("")) {
                    Intent intent = new Intent(mContext, TermsConditionActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Answer all questions", Toast.LENGTH_SHORT).show();
                }
            }
        });

        body_damages_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                // Get the selected Radio Button
                RadioButton body_damages_rb = findViewById(checkedId);
                Toast.makeText(mContext, "Selected Radio Button is : " + body_damages_rb.getText(), Toast.LENGTH_SHORT).show();
                str_body_damages = String.valueOf(body_damages_rb.getText());
                getGrade();
            }
        });

        lcd_glass_damage_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                // Get the selected Radio Button
                RadioButton lcd_glass_damage_rb = findViewById(checkedId);
                Toast.makeText(mContext, "Selected Radio Button is : " + lcd_glass_damage_rb.getText(), Toast.LENGTH_SHORT).show();
                str_lcd_glass_damage = String.valueOf(lcd_glass_damage_rb.getText());
                getGrade();
            }
        });

        lcd_damage_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                // Get the selected Radio Button
                RadioButton lcd_damage_rb = findViewById(checkedId);
                Toast.makeText(mContext, "Selected Radio Button is : " + lcd_damage_rb.getText(), Toast.LENGTH_SHORT).show();
                str_lcd_damage = String.valueOf(lcd_damage_rb.getText());
                getGrade();
            }
        });

        pasting_issue_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                // Get the selected Radio Button
                RadioButton pasting_issue_rb = findViewById(checkedId);
                Toast.makeText(mContext, "Selected Radio Button is : " + pasting_issue_rb.getText(), Toast.LENGTH_SHORT).show();
                str_pasting_issue = String.valueOf(pasting_issue_rb.getText());
                getGrade();
            }
        });

        yellow_border_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                // Get the selected Radio Button
                RadioButton yellow_border_rb = findViewById(checkedId);
                Toast.makeText(mContext, "Selected Radio Button is : " + yellow_border_rb.getText(), Toast.LENGTH_SHORT).show();
                str_yellow_border = String.valueOf(yellow_border_rb.getText());
                getGrade();
            }
        });
    }

    private void getLayoutUiIdfinds() {
        try {
            tv_grade = (TextView) findViewById(R.id.tv_grade);
            application_Version = (TextView) findViewById(R.id.application_Version);
            aSwitch1 = (SwitchMaterial) findViewById(R.id.switch1);
            single_Test = (Button) findViewById(R.id.btn_fullG);
            body_damages_rg = (RadioGroup) findViewById(R.id.body_damages);
            lcd_glass_damage_rg = (RadioGroup) findViewById(R.id.lcd_glass_damage);
            lcd_damage_rg = (RadioGroup) findViewById(R.id.lcd_damage);
            pasting_issue_rg = (RadioGroup) findViewById(R.id.pasting_issue);
            yellow_border_rg = (RadioGroup) findViewById(R.id.yellow_border);

        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getSoftwareVersion() {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String version = pInfo.versionName;
            System.out.println("Software Version :- " + version);
            application_Version.setText("Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getGrade() {
        try {
            if (str_body_damages.equals("No")
                    && str_lcd_glass_damage.equals("No")
                    && str_lcd_damage.equals("No")
                    && str_pasting_issue.equals("No")
                    && str_yellow_border.equals("No")) {
                str_grade = "Superb";
            } else {
                str_grade = "";
            }
            tv_grade.setText(str_grade);
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (aSwitch1.isChecked())
                str_switch_status = "ON";
            else
                str_switch_status = "OFF";
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            editor.putString("Voice_Assistant", str_switch_status);
            editor.apply();
            editor.commit();
            Intent intentGetInTouch = new Intent(mContext, GetInTouchActivity.class);
            intentGetInTouch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intentGetInTouch);
            GradingActivity.this.finish();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }
}