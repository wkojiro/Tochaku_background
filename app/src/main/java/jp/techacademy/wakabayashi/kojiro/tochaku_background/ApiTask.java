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

public class ApiTask implements SharedPreferences.OnSharedPreferenceChangeListener {


    //memo: Responseで受け取る変数
    private User user;
    private String res_token;
    private String res_id;
    private String res_username;
    private String res_email;

    private Context mContext;
    private ProgressDialog mProgress;

    //  private static RailsApi instance = null;


    public ApiTask(Context context, ProgressDialog progress){

        mContext = context;
        mProgress = progress;
    }
    public ApiTask(Context context){

        mContext = context;

    }



    protected Task<String> createAccountAsync(String username, String email, String password) {

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

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
                    //saveUserdata();
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


        /* 本来はこう言う書き方をすべき？
        // OKHTTPとの相性の問題か！？
        taskresult.InBackground(new GetCallback() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    tcs.setResult(object);
                } else {
                    tcs.setError(e);
                }
            }
        });

        client.callInBackground(new Callable<Void>() {
            public Void call() {
                // Do a bunch of stuff.
            }
        }).continueWith(...);

        */




        // リクエストして結果を受け取って
    /*
    この書き方（try catch execute系）は、
    okhttp3.Response response = client.newCall(request).execute(); ここでエラーになる

     */
    /*
        try {
            okhttp3.Response response = client.newCall(request).execute();
            Log.d("debug", String.valueOf(response));
            if (response.isSuccessful()) {

                String jsonData = response.body().string();
                taskresult.setResult(jsonData);

            } else {
                taskresult.setError(new HttpException(response.code()));
            }
        } catch (IOException e) {
            taskresult.setError(e);
            Log.e("hoge", "error orz:" + e.getMessage(), e);
        }

        return taskresult.getTask();

    }
    */

        //memo: 書き方Part2　（これもOKHTTP踏襲だからうまくいかない！？）

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

/*
   public Task<String> loginRequests(String email, String password) {
       final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
  //      final String[] result = {""};
        loginAsync(email, password).onSuccessTask(new Continuation<String, Task<String>>() {
            @Override
            public Task<String> then(Task<String> task) throws Exception {
                final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
                if(task.isCancelled()){
                    //task cancelled
                } else if(task.isFaulted()){
                    Exception error = task.getError();
                } else {
                   // String taskresult = task.getResult();
                    taskresult.setResult("OK");
                    Log.d("debug", task.getResult());
                }

                return saveUserdata(task.getResult());
            }
        }).onSuccess(new Continuation<String, Task<Void>>() {
            @Override
            public Task<Void> then(Task<String> task) throws Exception {
                final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

               // mProgress.dismiss();

               Toast.makeText(mContext,"ログインしました",Toast.LENGTH_SHORT).show();
                return null;
            }
        });

       //memo: 本来はここで切り分けたい。
       taskresult.setResult("OK");
       return taskresult.getTask();
    }
    */

    /*
        public Task<String> createAccountRequests(String username, String email, String password) {
            final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
            //      final String[] result = {""};
            createAccountAsync(username, email, password).onSuccessTask(new Continuation<String, Task<String>>() {
                @Override
                public Task<String> then(Task<String> task) throws Exception {
                    final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
                    if(task.isCancelled()){
                        //task cancelled
                    } else if(task.isFaulted()){
                        Exception error = task.getError();
                    } else {
                        // String taskresult = task.getResult();
                        taskresult.setResult("OK");
                        Log.d("debug", task.getResult());
                    }

                    return saveUserdata(task.getResult());
                }
            }).onSuccess(new Continuation<String, Task<String>>() {
                @Override
                public Task<String> then(Task<String> task) throws Exception {
                    final TaskCompletionSource<String> taskresult2 = new TaskCompletionSource<>();

                    Log.d("debug", "SaveUserInfoDone");
                    //（要確認）
                    taskresult2.setResult("OK");

                    return taskresult2.getTask();
                }
            });

            taskresult.setResult("OK");

            return taskresult.getTask();
        }

    */
    public Task<Void> editAccountAsync(String email, String access_token){

        return null;
    }

    public Task<Void> deleteAccountAsync(String email, String access_token){

        return null;
    }

    public Task<Void> logoutAsync(String email, String access_token) {

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


                    deleteUserdata();
                }
            }


        });

        return null;

    }

    public Task<Void> getDirectionsAsync(String email, String access_token){

        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email="+ email +"&token="+ access_token +"";
        String result = null;

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
                String s = "OK";
                if (response.isSuccessful()) {
                    taskresult.setResult(s);

                    String jsonData = response.body().string();
                    Log.d("debug", jsonData);

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

                }
            }
        });
        // リクエストして結果を受け取って

        return null;
    }

    public Task<Void> createDirectionAsync(String email ,String access_token,String destname, String destemail, String destaddress) {


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
                taskresult.setResult(s);
            }
        });
        // リクエストして結果を受け取って

        return null;
    }


    public Task<Void> editDirectionAsync(String email ,String access_token,String destname, String destemail, String destaddress, String url){

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
                taskresult.setResult(s);

                //memo: 本来はここで新たに取得しなおしたい
                // getDirectionsAsync(email,access_token);
                //finish();

            }
        });

        /*
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
        */


        return null;
    }

    public Task<Void> deleteDirectionAsync(String email,String access_token, String url){

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
                taskresult.setResult(s);

                //memo: 本来はここで新たに取得しなおしたい
                // getDirectionsAsync(email,access_token);
                //finish();

            }
        });
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




    //logout
    public Task<Void> deleteUserdata(){


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

        return null;


    }



    public void deleteDestination(){

    }




    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    private String isCurrent(){
        return Thread.currentThread().getName();
    }



