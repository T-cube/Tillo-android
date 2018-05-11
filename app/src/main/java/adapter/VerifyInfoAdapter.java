package adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.yumeng.tillo.R;

import java.util.List;

import bean.FriendBean;
import bean.UserInfo;
import constants.Constants;
import de.hdodenhof.circleimageview.CircleImageView;
import utils.AppSharePre;
import utils.ToastUtils;
import utils.Utils;

/**
 * 验证 信息
 */

public class VerifyInfoAdapter extends BaseAdapter {

    private List<FriendBean> mDatas;
    private LayoutInflater inflater;
    private Context context;
    private UserInfo userInfo;

    public VerifyInfoAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        userInfo = AppSharePre.getPersonalInfo();
    }

    public void setData(List<FriendBean> mList) {
        this.mDatas = mList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        VerifyHolder holder = null;
        final FriendBean bean = mDatas.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_add_friend, null);
            holder = new VerifyHolder();
            //头像
            holder.headCiv = Utils.findViewsById(convertView, R.id.item_adf_civ_head);
            //昵称
            holder.nickTv = Utils.findViewsById(convertView, R.id.item_adf_tv_nick);
            //手机号吗
            holder.phoneTv = Utils.findViewsById(convertView, R.id.item_adf_tv_phone);
            //备注信息
            holder.contentTv = Utils.findViewsById(convertView, R.id.item_adf_tv_content);
            //添加好友
            holder.addFriendTv = Utils.findViewsById(convertView, R.id.item_adf_tv_add);
            convertView.setTag(holder);
        } else {
            holder = (VerifyHolder) convertView.getTag();
        }
        //加载数据

        Glide.with(context).load(bean.getAvatar())
                .error(R.drawable.head)
                .into(holder.headCiv);
        //名字
        holder.nickTv.setText(bean.getName());
        //手机号
//        holder.phoneTv.setText();
        //备注
        if (!TextUtils.isEmpty(bean.getMark()))
            holder.contentTv.setText(bean.getMark());
        //添加按钮
        if (bean.getStatus().equals("STATUS_FRIEND_REQUEST_PADDING")) {
            holder.addFriendTv.setText("添加");
            holder.addFriendTv.setTextColor(context.getResources().getColor(R.color.textBlue));
            holder.addFriendTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //同意
                    operation("agree", bean.get_id(), position);
                }
            });
        } else {
            holder.addFriendTv.setText("已添加");
            holder.addFriendTv.setTextColor(context.getResources().getColor(R.color.colorGray));
        }

        return convertView;
    }

    //同意 好友
    public void operation(String status, String requestId, final int position) {
        OkGo.<String>post(Constants.TSHION_URL + Constants.operationRequest + status)
                .headers("Authorization", "Bearer " + userInfo.getAccess_token())
                .params("request_id", requestId)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject jsonObject = JSON.parseObject(response.body());
                        int status = jsonObject.getIntValue("status");
                        String message = jsonObject.getString("message");
                        if (status == 200) {
                            mDatas.get(position).setStatus("STATUS_FRIEND_REQUEST_AGREE");
                            notifyDataSetChanged();
                            Activity activity = (Activity) context;
                            activity.setResult(1);
                            activity.finish();
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


    public class VerifyHolder {
        private CircleImageView headCiv;
        private TextView nickTv, phoneTv, contentTv, addFriendTv;
    }
}
