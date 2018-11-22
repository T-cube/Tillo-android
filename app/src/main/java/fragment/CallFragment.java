package fragment;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.yumeng.tillo.R;

import base.BaseFragment;

/**
 * 通话界面
 */

public class CallFragment extends BaseFragment {
    private RecyclerView recyclerView;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_call;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {

    }
}
