package adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

/**
 * Created by zejian
 * Time  16/1/7 下午4:46
 * Email shinezejian@163.com
 * Description:
 */
public class EmotionGridViewAdapter extends BaseAdapter {

    private Context context;
    private List<String> emotionNames;
    private int itemWidth;
    private int emotion_map_type;

    public List<String> getEmotionNames() {
        return emotionNames;
    }

    public EmotionGridViewAdapter(Context context, List<String> emotionNames, int itemWidth, int emotion_map_type) {
        this.context = context;
        this.emotionNames = emotionNames;
        this.itemWidth = itemWidth;
        this.emotion_map_type = emotion_map_type;
    }

    @Override
    public int getCount() {
        // +1 最后一个为删除按钮
        return emotionNames == null ? 0 : emotionNames.size();
    }

    @Override
    public String getItem(int position) {
        return emotionNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv_emotion = new TextView(context);
        // 设置内边距
        tv_emotion.setPadding(itemWidth / 8, itemWidth / 8, itemWidth / 8, itemWidth / 8);
        LayoutParams params = new LayoutParams(itemWidth, itemWidth);
        tv_emotion.setLayoutParams(params);

//		//判断是否为最后一个item
//		if(position == getCount() - 1) {
//			iv_emotion.setImageResource(R.drawable.compose_emotion_delete);
//		} else {
//			String emotionName = emotionNames.get(position);
//			iv_emotion.setImageResource(EmotionUtils.getImgByName(emotion_map_type,emotionName));
//		}
        tv_emotion.setText(emotionNames.get(position));
        return tv_emotion;
    }

}
