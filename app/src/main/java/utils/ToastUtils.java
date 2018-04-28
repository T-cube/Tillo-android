package utils;

import android.content.Context;
import android.widget.Toast;

import application.MyApplication;


/**
 * Created by Huangjinfu on 2016/8/6.
 */
public class ToastUtils {
    private static ToastUtils mToastUtils;
    private static Context mContext;

    private ToastUtils() {
    }

    public static ToastUtils getInstance() {
        if (mToastUtils == null)
            mToastUtils = new ToastUtils();
        mContext = MyApplication.getContext();
        return mToastUtils;
    }

    //shortToast
    public void shortToast(String str) {
        Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
    }

    //longToast
    public void longToast(String str) {
        Toast.makeText(mContext, str, Toast.LENGTH_LONG).show();
    }
}
