package jp.techacademy.wakabayashi.kojiro.tochaku_background;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.Manifest;

import android.content.pm.PackageManager;

import android.net.Uri;

import android.provider.Settings;
import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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

/**
 * The only activity in this sample.
 *
 * Note: for apps running in the background on "O" devices (regardless of the targetSdkVersion),
 * location may be computed less frequently than requested when the app is not in the foreground.
 * Apps that use a foreground service -  which involves displaying a non-dismissable
 * notification -  can bypass the background location limits and request location updates as before.
 *
 * This sample uses a long-running bound and started service for location updates. The service is
 * aware of foreground status of this activity, which is the only bound client in
 * this sample. After requesting location updates, when the activity ceases to be in the foreground,
 * the service promotes itself to a foreground service and continues receiving location updates.
 * When the activity comes back to the foreground, the foreground service stops, and the
 * notification associated with that foreground service is removed.
 *
 * While the foreground service notification is displayed, the user has the option to launch the
 * activity from the notification. The user can also remove location updates directly from the
 * notification. This dismisses the notification and stops the service.
 *
 *
 * おもなToDO　
 * ・状態による切り分け（　User情報があるか、目的地情報があるか、Permissionがあるか）
 *
 *
 *
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener,OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_FIX_DEST_CODE = 111;
    // Used in checking for runtime permissions.
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;


    //memo: サービスとActivityがConnectされているかどうか Tracks the bound state of the service.
    private boolean mBound = false;

    // UI elements.
    private Button mRequestLocationUpdatesButton;
    private Button mRemoveLocationUpdatesButton;
    private TextView mTextView;
    private TextView mDestTextView;

    // GoogleMap
    private GoogleMap mMap = null;

    //memo: preference保存用
    //SharedPreferences sp;
    TextView mUsername;


    //memo: preferenceから現在登録されているユーザーを受け取る為の変数
    String username;
    String email;
    String access_token;


    //memo: preferenceから現在登録されている目的地を受け取る為の変数
    String address;
    String latitude; //StringにしているけどFloat
    String longitude;//StringにしているけどFloat
    String destname;
    String destemail;
    private Double destlatitude, destlongitude;
    private LatLng latlng;

    protected Location mCurrentLocation;
    protected LatLng currentlatlng;

    //memo: Tracks the status of the location updates request. Value changes when the user presses the Start Updates and Stop Updates buttons.
    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;

    Marker destmarker;
    Marker currentMarker;
    MarkerOptions currentMarkerOptions = new MarkerOptions();
    Polyline polylineFinal;
    PolylineOptions options;

    private Float originaldestance;
    private Float nowdestance;
    private double referencedestance;

    Double currentlatitude;
    Double currentlongitude;

    //memo: set value(スコープを要注意）
    Integer mailCount = 0;
    Integer mStatus = 0;
    //0:現在位置を取得します（現在位置を取得＆目的地があればセットされた名前を表示）　　
    //1:出発します（地図や距離を表示して計測スタート）
    //2:停止します（地図をDefaultに切り替えて、
    //3:停止中（０へ）


//memo; サービスとのコネクション状況の確認 onBind
    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            //memo: mServiceとはLocationUpdatesServiceそのもの。そこと接続するという意味か？（＝LocationUpdatesServiceを起動するということ？）
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //memo:サービスからの戻りを受け取る。（この位置に記述することが恐らく重要）
        myReceiver = new MyReceiver();
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("到着予報");
        toolbar.setTitleTextColor(Color.WHITE);

        setSupportActionBar(toolbar);

        //memo: set values
        Log.d("mStatus", "01"+String.valueOf(mStatus));
        mailCount = 0;
        mStatus = 0;
        Log.d("mStatus", "02"+String.valueOf(mStatus));


        mTextView = (TextView) findViewById(R.id.textView);
        mDestTextView = (TextView) findViewById(R.id.dest_text);
        mUsername = (TextView)findViewById(R.id.username);
        mTextView.setText("ddd");


        //memo: Permission 必要なし　単にmap の表示　コールバックはonMapReady
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //memo: Permission を得ると、Preferenceに保存しているようです。その状態をここでチェックしている。 Check that the user hasn't revoked permissions by going to Settings.
        if (Utils.requestingLocationUpdates(this)) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }
    }

    //memo: onCreateではなく、onStartに記述しているのは、バックグラウンドから戻って来ることを想定しているから？
    @Override
    protected void onStart() {
        //onStart は Activityが開始された時に呼ばれる。Activity生成されたがユーザーには見えない時。
        //Activityが起こされるときはほとんど、onStartが呼び出される。
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        mRequestLocationUpdatesButton = (Button) findViewById(R.id.request_location_updates_button);
        mRemoveLocationUpdatesButton = (Button) findViewById(R.id.remove_location_updates_button);


        Log.d("debug","いつ呼ばれるか");
        Toast.makeText(this,"ステータス"+mStatus,Toast.LENGTH_SHORT).show();
        // Restore the state of the buttons when the activity (re)launches.
        setButtonsState(Utils.requestingLocationUpdates(this));

        mRequestLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermissions()) {
                    requestPermissions();
                } else {
                    //ここにユーザと目的地のチェックをする
                    if (Utils.isEmptyUser(getApplicationContext())) {
                        Toast toast = Toast.makeText(MainActivity.this, "会員登録またはログインしてください。", Toast.LENGTH_LONG);
                        toast.show();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    } else if (Utils.isEmptyDest(getApplicationContext())){
                        Toast toast = Toast.makeText(MainActivity.this, "目的地を設定してください。", Toast.LENGTH_LONG);
                        toast.show();

                        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                        startActivityForResult(intent,REQUEST_FIX_DEST_CODE);

                    } else {

                        //memo: LocationUpdatesService　ClassのrequestLocationUpdatesを起動させている（APIを繋ぐ）
                        mService.requestLocationUpdates();

                        switch (mStatus){
                            case 0:

                                Toast.makeText(MainActivity.this, "Button mStatus"+mStatus, Toast.LENGTH_LONG).show();
                                mStatus = 1;
                                break;
                            case 1:
                                Toast.makeText(MainActivity.this, "Case1"+mStatus, Toast.LENGTH_LONG).show();
                                if(mailCount== 0) {
                                    Toast.makeText(MainActivity.this, "メール"+mStatus, Toast.LENGTH_LONG).show();
                                    new commingmail().execute(destname, destemail, String.valueOf(currentlatitude), String.valueOf(currentlongitude));
                                }
                                mailCount = 1;

                                mStatus = 2;
                                break;
                            case 2:
                                mStatus = 0;
                                break;

                        }
                        setButtonsState(Utils.requestingLocationUpdates(getApplicationContext()));
                    }
                }
            }
        });

        //memo: APIを明示的に削除する　（ここを押されたら地図及びメールカウントをリセットさせる）
        mRemoveLocationUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mService.removeLocationUpdates();

                //memo: 地図とメールとステータスをクリア
                mMap.clear();
                defaultMap();
                mStatus = 0;
                mailCount = 0;

                setButtonsState(Utils.requestingLocationUpdates(getApplicationContext()));

            }
        });


        /*
        Log.d("debug","いつ呼ばれるか");
        Toast.makeText(this,"ステータス"+mStatus,Toast.LENGTH_SHORT).show();
        // Restore the state of the buttons when the activity (re)launches.
        setButtonsState(Utils.requestingLocationUpdates(this));
        */
        //memo: ForeGroundかどうかを確認Activityが生きていたらサービスをバックグラウンドモードにする、（という意味だと思われる。）
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    //memo:今回の仕様は前回と違い、Backgroudでも同様に計測するので、ここではConnectionについて感知しない。
    //memo:foreかBackかによって
    @Override
    protected void onResume() {
        super.onResume();
        //memo:受け手となるReceiverを登録してあげる。
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(LocationUpdatesService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        //memo:バックに入るので、レシーバーは要らなくなるので登録解除する。
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        //memo:アプリ自体を止めた時は、サービスとActivityの関係も解く。
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        //memo:　リスナーも律儀にやめるのか？そこまで必要あるのかしら。通常は、セット（Listnerの登録とセットで解除も書くらしい。知らなかった）
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }


    //memo: startActivityForResultのコールバック
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MainActivityに", "戻ってきた");
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            //memo: 計測開始途中で設定画面に行った場合の処理
            case REQUEST_FIX_DEST_CODE:
                switch (resultCode){
                    case RESULT_OK:
                        Log.d("最初から","最初から");
                        //最初からやり直す
                        mMap.clear();
                        mailCount = 0;
                        mStatus = 0;
                        defaultMap();

//memo: 戻ってきてもmStatus= 0 のButton に切り替わらない
                        setButtonsState(Utils.requestingLocationUpdates(getApplicationContext()));

                        //  stopLocationUpdates();

                     //   updateUI();

                        Toast.makeText(this, "目的地が変更されました。" , Toast.LENGTH_LONG).show();
                        break;
                    case RESULT_CANCELED:

                        break;
                }
                break;
        }
    }


    /**
     * Returns the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        return  PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                mService.requestLocationUpdates();
            } else {
                //memo:パーミッションが拒否された場合にスナックバーがクリッかぶるになるっぽい Permission denied.
                setButtonsState(false);
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                .show();
            }
        }
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(MainActivity.this, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();

                mCurrentLocation = location;

                mTextView.setText(Utils.getLocationText(location));

                //memo:ここで切り分けが必要。
                /*
                イメージ的には、if(mStatus = 0).
                mStatusはボタン周りで変更させる。
                 */
                //memo: 現状ではPermission取得後に会員情報、目的地情報がなくてもfirstMapを呼びに来るのでここでエラーつまづく
                //ユーザー目的地があれば、表示、

                switch(mStatus) {
                    case 0:
                        defaultMap();
                        break;
                    case 1:
                        firstMap();
                        break;
                    case 2:
                        activeMap();
                        break;


                }
            }
        }
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {
            setButtonsState(sp.getBoolean(Utils.KEY_REQUESTING_LOCATION_UPDATES, false));
        }


        //memo:
        //getString(第一引数はキー、第二引数はデフォルト値）つまり、何も保存していない場合は、呼び出された時にデフォルト値である””を返す。
            username = sp.getString(Const.UnameKEY, "");
            email = sp.getString(Const.EmailKEY, "");
            access_token = sp.getString(Const.TokenKey, "");
        //memo: 現在設定されている目的地の取得


            destname = sp.getString(Const.DestnameKEY, "");
            address = sp.getString(Const.DestaddressKEY, "");
            destemail = sp.getString(Const.DestemailKEY, "");
            latitude = sp.getString(Const.DestLatitudeKEY, "");
            longitude = sp.getString(Const.DestLongitudeKEY, "");
            Log.d("目的地名", String.valueOf(destname));
            Log.d("目的地住所", String.valueOf(address));
            Log.d("目的地メールアドレス", String.valueOf(destemail));
            Log.d("緯度", String.valueOf(latitude));
            Log.d("経度", String.valueOf(longitude));

            //memo: 開発用。ログインしているかどうかを判別しやすくするため。後で消す
            mUsername = (TextView) findViewById(R.id.username);
            mUsername.setText(String.valueOf(username));

            //memo: 目的地が変更されたら即座に変更
            // 値がなければ””が返ってしまう。それをDoubleに変えられないからエラーとなっていた。）

            if (!Utils.isEmptyDest(this)) {
                destlatitude = Double.parseDouble(latitude);
                destlongitude = Double.parseDouble(longitude);

                latlng = new LatLng(destlatitude, destlongitude);
                Log.d("debug", "onSharedPreferenceChangedListner_setMarkerが呼ばれる");
                // 標準のマーカー
                //setMarker(destlatitude, destlongitude);
            }

    }




    //memo:変更予定
   private void setButtonsState(boolean requestingLocationUpdates) {
Log.d("debug","onStartで発火しているか");
       Log.d("debug","発火："+requestingLocationUpdates);
       Log.d("debug","発火："+mStatus);

        if (requestingLocationUpdates) {
        //memo: 計測中

            switch (mStatus) {
                case 0:
                    mRequestLocationUpdatesButton.setEnabled(false);
                    mRemoveLocationUpdatesButton.setEnabled(true);
                    mRemoveLocationUpdatesButton.setText("停止");
                    break;
                case 1:
                    mRequestLocationUpdatesButton.setEnabled(true);
                    mRemoveLocationUpdatesButton.setEnabled(true);
                    mRequestLocationUpdatesButton.setText("出発します！");
                    mRemoveLocationUpdatesButton.setText("キャンセル");
                    break;
                case 2:
                    mRequestLocationUpdatesButton.setEnabled(false);
                    mRemoveLocationUpdatesButton.setEnabled(true);
                    mRemoveLocationUpdatesButton.setText("停止");
                   // mRemoveLocationUpdatesButton.setVisibility(View.GONE);
                    //  mRequestLocationUpdatesButton.setEnabled(false);
                    break;
            }
        }else{
        //memo: 計測前
            mRequestLocationUpdatesButton.setEnabled(true);
            mRequestLocationUpdatesButton.setText("現在位置を取得");
            mRemoveLocationUpdatesButton.setEnabled(false);

        //    mRequestLocationUpdatesButton.setEnabled(true);
        //    mRemoveLocationUpdatesButton.setEnabled(false);
        }



    }

    //memo: ここはmapFragment.getMapAsync(this);に答えているだけ。ここで地図を生成しておく。
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady", "when do you call me?");
        mMap = googleMap;
        defaultMap();

    }
    private void defaultMap() {

        UiSettings us = mMap.getUiSettings();
        us.setMapToolbarEnabled(false);

        LatLng JAPAN = new LatLng(36, 139);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JAPAN, (float) 4.8));

        mDestTextView.setVisibility(View.INVISIBLE);


    }
    private void firstMap(){
//PermissionがOKとなっている状態。この前段階でUser情報は取得しておきたい


        mDestTextView.setVisibility(View.VISIBLE);
        if (currentMarker != null) {
            currentMarker.remove();
        }
        // 設定の取得
        UiSettings settings = mMap.getUiSettings();
        settings.setZoomControlsEnabled(true);

        currentlatitude = mCurrentLocation.getLatitude();
        currentlongitude = mCurrentLocation.getLongitude();


        /* hide for background*/
        mDestTextView.setText("目的地に［"+destname+"］がセットされました。目的地を変更するには［設定］画面から変更できます。");


            //memo: 目的地をセット
            destlatitude = Double.parseDouble(latitude);
            destlongitude = Double.parseDouble(longitude);

            latlng = new LatLng(destlatitude, destlongitude);
            setMarker(destlatitude, destlongitude);

        //memo:　現在位置をセット
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        currentlatlng = new LatLng(currentlatitude, currentlongitude);
        currentMarkerOptions.position(currentlatlng);
        currentMarkerOptions.title("現在位置");
        currentMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        currentMarker = mMap.addMarker(currentMarkerOptions);


        // mMap.animateCamera(CameraUpdateFactory.newLatLng(currentlatlng));

        //memo:　目的地と現在位置に線を引く（Routeでは無いからあんまり意味ない 、この間に移動を感知すると何回も線を引いてしまう。）

        /* hide for background */
        if(polylineFinal != null) {

            polylineFinal.remove();
        }
        options = new PolylineOptions();
        options.add(currentlatlng); //
        options.add(latlng); //
        options.color(0xcc00ffff);
        options.width(10);
        // options.geodesic(true); // 測地線で表示
        polylineFinal = mMap.addPolyline(options);


        //memo:　目的地と現在位置の距離を取る
        float[] results = new float[1];
        Location.distanceBetween(destlatitude, destlongitude, currentlatitude, currentlongitude, results);
        Toast.makeText(getApplicationContext(), "距離：" + ( (Float)(results[0]/1000) ).toString() + "Km", Toast.LENGTH_LONG).show();

        originaldestance = results[0]/1000;
        referencedestance = originaldestance * 0.3;

          mDestTextView.setText("目的地までの距離：" + originaldestance + "Km");

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(destmarker.getPosition());
        builder.include(currentMarker.getPosition());
        LatLngBounds bounds = builder.build();
        mMap.setPadding( 50,250,50,250); //   left,        top,       right,  bottom
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 120);

        mMap.moveCamera(cu);
        /**/

    }



    private void activeMap() {
        mDestTextView.setVisibility(View.VISIBLE);
        if (currentMarker != null) {
            currentMarker.remove();
        }
        // 設定の取得
        UiSettings  settings = mMap.getUiSettings();
        settings.setZoomControlsEnabled(true);

        currentlatitude = mCurrentLocation.getLatitude();
        currentlongitude = mCurrentLocation.getLongitude();

        //memo: 目的地をセット
        destlatitude = Double.parseDouble(latitude);
        destlongitude = Double.parseDouble(longitude);

        latlng = new LatLng(destlatitude, destlongitude);
        setMarker(destlatitude, destlongitude);

        //memo:　現在位置をセット
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        currentlatlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        currentMarkerOptions.position(currentlatlng);
        currentMarkerOptions.title("現在位置");
        currentMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        currentMarker = mMap.addMarker(currentMarkerOptions);

        polylineFinal.remove();


        //memo:　目的地と現在位置の距離を取る
        float[] results = new float[1];
        Location.distanceBetween(destlatitude, destlongitude, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), results);
        Toast.makeText(getApplicationContext(), "距離：" + ( (Float)(results[0]/1000) ).toString() + "Km", Toast.LENGTH_LONG).show();

        nowdestance = results[0]/1000;

        mDestTextView.setText("目的地までの距離：" + nowdestance + "Km");


        zoomMap(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());


        Log.d("debug", String.valueOf(referencedestance));
        Log.d("debug", String.valueOf(nowdestance));
        Log.d("debug", String.valueOf(nowdestance - referencedestance));

        Toast.makeText(this,"mailcount" + mailCount +"",Toast.LENGTH_LONG).show();

        if(nowdestance - referencedestance <= 0 && mailCount == 1) {

            // Log.d("debug",referencedestance);
            new commingmail().execute(destname, destemail, String.valueOf(currentlatitude), String.valueOf(currentlongitude));
            Toast.makeText(this, "全行程の７０％を通過しました。", Toast.LENGTH_LONG).show();
            mailCount = 2;
        }

    }

    private void setMarker(double destlatitude, double destlongitude) {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title(destname);
        destmarker = mMap.addMarker(markerOptions);

        // ズーム
        //zoomMap(destlatitude, destlongitude);
    }
    private void zoomMap(double destlatitude, double destlongitude) {
        /*
        memo: 1 ドアップ　0.1　何も見えない　10 海？何も見えない　0.9
         */
        double south = destlatitude * (1 - 0.00005);
        double west = destlongitude * (1 - 0.00005);
        double north = destlatitude * (1 + 0.00005);
        double east = destlongitude * (1 + 0.00005);


        // LatLngBounds (LatLng southwest, LatLng northeast)
        LatLngBounds bounds = LatLngBounds.builder()
                .include(new LatLng(south, west))
                .include(new LatLng(north, east))
                .build();

       Integer width = getResources().getDisplayMetrics().widthPixels;
       Integer height = getResources().getDisplayMetrics().heightPixels;

        // static CameraUpdate.newLatLngBounds(LatLngBounds bounds, int width, int height, int padding)

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 0));

    }


    private class commingmail extends AsyncTask<String, Void, Void> {
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




    //memo: 右上のメニュー
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.item1) {
            if (Utils.isEmptyUser(this)) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivityForResult(intent,REQUEST_FIX_DEST_CODE);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
