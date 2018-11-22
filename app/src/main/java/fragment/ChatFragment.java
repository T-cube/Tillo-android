package fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.flyco.tablayout.SlidingTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.hankkin.library.RefreshSwipeMenuListView;
import com.hankkin.library.SwipeMenu;
import com.hankkin.library.SwipeMenuCreator;
import com.hankkin.library.SwipeMenuItem;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.yumeng.tillo.AddFriendActivity;
import com.yumeng.tillo.ChatActivity;
import com.yumeng.tillo.ChooseLinkmanActivity;
import com.yumeng.tillo.MainActivity;
import com.yumeng.tillo.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.FindMultiCallback;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import adapter.ChatListAdapter;
import application.MyApplication;
import base.BaseFragment;
import bean.ChatBean;
import bean.ChatMessage;
import bean.ChatUserBean;
import bean.Conversation;
import bean.FriendInfo;
import bean.Group;
import bean.MessageEvent;
import bean.UserInfo;
import constants.Constants;
import pomelo.DataCallBack;
import pomelo.PomeloClient;
import service.PomeloService;
import utils.AppSharePre;
import utils.CommonPopupWindow;
import utils.SharedDataTool;
import utils.SoundPlayUtils;
import utils.ToastUtils;
import utils.Utils;

/**
 * tab:对话
 */

public class ChatFragment extends BaseFragment implements OnTabSelectListener, ViewPager.OnPageChangeListener {
    //    private List<Conversation> dataList = new ArrayList<>();
//        private ChatListAdapter chatListAdapter;
    //消息列表
//    private RefreshSwipeMenuListView rsmLv;
    //搜索框
//    private int position;
    private UserInfo userInfo;
    //声明控件
//    SlidingTabLayout mTabLayout;
    ViewPager mViewPager;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private String titles[] = new String[]{
            "消息"
    };
    String groupId = "";
    private List<FriendInfo> mList = new ArrayList<>();
    private ImageView addIv;
//    private LinearLayout stausBar;
//    private ImageView errorIv;
//    private ProgressBar loadPb;
//    private TextView stausTv;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
//        EventBus.getDefault().register(this);
        userInfo = AppSharePre.getPersonalInfo();
//        mTabLayout = myFindViewsById(R.id.chat_tl_tab);
        mViewPager = myFindViewsById(R.id.chat_vp_pager);
        initFragment();
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(4);
//        mTabLayout.setViewPager(mViewPager);
        EventBus.getDefault().register(this);
        addIv = Utils.findViewsById(view, R.id.search_iv_add_friend);
        addIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReminder(addIv);
            }
        });
//        stausBar = myFindViewsById(R.id.chat_tl_ll_bar);
//        errorIv = myFindViewsById(R.id.chat_tl_iv_error);
//        loadPb = myFindViewsById(R.id.chat_tl_pb_progress);
//        stausTv = myFindViewsById(R.id.chat_tl_tv);
//        stausBar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //设置
//                stausBar.setVisibility(View.VISIBLE);
//                errorIv.setVisibility(View.GONE);
//                loadPb.setVisibility(View.VISIBLE);
//                stausTv.setText("连接中...");
//                EventBus.getDefault().post(new MessageEvent(Constants.TARGET_SERVICE, Constants.MESSAGE_INIT_CLIENT, null));
//            }
//        });
        getFriendList();
//        rsmLv = Utils.findViewsById(view, R.id.fragment_chat_rsmlv_listview);
//        bindEvent();
    }


    //初始化fragment
    public void initFragment() {
        fragments.add(new MessageFragment());
//        fragments.add(new GroupFragment());
//        fragments.add(new CallFragment());
    }


