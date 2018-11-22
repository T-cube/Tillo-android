package adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;
import com.yumeng.tillo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import application.MyApplication;
import bean.Conversation;
import bean.FriendInfo;
import bean.UserInfo;
import de.hdodenhof.circleimageview.CircleImageView;
import listener.OnItemClickListener;
import listener.OnItemLongClickListener;
import utils.AppSharePre;
import utils.DataUtils;
import utils.Utils;
import utils.fileutil.FileUtils;

/**
 * Created by yumeng on 2018/6/14.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Conversation> mList;
    private Context mContext;
    private UserInfo userInfo;
    private String fileForderPath;
    private Handler handler = new Handler();
    private OnItemClickListener onItemClickListener = null;
    private OnItemLongClickListener onItemLongClickListener = null;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public MessageAdapter(Context context) {
        userInfo = AppSharePre.getPersonalInfo();
        this.mContext = context;
        mList = new ArrayList<>();
        fileForderPath = FileUtils.getSDPath() + File.separator + userInfo.getId();
        boolean makeFolderState = FileUtils.makeFolders(fileForderPath);
    }

    public void setData(List<Conversation> list) {
        this.mList = list;
        notifyDataSetChanged();
    }


    @Override

    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MessageViewHolder holder = null;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        holder = new MessageViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Conversation item = mList.get(position);
        if (item.getChatType().equals("single")){
            List<FriendInfo> friendInfos = item.getFriendInfo();
            FriendInfo friendInfo = friendInfos.get(0);
            //单聊
            if (headImageIsExist(friendInfo.getFriend_id())) {
                String path = fileForderPath + File.separator +friendInfo.getFriend_id()
                        + ".jpg";
                Glide.with(MyApplication.getContext())
                        .load(path)
                        .error(R.drawable.head)
                        .into(holder.headCiv);
            } else if (!TextUtils.isEmpty(item.getAvatar())) {
                //下载图片，保存
                downloadHeadImage(item.getAvatar(), friendInfo.getFriend_id(), holder.headCiv);
            } else {
                Glide.with(MyApplication.getContext())
                        .load(R.drawable.head)
                        .error(R.drawable.head)
                        .into(holder.headCiv);
            }
        }else {
            //群聊
            if (headImageIsExist(item.getRoomId())) {
                String path = fileForderPath + File.separator +item.getRoomId()
                        + ".jpg";
                Glide.with(MyApplication.getContext())
                        .load(path)
                        .error(R.drawable.head)
                        .into(holder.headCiv);
            } else if (!TextUtils.isEmpty(item.getAvatar())) {
                //下载图片，保存
                downloadHeadImage(item.getAvatar(),item.getRoomId(), holder.headCiv);
            } else {
                Glide.with(MyApplication.getContext())
                        .load(R.drawable.head)
                        .error(R.drawable.head)
                        .into(holder.headCiv);
            }
        }

        //昵称
        if (!TextUtils.isEmpty(item.getName()))
            holder.nickTv.setText(item.getName());

        //内容
        if (!TextUtils.isEmpty(item.getContent()))
            holder.contentTv.setText(item.getContent());
        //时间
        holder.dateTv.setText(DataUtils.times7(item.getTimestamp()));
        //未读消息数
        int totalCount = item.getMsgNum() + item.getOnlineMessage();
        if (totalCount > 0) {
            holder.unreadTv.setText(totalCount + "");
            holder.unreadTv.setVisibility(View.VISIBLE);
        } else
            holder.unreadTv.setVisibility(View.GONE);
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int pos = holder.getLayoutPosition();
                            onItemClickListener.onItemClick(holder.itemView, pos);
                        }
                    });
                }
            });
        }
        if (onItemLongClickListener != null) {
            //长按删除
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    onItemLongClickListener.onItemLongClick(holder.itemView, pos);
                    return true;
                }
            });

        }
//        int state = item.getFriendInfo().get(0).getIstop() == null ? 0 : Integer.parseInt(item.getFriendInfo().get(0).getIstop());
//        if (state == 0) {
//            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
//        } else {
//            holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.colorGray));
//        }

    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public boolean headImageIsExist(String targetId) {
        File headImage = new File(fileForderPath, targetId + ".jpg");
        if (headImage.exists())
            return true;
        else
            return false;
    }

    //下载头像
    public void downloadHeadImage(String avatar, String targetId, CircleImageView imageView) {
        if (TextUtils.isEmpty(avatar))
            return;
        String fileForderPath = utils.fileutil.FileUtils.getSDPath() + File.separator + targetId;
        boolean makeFolderState = utils.fileutil.FileUtils.makeFolders(fileForderPath);
        OkGo.<File>get(avatar)
                .execute(new FileCallback(fileForderPath, targetId + ".jpg") {
                    @Override
                    public void onSuccess(Response<File> response) {
                        File file = response.body();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //加载
                                Glide.with(MyApplication.getContext())
                                        .load(file.getAbsolutePath())
                                        .error(R.drawable.head)
                                        .into(imageView);
                            }
                        }, 200);

                    }

                    @Override
                    public void onError(Response<File> response) {
                    }
                });

    }


    class MessageViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView headCiv;
        private TextView nickTv, contentTv, dateTv, unreadTv;

        public MessageViewHolder(View itemView) {
            super(itemView);
            headCiv = Utils.findViewsById(itemView, R.id.item_message_civ_head);
            nickTv = Utils.findViewsById(itemView, R.id.item_message_tv_name);
            contentTv = Utils.findViewsById(itemView, R.id.item_message_tv_content);
            dateTv = Utils.findViewsById(itemView, R.id.item_message_tv_date);
            unreadTv = Utils.findViewsById(itemView, R.id.item_message_tv_unread);
        }
    }
}
