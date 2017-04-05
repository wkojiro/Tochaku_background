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
    EditText mEmailEditText;
    EditText mPasswordEditText;
    EditText mUserNameEditText;

    //memo: パーツ受け取り用
    User user;
    String username;
    String email;
    String password;

    //Responseで受け取る変数
    String res_token;
    String res_id;
    String res_username;
    String res_email;
    // String res_password;

    //API用変数
    HttpURLConnection con = null;//httpURLConnectionのオブジェクトを初期化している。
    BufferedReader reader = null;
    StringBuilder jsonData = new StringBuilder();
    InputStream inputStream = null;
    String result = "";
    String result2 = "";
    int status = 0;

    // 未実装
    ProgressDialog mProgress;

    /*  Login ログイン登録 */
    public String Login(String urlString , String[] params){

        final String json =
                "{\"user\":{" +
                        "\"email\":\"" + params[1] + "\"," +
                        "\"password\":\""+ params[2] + "\"" +
                        "}" +
                 "}";

        try {
            URL url = new URL(urlString); //URLを生成
            con = (HttpURLConnection) url.openConnection(); //HttpURLConnectionを取得する
            con.setRequestMethod(params[0]);
            con.setInstanceFollowRedirects(false); // HTTP リダイレクト (応答コード 3xx の要求) を、この HttpURLConnection インスタンスで自動に従うかどうかを設定します。
            con.setRequestProperty("Accept-Language", "jp");
            con.setDoOutput(true); //この URLConnection の doOutput フィールドの値を、指定された値に設定します。→イマイチよく理解できない（URL 接続は、入力または出力、あるいはその両方に対して使用できます。URL 接続を出力用として使用する予定である場合は doOutput フラグを true に設定し、そうでない場合は false に設定します。デフォルトは false です。）
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            // リスエストの送信
            OutputStream os = con.getOutputStream(); //この接続に書き込みを行う出力ストリームを返します
            con.connect();

            PrintStream ps = new PrintStream(os); //行の自動フラッシュは行わずに、指定のファイルで新しい出力ストリームを作成します。
            ps.print(json);// JsonをPOSTする
            ps.close();

            status = con.getResponseCode();
            Log.d("結果", String.valueOf(status));

            //memo: StatusCodeが２００番代であればOK
            //if (status == HttpURLConnection.HTTP_OK) {
            if (status/100 == 2) {

                reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));//デフォルトサイズのバッファーでバッファリングされた、文字型入力ストリームを作成します。
                String line = reader.readLine();
                while (line != null) {
                    jsonData.append(line);
                    line = reader.readLine();
                }
                System.out.println(jsonData.toString());

                // JSON to Java
                Gson gson = new Gson();

                user = gson.fromJson(jsonData.toString(), User.class);

                if (user != null) {
                    res_id = user.getUid();
                    res_token = user.getToken();
                    res_username = user.getUserName();
                    res_email = user.getEmail();

                    System.out.println("id = " + user.getUid());
                    System.out.println("username = " + user.getUserName());
                    System.out.println("access_token = " + user.getToken());
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
        //memo: StatusCodeが２００番代であればOK
        if (status/100 == 2){
            result2 = "OK";
        } else {
            result2 = "NG";
        }

        return result2;
    }

    /*  POST 新規会員登録 */
    //http://hmkcode.com/android-send-json-data-to-server/
    public String Post(String urlString , String[] params) {

        final String json =
                "{\"user\":{" +
                        "\"username\":\"" + params[1] + "\"," +
                        "\"email\":\"" + params[2] + "\"," +
                        "\"password\":\"" + params[3] + "\"," +
                        "\"password_confirmation\":\"" + params[3] + "\"" +
                        "}" +
                "}";

        try {

            URL url = new URL(urlString); //URLを生成
            con = (HttpURLConnection) url.openConnection(); //HttpURLConnectionを取得する
            con.setRequestMethod(params[0]);
            con.setInstanceFollowRedirects(false); // HTTP リダイレクト (応答コード 3xx の要求) を、この HttpURLConnection インスタンスで自動に従うかどうかを設定します。
            con.setRequestProperty("Accept-Language", "jp");
            con.setDoOutput(true); //この URLConnection の doOutput フィールドの値を、指定された値に設定します。→イマイチよく理解できない（URL 接続は、入力または出力、あるいはその両方に対して使用できます。URL 接続を出力用として使用する予定である場合は doOutput フラグを true に設定し、そうでない場合は false に設定します。デフォルトは false です。）
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            //
            OutputStream os = con.getOutputStream(); //この接続に書き込みを行う出力ストリームを返します
            con.connect();

            PrintStream ps = new PrintStream(os); //行の自動フラッシュは行わずに、指定のファイルで新しい出力ストリームを作成します。
            ps.print(json);// JsonをPOSTする
            ps.close();

            status = con.getResponseCode();
            Log.d("結果", String.valueOf(status));

            //memo: StatusCodeが２００番代であればOK
            //if (status == HttpURLConnection.HTTP_OK) {
            if (status/100 == 2) {
                reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));//デフォルトサイズのバッファーでバッファリングされた、文字型入力ストリームを作成します。
                String line = reader.readLine();
                while (line != null) {
                    jsonData.append(line);
                    line = reader.readLine();
                }
                System.out.println(jsonData.toString());

                // JSON to Java
                Gson gson = new Gson();

                //memo: JsonDataからユーザーインスタンスに格納
                user = gson.fromJson(jsonData.toString(), User.class);

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


        //memo: StatusCodeが２００番代であればOK
        if (status/100 == 2){
            result2 = "OK";
        } else {
            result2 = "NG";
        }

        return result2;
    }

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

                String type = "POST";
                username = mUserNameEditText.getText().toString();
                email = mEmailEditText.getText().toString();
                password = mPasswordEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6 ) {
                    // プログレスダイアログを表示する
                    // プログレスダイアログを表示する
                    mProgress.show();
                    new createAccount().execute(type, username ,email , password);
                   // createAccount(email, password);

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

                String type = "POST";
                email = mEmailEditText.getText().toString();
                password = mPasswordEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6) {

                    // プログレスダイアログを表示する
                    mProgress.show();
                    new loginAccount().execute(type, email , password);


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



            String urlString = "https://rails5api-wkojiro1.c9users.io/users.json";

            //params[0] is method
            user = new User();
            user.setUsername(params[1]);
            user.setEmail(params[2]);
            user.setPassword(params[3]);

            Post(urlString,params);

           // Log.d("result", String.valueOf(result));

            if(result2.equals("OK")){
                result = "OK";

            } else {
                result = "NG";
            }
                return result;

        }

        @Override
        protected void onPostExecute(String result) {
            View v = findViewById(android.R.id.content);

            if(result.equals("OK")) {
                saveUserdata();

                // プログレスダイアログを非表示にする
                mProgress.dismiss();
                Snackbar.make(v, "会員登録が完了しました。", Snackbar.LENGTH_LONG).show();
                finish();
            } else {
                Snackbar.make(v, "会員登録に失敗しました。通信環境をご確認下さい。", Snackbar.LENGTH_LONG).show();
                // プログレスダイアログを非表示にする
                mProgress.dismiss();
            }
        }
    }


    private class loginAccount extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params){

            mProgress.show();

            String urlString = "https://rails5api-wkojiro1.c9users.io/users/sign_in.json";
            user = new User();
            user.setEmail(params[1]);
            user.setPassword(params[2]);

            Login(urlString,params);

            if(result2.equals("OK")){
                result = "OK";

            } else {
                result = "NG";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            //response();
            View v = findViewById(android.R.id.content);
            if(result.equals("OK")) {
                saveUserdata();
                // プログレスダイアログを非表示にする
                mProgress.dismiss();
                Toast.makeText(LoginActivity.this, "ログインが完了しました。", Toast.LENGTH_LONG).show();
                //Snackbar.make(v, "ログインが完了しました。", Snackbar.LENGTH_LONG).show();
                finish();
            } else {
                // プログレスダイアログを非表示にする
                mProgress.dismiss();

                Toast.makeText(LoginActivity.this, "会員登録に失敗しました。通信環境をご確認下さい。", Toast.LENGTH_LONG).show();
                //Snackbar.make(v, "会員登録に失敗しました。通信環境をご確認下さい。", Snackbar.LENGTH_LONG).show();
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
      //  editor.putString(Const.PassKey, res_password);


        editor.commit();
    }


}
