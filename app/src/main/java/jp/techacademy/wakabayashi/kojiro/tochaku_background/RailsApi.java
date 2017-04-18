package jp.techacademy.wakabayashi.kojiro.tochaku_background;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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
    private ProgressDialog mProgress;

  //  private static RailsApi instance = null;


    public RailsApi(Context context, ProgressDialog progress){

      mContext = context;
      mProgress = progress;
    }
    public RailsApi(Context context){

      mContext = context;

    }



    public Task<String> createAccountAsync(String username, String email, String password) {

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "https://rails5api-wkojiro1.c9users.io/users.json"; //tocakudemo
        //String result = null;

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
                if (response.isSuccessful()) {


                    String jsonData = response.body().string();

                    taskresult.setResult(jsonData);
                    Log.d("debug", jsonData);

                }else{
                    taskresult.setError(new HttpException(response.code()));
                }

            }

        });

        Log.d("taskSource", String.valueOf(taskresult.getTask()));
        return taskresult.getTask();
    }


    public Task<String> loginAsync(String email, String password) {

        Log.d("Thread","LoginAsync"+Thread.currentThread().getName());
        final TaskCompletionSource taskresult = new TaskCompletionSource<>();

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "https://rails5api-wkojiro1.c9users.io/users/sign_in.json";

        //memo: ここでこれ必要か？？
        user = new User();
        user.setEmail(email);
        user.setPassword(password);

        final String json =
                "{\"user\":{" +
                        "\"email\":\"" + email + "\"," +
                        "\"password\":\"" + password + "\"" +
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



    //memo: リクエストしている

        client.newCall(request).enqueue(new Callback() {



            @Override
            public void onFailure(Call call, IOException e) {
                taskresult.setError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //String s = "OK";

                Log.d("Thread","LoginAsynconResponce"+Thread.currentThread().getName());
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    taskresult.setResult(jsonData);
                }else{
                    taskresult.setError(new HttpException(response.code()));

                }
            }

        });

        return taskresult.getTask();

    }


    //createAccount Or Login
    protected Task<String> saveUserdata(String jsonData) {

        Log.d("Thread","SaveUserdata"+Thread.currentThread().getName());
  // public void saveUserdata(){
       final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

       Gson gson = new Gson();

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

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.UidKEY , res_id);
        editor.putString(Const.UnameKEY, res_username);
        editor.putString(Const.EmailKEY, res_email);
        editor.putString(Const.TokenKey, res_token);

        editor.apply();

        taskresult.setResult("OK");
        //return taskresult.getTask();

       Log.d("debug", "doSaveUserInfo success");
       return taskresult.getTask();
   }


    public Task<Void> editAccountAsync(String email, String access_token){

        return null;
    }

    public Task<Void> deleteAccountAsync(String email, String access_token){

        return null;
    }

    public Task<String> logoutAsync(String email, String access_token) {

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
                Log.d("debug",response.toString());
                if (response.isSuccessful()) {
                    taskresult.setResult(s);


                   // deleteUserdata();
                } else {
                    taskresult.setError(new HttpException(response.code()));
                }
            }
        });
        return taskresult.getTask();
    }

    //logout
    protected Task<String> deleteUserdata(){

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        // Preferenceを削除する

        /*
        .commit() から .apply()に変更。 20170411
         */
        Realm mRealm = Realm.getDefaultInstance();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.edit().remove("username").remove("email").remove("access_token").remove("id").remove("position_id").remove("destname").remove("destaddress").remove("destemail").remove("latitude").remove("longitude").apply();
        //sp.edit().clear().commit();
        Log.d("Delete","done");


        mRealm.beginTransaction();
        mRealm.deleteAll();
        mRealm.commitTransaction();

        taskresult.setResult("OK");

        return taskresult.getTask();


    }


    public Task<String> getDirectionsAsync(String email, String access_token){

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email="+ email +"&token="+ access_token +"";
       // String result = null;

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(urlString)
                .get()
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
               // String s = "OK";
                if (response.isSuccessful()) {


                    String jsonData = response.body().string();
                    Log.d("debug", jsonData);

                    taskresult.setResult(jsonData);

                } else {
                    taskresult.setError(new HttpException(response.code()));
                }
            }
        });
        // リクエストして結果を受け取って

        return taskresult.getTask();
    }

    protected Task<String> saveDestinationdata(String jsonData){
        Log.d("Thread","SaveUserdata"+Thread.currentThread().getName());
        // public void saveUserdata(){
        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        JSONArray jsonarray = null;
        try {
            jsonarray = new JSONArray(jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < jsonarray.length(); i++) {
            try {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //Realm mrealm = Realm.getDefaultInstance();
        Realm mRealm= Realm.getDefaultInstance();
        mRealm.beginTransaction();
        Log.d("デリート前",String.valueOf(mRealm.isEmpty()));
        mRealm.where(Dest.class).findAll().deleteAllFromRealm();
        Log.d("デリート後",String.valueOf(mRealm.isEmpty()));
        mRealm.createOrUpdateAllFromJson(Dest.class,jsonarray); //Realm にそのまま吸い込まれた
        Log.d("後",String.valueOf(mRealm.isEmpty()));
        mRealm.commitTransaction();

        Log.d("debug", "doPost success");


        taskresult.setResult("OK");
        return taskresult.getTask();
    }



    public Task<String> createDirectionAsync(String email ,String access_token,String destname, String destemail, String destaddress) {


        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email=" + email + "&token=" + access_token + "";

        String result = null;
        Dest dest = new Dest();
        dest.setDestName(destname);
        dest.setDestEmail(destemail);
        dest.setDestAddress(destaddress);

        final String json =
                "{" +
                        "\"destname\":\"" + destname + "\"," +
                        "\"destemail\":\"" + destemail + "\"," +
                        "\"destaddress\":\"" + destaddress + "\"" +
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
                }else{
                    taskresult.setError(new HttpException(response.code()));
                }
            }
        });
        // リクエストして結果を受け取って

        return taskresult.getTask();
    }


    public Task<String> editDirectionAsync(String email ,String access_token,String destname, String destemail, String destaddress, String url){

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = url +"?email="+ email +"&token="+ access_token +"";

        final String json =
                "{" +
                        "\"destname\":\"" + destname + "\"," +
                        "\"destemail\":\"" + destemail + "\"," +
                        "\"destaddress\":\"" + destaddress + "\"" +
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

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                taskresult.setError(e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = "OK";

                if(response.isSuccessful()) {
                    taskresult.setResult(s);
                } else {
                    taskresult.setError(new HttpException(response.code()));
                }

            }
        });

        return taskresult.getTask();
    }

    public Task<String> deleteDirectionAsync(String email,String access_token, String url){

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString =  url+"?email="+ email +"&token="+ access_token +"";
        String result = null;

        // リクエストオブジェクトを作って
        Request request = new Request.Builder()
                .url(urlString)
                //.header("Authorization", credential)
                .delete()
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
                if(response.isSuccessful()) {
                    taskresult.setResult(s);
                } else {
                    taskresult.setError(new HttpException(response.code()));
                }

            }
        });


        return taskresult.getTask();
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
                }else{
                    taskresult.setError(new HttpException(response.code()));
                }

            }

        });

        Log.d("taskSource", String.valueOf(taskresult.getTask()));
        return taskresult.getTask();
    }



    public  Task<String> test() {



        return Task.forResult("OK");
    }


    public  Task<String> test01() {
        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        String s = "test01";

        taskresult.setResult(s);
        Log.d("debug","test01");
        return taskresult.getTask();

    }

    public  Task<String> test02() {
        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        String s = "test02";
        taskresult.setResult(s);
        Log.d("debug","test02");
        return taskresult.getTask();
    }

    public  Task<String> test03() {
        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        String s = "test03";
        taskresult.setResult(s);
        Log.d("debug","test03");
        return taskresult.getTask();
    }

    public Task<Void> test04() {
       // final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        String s = "test03";
       // taskresult.setResult(s);
        Log.d("debug","test03");
        return null;
    }

    private void shortwaterfallRequests() {

        test01().continueWithTask(new Continuation<String, Task<String>>() {
            @Override
            public Task<String> then(Task<String> task) throws Exception {
                return test02();
            }
        }).continueWithTask(new Continuation<String, Task<String>>() {
            @Override
            public Task<String> then(Task<String> task) throws Exception {
                return test03();
            }
        }).continueWith(new Continuation<String, Void>() {
            @Override
            public Void then(Task<String> task) throws Exception {
                return null;
            }
        });


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








    public void deleteDestination(){

    }




    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    private String isCurrent(){
        return Thread.currentThread().getName();
    }
}
