package jp.techacademy.wakabayashi.kojiro.tochaku_background;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;
import android.view.View;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by wkojiro on 2017/04/04.
 */

class Utils {

    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";


   //memo: PreferenceにPermissionしているかどうかを確認している。
    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }
    //memo: PreferenceにPermissionしたことを記述している。
    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }



    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }

//memo: 外部からアクセスできるようにPublicにしていたらダメだった。staticにしたらできた
    static boolean isEmptyUser(Context context) {

        return (PreferenceManager.getDefaultSharedPreferences(context).getString(Const.UnameKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(Const.EmailKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(Const.TokenKey, "").equals(""));


          /*
        if(PreferenceManager.getDefaultSharedPreferences(context).getString(Const.UnameKEY, "").equals("") && PreferenceManager.getDefaultSharedPreferences(context).getString(Const.EmailKEY, "").equals("")&& PreferenceManager.getDefaultSharedPreferences(context).getString(Const.TokenKey, "").equals("") ){
            return true;
        } else {
            return false;
        }*/
    }

    static boolean isEmptyDest(Context context){
        return(PreferenceManager.getDefaultSharedPreferences(context).getString(Const.DestnameKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(Const.DestaddressKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(Const.DestemailKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(Const.DestLatitudeKEY, "").equals("")
                && PreferenceManager.getDefaultSharedPreferences(context).getString(Const.DestLongitudeKEY, "").equals(""));
    }

}