package application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;

import com.lzy.okgo.OkGo;
import com.yumeng.tillo.MainActivity;
import com.yumeng.tillo.R;

import org.greenrobot.eventbus.EventBus;
import org.litepal.LitePal;

import java.util.Collections;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import base.BaseApplication;
import bean.MessageEvent;
import constants.Constants;
import pomelo.PomeloClient;
import receiver.NetWorkStateReceiver;
import utils.MyActivityLifeCycleCallbacks;
import utils.SharedDataTool;

/**
 * Created by Administrator on 2018/3/21.
 */

public class MyApplication extends BaseApplication {
    private PomeloClient client;
    NetWorkStateReceiver netWorkStateReceiver;
    private boolean isRegister = false;
    private int count = 0;
    private boolean isChanged = false;
    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        MyApplication.context = context;
    }

    /**
     * 维护Activity 的list
     */
    private static Stack<Activity> mActivitys = new Stack<>();

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        registerActivityLifeCallback();
        initGo();
//        frontOrBack();
        LitePal.initialize(this);
//        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
//            registerNetWorkChangedReceiver();
//            isRegister = true;
//
//        } else {
//            netWorkChangeCallback();
//            isRegister = false;
//        }
    }

    /**
     * 描述：注册界面的生命周期，也可以
     * 时间：2017/5/11
     */
    private void registerActivityLifeCallback() {
        registerActivityLifecycleCallbacks(new MyActivityLifeCycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                mActivitys.add(activity);
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                mActivitys.remove(activity);
            }
        });
    }

    //初始化
    public void initGo() {
        OkGo.getInstance().init(this);
    }

    /**
     * 关闭所有Activity
     */
    public static void closeAllActivity() {
        for (Activity ac : mActivitys) {
            if (ac != null) {
                ac.finish();
                ac.overridePendingTransition(0, R.anim.slide_right_out);
            }
        }
    }

    /**
     * 获取acitivity数量
     *
     * @return
     */
    public static int getActivityCount() {
        return mActivitys.size();
    }

    public static void closeActivityExceptMain() {
        for (Activity ac : mActivitys) {
            if (ac != null && !(ac instanceof MainActivity)) {
                ac.finish();
            }
        }
    }

    /**
     * 获取当前activity
     *
     * @return
     */
    public static Activity getCurrentActivity() {
        Activity targetActivity = null;
        try {
            targetActivity = mActivitys.peek();
        } catch (EmptyStackException e) {
            targetActivity = null;
        }

        return targetActivity;
    }


    public PomeloClient getClient() {
        return client;
    }

    public void setClient(PomeloClient client) {
        this.client = client;
    }


    //注册网络监听广播
    public void registerNetWorkChangedReceiver() {
        netWorkStateReceiver = new NetWorkStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
//        unregisterReceiver(netWorkStateReceiver);
    }

    public void netWorkChangeCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.requestNetwork(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
            }
        });
    }


    /**
     * 判断在前台还是后台
     */
    private void frontOrBack() {
        //前后台切换判断
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (count == 0) {
                    Log.v("vergo", "**********切到前台**********");
                    SharedDataTool.setBoolean(activity, "isBackGround", false);
                    //重新连接PomeloClient
                    if (isChanged)
                        EventBus.getDefault().post(new MessageEvent(Constants.TARGET_SERVICE, Constants.MESSAGE_INIT_CLIENT, null));
                    isChanged = false;
                }
                count++;
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
                count--;
                if (count == 0) {
                    Log.v("vergo", "**********切到后台**********");
                    SharedDataTool.setBoolean(activity, "isBackGround", true);
                    isChanged = true;
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //信鸽 开启厂商通道初始化代码

    }
}
