package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.yumeng.tillo.R;

import java.util.List;

import bean.Conversation;
import bean.FriendInfo;
import de.hdodenhof.circleimageview.CircleImageView;
import q.rorbin.badgeview.Badge;
import utils.DataUtils;
import utils.Utils;

/**
 * 对话-列表
 */

public class ChatListAdapter extends BaseAdapter {
    private Context context;
    private List<Conversation> mList;
    private LayoutInflater inflater;

    public ChatListAdapter(Context context, List<Conversation> beanList) {
        this.context = context;
        this.mList = beanList;
        inflater = LayoutInflater.from(context);
    }


    public void setData(List<Conversation> dataList) {
        this.mList = dataList;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ChatListHolder holder = null;
        Conversation item = mList.get(i);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_chat_list, null);
            holder = new ChatListHolder();
            //头像
            holder.headCiv = Utils.findViewsById(convertView, R.id.item_chat_civ_head);
            //昵称
            holder.nickTv = Utils.findViewsById(convertView, R.id.item_chat_tv_nick);
            //日期
            holder.dateTv = Utils.findViewsById(convertView, R.id.item_chat_tv_date);
            //内容
            holder.contentTv = Utils.findViewsById(convertView, R.id.item_chat_tv_msg);
            holder.itemRl = Utils.findViewsById(convertView, R.id.item_chat_Rl_layout);
//            holder.badge = new QBadgeView(MyApplication.getContext()).bindTarget(convertView.findViewById(R.id.item_chat_Rl_layout));
//            holder.badge.setBadgeTextSize(10, true);
//            holder.badge.setShowShadow(true);
//            holder.badge.setBadgeBackgroundColor(android.graphics.Color.parseColor("#ff6379"));
            convertView.setTag(holder);
        } else {
            holder = (ChatListHolder) convertView.getTag();
        }
        //加载数据
        FriendInfo friendInfo=item.getFriendInfo().get(0);
        //昵称
        holder.nickTv.setText(friendInfo.getShowname());
        //头像
        Glide.with(context).load(friendInfo.getAvatar()).error(R.drawable.head)
                .into(holder.headCiv);
        holder.dateTv.setText(DataUtils.times7(item.getTimestamp()));
        holder.contentTv.setText(item.getContent());
//        holder.badge.setBadgeNumber(5);
        return convertView;
    }


    public class ChatListHolder {
        private CircleImageView headCiv;
        private TextView nickTv, dateTv, contentTv;
        private RelativeLayout itemRl;
        Badge badge;
    }


}
