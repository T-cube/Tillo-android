package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yumeng.tillo.R;

import java.util.List;

import bean.FriendBean;
import bean.FriendInfo;
import de.hdodenhof.circleimageview.CircleImageView;
import utils.Utils;

/**
 * 通讯录列表适配器
 */

public class AddressBookAdapter extends BaseAdapter {
    private Context context;
    private List<FriendInfo> dataList;
    private LayoutInflater mInflater;


    public AddressBookAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }


    public void setDatas(List<FriendInfo> mList) {
        this.dataList = mList;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View converView, ViewGroup viewGroup) {
        AddressHolder holder = null;
        if (converView == null) {
            holder = new AddressHolder();
            converView = mInflater.inflate(R.layout.item_address_list, null);
            holder.headCiv = Utils.findViewsById(converView, R.id.address_list_civ_head);
            holder.nickTv = Utils.findViewsById(converView, R.id.address_list_tv_name);
            holder.tipsTv = Utils.findViewsById(converView, R.id.address_list_tv_letter);
            holder.callIv = Utils.findViewsById(converView, R.id.address_list_iv_call);
            converView.setTag(holder);
        } else {
            holder = (AddressHolder) converView.getTag();
        }
        dataList.get(position).getPinyin();
        String word = dataList.get(position).getHeaderWord();
        String name = dataList.get(position).getName();
        holder.nickTv.setText(name);
        holder.tipsTv.setText(word);
        if (position == 0) {
            holder.tipsTv.setVisibility(View.VISIBLE);
        } else {
            String beforeWord = dataList.get(position - 1).getHeaderWord();
            if (beforeWord.equals(word)) {
                holder.tipsTv.setVisibility(View.INVISIBLE);
            } else {
                holder.tipsTv.setVisibility(View.VISIBLE);
            }

        }
        return converView;
    }


    public class AddressHolder {

        private CircleImageView headCiv;
        private TextView nickTv, tipsTv;
        private ImageView callIv;


    }


}
