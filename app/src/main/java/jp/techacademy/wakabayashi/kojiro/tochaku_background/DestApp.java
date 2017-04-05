package jp.techacademy.wakabayashi.kojiro.tochaku_background;

/**
 * Created by wkojiro on 2017/03/15.
 */

import android.app.Application;

import io.realm.Realm;

public class DestApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}

