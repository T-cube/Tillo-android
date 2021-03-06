package adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import bean.ChatMessage;
import bean.GroupInfo;
import bean.UserInfo;
import de.hdodenhof.circleimageview.CircleImageView;
import listener.OnItemClickListener;
import utils.AppSharePre;
import utils.Utils;
import utils.fileutil.FileUtils;

/**
 * 群会话列表
 */

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.GroupViewHolder> {
    private List<GroupInfo> mList;
    private Context mContext;
    private UserInfo userInfo;
    private String fileForderPath;
    private Handler handler = new Handler();
    private OnItemClickListener onItemClickListener = null;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public GroupListAdapter(Context context) {
        userInfo = AppSharePre.getPersonalInfo();
        fileForderPath = FileUtils.getSDPath() + File.separator + userInfo.getId();
        this.mContext = context;
        mList = new ArrayList<>();
    }

    public void setData(List<GroupInfo> list) {
        this.mList = list;
        notifyDataSetChanged();
    }


    @Override

    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        GroupViewHolder holder = null;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        holder = new GroupViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        GroupInfo item = mList.get(position);
        //昵称
        if (!TextUtils.isEmpty(item.getName()))
            holder.nickTv.setText(item.getName());
        //群头像
        if (headImageIsExist(item.getRoomId())) {
            Glide.with(MyApplication.getContext())
                    .load(senderImagePath(item.getRoomId()))
                    .error(R.drawable.head)
                    .into(holder.headCiv);
        } else {
            if (!TextUtils.isEmpty(item.getAvatar())) {
                downloadHeadImage(item, holder.headCiv);
            }
        }

        //添加点击事件
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


    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }


    class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView nickTv, unreadTv;
        private CircleImageView headCiv;

        public GroupViewHolder(View itemView) {
            super(itemView);
            nickTv = Utils.findViewsById(itemView, R.id.item_group_tv_name);
            unreadTv = Utils.findViewsById(itemView, R.id.item_group_tv_unread);
            headCiv = Utils.findViewsById(itemView, R.id.item_group_civ_head);
        }
    }

    public boolean headImageIsExist(String targetId) {
        File headImage = new File(fileForderPath, targetId + ".jpg");
        if (headImage.exists())
            return true;
        else
            return false;
    }

    public String senderImagePath(String targetId) {
        return fileForderPath + File.separator + targetId + ".jpg";
    }

    //下载头像
    public void downloadHeadImage(GroupInfo groupInfo, CircleImageView headCiv) {
        if (TextUtils.isEmpty(groupInfo.getAvatar()))
            return;
        boolean makeFolderState = utils.fileutil.FileUtils.makeFolders(fileForderPath);
        File cameraFile = new File(fileForderPath, groupInfo.getRoomId() + ".jpg");
        OkGo.<File>get(groupInfo.getAvatar())
                .headers("Authorization", "Bearer " + userInfo.getToken())
                .execute(new FileCallback(fileForderPath, groupInfo.getRoomId() + ".jpg") {
                    @Override
                    public void onSuccess(Response<File> response) {
                        File file = response.body();
                        String imagePath = file.getAbsolutePath();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(MyApplication.getContext())
                                        .load(imagePath)
                                        .error(R.drawable.head)
                                        .into(headCiv);
                            }
                        }, 200);
                    }

                    @Override
                    public void onError(Response<File> response) {
                    }
                });
    }
}
