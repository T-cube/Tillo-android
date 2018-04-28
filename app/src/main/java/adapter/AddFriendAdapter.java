package adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.yumeng.tillo.R;

import java.util.List;

import bean.FriendBean;
import de.hdodenhof.circleimageview.CircleImageView;
import utils.Utils;

/**
 * 添加好友(接收方)  适配器
 */

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.AddFriendViewHolder> {

    private List<FriendBean> mDatas;

    @Override
    public AddFriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AddFriendViewHolder holder = null;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_friend, parent, false);
        holder = new AddFriendViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(AddFriendViewHolder holder, int position) {
        //加载数据


        holder.addFriendTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //添加好友


            }
        });

    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    class AddFriendViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView headCiv;
        private TextView nickTv, contentTv, phoneTv, addFriendTv;

        public AddFriendViewHolder(View itemView) {
            super(itemView);
            headCiv = Utils.findViewsById(itemView, R.id.item_adf_cv_card);
            nickTv = Utils.findViewsById(itemView, R.id.item_adf_tv_nick);
            contentTv = Utils.findViewsById(itemView, R.id.item_adf_tv_content);
            phoneTv = Utils.findViewsById(itemView, R.id.item_adf_tv_phone);
            addFriendTv = Utils.findViewsById(itemView, R.id.item_adf_tv_add);

        }
    }
}
