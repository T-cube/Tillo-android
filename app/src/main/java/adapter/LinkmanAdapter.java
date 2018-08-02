package adapter;

import android.content.Context;
import android.os.Handler;
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
import com.yumeng.tillo.R;

import java.io.File;
import java.util.List;

import application.MyApplication;
import bean.FriendInfo;
import bean.UserInfo;
import de.hdodenhof.circleimageview.CircleImageView;
import utils.AppSharePre;
import utils.Utils;
import utils.fileutil.FileUtils;

/**
 * 联系人界面 适配器
 */

public class LinkmanAdapter extends RecyclerView.Adapter<LinkmanAdapter.ViewHolder> {
    private Context context;
    private List<FriendInfo> dataList;
    private LayoutInflater mInflater;
    private UserInfo userInfo;
    private String fileForderPath;
    private Handler handler = new Handler();

    public LinkmanAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        userInfo = AppSharePre.getPersonalInfo();
        fileForderPath = FileUtils.getSDPath() + File.separator + userInfo.getUid();
        boolean makeFolderState = FileUtils.makeFolders(fileForderPath);
    }

    public void setDatas(List<FriendInfo> mList) {
        this.dataList = mList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_address_list, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        dataList.get(position).getPinyin();
        String name = dataList.get(position).getShowname();
        if (!TextUtils.isEmpty(name))
            holder.nickTv.setText(name);
        if (headImageIsExist(dataList.get(position).getFriend_id())) {
            String path = fileForderPath + File.separator + dataList.get(position).getFriend_id()
                    + ".jpg";
            Glide.with(MyApplication.getContext())
                    .load(path)
                    .error(R.drawable.head)
                    .into(holder.headCiv);
        } else {
            //下载图片，保存
            downloadHeadImage(dataList.get(position).getAvatar(), dataList.get(position).getFriend_id(), holder.headCiv);
        }

    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
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
        String fileForderPath = utils.fileutil.FileUtils.getSDPath() + File.separator + userInfo.getUid();
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

    class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView headCiv;
        private TextView nickTv;

        public ViewHolder(View itemView) {
            super(itemView);
            headCiv = Utils.findViewsById(itemView, R.id.item_address_civ_head);
            nickTv = Utils.findViewsById(itemView, R.id.item_address_tv_name);
        }
    }
}
