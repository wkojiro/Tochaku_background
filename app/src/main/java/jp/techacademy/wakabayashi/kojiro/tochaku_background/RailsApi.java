package jp.techacademy.wakabayashi.kojiro.tochaku_background;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;

import bolts.Task;
import bolts.TaskCompletionSource;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by wkojiro on 2017/04/13.
 *
 * 参考サイト
 * http://blog.xin9le.net/entry/2012/07/30/123150
 *
 *
 *
 *
 *
 */

public class RailsApi implements SharedPreferences.OnSharedPreferenceChangeListener {


    //memo: Responseで受け取る変数
    private User user;
    private String res_token;
    private String res_id;
    private String res_username;
    private String res_email;

    private Context mContext;

  //  private static RailsApi instance = null;


    public RailsApi(){

      //  DestApp da = (DestApp) this.getApplication();

    }


    protected Task<String> createAccountAsync(String username, String email, String password) {

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "https://rails5api-wkojiro1.c9users.io/users.json";
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
                taskresult.setError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = "OK";
                if (response.isSuccessful()){
                    taskresult.setResult(s);

                    String jsonData = response.body().string();
                    Log.d("debug", jsonData);

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

                        //memo: 会員情報をPreferenceに保存
                   //  saveUserdata();


                }

            }

        });



        Log.d("taskSource", String.valueOf(taskresult.getTask()));
        return taskresult.getTask();
    }

    public Task<Void> loginAsync(String email, String password) {

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "https://rails5api-wkojiro1.c9users.io/users/sign_in.json";
        String result = null;
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
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                taskresult.setError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = "OK";
                if (response.isSuccessful()) {
                    taskresult.setResult(s);

                    String jsonData = response.body().string();
                    //   Log.d("debug", result);

                    // JSON to Java
                    Gson gson = new Gson();

                    //memo: JsonDataからユーザーインスタンスに格納 http://qiita.com/u-chida/items/cbdd040e4199a10936dc
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
                    Log.d("debug", "doPost success");
                    //memo: 会員情報をPreferenceに保存
                     saveUserdata();
                }
            }

        });


        return null;
    }


    public Task<Void> editAccountAsync(String email, String access_token){

        return null;
    }

    public Task<Void> deleteAccountAsync(String email, String access_token){

        return null;
    }

    public Task<Void> logoutAsync(String email, String access_token) throws IOException {

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "https://rails5api-wkojiro1.c9users.io/users/sign_out.json";
      //  String result = null;

        final String json =
                "{\"user\":{" +
                        "\"email\":\"" + email + "\"," +
                        "\"access_token\":\"" + access_token + "\"" +
                        "}" +
                "}";

        RequestBody body = RequestBody.create(JSON, json);

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(urlString)
                //.header("Authorization", credential)
                .delete(body)
                .build();

        // クライアントオブジェクトを作って
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                taskresult.setError(e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = "OK";
                if (response.isSuccessful()) {
                    taskresult.setResult(s);
                }
            }


        });


        return null;



    }

    public Task<Void> getDirectionsAsync(){

        return null;
    }

    public Task<Void> createDirectionAsync(){

        return null;
    }

    public Task<Void> editDirectionAsync(){

        return null;
    }

    public Task<Void> deleteDirectionAsync(){

        return null;
    }

    public Task<String>  postMailAsync(String email,String access_token,String destname,String destemail, String nowlatitude, String nowlongitude) {
        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "https://rails5api-wkojiro1.c9users.io/trackings.json?email="+email+"&token="+access_token+"";
       // String result = null;

       // String[] params = {"東京駅","wkojiro22@gmail.com","35.681298","139.766247"};

        final String json =
                "{" +
                        "\"destname\":\"" + destname + "\"," +
                        "\"destemail\":\"" + destemail + "\"," +
                        "\"destaddress\":\"\"," +
                        "\"nowlatitude\":\"" + nowlatitude + "\"," +
                        "\"nowlongitude\":\"" + nowlongitude + "\"" +
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
                taskresult.setError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = "OK";
                if (response.isSuccessful()){
                    taskresult.setResult(s);
                }

            }

        });

        Log.d("taskSource", String.valueOf(taskresult.getTask()));
        return taskresult.getTask();
    }



    public  Task<String> test() {



        return Task.forResult("OK");
    }

    public Task<String> succeedAsync() {
        TaskCompletionSource<String> successful = new TaskCompletionSource<>();
        successful.setResult("The good result.");
        return successful.getTask();
    }

    public Task<String> failAsync() {
        TaskCompletionSource<String> failed = new TaskCompletionSource<>();
        failed.setError(new RuntimeException("An error message."));
        return failed.getTask();
    }


    //createAccount Or Login
    private void saveUserdata() {
       // instance = this;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.UidKEY , res_id);
        editor.putString(Const.UnameKEY, res_username);
        editor.putString(Const.EmailKEY, res_email);
        editor.putString(Const.TokenKey, res_token);


        editor.apply();

    }

    //logout
    private void deleteUserdata(){


    }

    private void addDestinations(){

    }

    private void deleteDestination(){

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
