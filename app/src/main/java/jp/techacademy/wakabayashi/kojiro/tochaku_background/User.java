package jp.techacademy.wakabayashi.kojiro.tochaku_background;

/**
 * Created by wkojiro on 2017/03/13.
 */
//import com.google.gson.annotations.SerializedName;

public class User {
    private String id;
    private String username;
    private String email;
    private String password;
    private String access_token;
    private String created_at;
    private String updated_at;

    public User(){

    }


    public String getUid(){
        return id;
    }
    public String getUserName() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return access_token;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public String getUpdatedAt() {
        return updated_at;
    }



}
