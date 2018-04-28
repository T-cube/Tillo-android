package base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * monker on 2017/5/12.
 */

public abstract class BaseFragment extends Fragment {

    public BaseActivity getmActivity() {
        return mActivity;
    }

    public void setmActivity(BaseActivity mActivity) {
        this.mActivity = mActivity;
    }

    protected BaseActivity mActivity;
    protected ViewGroup WMContent;


    /**
     * 获得全局的，防止使用getActivity()为空
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mActivity = (BaseActivity) context;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        this.mActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        WMContent = (ViewGroup) LayoutInflater.from(mActivity).inflate(getLayoutId(), container, false);
        initView(WMContent, savedInstanceState);
        return WMContent;
    }


    /**
     * 获取控件的方法
     */
    public <T extends View> T myFindViewsById(int viewId) {
        View view = WMContent.findViewById(viewId);
        return (T) view;
    }



    /**
     * 该抽象方法就是 onCreateView中需要的layoutID
     *
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 该抽象方法就是 初始化view
     *
     * @param view
     * @param savedInstanceState
     */
    protected abstract void initView(View view, Bundle savedInstanceState);


    @Override
    public void onDestroy() {
        super.onDestroy();

    }




}