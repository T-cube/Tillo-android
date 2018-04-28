package utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * @author 艹羊
 * @project Checaiduo_Android
 * @date 2017/6/1 下午5:27
 * @description
 */

public abstract class MyActivityLifeCycleCallbacks implements Application.ActivityLifecycleCallbacks {


    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
