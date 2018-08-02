package adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;
import com.yumeng.tillo.AddMemberActivity;
import com.yumeng.tillo.R;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import application.MyApplication;
import bean.GroupMember;
import bean.UserInfo;
import de.hdodenhof.circleimageview.CircleImageView;
import utils.AppSharePre;
import utils.ToastUtils;
import utils.Utils;

/**
 * Created by yumeng on 2018/7/7.
 */

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.GroupMemberViewHolder> {
    private List<GroupMember> groupMemberList = new ArrayList<>();
    private Context mContext;
    private UserInfo userInfo;
    private String fileForderPath;
    private String groupId;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private boolean isOwener = false;

    public GroupMemberAdapter(Context context, String groupId, boolean isOwener) {
        this.mContext = context;
        this.groupId = groupId;
        this.isOwener = isOwener;
        userInfo = AppSharePre.getPersonalInfo();
    }

    public void setDatas(List<GroupMember> list) {
        this.groupMemberList = list;
        notifyDataSetChanged();
    }

    @Override
    public GroupMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member, parent, false);
        return new GroupMemberViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(GroupMemberViewHolder holder, int position) {

        if (position != getItemCount() - 1) {
            //判断是不是最后一个
            GroupMember item = groupMemberList.get(position);
            if (!TextUtils.isEmpty(item.getAvatar())) {
                //头像地址不为空
                if (headImageIsExist(item.getAvatar()))
                    Glide.with(MyApplication.getContext())
                            .load(senderImagePath(item.getUid()))
                            .error(R.drawable.head)
                            .into(holder.headCiv);
                else {
                    downloadHeadImage(item, holder.headCiv);
                }
            } else
                holder.headCiv.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.head));
            holder.nameTv.setText(item.getName());
        } else if (position == getItemCount() - 1 && position != 0) {
            holder.headCiv.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.add_member_icon));
            holder.headCiv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isOwener) {
                        //跳转到添加成员界面
                        Intent addMemberIntent = new Intent(mContext, AddMemberActivity.class);
                        addMemberIntent.putExtra("groupId", groupId);
                        ((Activity) mContext).startActivityForResult(addMemberIntent, 1);
                    } else
                        ToastUtils.getInstance().shortToast("你不是群主，无法添加群成员！");
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        if (groupMemberList == null)
            return 1;
        else if (groupMemberList.size() < 10)
            return groupMemberList.size() + 1;
        else
            return 11;
    }

    class GroupMemberViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView headCiv;
        private TextView nameTv;

        public GroupMemberViewHolder(View itemView) {
            super(itemView);
            headCiv = Utils.findViewsById(itemView, R.id.item_group_civ_head);
            nameTv = Utils.findViewsById(itemView, R.id.item_group_tv_name);
        }
    }

    //下载头像
    public void downloadHeadImage(GroupMember groupMember, CircleImageView headCiv) {
        if (TextUtils.isEmpty(groupMember.getAvatar()))
            return;
        boolean makeFolderState = utils.fileutil.FileUtils.makeFolders(fileForderPath);
        File cameraFile = new File(fileForderPath, groupMember.getUid() + ".jpg");
        OkGo.<File>get(groupMember.getAvatar())
                .execute(new FileCallback(fileForderPath, groupMember.getUid() + ".jpg") {
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
}
