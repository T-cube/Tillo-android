package adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yumeng.tillo.R;

import java.util.List;

import bean.NationCodeBean;
import utils.StringUtils;
import utils.Utils;


/**
 * 国家号码
 */

public class NationCodeAdapter extends BaseAdapter {
    private Context context;
    private List<NationCodeBean> dataList;
    private LayoutInflater mInflater;


    public NationCodeAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<NationCodeBean> list) {
        this.dataList = list;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        NationCodeHolder holder = null;
        NationCodeBean item = dataList.get(position);
        if (convertView == null) {
            holder = new NationCodeHolder();
            convertView = mInflater.inflate(R.layout.item_nation_code, null);
            holder.headTv = Utils.findViewsById(convertView, R.id.item_nation_tv_head);
            holder.headLl = Utils.findViewsById(convertView, R.id.item_nation_ll_head);
            holder.codeTv = Utils.findViewsById(convertView, R.id.item_nation_tv_code);
            holder.nameTv = Utils.findViewsById(convertView, R.id.item_nation_tv_name);
            convertView.setTag(holder);
        } else {
            holder = (NationCodeHolder) convertView.getTag();
        }
        //加载数据
        holder.headTv.setText(item.getHeadWord());
        if (position == 0) {
            holder.headLl.setVisibility(View.VISIBLE);
        } else {
            //比对前后项的首字母是否一致
            String beforeWord = dataList.get(position - 1).getHeadWord();
            if (beforeWord.equals(item.getHeadWord())) {
                holder.headLl.setVisibility(View.GONE);
            } else {
                holder.headLl.setVisibility(View.VISIBLE);
            }

        }
        holder.nameTv.setText(item.getName());
        if (!TextUtils.isEmpty(item.getDial_code()))
            holder.codeTv.setText(item.getDial_code());
        return convertView;
    }


    class NationCodeHolder {
        private TextView headTv, nameTv, codeTv;
        private LinearLayout headLl;
    }
}
