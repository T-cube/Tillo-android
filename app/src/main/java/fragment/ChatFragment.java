package fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hankkin.library.RefreshSwipeMenuListView;
import com.hankkin.library.SwipeMenu;
import com.hankkin.library.SwipeMenuCreator;
import com.hankkin.library.SwipeMenuItem;
import com.yumeng.tillo.AddFriendActivity;
import com.yumeng.tillo.ChatActivity;
import com.yumeng.tillo.MainActivity;
import com.yumeng.tillo.R;

import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.FindMultiCallback;

import java.util.ArrayList;
import java.util.List;

import adapter.ChatListAdapter;
import application.MyApplication;
import base.BaseFragment;
import bean.ChatBean;
import bean.ChatMessage;
import bean.ChatUserBean;
import bean.Conversation;
import bean.FriendInfo;
import bean.UserInfo;
import pomelo.DataCallBack;
import pomelo.PomeloClient;
import utils.AppSharePre;
import utils.CommonPopupWindow;
import utils.ToastUtils;
import utils.Utils;

/**
 * tab:对话
 */

public class ChatFragment extends BaseFragment implements RefreshSwipeMenuListView.OnRefreshListener {
    private List<Conversation> dataList = new ArrayList<>();
    private ChatListAdapter chatListAdapter;
    //消息列表
    private RefreshSwipeMenuListView rsmLv;
    //通讯录、极速模式、更多
    private ImageView addressBookIv, fastModeIv, moreIv;
    //搜索框
    private int position;
    private UserInfo userInfo;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        userInfo = AppSharePre.getPersonalInfo();
        rsmLv = Utils.findViewsById(view, R.id.fragment_chat_rsmlv_listview);
        addressBookIv = Utils.findViewsById(view, R.id.fragment_chat_iv_address_book);
        fastModeIv = Utils.findViewsById(view, R.id.fragment_chat_iv_fast);
        moreIv = Utils.findViewsById(view, R.id.fragment_chat_iv_more);
        bindEvent();
    }


    public void bindEvent() {
        chatListAdapter = new ChatListAdapter(MyApplication.getContext(), dataList);
        rsmLv.setAdapter(chatListAdapter);
        rsmLv.setListViewMode(RefreshSwipeMenuListView.HEADER);
        rsmLv.setOnRefreshListener(this);
        //左滑菜单
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // 创建滑动选项
                SwipeMenuItem moreItem = new SwipeMenuItem(
                        MyApplication.getContext());
                // 设置选项背景
                moreItem.setBackground(getResources().getDrawable(R.drawable.menu_corner_shape));
                // 设置选项宽度
                moreItem.setWidth(dp2px(80, MyApplication.getContext()));
                // 设置选项标题
                moreItem.setTitle("更多");
                // 设置选项标题
                moreItem.setTitleSize(15);
                // 设置选项标题颜色
                moreItem.setTitleColor(getResources().getColor(R.color.menu_text1));
                // 添加选项
                menu.addMenuItem(moreItem);
                // 创建删除选项
                SwipeMenuItem pigeonholeItem = new SwipeMenuItem(MyApplication.getContext());
                pigeonholeItem.setBackground(getResources().getDrawable(R.drawable.menu_corner_blue_shape));
                pigeonholeItem.setWidth(dp2px(80, MyApplication.getContext()));
                pigeonholeItem.setTitle("归档");
                pigeonholeItem.setTitleSize(15);
                pigeonholeItem.setTitleColor(getResources().getColor(R.color.menu_text1));
                menu.addMenuItem(pigeonholeItem);
            }
        };
        rsmLv.setMenuCreator(creator);
        rsmLv.setOnMenuItemClickListener(new RefreshSwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0: //第一个选项
                        Toast.makeText(getActivity(), "更多", Toast.LENGTH_SHORT).show();
                        break;
                    case 1: //第二个选项
//                        del(position, rsmLv.getChildAt(position + 1 - rsmLv.getFirstVisiblePosition()));
                        break;

                }
            }
        });
        rsmLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FriendInfo friendInfo = dataList.get(i - 1).getFriendInfo().get(0);
                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                chatIntent.putExtra("target", friendInfo.getFriend_id());
                chatIntent.putExtra("userName", friendInfo.getShowname());
                chatIntent.putExtra("roomId", friendInfo.getRoomid());
                chatIntent.putExtra("avatar", friendInfo.getAvatar());
                startActivity(chatIntent);

            }
        });


        addressBookIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivity = (MainActivity) getmActivity();
                mainActivity.toggle();
            }
        });
        moreIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReminder(moreIv);
            }
        });
    }

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
            }
        });
        popupWindow.showAsDropDown(view, 0, 0);
    }

    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        rsmLv.postDelayed(new Runnable() {
            @Override
            public void run() {
                rsmLv.complete();
                ToastUtils.getInstance().shortToast("下拉刷新");
            }
        }, 2000);

    }


    /**
     * 加载更多
     */
    @Override
    public void onLoadMore() {
        rsmLv.postDelayed(new Runnable() {
            @Override
            public void run() {
                rsmLv.complete();
                ToastUtils.getInstance().shortToast("加载更多");
            }
        }, 2000);
    }

    public int dp2px(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }


    /**
     * 删除item动画
     *
     * @param index
     * @param v
     */
    private void del(final int index, View v) {
        final Animation animation = (Animation) AnimationUtils.loadAnimation(v.getContext(), R.anim.list_anim);
        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                dataList.remove(index);
                position = index;
                chatListAdapter.notifyDataSetChanged();
                animation.cancel();
            }
        });
        v.startAnimation(animation);
    }


    private CommonPopupWindow popupWindow;

    //获取聊天信息
    public void initChatInfo(PomeloClient client) {
        JSONObject msg = new JSONObject();
        client.request("account.accountHandler.initInfo", msg, new DataCallBack() {
            @Override
            public void responseData(JSONObject message) {
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(message.toString());
                List<ChatBean> messageList = JSON.parseArray(jsonObject.getString("data"), ChatBean.class);
                //获取对话列表
                if (messageList.size() > 0) {
                    //加载首页消息数据
                    dataList.addAll(getChatListData(messageList));
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            chatListAdapter.setData(dataList);
                        }
                    }, 300);
                } else {


                }
            }
        });

    }

    //获取对话列表数据
    public List<Conversation> getChatListData(List<ChatBean> list) {
        List<Conversation> tempList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Conversation conversation = new Conversation();
            conversation.setRoomid(list.get(i).getMessage().getRoomid());
            conversation.setTimestamp(list.get(i).getMessage().getTimestamp());
            conversation.setContent(list.get(i).getMessage().getContent());
            conversation.saveOrUpdate("roomid=?", list.get(i).getMessage().getRoomid());
            tempList.add(conversation);
        }
        return tempList;
    }

    //收到新消息
    public void receiveNewsMessage(ChatMessage messageBean) {
        List<Conversation> tempList = DataSupport.where("roomid=?", messageBean.getRoomid())
                .find(Conversation.class);
        Conversation conversation = new Conversation();
        Activity currentActivity = MyApplication.getCurrentActivity();
        int unreadTemp = 0;
        if (tempList == null) {
            unreadTemp = 1;
        } else if (tempList.size() == 1) {
            //已存在的会话
            unreadTemp = conversation.getUnread() + 1;
        }
        //不存在的会话
        if (currentActivity instanceof MainActivity) {
            conversation.setUnread(unreadTemp);
            messageBean.save();
        } else if (currentActivity instanceof ChatActivity) {
            //聊天界面不是 对应的消息会话界面
            if (!((ChatActivity) currentActivity).getRoomId().equals(messageBean.getRoomid()))
                conversation.setUnread(unreadTemp);
        }
        String content = "";
        switch (messageBean.getType()) {
            case "text":
                content = messageBean.getContent();
                break;
            case "image":
                content = "...[图片]";
                break;
            case "file":
                content = "...[文件]";
                break;
            case "audio":
                content = "...[语音]";
                break;
            case "video":
                content = "...[video]";
                break;
            case "link":
                content = messageBean.getUrl();
                break;
        }
        conversation.setContent(content);
        conversation.setRoomid(messageBean.getRoomid());
        conversation.setTimestamp(messageBean.getTimestamp());
        conversation.saveOrUpdate("roomid=?", messageBean.getRoomid());
        DataSupport.findAllAsync(Conversation.class).listen(new FindMultiCallback() {
            @Override
            public <T> void onFinish(List<T> t) {
                dataList= (List<Conversation>) t;
                chatListAdapter.setData(dataList);
            }
        });
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                chatListAdapter.setData(dataList);
//            }
//        }, 500);
    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
}
