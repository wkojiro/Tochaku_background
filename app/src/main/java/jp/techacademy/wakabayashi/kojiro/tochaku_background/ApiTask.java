package jp.techacademy.wakabayashi.kojiro.tochaku_background;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;
import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
/**
 * Created by wkojiro on 2017/04/17.
 */

public class ApiTask implements SharedPreferences.OnSharedPreferenceChangeListener {

    //memo: Responseで受け取る変数
    private User user;
    private String res_token;
    private String res_id;
    private String res_username;
    private String res_email;

    private Context mContext;
    private ProgressDialog mProgress;


    public ApiTask(Context context, ProgressDialog progress){

        mContext = context;
        mProgress = progress;
    }
    public ApiTask(Context context){

        mContext = context;

    }

    protected void createAccountAsync(String username, String email, String password) {

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "https://rails5api-wkojiro1.c9users.io/users.json"; //tocakudemo
        String result = null;

        //memo: コンストラクタ。初期化している。Responseが上書く。
        user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        final String json =
                "{\"user\":{" +
                        "\"username\":\"" + username + "\"," +
                        "\"email\":\"" + email + "\"," +
                        "\"password\":\"" + password + "\"," +
                        "\"password_confirmation\":\"" + password + "\"" +
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

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                Toast.makeText(mContext,"失敗しました。",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {


                    String jsonData = response.body().string();
                    Log.d("debug", jsonData);

                    //memo: 会員情報をPreferenceに保存
                    saveUserdata();
                }
            }
        });
    }

    protected String loginAsync(String email, String password) {

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "https://rails5api-wkojiro1.c9users.io/users/sign_in.json";

        String result = "";
        String jsonData;
        //memo: ここでこれ必要か？？
        user = new User();
        user.setEmail(email);
        user.setPassword(password);

        final String json =
                "{\"user\":{" +
                        "\"email\":\"" + email + "\"," +
                        "\"password\":\""+ password + "\"" +
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
                jsonData = response.body().string();

                Gson gson = new Gson();
                result = "";
                user = gson.fromJson(jsonData, User.class);
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
                Log.d("debug", "doDeletedest success");

            }
        } catch (IOException e) {
            e.printStackTrace();
            result = "NG";
            Log.e("hoge", "error orz:" + e.getMessage(), e);
        }
        // 返す

        saveUserdata();
        return result;
    }





    private void saveUserdata() {
        // public void saveUserdata(){


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.UidKEY , res_id);
        editor.putString(Const.UnameKEY, res_username);
        editor.putString(Const.EmailKEY, res_email);
        editor.putString(Const.TokenKey, res_token);

        editor.apply();



        Log.d("debug", "doSaveUserInfo success");



    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
