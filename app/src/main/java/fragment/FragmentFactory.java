package fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.EditText;

/**
 * Created by zejian
 * Time  16/1/7 上午11:40
 * Email shinezejian@163.com
 * Description:产生fragment的工厂类
 */
public class FragmentFactory {

    public static final String EMOTION_MAP_TYPE = "EMOTION_MAP_TYPE";
    private static FragmentFactory factory;

    private FragmentFactory() {

    }

    /**
     * 双重检查锁定，获取工厂单例对象
     *
     * @return
     */
    public static FragmentFactory getSingleFactoryInstance() {
        if (factory == null) {
            synchronized (FragmentFactory.class) {
                if (factory == null) {
                    factory = new FragmentFactory();
                }
            }
        }
        return factory;
    }

    /**
     * 获取fragment的方法
     */
    public Fragment getFragment(EditText editText) {
        EmotiomComplateFragment fragment = EmotiomComplateFragment.getInstance(editText);

        return fragment;
    }

}
