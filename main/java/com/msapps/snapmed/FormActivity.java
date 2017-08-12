package com.msapps.snapmed;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Maninder on 2017-08-12.
 */

public class FormActivity extends AppCompatActivity {
    private String data[] = new String[5];
    private ArrayList<String> ids = new ArrayList<>(3);
    private OkHttpClient client;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        Button submit_button = (Button) findViewById(R.id.submit_button);
        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView_q1 = (TextView) findViewById(R.id.question_1_response);
                TextView textView_q2 = (TextView) findViewById(R.id.question_2_response);

                data[0] = String.valueOf(textView_q1.getText());
                data[1] = String.valueOf(textView_q2.getText());
                getWebService();
            }
        });

        Button report_button = (Button) findViewById(R.id.report_button);
        report_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONResult(data);
            }
        });
    }

    public void JSONResult(String[] data){
        TextView result = (TextView) findViewById(R.id.info);
        String text = String.valueOf(result.getText()).substring(String.valueOf(result.getText()).indexOf(":")+ 1);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] { "doctorpatel8@gmail.com" });
        intent.putExtra(Intent.EXTRA_SUBJECT, "Patient Info");
        intent.putExtra(Intent.EXTRA_TEXT, "Hey, the patient is feeling: \n\n" + data[0] + "\n\n\n" + "They are feeling this way because: \n\n" + data[1] + "\n\n\nThe potential risk factors are: \n" + text);

        startActivity(Intent.createChooser(intent, "Send Email"));
    }
    public void getWebService(){
        String param = "{\n\t\"text\": \""+data[0]+" \"\n}";
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, param);

        final Request request = new Request.Builder()
                .url("https://api.infermedica.com/v2/parse")
                .post(body)
                .addHeader("app-id", "7f653318")
                .addHeader("app-key", "e08babe2c84b8fde91bd2af92b8d0095")
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "5354c21f-d7ce-1c67-3332-1f7771a5c2a1")
                .build();

        final Response response = null;
    try{
        client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        data[2] = "Failure";
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String query = null;
                        try {
                            query = response.body().string();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        Button report_button = (Button) findViewById(R.id.report_button);
                        report_button.setVisibility(View.VISIBLE);
                        TextView result = (TextView) findViewById(R.id.info);
                        Log.d("RESULTS!!!!!!!!!!!!", query);

                        //To collect common names
                        JSONObject obj = null;
                        try {
                            obj = new JSONObject(query);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        JSONArray arr = null;
                        try {
                            arr = obj.getJSONArray("mentions");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Print to screen: "Your risk factor:"
                        for (int i = 0; i < arr.length(); i++) {
                            try {
                                data[2+i] = arr.getJSONObject(i).getString("common_name");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Print common name and newline character
                            Log.d("RESULT!!!","First Risk: "+ data[2] +"\n"+"Second Risk: "+ data[3] +"\n"+"Third Risk: "+ data[4]);
                        }

                        for(int i = 2; i < data.length-1; i++) {
                            if(data[i].equals(data[i+1])){
                                data[i] = null;
                            }
                        }
                        String phrase = "Risk Factors: \n";
                        for(int i = 0; i < 3; i++){
                            if(data[i+2] != null){
                                phrase = phrase + data[i+2] + "\n";
                            }
                        }
                        result.setText(phrase);

                        //To collect IDS

                        JSONObject obj2 = null;
                        try {
                            obj2 = new JSONObject(query);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        JSONArray arr2 = null;
                        try {
                            arr2 = obj2.getJSONArray("mentions");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Print to screen: "Your risk factor:"
                        for (int i = 0; i < arr2.length(); i++) {
                            try {
                                ids.add(arr2.getJSONObject(i).getString("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        for(int i = 0; i < ids.size()-1; i++) {
                            if(ids.get(i).equals(ids.get(i+1))){
                                ids.remove(i);
                            }
                        }
                        for(int i = 0; i < 3; i++){
                            if(data[i+2] != null){
                                phrase = phrase + data[i+2] + "\n";
                            }
                        }
                    }
                });
            }
        });
    }finally {

    }
    }
  /*  public void getSeverity(){
        TextView t1 = (TextView) findViewById(R.id.gender);
        TextView t2 = (TextView) findViewById(R.id.age);
        String ending = " \n  ]\n}";
        String param = "{\n  \"sex\": \""+String.valueOf(t1.getText())+"\",\n  \"age\": "+String.valueOf(t2.getText())+",\n  \"evidence\": [\n   ";

        for(int i = 0; i < ids.size();i++){
            if(i != ids.size()-1) {
                param = param + JSONObjectStrings(i) + ",";
            }
        }
        param += ending;

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        this.client = new OkHttpClient();

        RequestBody body = RequestBody.create(mediaType, param);
        final Request request = new Request.Builder()
                .url("https://api.infermedica.com/v2/triage")
                .post(body)
                .addHeader("app-id", "7f653318")
                .addHeader("app-key", "e08babe2c84b8fde91bd2af92b8d0095")
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .addHeader("postman-token", "20d341eb-2e64-02f9-800e-252cb451ac23")
                .build();


        final Response response = null;
        try{
            client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            data[2] = "Failure";
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            String query = null;
                            try {
                                query = response.body().string();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }


                        }
                    });
                }
            });
    }finally {

        }
    }
    public String JSONObjectStrings(int index){
        JSONObject obj = new JSONObject();

        try {
            obj.put("id", ids.get(index));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            obj.put("choice_id","present");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }*/
}