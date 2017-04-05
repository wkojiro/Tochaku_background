package jp.techacademy.wakabayashi.kojiro.tochaku_background;

/*

１　このActivityがonCreateされた時にAPIを叩いて、登録されているDestの一覧をGetする。
２　一覧表示する

http://qiita.com/kskso9/items/01c8bbb39355af9ec25e
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONObject;

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
import java.util.ArrayList;
import java.util.Map;


//Realm関連
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class SettingActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String EXTRA_DEST = "jp.techacademy.wakabayashi.kojiro.tochaku.DEST";
    private static final int REQUEST_DEST_CODE = 123;


    //パーツの定義
    TextView mUserNameText;
    TextView mEmailText;
    TextView mDestCountText;
    Button mProfileButton;
    Button mLogoutButton;


    public Dest destRealm;
    private Realm mRealm;
    private RealmResults<Dest> mDestRealmResults;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {

            //memo: 目的地一覧を取得
            Log.d("Reload","reload");
            reloadListView();
        }
    };


    //リストView
    private ListView mListView;
    //GetのResponseを受けるパラメータ
    private ArrayList<Dest> mDestArrayList;
    private DestAdapter mDestAdapter;

    ProgressDialog mProgress;

    //API通信のための会員Email及びToken(Preferenseから取得）
    String apiusername;
    String apiemail;
    String apitoken;

    //削除する目的地をいれておく
    Dest dest;


    SharedPreferences sp;

    //API用変数
    HttpURLConnection con = null;//httpURLConnectionのオブジェクトを初期化している。
    BufferedReader reader = null;
    // StringBuilder jsonData = new StringBuilder();
    InputStream inputStream = null;
    String result = "";
    String result2 = "";
    int selected_position = 0;
    int status = 0;



    public String Deletedest(String[] params){


        String urlString =  params[0]+"?email="+ apiemail +"&token="+ apitoken +"";

        try{
            URL url = new URL(urlString);
            con = (HttpURLConnection) url.openConnection(); //HttpURLConnectionを取得する
            con.setRequestMethod("DELETE");
            con.setInstanceFollowRedirects(false); // HTTP リダイレクト (応答コード 3xx の要求) を、この HttpURLConnection インスタンスで自動に従うかどうかを設定します。
            con.setRequestProperty("Accept-Language", "jp");
            con.setDoOutput(false); //この URLConnection の doOutput フィールドの値を、指定された値に設定します。→イマイチよく理解できない（URL 接続は、入力または出力、あるいはその両方に対して使用できます。URL 接続を出力用として使用する予定である場合は doOutput フラグを true に設定し、そうでない場合は false に設定します。デフォルトは false です。）
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

           // OutputStream os = con.getOutputStream(); //この接続に書き込みを行う出力ストリームを返します
            con.connect();

            // con.getResponseCode();

            status = con.getResponseCode();
            Log.d("レスポンス",String.valueOf(status));

        }catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (status/100 == 2){
            result2 = "OK";
        } else {
            result2 = "NG";
        }
        return result2;
    }


    public String Deletelogout(){

        String urlString = "https://rails5api-wkojiro1.c9users.io/users/sign_out.json";


        final String json =
                "{\"user\":{" +
                        "\"email\":\"" + apiemail + "\"," +
                        "\"access_token\":\"" + apitoken + "\"" +
                        "}" +
                "}";

        System.out.println(json.toString());
        try {

            URL url = new URL(urlString);
            con = (HttpURLConnection) url.openConnection(); //HttpURLConnectionを取得する
            con.setRequestMethod("DELETE");
            con.setInstanceFollowRedirects(false); // HTTP リダイレクト (応答コード 3xx の要求) を、この HttpURLConnection インスタンスで自動に従うかどうかを設定します。
            con.setRequestProperty("Accept-Language", "jp");
            con.setDoOutput(false); //この URLConnection の doOutput フィールドの値を、指定された値に設定します。→イマイチよく理解できない（URL 接続は、入力または出力、あるいはその両方に対して使用できます。URL 接続を出力用として使用する予定である場合は doOutput フラグを true に設定し、そうでない場合は false に設定します。デフォルトは false です。）
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");


            // リスエストの送信
            OutputStream os = con.getOutputStream(); //この接続に書き込みを行う出力ストリームを返します
            con.connect();

            // con.getResponseCode();

            PrintStream ps = new PrintStream(os); //行の自動フラッシュは行わずに、指定のファイルで新しい出力ストリームを作成します。
            ps.print(json);// JsonをPOSTする
            ps.close();
            status = con.getResponseCode();
            Log.d("レスポンス",String.valueOf(status));


        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // mProgress.dismiss();

        if (status/100 == 2){
            result2 = "OK";
        } else {
            result2 = "NG";
        }
        return result2;

    }


    //memo: GetDestList
    public String GetdestList(){

        String urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email="+ apiemail +"&token="+ apitoken +"";

        try {

            URL url = new URL(urlString); //URLを生成
            con = (HttpURLConnection) url.openConnection(); //HttpURLConnectionを取得する
            con.setRequestMethod("GET");
            con.setInstanceFollowRedirects(false); // HTTP リダイレクト (応答コード 3xx の要求) を、この HttpURLConnection インスタンスで自動に従うかどうかを設定します。
            con.setRequestProperty("Accept-Language", "jp");
           // con.setDoInput(true); //この URLConnection の doOutput フィールドの値を、指定された値に設定します。→イマイチよく理解できない（URL 接続は、入力または出力、あるいはその両方に対して使用できます。URL 接続を出力用として使用する予定である場合は doOutput フラグを true に設定し、そうでない場合は false に設定します。デフォルトは false です。）
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            inputStream = con.getInputStream(); //GETだから
            con.connect();

            status = con.getResponseCode();
            Log.d("5結果",String.valueOf(status));
            if(status/100 == 2){

                reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));//デフォルトサイズのバッファーでバッファリングされた、文字型入力ストリームを作成します。
                String line = reader.readLine();

                StringBuilder jsonData = new StringBuilder();
                while (line != null) {
                    jsonData.append(line);
                    line = reader.readLine();
                }

               System.out.println(jsonData.toString());

                JSONArray jsonarray = new JSONArray(jsonData.toString());
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);
                }

              Log.d("6jsonArray",String.valueOf(jsonarray));

                //Realm mrealm = Realm.getDefaultInstance();
                mRealm= Realm.getDefaultInstance();
                mRealm.beginTransaction();
                Log.d("デリート前",String.valueOf(mRealm.isEmpty()));
                mRealm.where(Dest.class).findAll().deleteAllFromRealm();
                Log.d("デリート後",String.valueOf(mRealm.isEmpty()));
                mRealm.createOrUpdateAllFromJson(Dest.class,jsonarray); //Realm にそのまま吸い込まれた
                Log.d("後",String.valueOf(mRealm.isEmpty()));
                mRealm.commitTransaction();

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
        if (status/100 == 2){
            result2 = "OK";
        } else {
            result2 = "NG";
        }
        return result2;
    }


    @Override
    protected void onStart(){
        super.onStart();
        //memo: 目的地一覧を取得
       // Log.d("onstart","G");new getDestinations().execute();

    }


    /* onCreate */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("SettingActivity","onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // ActionBarを設定する// ツールバーをアクションバーとしてセット
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setTitle("タイトル");

        //memo: Login時に保存したユーザーデータを取得
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);

        apiusername = sp.getString(Const.UnameKEY, "");
        apiemail = sp.getString(Const.EmailKEY, "");
        apitoken = sp.getString(Const.TokenKey, "");

        //memo: Fixed features
        mUserNameText = (TextView) findViewById(R.id.userNameText);
        mUserNameText.setText(apiusername);
        mEmailText = (TextView) findViewById(R.id.EmailText);
        mEmailText.setText(apiemail);
        mDestCountText = (TextView) findViewById(R.id.DestsText);

        //memo:
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("処理中...");

        mProfileButton = (Button) findViewById(R.id.ProfileButton);
        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


                Log.d("プロフィールボタン","プロフィールボタン");


            }
        });

        mLogoutButton = (Button) findViewById(R.id.LogoutButton);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                Log.d("ログアウトボタン","ログアウト");

                new logout().execute();


            }
        });


        mProgress.show();
        //memo: 目的地一覧を取得
        new getDestinations().execute();

        //memo: 現在保存されているRealmの中身を取得＆並べ替え
        mRealm = Realm.getDefaultInstance();
        mDestRealmResults = mRealm.where(Dest.class).findAllSorted("id",Sort.ASCENDING);
     //   mDestRealmResults.sort("id",Sort.ASCENDING);


        //memo: リスナーの設定
        mRealm.addChangeListener(mRealmListener);
        Log.d("1.リザルト",String.valueOf(mDestRealmResults.size()));


        // ListViewの設定（器のみ）
        mDestAdapter = new DestAdapter(this,this);
        mListView = (ListView) findViewById(R.id.listView);

        //memo: 機能していない？
        mDestAdapter.notifyDataSetChanged();




        //memo: ListViewをタップしたときの処理(目的地の編集画面に行く時のリスナー）
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ①入力・編集する画面に遷移させる ②トチェックを表示して、この目的地を選択し、選択されたものはPreferenceに保存される。

                dest = (Dest) parent.getAdapter().getItem(position);


                Log.d("PositionID",String.valueOf(dest.getPositionId()));
                Log.d("id",String.valueOf(dest.getId()));
                Log.d("name",String.valueOf(dest.getDestName()));
                Log.d("Url",String.valueOf(dest.getDestUrl()));


                //memo: destのIDを送る
                Intent intent = new Intent(SettingActivity.this, DestActivity.class);
                intent.putExtra(EXTRA_DEST, dest.getId());
                //startActivity(intent);
                startActivityForResult(intent,REQUEST_DEST_CODE);

            }
        });

        //memo: ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // タスクを削除する
                Log.d("タスク","削除");
             //   new delete().execute();

                dest = (Dest) parent.getAdapter().getItem(position);
                // ダイアログを表示する


                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);

                builder.setTitle("削除");
                builder.setMessage(dest.getDestName() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Rails側削除
                        new deletedest().execute(String.valueOf(dest.getDestUrl()));

                    }
                });
                // アラートダイアログのキャンセルボタンを設定します。nullは何もしない。
                builder.setNegativeButton("CANCEL", null);

                //AlertDialog dialog = builder.create();

                // アラートダイアログを表示します
                builder.show();


                return true;
            }
        });

        reloadListView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
     //   super.onActivityResult(requestCode, resultCode, data);
        Log.d("戻ってきた?", "戻ってきた?？");
        switch (requestCode) {
            //SecondActivityから戻ってきた場合
            case (REQUEST_DEST_CODE):
                if (resultCode == RESULT_OK) {
                    //OKボタンを押して戻ってきたときの処理
                    //memo: 目的地一覧を取得
                    String returnValue = data.getStringExtra("Result");
                    Log.d("戻ってきた", returnValue);
                    new getDestinations().execute();
                   // Log.v("Edit Text", data.getExtra("INPUT_STRING"));
                } else if (resultCode == RESULT_CANCELED) {
                    //キャンセルボタンを押して戻ってきたときの処理


                } else {
                    //その他
                }
                break;
            default:
                break;
        }
    }


    private void reloadListView() {

        //memo: 現在保存されているRealmの中身を取得＆並べ替え
        mRealm = Realm.getDefaultInstance();
        mDestRealmResults = mRealm.where(Dest.class).findAllSorted("id",Sort.ASCENDING);
        Log.d("ReloadView.リザルト",String.valueOf(mDestRealmResults.size()));
       // mDestRealmResults.sort("id",Sort.DESCENDING);
       // Log.d("reloadlistview", String.valueOf(mDestRealmResults));


        //memo: 仮説：現状だとmDestRealmResultsが変わったあとにListViewに表示している。

        mDestArrayList = new ArrayList<>();

        for (int i = 0; i < mDestRealmResults.size(); i++){
            if(!mDestRealmResults.get(i).isValid()) continue;

            Dest dest = new Dest();

            dest.setId(mDestRealmResults.get(i).getId());
            dest.setPositionId(mDestRealmResults.get(i).getPositionId());
            dest.setDestName(mDestRealmResults.get(i).getDestName());
            dest.setDestEmail(mDestRealmResults.get(i).getDestEmail());
            dest.setDestAddress(mDestRealmResults.get(i).getDestAddress());
            dest.setDestLatitude(mDestRealmResults.get(i).getDestLatitude());
            dest.setDestLongitude(mDestRealmResults.get(i).getDestLongitude());
            dest.setDestUrl(mDestRealmResults.get(i).getDestUrl());

            mDestArrayList.add(dest);
        }

        mDestAdapter.setDestArrayList(mDestArrayList);
        mListView.setAdapter(mDestAdapter);
        mDestAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("変更","SettingActivityに書かれているLogです。");

        //memo: 目的地一覧を取得
       // new getDestinations().execute();
       // reloadListView();
    }


    private class deletedest extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params){

            Deletedest(params);
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
                //deleteUserdata();
                //Realm側削除
                mRealm = Realm.getDefaultInstance();
                RealmResults<Dest> results = mRealm.where(Dest.class).equalTo("id", dest.getId()).findAll();

                mRealm.beginTransaction();
                results.deleteAllFromRealm();

                mRealm.commitTransaction();


                Snackbar.make(v, "削除しました", Snackbar.LENGTH_LONG).show();

                //memo: 目的地一覧を取得
                new getDestinations().execute();
                //finish();
            } else {
                Snackbar.make(v, "削除に失敗しました。通信環境をご確認下さい。", Snackbar.LENGTH_LONG).show();
                mProgress.dismiss();


            }

        }
    }


    private class logout extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            Deletelogout();

            if(result2.equals("OK")){
                result = "OK";

            } else {
                result = "NG";
            }
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

    //memo: ログアウト時にPreferenceを削除する。（＊sp.edit().clear().commit() だと何故かListenerが反応しない。
    private void deleteUserdata() {
        // Preferenceを削除する

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().remove("username").remove("email").remove("access_token").remove("id").remove("position_id").remove("destname").remove("destaddress").remove("destemail").remove("latitude").remove("longitude").commit();
        //sp.edit().clear().commit();
        Log.d("Delete","done");
        apiemail = null;
        apiusername = null;
        apitoken = null;

        mRealm.beginTransaction();
        mRealm.deleteAll();
        mRealm.commitTransaction();

    }


    //memo: 目的地一覧をGET
    private class getDestinations extends AsyncTask<String, Void, String> {
        @Override
        protected  String doInBackground(String... params) {


            GetdestList();
            if(result2.equals("OK")){
                result = "OK";

            } else {
                result = "NG";
            }

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

    public void addDestination(Integer selected_position) {
        Log.d("selected_position",String.valueOf(selected_position));


      //  final Dest dest = (Dest) parent.getAdapter().getItem(selected_position);

        Realm realm = Realm.getDefaultInstance();

        //memo: destIdで検索して当該のデータを取得 positionは０はじまり、position_idは１はじまりだから＋１する。
        destRealm = realm.where(Dest.class).equalTo("position_id", selected_position +1 ).findFirst();
        realm.close();


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        //sp.registerOnSharedPreferenceChangeListener(this);
        sp.edit().remove("id").remove("position_id").remove("destname").remove("destaddress").remove("destemail").remove("latitude").remove("longitude").commit();


        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(Const.RailsKEY,destRealm.getId());
        editor.putInt(Const.PositionKey,destRealm.getPositionId());
        editor.putString(Const.DestnameKEY , destRealm.getDestName());
        editor.putString(Const.DestaddressKEY, destRealm.getDestAddress());
        editor.putString(Const.DestemailKEY, destRealm.getDestEmail());
        editor.putString(Const.DestLatitudeKEY, destRealm.getDestLatitude());
        editor.putString(Const.DestLongitudeKEY, destRealm.getDestLongitude());
        //  editor.putString(Const.PassKey, res_password);

        editor.commit();

        Toast.makeText(this, "目的地を設定しました", Toast.LENGTH_LONG).show();

        Intent resultintent = new Intent();
        resultintent.putExtra("Result","OK");
        setResult(RESULT_OK,resultintent);
        finish();

        //memo: destのIDを送る



    }

    //memo: 右上のメニューボタン
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.option_menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.item2) {
            Intent intent = new Intent(getApplicationContext(), DestActivity.class);
            startActivityForResult(intent,REQUEST_DEST_CODE);

            return true;
        }
        return super.onOptionsItemSelected(item);


    }
}
