package jp.techacademy.wakabayashi.kojiro.tochaku_background;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

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

public class RailsApi {




    public RailsApi(){

    }

    public Task<String>  postMailAsync(String destname,String destemail, String nowlatitude, String nowlongitude) throws IOException {
        final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();

        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        String urlString = "https://rails5api-wkojiro1.c9users.io/trackings.json?email=test00@test.com&token=1:YUJo6C_adXVod4na3onD";
        String result = null;

        String[] params = {"東京駅","wkojiro22@gmail.com","35.681298","139.766247"};

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

    public Task<Void> login() {


        return null;
    }

    public Task<Void> logout() {


        return null;
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

}
