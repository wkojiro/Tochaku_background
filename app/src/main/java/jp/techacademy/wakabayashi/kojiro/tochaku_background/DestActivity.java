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

public class DestActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener  {

    Dest mDest;

    //パーツの定義
    EditText mDestNameText;
    EditText mDestEmailText;
    EditText mDestAddressText;
    Button createButton;

    //パーツから受け取るためのパラメータ
    Dest dest;
    String destname;
    String destemail;
    String destaddress;
    String desturl;




    ProgressDialog mProgress;

    //API通信のための会員Email及びToken(Preferenseから取得）
    String username;
    String email;
    String access_token;

    //preferenceから取得用
    String name;
    String address;
    String demail;
    String dlatitude;
    String dlongitude;



    //Responseを受け取るためのパラメータ(必要なのか？）
    Integer res_destid;
    Integer res_destpositionid;
    String res_destname;
    String res_destemail;
    String res_destaddress;
    String res_destlatitude;
    String res_destlongitude;


    String result = "";
    String result2 = "";
    int status = 0;

    //memo: preferencceの書き換えを検知するListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("変更", "Destに書かれているLogです。");


    }

    public String Edit(String[] params){
        HttpURLConnection con = null;//httpURLConnectionのオブジェクトを初期化している。
        BufferedReader reader = null;
        StringBuilder jsonData = new StringBuilder();
        String urlString = params[3] +"?email="+ email +"&token="+ access_token +"";

        InputStream inputStream = null;


        final String json =
                "{" +
                        "\"destname\":\"" + params[0] + "\"," +
                        "\"destemail\":\"" + params[1] + "\"," +
                        "\"destaddress\":\"" + params[2] + "\"" +
                        "}";

        try {

            URL url = new URL(urlString); //URLを生成
            con = (HttpURLConnection) url.openConnection(); //HttpURLConnectionを取得する
            con.setRequestMethod("PUT");
            con.setInstanceFollowRedirects(false); // HTTP リダイレクト (応答コード 3xx の要求) を、この HttpURLConnection インスタンスで自動に従うかどうかを設定します。
            con.setRequestProperty("Accept-Language", "jp");
            con.setDoOutput(true); //この URLConnection の doOutput フィールドの値を、指定された値に設定します。→イマイチよく理解できない（URL 接続は、入力または出力、あるいはその両方に対して使用できます。URL 接続を出力用として使用する予定である場合は doOutput フラグを true に設定し、そうでない場合は false に設定します。デフォルトは false です。）
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            // リスエストの送信
            OutputStream os = con.getOutputStream(); //この接続に書き込みを行う出力ストリームを返します
            con.connect();
            // con.getResponseCode();


            PrintStream ps = new PrintStream(os); //行の自動フラッシュは行わずに、指定のファイルで新しい出力ストリームを作成します。
            ps.print(json);// JsonをPOSTする
            ps.close();
            status = con.getResponseCode();
            Log.d("結果",String.valueOf(status));
            //if(status == HttpURLConnection.HTTP_OK) {
            if(status/100 == 2){
                reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));//デフォルトサイズのバッファーでバッファリングされた、文字型入力ストリームを作成します。
                String line = reader.readLine();
                while (line != null) {
                    jsonData.append(line);
                    line = reader.readLine();
                }
                System.out.println(jsonData.toString());
            }
            con.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("目的地登録", "Postしてみました");
        // mProgress.dismiss();

        if (status/100 == 2){
            result2 = "OK";
        } else {
            result2 = "NG";
        }



        // return result
        return result2;
    }



    /* Post ここでは目的地の住所などをPostするだけ。Responseは最悪無視しても問題ないはず。一覧は別画面でGetするから。*/
    public String Post(Dest dest){
        HttpURLConnection con = null;//httpURLConnectionのオブジェクトを初期化している。
        BufferedReader reader = null;
        StringBuilder jsonData = new StringBuilder();
        String urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email="+ email +"&token="+ access_token +"";

        InputStream inputStream = null;
        String result = "";


        String name = dest.getDestName();
        String email = dest.getDestEmail();
        String address = dest.getDestAddress();
        // String password2 = user.getPassword();

        final String json =
                "{" +
                        "\"destname\":\"" + name + "\"," +
                        "\"destemail\":\"" + email + "\"," +
                        "\"destaddress\":\"" + address + "\"" +
                "}";

        try {

            URL url = new URL(urlString); //URLを生成
            con = (HttpURLConnection) url.openConnection(); //HttpURLConnectionを取得する
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(false); // HTTP リダイレクト (応答コード 3xx の要求) を、この HttpURLConnection インスタンスで自動に従うかどうかを設定します。
            con.setRequestProperty("Accept-Language", "jp");
            con.setDoOutput(true); //この URLConnection の doOutput フィールドの値を、指定された値に設定します。→イマイチよく理解できない（URL 接続は、入力または出力、あるいはその両方に対して使用できます。URL 接続を出力用として使用する予定である場合は doOutput フラグを true に設定し、そうでない場合は false に設定します。デフォルトは false です。）
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            // リスエストの送信
            OutputStream os = con.getOutputStream(); //この接続に書き込みを行う出力ストリームを返します
            con.connect();
            // con.getResponseCode();

            PrintStream ps = new PrintStream(os); //行の自動フラッシュは行わずに、指定のファイルで新しい出力ストリームを作成します。
            ps.print(json);// JsonをPOSTする
            ps.close();
            status = con.getResponseCode();
            Log.d("結果",String.valueOf(status));
            //if(status == HttpURLConnection.HTTP_OK) {
            if(status/100 == 2){
                //多分ここからResponseのための器をつくっている。
                //戻り値の指定をしないと動かないのかな？

                reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));//デフォルトサイズのバッファーでバッファリングされた、文字型入力ストリームを作成します。
                String line = reader.readLine();
                while (line != null) {
                    jsonData.append(line);
                    line = reader.readLine();
                }

                System.out.println(jsonData.toString());

                // JSON to Java
                Gson gson = new Gson();
                dest = gson.fromJson(jsonData.toString(), Dest.class);

                if (dest != null) {
                    res_destid = dest.getId();
                    res_destpositionid = dest.getPositionId();
                    res_destname = dest.getDestName();
                    res_destemail = dest.getDestEmail();
                    res_destaddress = dest.getDestAddress();
                    res_destlatitude = dest.getDestLatitude();
                    res_destlongitude = dest.getDestLongitude();

                    System.out.println("ID = " + res_destid);
                    System.out.println("positionID = " + dest.getPositionId());

                }
            }
            con.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // mProgress.dismiss();

        if (status/100 == 2){
            result2 = "OK";
        } else {
            result2 = "NG";
        }



        // return result
        return result2;

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

                        new DestActivity.createDestination().execute();

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

            //Log.d("doinbackground",String.valueOf(mDest.getDestUrl()));
            Edit(params);
            //Log.d("dest",String.valueOf(mDest));
            if(result2.equals("OK")){
                result = "OK";

            } else {
                result = "NG";
            }
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

            dest = new Dest();
            dest.setDestName(destname);
            dest.setDestEmail(destemail);
            dest.setDestAddress(destaddress);

            Post(dest);
            Log.d("dest",String.valueOf(dest));
            if(result2.equals("OK")){
                result = "OK";

            } else {
                result = "NG";
            }
            return result;

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("Post","done");
            if(result.equals("OK")) {
                View v = findViewById(android.R.id.content);
                Snackbar.make(v, "目的地を追加しました。", Snackbar.LENGTH_LONG).show();

                finish();
            } else {
                View v = findViewById(android.R.id.content);
                Snackbar.make(v, "目的地を追加することができませんでした。", Snackbar.LENGTH_LONG).show();

            }
        }
    }
}
