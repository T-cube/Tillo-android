package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.yumeng.tillo.R;

import java.util.List;

import bean.FriendBean;
import de.hdodenhof.circleimageview.CircleImageView;
import utils.Utils;

/**
 * 黑名单 适配器
 */

public class BlackListAdapter extends BaseAdapter {

    List<FriendBean> mDatas;
    private Context context;
    private LayoutInflater mInflater;


    public BlackListAdapter(Context context, List<FriendBean> mList) {
        this.context = context;
        mDatas = mList;
        mInflater = LayoutInflater.from(context);
    }

    public void setmDatas(List<FriendBean> mList) {
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
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        BlackListHolder holder = null;
        if (convertView == null) {
            holder = new BlackListHolder();
            convertView = mInflater.inflate(R.layout.item_black_list, null);
            holder.headCiv = Utils.findViewsById(convertView, R.id.item_black_list_tv_name);
            holder.nameTv = Utils.findViewsById(convertView, R.id.item_black_list_tv_name);
            holder.addFriendTv = Utils.findViewsById(convertView, R.id.item_black_list_tv_add);
            convertView.setTag(holder);

        } else {
            holder = (BlackListHolder) convertView.getTag();

        }
        //加载数据




        return convertView;
    }

    /**
     * 黑名单 列表 holder
     */

    class BlackListHolder {
        private CircleImageView headCiv;
        private TextView nameTv, addFriendTv;


    }
}
