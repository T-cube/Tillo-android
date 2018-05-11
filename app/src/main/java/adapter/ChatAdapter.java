package adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yumeng.tillo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;

import application.MyApplication;
import bean.ChatMessage;
import bean.UserInfo;
import constants.Constants;
import de.hdodenhof.circleimageview.CircleImageView;
import listener.OnItemClickListener;
import utils.AppSharePre;
import utils.DataUtils;
import utils.MediaPlayerManager;
import utils.Utils;
import utils.fileutil.FileUtils;
import views.ProcessImageView;

/**
 * 一对一 聊天   会话列表
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * 1、发送文本、2、发送图片、3、发送语音、4、发送视频5、发送文件、6、发送链接
     * 7、接收文本、8、接收图片、9、接收语音、10、接收视频、11、接收文件、12、接收链接
     */
    private final int CHAT_SEND_TXT = 1, CHAT_SEND_IMAGE = 2, CHAT_SEND_VOICE = 3, CHAT_SEND_VIDEO = 4,
            CHAT_SEND_FILE = 5, CHAT_SEND_LINK = 6,
            CHAT_RECEIVE_TXT = 7, CHAT_RECEIVE_IMAGE = 8,
            CHAT_RECEIVE_VOICE = 9, CHAT_RECEIVE_VIDEO = 10,
            CHAT_RECEIVE_FILE = 11, CHAT_RECEIVE_LINK = 12;

    private OnItemClickListener mOnItemClickListener = null;
    //消息列表
    private List<ChatMessage> mDatas = new ArrayList<>();
    private UserInfo userInfo;
    private Context mContext;
    private String avatar;
    private int mPosition = -1;
    private String fileForderPath;
    private RxPermissions rxPermissions;
    private ArrayList<String> downloadList;

    public ChatAdapter(Context context, String avatar) {
        this.mContext = context;
        this.avatar = avatar;
        userInfo = AppSharePre.getPersonalInfo();
        fileForderPath = FileUtils.getSDPath() + File.separator + userInfo.getUid();
        rxPermissions = new RxPermissions((Activity) context);
        downloadList = new ArrayList<>();
    }

    public void setmDatas(List<ChatMessage> mList) {
        this.mDatas = mList;
        notifyDataSetChanged();
    }

    public void appendDatas(List<ChatMessage> historyList) {
        mDatas.addAll(historyList);
        notifyDataSetChanged();
    }


    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View view = null;
        switch (viewType) {
            case CHAT_SEND_TXT:
                //发送文本信息
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_text_right, parent, false);
                holder = new ChatTextSendHolder(view);
                break;
            case CHAT_SEND_IMAGE:
                //发送图片
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right_image, parent, false);
                holder = new ChatImageSendHolder(view);
                break;
            case CHAT_SEND_VOICE:
                //发送语音
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right_voice, parent, false);
                holder = new ChatVoiceSendHolder(view);
                break;
            case CHAT_RECEIVE_TXT:
                //接收文字信息
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_text_left, parent, false);
                holder = new ChatTextReceivedHolder(view);
                break;
            case CHAT_RECEIVE_IMAGE:
                //接收图片
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left_image, parent, false);
                holder = new ChatImageReceivedHolder(view);
                break;
            case CHAT_RECEIVE_VOICE:
                //接收语音
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left_voice, parent, false);
                holder = new ChatVoiceReceivedHolder(view);
                break;
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_text_left, parent, false);
                holder = new ChatTextReceivedHolder(view);
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ChatMessage bean = mDatas.get(position);
//        if (mOnItemClickListener != null) {
//            //添加点击事件
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int pos = holder.getLayoutPosition();
//                    mOnItemClickListener.onItemClick(holder.itemView, pos);
//                }
//            });
//        }
        switch (getItemViewType(position)) {
            case CHAT_SEND_TXT:
                //发送文本
                ChatTextSendHolder chatTextSendHolder = (ChatTextSendHolder) holder;
                chatTextSendHolder.timeTv.setText(DataUtils.times7(bean.getTimestamp()));
                chatTextSendHolder.contentTv.setText(bean.getContent());
                break;

            case CHAT_SEND_IMAGE:
                //发送图片
                ChatImageSendHolder chatImageSendHolder = (ChatImageSendHolder) holder;
                chatImageSendHolder.dateTv.setText("今天");
                chatImageSendHolder.timeTv.setText("11：30PM");
                Glide.with(MyApplication.getContext()).load(R.drawable.head).into(chatImageSendHolder.imageIv);
                //添加点击事件
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });
                break;
            case CHAT_SEND_VOICE:
                //发送语音
                final ChatVoiceSendHolder chatVoiceSendHolder = (ChatVoiceSendHolder) holder;
                chatVoiceSendHolder.dateTv.setText("今天");
                chatVoiceSendHolder.durationTv.setText(bean.getDuration() + "'");
                chatVoiceSendHolder.timeTv.setText(DataUtils.times7(bean.getTimestamp()));
                if (mPosition == position) {
                    chatVoiceSendHolder.stopIv.setVisibility(View.VISIBLE);
                    chatVoiceSendHolder.playIv.setVisibility(View.GONE);
                } else {
                    chatVoiceSendHolder.playIv.setVisibility(View.VISIBLE);
                    chatVoiceSendHolder.stopIv.setVisibility(View.GONE);
                }
                chatVoiceSendHolder.stopIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaPlayerManager.stop();
                        mPosition = -1;
                        notifyDataSetChanged();
                    }
                });
                //添加点击事件
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("sourceId", bean.getSourceid());
                        //播放语音
                        if (!TextUtils.isEmpty(bean.getLocal_path())) {
                            //播放语音
                            mPosition = position;
                            notifyDataSetChanged();
                            MediaPlayerManager.playSound(bean.getLocal_path(), new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    //播放完成，关闭动画
                                    MediaPlayerManager.stop();
                                    mPosition = -1;
                                    notifyDataSetChanged();
                                }
                            });
                        }

                    }
                });
                break;
            case CHAT_RECEIVE_TXT:
                //接收文本信息
                ChatTextReceivedHolder chatTextReceivedHolder = (ChatTextReceivedHolder) holder;
                chatTextReceivedHolder.dateTv.setText("今天");
                Glide.with(mContext).load(avatar).error(R.drawable.head).into(chatTextReceivedHolder.headCiv);
                chatTextReceivedHolder.timeTv.setText(DataUtils.times7(bean.getTimestamp()));
                chatTextReceivedHolder.contentTv.setText(bean.getContent());
                break;
            case CHAT_RECEIVE_IMAGE:
                //接收图片
                ChatImageReceivedHolder chatImageReceivedHolder = (ChatImageReceivedHolder) holder;
                chatImageReceivedHolder.dateTv.setText("今天");
                chatImageReceivedHolder.timeTv.setText("11:30PM");
                Glide.with(MyApplication.getContext()).load(R.drawable.head).into(chatImageReceivedHolder.imageIv);
                //添加点击事件
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                break;
            case CHAT_RECEIVE_VOICE:
                //接收语音
                final ChatVoiceReceivedHolder chatVoiceReceivedHolder = (ChatVoiceReceivedHolder) holder;
                chatVoiceReceivedHolder.dateTv.setText("今天");
                chatVoiceReceivedHolder.timeTv.setText(DataUtils.times7(bean.getTimestamp()));
                chatVoiceReceivedHolder.durationTv.setText(bean.getDuration() + "'");
                if (mPosition == position) {
                    chatVoiceReceivedHolder.stopIv.setVisibility(View.VISIBLE);
                    chatVoiceReceivedHolder.playIv.setVisibility(View.GONE);
                    chatVoiceReceivedHolder.progressPb.setVisibility(View.GONE);
                    chatVoiceReceivedHolder.stopIv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //播放完成，关闭动画
                            MediaPlayerManager.stop();
                            mPosition = -1;
                            notifyDataSetChanged();
                        }
                    });
                } else {
                    chatVoiceReceivedHolder.playIv.setVisibility(View.VISIBLE);
                    chatVoiceReceivedHolder.stopIv.setVisibility(View.GONE);
                    chatVoiceReceivedHolder.progressPb.setVisibility(View.GONE);
                }
                Glide.with(MyApplication.getContext()).load(R.drawable.head).into(chatVoiceReceivedHolder.headIv);
                //添加点击事件
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //播放语音
                        if (!TextUtils.isEmpty(bean.getLocal_path())) {
                            //播放语音
                            mPosition = position;
                            notifyDataSetChanged();
                            MediaPlayerManager.playSound(bean.getLocal_path(), new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    //播放完成，关闭动画
                                    MediaPlayerManager.stop();
                                    mPosition = -1;
                                    notifyDataSetChanged();
                                }
                            });
                        } else {
                            chatVoiceReceivedHolder.itemView.setEnabled(false);
                            chatVoiceReceivedHolder.progressPb.setVisibility(View.VISIBLE);
                            chatVoiceReceivedHolder.playIv.setVisibility(View.GONE);
                            chatVoiceReceivedHolder.stopIv.setVisibility(View.GONE);
                            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    .subscribe(new io.reactivex.functions.Consumer<Boolean>() {
                                        @Override
                                        public void accept(Boolean aBoolean) throws Exception {
                                            //权限已经开启
                                            if (aBoolean) {
                                                downloadAudioFile(bean.getSourceid(), chatVoiceReceivedHolder, bean, position);
                                            } else {
                                                //未开启权限，弹出提示框

                                            }


                                        }
                                    });
                        }
                    }
                });
                break;


        }


    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        int type = 0;
        switch (mDatas.get(position).getType()) {
            case "text":
                //文本信息
                if (userInfo.getUid().equals(mDatas.get(position).getFrom()))
                    type = CHAT_SEND_TXT;
                else
                    type = CHAT_RECEIVE_TXT;

                break;
            case "image":
                //图片
                if (userInfo.getUid().equals(mDatas.get(position).getFrom()))
                    type = CHAT_SEND_IMAGE;
                else
                    type = CHAT_RECEIVE_IMAGE;
                break;
            case "audio":
                //语音
                if (userInfo.getUid().equals(mDatas.get(position).getFrom()))
                    type = CHAT_SEND_VOICE;
                else
                    type = CHAT_RECEIVE_VOICE;
                break;
            case "video":
                //视频
                if (userInfo.getUid().equals(mDatas.get(position).getFrom()))
                    type = CHAT_SEND_VIDEO;
                else
                    type = CHAT_RECEIVE_VIDEO;
                break;
            case "file":
                //文件
                if (userInfo.getUid().equals(mDatas.get(position).getFrom()))
                    type = CHAT_SEND_FILE;
                else
                    type = CHAT_RECEIVE_FILE;
                break;
            case "link":
                //链接
                if (userInfo.getUid().equals(mDatas.get(position).getFrom()))
                    type = CHAT_SEND_LINK;
                else
                    type = CHAT_RECEIVE_LINK;
                break;

        }
        return type;
    }

    /**
     * 发送文字 右侧
     */
    class ChatTextSendHolder extends RecyclerView.ViewHolder {
        private TextView dateTv, contentTv, timeTv;
        private LinearLayout contentLl;

        public ChatTextSendHolder(View itemView) {
            super(itemView);
            dateTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_date);
            contentTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_content);
            timeTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_time);
            contentLl = Utils.findViewsById(itemView, R.id.item_chat_ll_right_content);
        }
    }

    /**
     * 接受文字 左侧
     */
    class ChatTextReceivedHolder extends RecyclerView.ViewHolder {
        private TextView dateTv, contentTv, timeTv;
        private LinearLayout contentLl;
        private CircleImageView headCiv;

        public ChatTextReceivedHolder(View itemView) {
            super(itemView);
            dateTv = Utils.findViewsById(itemView, R.id.item_chat_txt_left_date);
            contentTv = Utils.findViewsById(itemView, R.id.item_chat_txt_left_content);
            timeTv = Utils.findViewsById(itemView, R.id.item_chat_txt_left_time);
            headCiv = Utils.findViewsById(itemView, R.id.item_chat_civ_head);
            contentLl = Utils.findViewsById(itemView, R.id.item_chat_ll_content);
        }
    }

    /**
     * 图片发送
     */
    class ChatImageSendHolder extends RecyclerView.ViewHolder {
        private TextView dateTv, timeTv;
        private ProcessImageView imageIv;

        public ChatImageSendHolder(View itemView) {
            super(itemView);
            dateTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_date);
            timeTv = Utils.findViewsById(itemView, R.id.item_chat_img_tv_time);
            imageIv = Utils.findViewsById(itemView, R.id.item_chat_image_piv_image);
        }
    }

    /**
     * 图片接收
     */
    class ChatImageReceivedHolder extends RecyclerView.ViewHolder {
        private TextView dateTv, timeTv;
        private ImageView imageIv;

        public ChatImageReceivedHolder(View itemView) {
            super(itemView);
            dateTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_date);
            timeTv = Utils.findViewsById(itemView, R.id.item_chat_img_tv_time);
            imageIv = Utils.findViewsById(itemView, R.id.item_chat_img_iv_image);
        }
    }

    /**
     * 语音发送
     */

    class ChatVoiceSendHolder extends RecyclerView.ViewHolder {
        private TextView dateTv, timeTv, durationTv;
        private LinearLayout contentLl;
        private ProgressBar progressPb;
        private ImageView playIv, stopIv;

        public ChatVoiceSendHolder(View itemView) {
            super(itemView);
            dateTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_date);
            contentLl = Utils.findViewsById(itemView, R.id.item_chat_right_voice_ll_content);
            timeTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_time);
            durationTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_duration);
            progressPb = Utils.findViewsById(itemView, R.id.item_chat_voice_right_progress);
            playIv = Utils.findViewsById(itemView, R.id.item_chat_voice_right_plays);
            stopIv = Utils.findViewsById(itemView, R.id.item_chat_voice_right_stop);
        }
    }

    /**
     * 语音接收
     */
    class ChatVoiceReceivedHolder extends RecyclerView.ViewHolder {
        private TextView dateTv, timeTv, durationTv;
        private LinearLayout contentLl;
        private ProgressBar progressPb;
        private ImageView playIv, stopIv;
        private CircleImageView headIv;

        public ChatVoiceReceivedHolder(View itemView) {
            super(itemView);
            dateTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_date);
            timeTv = Utils.findViewsById(itemView, R.id.item_chat_txt_left_time);
            durationTv = Utils.findViewsById(itemView, R.id.item_chat_txt_left_duration);
            contentLl = Utils.findViewsById(itemView, R.id.item_chat_left_voice_ll_content);
            progressPb = Utils.findViewsById(itemView, R.id.item_chat_voice_left_progress);
            playIv = Utils.findViewsById(itemView, R.id.item_chat_voice_left_plays);
            stopIv = Utils.findViewsById(itemView, R.id.item_chat_voice_left_stop);
            headIv = Utils.findViewsById(itemView, R.id.item_chat_civ_head);
        }
    }


    //下载音频文件
    public void downloadAudioFile(String url, final ChatVoiceReceivedHolder chatVoiceReceivedHolder, final ChatMessage message, final int position) {
        final String fileName = generateFileName();
        Log.e("audioReusult", Constants.TSHION_URL + Constants.downloadFile + url);
//        Constants.TSHION_URL + Constants.downloadFile + url
        OkGo.<File>get(Constants.TSHION_URL + Constants.downloadFile + url)
                .headers("Authorization", "Bearer " + userInfo.getAccess_token())
                .execute(new FileCallback(fileForderPath, fileName) {
                    @Override
                    public void onSuccess(Response<File> response) {
                        File file = response.body();
                        Log.e("TAG", file.getAbsolutePath());
                        //保存文件
                        ChatMessage item = new ChatMessage();
                        item.setLocal_path(file.getAbsolutePath());
                        item.setSourceid(message.getSourceid());
                        item.setTimestamp(message.getTimestamp());
                        item.setRoomid(message.getRoomid());
                        item.setTarget(message.getTarget());
                        item.setFrom(message.getFrom());
                        item.setMessage_id(message.getMessage_id());
                        item.setDuration(message.getDuration());
                        item.setType(message.getType());
                        item.setContent(message.getContent());
                        message.setLocal_path(file.getAbsolutePath());
                        mDatas.set(position, message);
                        boolean isSave = item.saveOrUpdate("roomid=? and timestamp=?", message.getRoomid(), message.getTimestamp() + "");
                        chatVoiceReceivedHolder.itemView.setEnabled(true);
                        notifyDataSetChanged();

                    }

                    @Override
                    public void onError(Response<File> response) {
                        chatVoiceReceivedHolder.itemView.setEnabled(true);
                        notifyDataSetChanged();

                    }
                });
    }

    private String generateFileName() {
        return UUID.randomUUID().toString() + ".aac";
    }
}
