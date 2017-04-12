package jp.techacademy.wakabayashi.kojiro.tochaku_background;

/*
目的地の登録のためのActivity


 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class DestActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener  {

    private Dest mDest;

    //パーツの定義
    private EditText mDestNameText;
    private EditText mDestEmailText;
    private EditText mDestAddressText;
    private Button createButton;

    //パーツから受け取るためのパラメータ
    private Dest dest;
    private String destname;
    private String destemail;
    private String destaddress;
    private String desturl;




    ProgressDialog mProgress;

    //API通信のための会員Email及びToken(Preferenseから取得）
    private String username;
    private String email;
    private String access_token;

    //preferenceから取得用
    private String name;
    private String address;
    private String demail;
    private String dlatitude;
    private String dlongitude;






    private String result = "";


    //memo: preferencceの書き換えを検知するListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("変更", "Destに書かれているLogです。");


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dest);

        setTitle("目的地の追加");

        //memo: 保存されているユーザー情報をあらかじめ取得しておく。API用
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);
        username = sp.getString(Const.UnameKEY, "");
        email = sp.getString(Const.EmailKEY, "");
        access_token = sp.getString(Const.TokenKey, "");


        //memo: 保存されている目的地情報をあらかじめ取得しておく。（用途：　　　）
        name = sp.getString(Const.DestnameKEY, "");
        address = sp.getString(Const.DestaddressKEY, "");
        demail = sp.getString(Const.DestemailKEY,"");
        dlatitude = sp.getString(Const.DestLatitudeKEY,"");
        dlongitude = sp.getString(Const.DestLongitudeKEY,"");

        Log.d("user name",String.valueOf(username));
        Log.d("Email",String.valueOf(username));
        Log.d("トークン",String.valueOf(access_token));
        Log.d("name",String.valueOf(name));
        Log.d("address",String.valueOf(address));
        Log.d("dlatitude",String.valueOf(dlatitude));

        //memo: SettingActivityからもらってきたdestID情報（用途：目的地編集用）
        Intent intent = getIntent();
        int destId = intent.getIntExtra(SettingActivity.EXTRA_DEST, -1);
        Log.d("destId",String.valueOf(destId));

        //memo: SettubgActivityに戻るためのIntent
      //  Intent backintent = new Intent();



        //memo: Realmを用意（用途：ここでは目的地編集用及び新規登録用）
        Realm realm = Realm.getDefaultInstance();
        mDest = realm.where(Dest.class).equalTo("id", destId).findFirst();
        realm.close();


        //memo: 該当のIDがない場合（新規登録の場合）
        if (mDest == null) {
            mDestNameText = (EditText) findViewById(R.id.destNameText);
            mDestEmailText = (EditText) findViewById(R.id.destEmailText);
            mDestAddressText = (EditText) findViewById(R.id.destAddressText);
        } else
        //memo: IDがある場合（新規登録の場合）
            {
            mDestNameText = (EditText) findViewById(R.id.destNameText);
            mDestEmailText = (EditText) findViewById(R.id.destEmailText);
            mDestAddressText = (EditText) findViewById(R.id.destAddressText);

         //memo: まず今ある情報を表示しておく。（この時点でrailsidは必要なし）
            mDestNameText.setText(mDest.getDestName());
            mDestEmailText.setText(mDest.getDestEmail());
            mDestAddressText.setText(mDest.getDestAddress());
        }


        createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if(mDest == null) {
                //新規登録
                    destname = mDestNameText.getText().toString();
                    destemail = mDestEmailText.getText().toString();
                    destaddress = mDestAddressText.getText().toString();

                    if (destname.length() != 0 && destemail.length() != 0 && destaddress.length() != 0) {

                        new DestActivity.createDestination().execute(destname,destemail,destaddress);

                    } else {
                        Log.d("目的地登録エラー", "ddd");
                        // エラーを表示する
                        Snackbar.make(v, "目的地の情報が正しく入力されていません", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                //更新登録
                    Log.d("更新登録", "ddd");

                    destname = mDestNameText.getText().toString();
                    destemail = mDestEmailText.getText().toString();
                    destaddress = mDestAddressText.getText().toString();
                    desturl = mDest.getDestUrl();
                    Log.d("更新登録", desturl);
                    new DestActivity.editDestination().execute(destname,destemail,destaddress,desturl);

                }
            }

        });
    }


    private class editDestination extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {

            final MediaType JSON
                    = MediaType.parse("application/json; charset=utf-8");

            String urlString = params[3] +"?email="+ email +"&token="+ access_token +"";

            final String json =
                    "{" +
                            "\"destname\":\"" + params[0] + "\"," +
                            "\"destemail\":\"" + params[1] + "\"," +
                            "\"destaddress\":\"" + params[2] + "\"" +
                    "}";

            RequestBody body = RequestBody.create(JSON, json);

            // リクエストオブジェクトを作って
            Request request = new Request.Builder()
                    .url(urlString)
                    //.header("Authorization", credential)
                    .put(body)
                    .build();

            // クライアントオブジェクトを作って
            OkHttpClient client = new OkHttpClient();



            // リクエストして結果を受け取って
            try {
                okhttp3.Response response = client.newCall(request).execute();
                Log.d("debug", String.valueOf(response));
                if (response.isSuccessful()){

                    String jsonData = response.body().string();
                    //   Log.d("debug", result);


                    result = "OK";
                    Log.d("debug", "doPost success");
                    //  Log.d("debug", result);

                }
            } catch (IOException e) {
                e.printStackTrace();
                result = "NG";
                Log.e("hoge", "error orz:" + e.getMessage(), e);
            }

            // 返す
            return result;



        }
        @Override
        protected void onPostExecute(String result) {


            Log.d("","done");
            Intent resultintent = new Intent();

            if(result.equals("OK")) {

                View v = findViewById(android.R.id.content);
                Snackbar.make(v, "目的地情報を更新しました。", Snackbar.LENGTH_LONG).show();

                resultintent.putExtra("Result","OK");
                setResult(RESULT_OK,resultintent);
                Log.d("インテント", String.valueOf(resultintent));

                finish();
            } else {
                View v = findViewById(android.R.id.content);

                resultintent.putExtra("Result","NG");
                setResult(RESULT_CANCELED, resultintent);
                Snackbar.make(v, "目的地情報を更新ができませんでした。", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class createDestination extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {

            final MediaType JSON
                    = MediaType.parse("application/json; charset=utf-8");

            String urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email="+ email +"&token="+ access_token +"";

            String result = null;
            dest = new Dest();
            dest.setDestName(params[0]);
            dest.setDestEmail(params[1]);
            dest.setDestAddress(params[2]);

            final String json =
                    "{" +
                            "\"destname\":\"" + params[0] + "\"," +
                            "\"destemail\":\"" + params[1] + "\"," +
                            "\"destaddress\":\"" + params[2] + "\"" +
                     "}";

            RequestBody body = RequestBody.create(JSON, json);

            // リクエストオブジェクトを作って
            Request request = new Request.Builder()
                    .url(urlString)
                    //.header("Authorization", credential)
                    .post(body)
                    .build();

            // クライアントオブジェクトを作って
            OkHttpClient client = new OkHttpClient();



            // リクエストして結果を受け取って
            try {
                okhttp3.Response response = client.newCall(request).execute();
                Log.d("debug", String.valueOf(response));
                if (response.isSuccessful()){

                    String jsonData = response.body().string();
                    //   Log.d("debug", result);


                    result = "OK";
                    Log.d("debug", "doPost success");
                    //  Log.d("debug", result);

                }
            } catch (IOException e) {
                e.printStackTrace();
                result = "NG";
                Log.e("hoge", "error orz:" + e.getMessage(), e);
            }

            // 返す
            return result;

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("Post","done");


            Intent resultintent = new Intent();

            if(result.equals("OK")) {
                View v = findViewById(android.R.id.content);

                //memo: 削除されたので、reloadの役割として目的地一覧を取得
            //    new SettingActivity.getDestinations().execute();


                resultintent.putExtra("Result","OK");
                setResult(RESULT_OK,resultintent);
                Log.d("インテント", String.valueOf(resultintent));

                Snackbar.make(v, "目的地を追加しました。", Snackbar.LENGTH_LONG).show();

                finish();
            } else {
                View v = findViewById(android.R.id.content);

                resultintent.putExtra("Result","NG");
                setResult(RESULT_CANCELED, resultintent);
                Snackbar.make(v, "目的地を追加することができませんでした。", Snackbar.LENGTH_LONG).show();

            }
        }
    }
}