//    public void bindEvent() {
//        chatListAdapter = new ChatListAdapter(MyApplication.getContext(), dataList);
//        rsmLv.setAdapter(chatListAdapter);
//        rsmLv.setListViewMode(RefreshSwipeMenuListView.HEADER);
//        rsmLv.setOnRefreshListener(this);
//        //左滑菜单
//        SwipeMenuCreator creator = new SwipeMenuCreator() {
//            @Override
//            public void create(SwipeMenu menu) {
//                // 创建滑动选项
//                SwipeMenuItem moreItem = new SwipeMenuItem(
//                        MyApplication.getContext());
//                // 设置选项背景
//                moreItem.setBackground(getResources().getDrawable(R.drawable.menu_corner_shape));
//                // 设置选项宽度
//                moreItem.setWidth(dp2px(80, MyApplication.getContext()));
//                // 设置选项标题
//                moreItem.setTitle("更多");
//                // 设置选项标题
//                moreItem.setTitleSize(15);
//                // 设置选项标题颜色
//                moreItem.setTitleColor(getResources().getColor(R.color.menu_text1));
//                // 添加选项
//                menu.addMenuItem(moreItem);
//                // 创建删除选项
//                SwipeMenuItem pigeonholeItem = new SwipeMenuItem(MyApplication.getContext());
//                pigeonholeItem.setBackground(getResources().getDrawable(R.drawable.menu_corner_blue_shape));
//                pigeonholeItem.setWidth(dp2px(80, MyApplication.getContext()));
//                pigeonholeItem.setTitle("归档");
//                pigeonholeItem.setTitleSize(15);
//                pigeonholeItem.setTitleColor(getResources().getColor(R.color.menu_text1));
//                menu.addMenuItem(pigeonholeItem);
//            }
//        };
//        rsmLv.setMenuCreator(creator);
//        rsmLv.setOnMenuItemClickListener(new RefreshSwipeMenuListView.OnMenuItemClickListener() {
//            @Override
//            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
//                switch (index) {
//                    case 0: //第一个选项
//                        Toast.makeText(getActivity(), "更多", Toast.LENGTH_SHORT).show();
//                        break;
//                    case 1: //第二个选项
////                        del(position, rsmLv.getChildAt(position + 1 - rsmLv.getFirstVisiblePosition()));
//                        break;
//
//                }
//            }
//        });
//        rsmLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                FriendInfo friendInfo = dataList.get(i - 1).getFriendInfo().get(0);
//                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
//                chatIntent.putExtra("target", friendInfo.getFriend_id());
//                chatIntent.putExtra("userName", friendInfo.getShowname());
//                chatIntent.putExtra("roomId", friendInfo.getRoomid());
//                chatIntent.putExtra("avatar", friendInfo.getAvatar());
//                startActivity(chatIntent);
//
//            }
//        });

    public void showReminder(View view) {
        if (popupWindow != null && popupWindow.isShowing()) return;
        popupWindow = new CommonPopupWindow.Builder(getActivity())
                .setView(R.layout.popmenu_chat)
                .setWidthAndHeight(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .create();
        View inflate = popupWindow.getContentView();
        View toChatGroup = Utils.findViewsById(inflate, R.id.menu_ll_to_group);
        View toAddFriend = Utils.findViewsById(inflate, R.id.menu_ll_to_add_friend);
        View toScan = Utils.findViewsById(inflate, R.id.menu_ll_to_scan);
        toChatGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发起群聊
                Intent groupIntent = new Intent(getActivity(), ChooseLinkmanActivity.class);
                groupIntent.putExtra("friendList", (Serializable) mList);
                startActivity(groupIntent);
            }
        });

        toAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //添加好友
                Intent addFriendIntent = new Intent(getActivity(), AddFriendActivity.class);
                startActivity(addFriendIntent);
                popupWindow.dismiss();
            }
        });
        toScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //扫一扫
                sendTextMessage();
            }
        });
        popupWindow.showAsDropDown(view, 0, 0);
    }

    public int dp2px(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }


