package com.xtracover.xcqc.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.UserSession;

public class AddTesterNameActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private UserSession userSession;
    private TextView back_text;
    private EditText testerId_et;
    private Button saveTesterInfo;
    private String testerId = "";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tester_name);
        mContext = this;
        userSession = new UserSession(mContext);

        getLayoutUiIdFind();

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    private void getLayoutUiIdFind() {
        try {
            back_text = (TextView) findViewById(R.id.back_text);
            testerId_et = (EditText) findViewById(R.id.testerId_et);
            saveTesterInfo = (Button) findViewById(R.id.saveTesterInfo);

            saveTesterInfo.setOnClickListener(this);
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveTesterInfo:
                if (isTesterIdDataValidate()) {
                    editor.putString("testerId", testerId);
                    editor.apply();
                    editor.commit();
                    Intent intent = new Intent(mContext, TermsConditionActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }

    private boolean isTesterIdDataValidate() {
        try {
            testerId = testerId_et.getText().toString().trim();
            if (testerId.isEmpty()) {
                testerId_et.setError("Enter tester id");
                testerId_et.requestFocus();
                return false;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
        return true;
    }
}