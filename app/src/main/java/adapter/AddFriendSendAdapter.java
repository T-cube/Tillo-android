package adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.yumeng.tillo.R;

import java.util.List;

import bean.AddFriendBean;
import bean.UserInfo;
import constants.Constants;
import de.hdodenhof.circleimageview.CircleImageView;
import utils.AppSharePre;
import utils.ToastUtils;
import utils.Utils;

/**
 * 添加好友(发起方)
 */

public class AddFriendSendAdapter extends RecyclerView.Adapter<AddFriendSendAdapter.ADSendHolder> {

    private List<AddFriendBean> mDatas;
    private Context mContext;
    private UserInfo userInfo;

    public AddFriendSendAdapter(Context context) {
        this.mContext = context;
        userInfo = AppSharePre.getPersonalInfo();
    }


    public void setmDatas(List<AddFriendBean> mList) {
        this.mDatas = mList;
        notifyDataSetChanged();
    }


    @Override
    public ADSendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ADSendHolder holder = null;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_friend, parent, false);
        holder = new ADSendHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ADSendHolder holder, int position) {
        final AddFriendBean bean = mDatas.get(position);
        //加载数据
        holder.nickTv.setText(bean.getName());
        holder.phoneTv.setText(bean.getMobile());
        Glide.with(mContext).load(bean.getAvatar()).error(R.drawable.head).into(holder.headCiv);
        holder.addFriendTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送添加好友请求
                initMarkDialog(bean.get_id());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    /**
     * 添加好友(发起方)holder
     */
    class ADSendHolder extends RecyclerView.ViewHolder {
        private CircleImageView headCiv;
        private TextView nickTv, phoneTv, addFriendTv;


        public ADSendHolder(View itemView) {
            super(itemView);
            headCiv = Utils.findViewsById(itemView, R.id.item_search_civ_head);
            nickTv = Utils.findViewsById(itemView, R.id.item_search_tv_nick);
            phoneTv = Utils.findViewsById(itemView, R.id.item_search_tv_phone);
            addFriendTv = Utils.findViewsById(itemView, R.id.item_search_tv_add);
        }
    }

    //发送添加好友请求
    public void sendFriendRequest(String requestId, String mark) {
        OkGo.<String>post(Constants.TSHION_URL + Constants.addFriendRequest)
                .headers("Authorization", "Bearer " + userInfo.getAccess_token())
                .params("user_id", requestId)
                .params("mark", mark)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject jsonObject = JSON.parseObject(response.body());
                        String message = jsonObject.getString("message");
                        if (jsonObject.getIntValue("status") == 200) {
                            ToastUtils.getInstance().shortToast("已发送好友请求！");
                        } else {
                            ToastUtils.getInstance().shortToast(message);
                        }

                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Log.e("TAG", response.body() + "code:" + response.code());
                    }
                });

    }

    //填写备注对话框
    public void initMarkDialog(final String requestId) {
        final Dialog dialog = new Dialog(mContext, R.style.AlertDialogStyle);
        //        //填充对话框的布局
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.dialog_verify, null);
//        //初始化控件
        //将布局设置给Dialog
        dialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        //设置Dialog从窗体从中间弹出
        WindowManager m = ((Activity) mContext).getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.width = (int) (d.getWidth() * 0.65); // 宽度设置为屏幕的0.65
        dialogWindow.setAttributes(p);
        dialogWindow.setGravity(Gravity.CENTER);
        dialog.setCancelable(true);
        dialog.show();//显示对话框

        final EditText inputEt = Utils.findViewsById(inflate, R.id.dialog_verify_et_mark);
        View cancelV = Utils.findViewsById(inflate, R.id.dialog_verify_tv_cancel);
        cancelV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        View confirmV = Utils.findViewsById(inflate, R.id.dialog_verify_tv_confirm);
        confirmV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String markStr = inputEt.getText().toString().trim();
                sendFriendRequest(requestId, markStr);
                dialog.dismiss();
            }
        });
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                showKeyboard(inputEt);
            }
        });
    }

    //弹出软键盘
    public void showKeyboard(EditText editText) {
        //其中editText为dialog中的输入框的 EditText
        if (editText != null) {
            //设置可获得焦点
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            //请求获得焦点
            editText.requestFocus();
            //调用系统输入法
            InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(editText, 0);
        }
    }

}