//    /**
//     * 删除item动画
//     *
//     * @param index
//     * @param v
//     */
//    private void del(final int index, View v) {
//        final Animation animation = (Animation) AnimationUtils.loadAnimation(v.getContext(), R.anim.list_anim);
//        animation.setAnimationListener(new Animation.AnimationListener() {
//            public void onAnimationStart(Animation animation) {
//            }
//
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            public void onAnimationEnd(Animation animation) {
//                dataList.remove(index);
//                position = index;
//                chatListAdapter.notifyDataSetChanged();
//                animation.cancel();
//            }
//        });
//        v.startAnimation(animation);
//    }


    private CommonPopupWindow popupWindow;


    //适配器
    class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {
        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }


    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    //获取好友列表
    public void getFriendList() {
        OkGo.<String>get(Constants.FriendUrl + Constants.getFriendList)
                .headers("Authorization", userInfo.getToken())
                .params("userId", userInfo.getId())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(response.body());
                        String message = jsonObject.getString("message");
                        int status = jsonObject.getIntValue("status");
                        if (status == 200) {
                            //获取好友列表
                            mList = JSON.parseArray(jsonObject.getString("data"), FriendInfo.class);
                            //对集合排序
                            Collections.sort(mList, new Comparator<FriendInfo>() {
                                @Override
                                public int compare(FriendInfo lhs, FriendInfo rhs) {
                                    //根据拼音进行排序
                                    return lhs.getPinyin().compareTo(rhs.getPinyin().toUpperCase());
                                }
                            });

                        } else {
                            ToastUtils.getInstance().shortToast(message);
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * EventBus消息接收
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventReceiver(MessageEvent messageEvent) {
        String target = messageEvent.getTarget();
        String behavior = messageEvent.getBehavior();
        if (target.equals(Constants.TARGET_MAIN_ACTIVITY)) {
            if (behavior.equals(Constants.MESSAGE_EVENT_FRIEND_AGREE))
                getFriendList();
        }
//        else if (behavior.equals(Constants.MESSAGE_CHAT_DOT)) {
//            //更新聊天消息红点
//            if (mTabLayout.getCurrentTab() != 0)
//                mTabLayout.showDot(0);
//        } else if (behavior.equals(Constants.MESSAGE_GROUP_CHAT_DOT)) {
//            //更新群聊消息红点
//            if (mTabLayout.getCurrentTab() != 1)
//                mTabLayout.showDot(1);
//        } else if (behavior.equals(Constants.MESSAGE_CONNECT_SUCCESS)) {
////            stausBar.setVisibility(View.GONE);
//        } else if (behavior.equals(Constants.MESSAGE_DISCONNECT_ERROR)) {
////            errorIv.setVisibility(View.VISIBLE);
////            loadPb.setVisibility(View.GONE);
////            stausTv.setText("连接失败，请重新连接");
////            stausBar.setVisibility(View.VISIBLE);
//        }
    }

    //tab点击事件

    @Override
    public void onTabSelect(int position) {
        //清除被选中的消息红点
//        mTabLayout.hideMsg(position);
    }

    @Override
    public void onTabReselect(int position) {


    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


    }


    @Override
    public void onPageSelected(int position) {
        //清除被选中的消息红点
//        mTabLayout.hideMsg(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }



    //发送消息
    public void sendTextMessage() {
        String uuid = UUID.randomUUID().toString();
        org.json.JSONObject params = new org.json.JSONObject();
        Log.e("TAG_userId","userInfo.getId():"+userInfo.getId());
        try {
            params.put("sender", "15");
            params.put("roomId", "504664368961552384");
            params.put("content", "123");
            params.put("type", "text");
            params.put("backId", uuid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("TAG_token",userInfo.getToken());
        OkGo.<String>post(Constants.GroupUrl + "/push")
                .headers("Authorization", userInfo.getToken())
                .upJson(params)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.e("TAG_groupPush", response.body());
                    }

                    @Override
                    public void onError(Response<String> response) {
                        Log.e("TAG_push_error", response.getException().getMessage());

                    }
                });
    }
}
