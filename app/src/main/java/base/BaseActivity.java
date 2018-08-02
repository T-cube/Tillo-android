package base;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.yumeng.tillo.R;

import java.util.List;

import bean.UserInfo;
import utils.AppSharePre;
import utils.StatusBarUtil;


public abstract class BaseActivity extends AppCompatActivity {
    protected FragmentManager fragmentManager;
    protected Context mContext;
    protected UserInfo loginuser;
    //用来控制应用前后台切换的逻辑
    private boolean isCurrentRunningForeground = true;
    protected boolean isOpenCamera = false;

    abstract protected void initView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        initStart();
        loginuser = AppSharePre.getPersonalInfo();

        // 设置所有Activity禁止横屏展示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        StatusBarUtil.setColor(this, getResources().getColor(R.color.white), 0);
//        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.transparent_black), 0);
//        StatusBarUtil.setTranslucent(this,50);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);//初始化UI界面
        initView();
    }


    public void backClick(View view) {
        finish();
    }

    private void initStart() {
        mContext = this;
        fragmentManager = getSupportFragmentManager();//fragment管理者
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//设置为竖屏
    }

    /**
     * 跳转Activity
     */
    public void startActivity(Class<?> cls) {
        Intent intent = new Intent(BaseActivity.this, cls);
        startActivity(intent);
    }


    /**
     * 跳转Activity
     * 包含数据传送
     */
    public void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(BaseActivity.this, cls);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * 跳转Activity
     * 包含数据传送
     * 包含过度动画的实现
     */
    public void startActivity(Class<?> cls, Bundle bundle, int enterAnim, int exitAnim) {
        startActivity(cls, bundle);
        overridePendingTransition(enterAnim, exitAnim);
    }

    /**
     * 跳转Activity
     * 包含过度动画的实现
     *
     * @param cls       跳转的类
     * @param enterAnim 进入的动画
     * @param exitAnim  退出的动画
     */
    public void startActivity(Class<?> cls, int enterAnim, int exitAnim) {
        startActivity(cls);
        overridePendingTransition(enterAnim, exitAnim);
    }

    /**
     * 通过控件的Id获取对于的控件
     */
    protected <T extends View> T findViewsById(int viewId) {
        View view = findViewById(viewId);
        return (T) view;
    }

    /**
     * 通过控件的Id获取对于的控件
     */
    protected <T extends View> T findViewsById(View parent, int viewId) {
        View view = parent.findViewById(viewId);
        return (T) view;
    }


    /**
     * 退出加动画
     *
     * @param exitAnim 退出动画
     */
    public void finish(int exitAnim) {
        finish();
//        overridePendingTransition(0, exitAnim);
    }

    /**
     * 获取Bundle
     */
    protected Bundle getBundle() {
        Bundle extras = getIntent().getExtras();
        return extras;
    }

    /**
     * 销毁Activity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * 网络断开
     */
    public void onDisconnect() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isCurrentRunningForeground) {
            Log.d("Base", ">>>>>>>>>>>>>>>>>>>切到前台 activity process");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        isCurrentRunningForeground = isRunningForeground();
        if (!isCurrentRunningForeground) {
            Log.d("Base", ">>>>>>>>>>>>>>>>>>>切到后台 activity process");
            if (!isOpenCamera) {
                //拍照
                Log.d("Base", ">>>>>>>>>>>>>>>>>>>不打开相机");
            } else {
                Log.d("Base", ">>>>>>>>>>>>>>>>>>>打开了相机");
            }

        }

    }

    public boolean isRunningForeground() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        Log.d("Base", "appSize:" + appProcessInfos.size());
        // 枚举进程
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
            Log.d("Base", appProcessInfo.importance + "");
            if (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcessInfo.processName.equals(this.getApplicationInfo().processName)) {
//                    Log.d("Base", "EntryActivity isRunningForeGround");
                    return true;
                }
            }
        }
//        Log.d("Base", "EntryActivity isRunningBackGround");
        return false;
    }

}
