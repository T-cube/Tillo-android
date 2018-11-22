package adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
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
import java.util.List;

import application.MyApplication;
import bean.GroupMember;
import bean.UserInfo;
import de.hdodenhof.circleimageview.CircleImageView;
import listener.OnItemClickListener;
import utils.AppSharePre;
import utils.Utils;
import utils.fileutil.FileUtils;

/**
 * 删除成员
 */

public class DeleteMemberAdapter extends RecyclerView.Adapter<DeleteMemberAdapter.DeleteMemberHolder> {
    private List<GroupMember> memberList;
    private Context mContext;
    private OnItemClickListener itemClickListener = null;
    private String fileForderPath;
    private UserInfo userInfo;
    private String selectUid;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public String getSelectUid() {
        return selectUid;
    }

    public void setSelectUid(String selectUid) {
        this.selectUid = selectUid;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public DeleteMemberAdapter(Context context, List<GroupMember> members) {
        this.mContext = context;
        this.memberList = members;
        userInfo = AppSharePre.getPersonalInfo();
        //默认为第一个成员的id
        selectUid = memberList.get(0).getUserId();
        fileForderPath = FileUtils.getSDPath() + File.separator + userInfo.getId();
    }

    @Override
    public DeleteMemberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choose_linkman, parent, false);
        return new DeleteMemberHolder(inflate);
    }

    @Override
    public void onBindViewHolder(DeleteMemberHolder holder, int position) {
        final GroupMember item = memberList.get(position);
        //昵称
        if (!TextUtils.isEmpty(item.getName()))
            holder.nickTv.setText(item.getName());
        //头像
        if (headImageIsExist(item.getUserId())) {
            String path = fileForderPath + File.separator + item.getUserId()
                    + ".jpg";
            Glide.with(MyApplication.getContext())
                    .load(path)
                    .error(R.drawable.head)
                    .into(holder.headCiv);
        } else {
            //下载图片
            downloadHeadImage(item.getAvatar(), item.getUserId(), holder.headCiv);
        }
        //判断是否选中
        if (item.getUserId().equals(selectUid)) {
            holder.checkIv.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.choose_select));
        } else {
            holder.checkIv.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.choose_unselect));
        }

        //添加点击事件
        if (itemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    itemClickListener.onItemClick(holder.itemView, pos);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return memberList == null ? 0 : memberList.size();
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
        String fileForderPath = FileUtils.getSDPath() + File.separator + userInfo.getId();
        boolean makeFolderState = FileUtils.makeFolders(fileForderPath);
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


    class DeleteMemberHolder extends RecyclerView.ViewHolder {
        private TextView nickTv;
        private CircleImageView headCiv;
        private ImageView checkIv;

        public DeleteMemberHolder(View itemView) {
            super(itemView);
            nickTv = Utils.findViewsById(itemView, R.id.item_address_tv_name);
            headCiv = Utils.findViewsById(itemView, R.id.item_address_civ_head);
            checkIv = Utils.findViewsById(itemView, R.id.item_address_iv_select);
        }
    }
}
