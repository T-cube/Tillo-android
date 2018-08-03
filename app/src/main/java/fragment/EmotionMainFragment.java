package fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.yumeng.tillo.R;

import java.util.ArrayList;
import java.util.List;

import adapter.NoHorizontalScrollerVPAdapter;
import base.BaseFragment;
import views.EmotionKeyboard;
import views.NoHorizontalScrollerViewPager;

/**
 * emoji表情主界面
 */

public class EmotionMainFragment extends BaseFragment {
    //表情面板
    private EmotionKeyboard mEmotionKeyboard;
    //绑定的视图
    private View contentView;
    //绑定的编辑框
    private EditText inputEt;
    //表情按钮
    private ImageView emojiIv;
    List<Fragment> fragments = new ArrayList<>();
    //不可横向滚动的ViewPager
    private NoHorizontalScrollerViewPager viewPager;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_emotion_main;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        mEmotionKeyboard = EmotionKeyboard.with(getActivity())
                .setEmotionView(view.findViewById(R.id.ll_emotion_layout))//绑定表情面板
                .bindToContent(contentView)//绑定内容view
                .bindToEditText(!true ? ((EditText) contentView) : ((EditText) inputEt))//判断绑定那种EditView
                .bindToEmotionButton(emojiIv)//绑定表情按钮
                .build();
        viewPager = myFindViewsById(R.id.vp_emotionview_layout);
        replaceFragment();
    }

    public void setContentView(View contentView) {
        this.contentView = contentView;
    }

    public void setInputEt(EditText inputEt) {
        this.inputEt = inputEt;
    }

    public void setEmojiIv(ImageView emojiIv) {
        this.emojiIv = emojiIv;
    }

    private void replaceFragment() {
        //创建fragment的工厂类
        FragmentFactory factory = FragmentFactory.getSingleFactoryInstance();
        //创建修改实例
        EmotiomComplateFragment f1 = (EmotiomComplateFragment) factory.getFragment(inputEt);
        fragments.add(f1);
//        Bundle b = null;
//        for (int i = 0; i < 7; i++) {
//            b = new Bundle();
//            b.putString("Interge", "Fragment-" + i);
//            Fragment1 fg = Fragment1.newInstance(Fragment1.class, b);
//            fragments.add(fg);
//        }
        NoHorizontalScrollerVPAdapter adapter = new NoHorizontalScrollerVPAdapter(getActivity().getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
    }


    /**
     * 是否拦截返回键操作，如果此时表情布局未隐藏，先隐藏表情布局
     *
     * @return true则隐藏表情布局，拦截返回键操作
     * false 则不拦截返回键操作
     */
    public boolean isInterceptBackPress() {
        return mEmotionKeyboard.interceptBackPress();
    }

}
