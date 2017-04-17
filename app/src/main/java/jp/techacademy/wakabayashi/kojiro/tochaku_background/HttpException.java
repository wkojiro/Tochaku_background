package jp.techacademy.wakabayashi.kojiro.tochaku_background;

/**
 * Created by wkojiro on 2017/04/17.
 */

public class HttpException extends Exception{
    private final int httpCode;
    public HttpException(int httpCode) {
        this.httpCode = httpCode;
    }
}
