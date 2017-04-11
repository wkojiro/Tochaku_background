package jp.techacademy.wakabayashi.kojiro.tochaku_background;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


import static java.sql.DriverManager.println;


/*

ネットワークに繋がらなかった時
エラー時の対応（エラーメッセージのAPI)

 */


public class LoginActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    //memo: パーツの定義
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mUserNameEditText;

    //memo: パーツ受け取り用
    private User user;
    private String username;
    private String email;
    private String password;

    //memo: Responseで受け取る変数
    private String res_token;
    private String res_id;
    private String res_username;
    private String res_email;

    //memo: プログレス
    private ProgressDialog mProgress;


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // handle the preference change here
        Log.d("変更情報","ログインしました");


    }

    /* onCreate */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //memo: UIの準備
        setTitle("ログイン");

        mUserNameEditText = (EditText) findViewById(R.id.usernameText);
        mEmailEditText = (EditText) findViewById(R.id.emailText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordText);

        //memo:
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("処理中...");

        //memo: 新規登録
        Button createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //memo: キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


                username = mUserNameEditText.getText().toString();
                email = mEmailEditText.getText().toString();
                password = mPasswordEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6 ) {

                    mProgress.show();

                    new createAccount().execute(username ,email , password);

                } else {

                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();

                }
            }
        });

        //memo: ログイン
        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


                email = mEmailEditText.getText().toString();
                password = mPasswordEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6) {

                    // プログレスダイアログを表示する
                    mProgress.show();
                    new loginAccount().execute(email , password);


                  //  login(email, password);
                } else {

                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();

                    // プログレスダイアログを非表示にする

                }
            }
        });
    }

    private class createAccount extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            final MediaType JSON
                    = MediaType.parse("application/json; charset=utf-8");

            String urlString = "https://rails5api-wkojiro1.c9users.io/users.json";
            String result = null;

            //memo: コンストラクタ。初期化している。Responseが上書く。
            user = new User();
            user.setUsername(params[0]);
            user.setEmail(params[1]);
            user.setPassword(params[2]);

            final String json =
                    "{\"user\":{" +
                            "\"username\":\"" + params[0] + "\"," +
                            "\"email\":\"" + params[1] + "\"," +
                            "\"password\":\"" + params[2] + "\"," +
                            "\"password_confirmation\":\"" + params[2] + "\"" +
                            "}" +
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

                    // JSON to Java
                    Gson gson = new Gson();

                    //memo: JsonDataからユーザーインスタンスに格納 http://qiita.com/u-chida/items/cbdd040e4199a10936dc
                 //   user = gson.fromJson(jsonData.toString(), User.class);
                    user = gson.fromJson(jsonData, User.class);
                    Log.d("debug", String.valueOf(response.body().toString()));
                    Log.d("debug", String.valueOf(user));

                    //memo: 上でResponseから取得したUserを今度はPreferenceに保存する為に変数に格納
                    if (user != null) {
                        res_id = user.getUid();
                        res_token = user.getToken();
                        res_username = user.getUserName();
                        res_email = user.getEmail();
                        // res_password = user.getPassword();
                        Log.d("レスポンス", res_id);
                        System.out.println("username = " + user.getUserName());
                        System.out.println("username = " + res_username);

                    }
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
            View v = findViewById(android.R.id.content);

            if(result.equals("OK")) {

                //memo: 会員情報をPreferenceに保存
                saveUserdata();

                mProgress.dismiss();
                Toast.makeText(LoginActivity.this,"会員登録が完了しました。",Toast.LENGTH_SHORT).show();

                finish();
            } else {
                Toast.makeText(LoginActivity.this,"会員登録に失敗しました。通信環境をご確認下さい。",Toast.LENGTH_SHORT).show();
                mProgress.dismiss();
            }
        }
    }


    private class loginAccount extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params){

            final MediaType JSON
                    = MediaType.parse("application/json; charset=utf-8");

            String urlString = "https://rails5api-wkojiro1.c9users.io/users/sign_in.json";
            String result = null;
            user = new User();
            user.setEmail(params[0]);
            user.setPassword(params[1]);

            final String json =
                    "{\"user\":{" +
                            "\"email\":\"" + params[0] + "\"," +
                            "\"password\":\""+ params[1] + "\"" +
                            "}" +
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
                    //memo: responseにresponcedataが全部入っている。

                    String jsonData = response.body().string();
                    //   Log.d("debug", result);

                    // JSON to Java
                    Gson gson = new Gson();

                    //memo: JsonDataからユーザーインスタンスに格納 http://qiita.com/u-chida/items/cbdd040e4199a10936dc
                    //   user = gson.fromJson(jsonData.toString(), User.class);
                    user = gson.fromJson(jsonData, User.class);
                    Log.d("debug", String.valueOf(response.body().toString()));
                    Log.d("debug", String.valueOf(user));

                    //memo: 上でResponseから取得したUserを今度はPreferenceに保存する為に変数に格納
                    if (user != null) {
                        res_id = user.getUid();
                        res_token = user.getToken();
                        res_username = user.getUserName();
                        res_email = user.getEmail();
                        // res_password = user.getPassword();
                        Log.d("レスポンス", res_id);
                        System.out.println("username = " + user.getUserName());
                        System.out.println("username = " + res_username);

                    }
                    result = "OK";
                    Log.d("debug", "doPost success");
                    //  Log.d("debug", result);

                }
            } catch (IOException e) {
                e.printStackTrace();
                result = "NG";
                Log.e("hoge", "error orz:" + e.getMessage(), e);
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            //response();
            View v = findViewById(android.R.id.content);
            if(result.equals("OK")) {
                saveUserdata();

                mProgress.dismiss();
                Toast.makeText(LoginActivity.this, "ログインが完了しました。", Toast.LENGTH_LONG).show();

                finish();
            } else {

                mProgress.dismiss();
                Toast.makeText(LoginActivity.this, "ログインに失敗しました。通信環境をご確認下さい。", Toast.LENGTH_LONG).show();

            }
        }
    }

    public void saveUserdata() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.UidKEY , res_id);
        editor.putString(Const.UnameKEY, res_username);
        editor.putString(Const.EmailKEY, res_email);
        editor.putString(Const.TokenKey, res_token);


        editor.commit();
    }


}
