package jp.techacademy.wakabayashi.kojiro.tochaku_background;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import io.realm.Realm;


/**
 * Created by wkojiro on 2017/03/15.
 */

public class DestAdapter extends BaseAdapter{

    private SettingActivity activity;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Dest> mDestArrayList;
    Dest RailsRealm;
    Integer checked_id = -1;
    Integer rails_id = -1;
    public Integer selected_position = -1;


    public DestAdapter(Context context,SettingActivity activity) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.activity = activity;
    }

    public void setDestArrayList(ArrayList<Dest> destArrayList){
        mDestArrayList = destArrayList;
    }

    @Override
    public int getCount() {
        return mDestArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDestArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if(convertView == null){
            //convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null);

            convertView = mLayoutInflater.inflate(R.layout.list_dests, parent ,false);
        }

        final TextView textView1 = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView textView2 = (TextView) convertView.findViewById(R.id.addressTextView);
        TextView textView3 = (TextView) convertView.findViewById(R.id.emailTextView);
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);



        textView1.setText(mDestArrayList.get(position).getDestName() + (mDestArrayList.get(position).getPositionId()));
        textView2.setText(mDestArrayList.get(position).getDestAddress());
        textView3.setText(mDestArrayList.get(position).getDestEmail());

        //checkBox.setChecked(true);

        Log.d("checked", String.valueOf(mDestArrayList.get(position).getPositionId()));
        Log.d("ID", String.valueOf(mDestArrayList.get(position).getId()));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.activity);
       // checked_id = sp.getInt(Const.PositionKey,-1) - 1;
        if(sp.getInt(Const.PositionKey,-1) != -1){
            Log.d("aa","dd");


        rails_id = sp.getInt(Const.RailsKEY,-1);


            Log.d("checked_id", String.valueOf(checked_id));
            Log.d("保存されたRailsID", String.valueOf(rails_id));


        //memo: 最新のRealmとRailsIDの同期をとる
        Realm realm = Realm.getDefaultInstance();

        //memo: destIdで検索して当該のデータを取得 positionは０はじまり、position_idは１はじまりだから＋１する。
        RailsRealm = realm.where(Dest.class).equalTo("id", rails_id ).findFirst();
        realm.close();

        checked_id = RailsRealm.getPositionId() -1;

      //  if (selected_position == position || checked_id == position) {
            if(checked_id == position){
                checkBox.setChecked(true);

            } else {
                checkBox.setChecked(false);

            }
        }

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                if(cb.isChecked() == true)
                {
                    selected_position = position ;
                    //Context context = parent.getContext();
                  //  Snackbar.make(v, "目的地を設定しました", Snackbar.LENGTH_LONG).show();


                   // String result3 = "OK";
                    //memo: 親activityのメソッドを呼んでいる
                    activity.addDestination(selected_position);

                }
                else
                {
                    selected_position = -1;
                }
                notifyDataSetChanged();
            }
        });

      //  checkBox.setChecked(position == selected_position);
     //   Log.d("3selected_position最終", String.valueOf(selected_position));
     //   Log.d("4position最終", String.valueOf(position));



        return convertView;
    }




}
