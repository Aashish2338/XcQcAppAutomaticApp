package com.xtracover.xcqc.Services;

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.transform.ErrorListener;

public class GetCurrentInternetDatetime {

    Activity activity;
    String datetime_url = "https://www.timeapi.io/api/Time/current/coordinate?latitude=22.5726&longitude=88.3639";
    RequestQueue requestQueue;

    public GetCurrentInternetDatetime(Activity activity) {
        this.activity = activity;

        requestQueue = Volley.newRequestQueue(activity);
    }

    public void getDateTime(VolleyCallBack volleyCallBack){

        JSONObject jsonObject = new JSONObject();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, datetime_url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    volleyCallBack.onGetDateTime(response.getString("year"), response.getString("month"), response.getString("day"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);
    }

    public interface VolleyCallBack{

        void onGetDateTime(String year, String month, String day);
    }
}
