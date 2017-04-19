package jp.techacademy.wakabayashi.kojiro.tochaku_background;

/**
 * Created by wkojiro on 2017/03/15.
 *
 *
 * http://techbooster.org/android/application/2353/
 * onCreate	Applicationクラス作成時
 * onTerminate	Applicationクラス終了時
 * onLowMemory	使用出来るメモリが少なくなった時
 * onConfigurationChanged	端末の状態が変わった時（オリエンテーションの変更など
 *
 *
 *
 *
 */

import android.app.Application;
import android.util.Log;

import io.realm.Realm;

public class DestApp extends Application {

    private final String TAG = "DEBUG-APPLICATION";
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);


        Log.v(TAG,"--- onCreate() in ---");

    }





}

