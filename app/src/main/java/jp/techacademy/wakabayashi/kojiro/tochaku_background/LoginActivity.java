package jp.techacademy.wakabayashi.kojiro.tochaku_background;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;


/*

ネットワークに繋がらなかった時
エラー時の対応（エラーメッセージのAPI)

 */


public class LoginActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    //memo: パーツの定義
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mUserNameEditText;

    //memo: パーツ受け取り用
    private User user;
    private String username;
    private String email;
    private String password;

    //memo: Responseで受け取る変数
    private String res_token;
    private String res_id;
    private String res_username;
    private String res_email;

    //memo: プログレス
    private ProgressDialog mProgress;


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

                username = mUserNameEditText.getText().toString();
                email = mEmailEditText.getText().toString();
                password = mPasswordEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6 ) {
                    mProgress.show();

                  //  new RailsApi(LoginActivity.this,mProgress).createAccountAsync(username,email,password);



                    new ApiTask(LoginActivity.this,mProgress).createAccountAsync(username,email,password);


                    /*
                    if(result.equals("OK")){

                        Toast.makeText(LoginActivity.this,"アカウント作成しました。",Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                        finish();

                    } else {

                        Toast.makeText(LoginActivity.this,"アカウントの作成に失敗しました。",Toast.LENGTH_SHORT).show();
                        mProgress.dismiss();
                    }
                    */

                    finish();

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


                email = mEmailEditText.getText().toString();
                password = mPasswordEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6) {

                    // プログレスダイアログを表示する
                   mProgress.show();

                   new RailsApi(LoginActivity.this,mProgress).loginAsync(email,password).onSuccessTask(new Continuation<String, Task<String>>() {
                       @Override
                       public Task<String> then(Task<String> task) throws Exception {
                           final TaskCompletionSource<String> taskresult = new TaskCompletionSource<>();
                           taskresult.setResult("OK");


                           return new RailsApi(LoginActivity.this,mProgress).saveUserdata(task.getResult());
                       }
                   }).onSuccess(new Continuation<String, Task<String>>() {
                       @Override
                       public Task<String> then(Task<String> task) throws Exception {

                           mProgress.dismiss();

                           Toast.makeText(LoginActivity.this,"ログインしました",Toast.LENGTH_SHORT).show();

                           finish();

                           return null;
                       }
                   });
                  //  new ApiTask(LoginActivity.this,mProgress).loginAsync(email,password);

                    /*
                    mProgress.dismiss();

                    Toast.makeText(LoginActivity.this,"ログインしました",Toast.LENGTH_SHORT).show();

                    finish();
                    */



                } else {

                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();

                    // プログレスダイアログを非表示にする

                }
            }
        });
    }

}