/*

    以下は、AsyncTask okHttpの書き方

    注意：本来は、別のActivityに書かれたものを単に保存用にコピペしています。したがってこの場所ですぐに使えるかどうかは不明です。


 */




    /*


   //メール送信
       public class mailSet extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {

            PostMail(params);

            return null;
        }

        @Override
        protected void onPostExecute(Void result){




        }
    }
    public String PostMail(String[] params){
        HttpURLConnection con = null;//httpURLConnectionのオブジェクトを初期化している。
        BufferedReader reader = null;
        StringBuilder jsonData = new StringBuilder();
        String urlString = "https://rails5api-wkojiro1.c9users.io/trackings.json?email="+ email +"&token="+ access_token +"";

        InputStream inputStream = null;
        String result = "";

        final String json =
                "{" +
                        "\"destname\":\"" + params[0] + "\"," +
                        "\"destemail\":\"" + params[1] + "\"," +
                        "\"destaddress\":\"\"," +
                        "\"nowlatitude\":\"" + params[2] + "\"," +
                        "\"nowlongitude\":\"" + params[3] + "\"" +
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
            final int status = con.getResponseCode();
            Log.d("結果",String.valueOf(status));
            if(status == HttpURLConnection.HTTP_OK) {
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

        Log.d("メール送信", "Postしてみました");
        // mProgress.dismiss();

        result = "OK";

        return result;
    }


//目的地の削除
    private class deletedest extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params){

            final MediaType JSON
                    = MediaType.parse("application/json; charset=utf-8");

            String urlString =  params[0]+"?email="+ apiemail +"&token="+ apitoken +"";
            String result = null;

            // リクエストオブジェクトを作って
            Request request = new Request.Builder()
                    .url(urlString)
                    //.header("Authorization", credential)
                    .delete()
                    .build();

            // クライアントオブジェクトを作って
            OkHttpClient client = new OkHttpClient();

            // リクエストして結果を受け取って
            try {
                okhttp3.Response response = client.newCall(request).execute();
                Log.d("debug", String.valueOf(response));
                if (response.isSuccessful()){

                    result = "OK";
                    Log.d("debug", "doDeletedest success");

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
                //deleteUserdata();
                //Realm側削除
                mRealm = Realm.getDefaultInstance();
                RealmResults<Dest> results = mRealm.where(Dest.class).equalTo("id", dest.getId()).findAll();

                mRealm.beginTransaction();
                results.deleteAllFromRealm();

                mRealm.commitTransaction();

                Snackbar.make(v, "削除しました", Snackbar.LENGTH_LONG).show();

                //memo: 削除されたので、reloadの役割として目的地一覧を取得
                new getDestinations().execute();
                //finish();
            } else {
                Snackbar.make(v, "削除に失敗しました。通信環境をご確認下さい。", Snackbar.LENGTH_LONG).show();
                mProgress.dismiss();

            }
        }
    }

//ログアウト
    private class logout extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            final MediaType JSON
                    = MediaType.parse("application/json; charset=utf-8");

            String urlString = "https://rails5api-wkojiro1.c9users.io/users/sign_out.json";
            String result = null;

            final String json =
                    "{\"user\":{" +
                            "\"email\":\"" + apiemail + "\"," +
                            "\"access_token\":\"" + apitoken + "\"" +
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

            // リクエストして結果を受け取って
            try {
                okhttp3.Response response = client.newCall(request).execute();
                Log.d("debug", String.valueOf(response));
                if (response.isSuccessful()){

                    result = "OK";
                    Log.d("debug", "doPost success");

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

            View v = findViewById(android.R.id.content);
            if(result.equals("OK")) {
                deleteUserdata();

                Snackbar.make(v, "ログアウトしました", Snackbar.LENGTH_LONG).show();

                finish();

                mProgress.dismiss();
            } else {
                Snackbar.make(v, "ログアウトに失敗しました。通信環境をご確認下さい。", Snackbar.LENGTH_LONG).show();
                mProgress.dismiss();
            }
        }
    }
/*

//目的地を編集
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

//目的地を作成
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
    */
    //memo: 目的地一覧をGET
    /*
    private class getDestinations extends AsyncTask<String, Void, String> {
        @Override
        protected  String doInBackground(String... params) {

            final MediaType JSON
                    = MediaType.parse("application/json; charset=utf-8");

            String urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email="+ apiemail +"&token="+ apitoken +"";
            String result = null;

            // リクエストオブジェクトを作って
            Request request = new Request.Builder()
                    .url(urlString)
                    .get()
                    .build();

            // クライアントオブジェクトを作って
            OkHttpClient client = new OkHttpClient();

            // リクエストして結果を受け取って
            try {
                Response response = client.newCall(request).execute();
                Log.d("debug", String.valueOf(response));


                if (response.isSuccessful()){

                    String jsonData = response.body().string();
                    Log.d("debug", jsonData);

                    JSONArray jsonarray = new JSONArray(jsonData);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                    }

                    //Realm mrealm = Realm.getDefaultInstance();
                    mRealm= Realm.getDefaultInstance();
                    mRealm.beginTransaction();
                    Log.d("デリート前",String.valueOf(mRealm.isEmpty()));
                    mRealm.where(Dest.class).findAll().deleteAllFromRealm();
                    Log.d("デリート後",String.valueOf(mRealm.isEmpty()));
                    mRealm.createOrUpdateAllFromJson(Dest.class,jsonarray); //Realm にそのまま吸い込まれた
                    Log.d("後",String.valueOf(mRealm.isEmpty()));
                    mRealm.commitTransaction();

                    result = "OK";
                    Log.d("debug", "doPost success");
                    //  Log.d("debug", result);

                }
            } catch (IOException | JSONException e) { //Java SE 7 以降で有効な次の例では、重複したコードをなくすことができます。http://docs.oracle.com/javase/jp/7/technotes/guides/language/catch-multiple.html
                e.printStackTrace();
                result = "NG";
                Log.e("error", "error orz:" + e.getMessage(), e);
            }
            // 返す
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("GetDestination","done");
            View v = findViewById(android.R.id.content);
            if(result.equals("OK")) {
                //saveUserdata();

                Snackbar.make(v, "目的地の一覧を取得しました", Snackbar.LENGTH_LONG).show();
                mProgress.dismiss();

            } else {
                Snackbar.make(v, "目的地の一覧取得に失敗しました。通信環境をご確認下さい。", Snackbar.LENGTH_LONG).show();
                mProgress.dismiss();
            }
        }
    }
    */






}
