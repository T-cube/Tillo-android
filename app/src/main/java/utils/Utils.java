package utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.math.BigDecimal;

/**
 * Created by Huangjinfu on 2016/7/29.
 */
public class Utils {

    /**
     * 通过控件的Id获取对应的控件
     */
    public static <T extends View> T findViewsById(Activity activity, int viewId) {
        View view = activity.findViewById(viewId);
        return (T) view;
    }

    /**
     * 通过控件的Id获取对应的控件
     */
    public static <T extends View> T findViewsById(View parent, int viewId) {
        View view = parent.findViewById(viewId);
        return (T) view;
    }

    /**
     * 状态栏
     *
     * @param activity
     */
    public static void setSteepStatusBar(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        rootView.setFitsSystemWindows(true);
        rootView.setClipToPadding(true);
    }

    public static double getPercentRate(double params) {
        BigDecimal b = new BigDecimal(params * 100);
        double newIncomeRate = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return newIncomeRate;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Is the live streaming still available
     * @return is the live streaming is available
     */
    public static boolean isLiveStreamingAvailable() {
        // Todo: Please ask your app server, is the live streaming still available
        return true;
    }
}
