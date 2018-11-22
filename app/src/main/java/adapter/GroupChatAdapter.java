package adapter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yumeng.tillo.ChatActivity;
import com.yumeng.tillo.GroupChatActivity;
import com.yumeng.tillo.R;

import org.json.JSONException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import application.MyApplication;
import bean.ChatMessage;
import bean.GroupMember;
import bean.UserInfo;
import callback.ReSendMessageCallback;
import constants.Constants;
import de.hdodenhof.circleimageview.CircleImageView;
import listener.OnItemClickListener;
import listener.OnItemLongClickListener;
import utils.AppSharePre;
import utils.DataUtils;
import utils.MediaPlayerManager;
import utils.SharedDataTool;
import utils.Utils;
import utils.fileutil.FileUtils;
import views.ProcessImageView;
import views.RCRelativeLayout;

/**
 * 一对一 聊天   会话列表
 */

public class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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
    private int mPosition = -1;
    private String fileForderPath;
    private RxPermissions rxPermissions;
    private Set<String> downloadList;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private List<GroupMember> members = new ArrayList<>();
    private OnItemLongClickListener onItemLongClickListener = null;

    public Set<String> getDownloadList() {
        return downloadList;
    }

    public void setDownloadList(Set<String> downloadList) {
        this.downloadList = downloadList;
    }

    public GroupChatAdapter(Context context) {
        this.mContext = context;
        userInfo = AppSharePre.getPersonalInfo();
        fileForderPath = FileUtils.getSDPath() + File.separator + userInfo.getId();
        rxPermissions = new RxPermissions((Activity) context);
        downloadList = new HashSet<>();
    }

    public void setmDatas(List<ChatMessage> mList) {
        this.mDatas = mList;
        notifyDataSetChanged();
    }

    public void setMemberList(List<GroupMember> memberList) {
        this.members = memberList;
        notifyDataSetChanged();
    }

    public void appendDatas(List<ChatMessage> historyList) {
        mDatas.addAll(historyList);
        notifyDataSetChanged();
    }


    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_chat_txt_lef, parent, false);
                holder = new ChatTextReceivedHolder(view);
                break;
            case CHAT_RECEIVE_IMAGE:
                //接收图片
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_chat_left_image, parent, false);
                holder = new ChatImageReceivedHolder(view);
                break;
            case CHAT_RECEIVE_VOICE:
                //接收语音
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_chat_left_voice, parent, false);
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
        GroupMember item = getSenderInfo(bean.getSender());
        if (item == null)
            return;
        String senderAvatar = "";
        if (!TextUtils.isEmpty(item.getAvatar()))
            senderAvatar = item.getAvatar();
        switch (getItemViewType(position)) {
            case CHAT_SEND_TXT:
                //发送文本
                ChatTextSendHolder chatTextSendHolder = (ChatTextSendHolder) holder;
                if (bean.getSendState() == 0) {
                    //发送中
                    chatTextSendHolder.loadPb.setVisibility(View.VISIBLE);
                    chatTextSendHolder.sendErrorIv.setVisibility(View.GONE);
                } else if (bean.getSendState() == 1) {
                    //发送成功
                    chatTextSendHolder.sendErrorIv.setVisibility(View.GONE);
                    chatTextSendHolder.loadPb.setVisibility(View.GONE);
                    if (bean.isDisplayTime()) {
                        //时间间隔是否大于5分钟，
                        chatTextSendHolder.dateTv.setText(bean.getDescrpiton());
                        chatTextSendHolder.dateTv.setVisibility(View.VISIBLE);
                    } else {
                        //不显示时间分割
                        chatTextSendHolder.dateTv.setVisibility(View.GONE);
                    }
                    if (onItemLongClickListener != null) {
                        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                int pos = holder.getLayoutPosition();
                                onItemLongClickListener.onItemLongClick(holder.itemView, pos);
                                return true;
                            }
                        });
                    }
                } else {
                    //发送失败
                    chatTextSendHolder.sendErrorIv.setVisibility(View.VISIBLE);
                    chatTextSendHolder.loadPb.setVisibility(View.GONE);
                    chatTextSendHolder.sendErrorIv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //重新发送消息
                            chatTextSendHolder.sendErrorIv.setVisibility(View.GONE);
                            chatTextSendHolder.loadPb.setVisibility(View.VISIBLE);
                            bean.setSendState(0);
                            notifyItemChanged(position);
                            GroupChatActivity chatActivity = (GroupChatActivity) mContext;
                            chatActivity.resendTextMessage(bean, position, new ReSendMessageCallback() {
                                @Override
                                public void onSuccess() {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            chatTextSendHolder.sendErrorIv.setVisibility(View.GONE);
                                            chatTextSendHolder.loadPb.setVisibility(View.GONE);
                                        }
                                    }, 200);
                                }

                                @Override
                                public void onError() {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            chatTextSendHolder.sendErrorIv.setVisibility(View.VISIBLE);
                                            chatTextSendHolder.loadPb.setVisibility(View.GONE);
                                        }
                                    }, 200);
                                }
                            });
                        }
                    });
                }
                chatTextSendHolder.contentTv.setText(bean.getContent());
                break;

            case CHAT_SEND_IMAGE:
                //发送图片
                ChatImageSendHolder chatImageSendHolder = (ChatImageSendHolder) holder;
                if (!TextUtils.isEmpty(bean.getTimestamp() + ""))
                    chatImageSendHolder.timeTv.setText(DataUtils.times7(bean.getTimestamp()));
                else
                    chatImageSendHolder.timeTv.setText("");
                if (bean.getSendState() == 2 && TextUtils.isEmpty(bean.getSourceId())) {
                    //上传图片失败,点击重发按钮重新上传
                    chatImageSendHolder.sendErrorIv.setVisibility(View.VISIBLE);
                    chatImageSendHolder.sendErrorIv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //点击重新上传图片
                            chatImageSendHolder.sendErrorIv.setVisibility(View.GONE);
                            uploadImage(bean, chatImageSendHolder.imageIv, position, chatImageSendHolder.sendErrorIv, new ReSendMessageCallback() {
                                @Override
                                public void onSuccess() {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            chatImageSendHolder.sendErrorIv.setVisibility(View.GONE);
                                        }
                                    }, 200);
                                }

                                @Override
                                public void onError() {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            chatImageSendHolder.sendErrorIv.setVisibility(View.VISIBLE);
                                        }
                                    }, 200);
                                }
                            });
                        }
                    });
                } else {
                    //发送成功
                    if (bean.isDisplayTime()) {
                        //时间间隔是否大于5分钟，
                        chatImageSendHolder.dateTv.setText(bean.getDescrpiton());
                        chatImageSendHolder.dateTv.setVisibility(View.VISIBLE);
                    } else {
                        //不显示时间分割
                        chatImageSendHolder.dateTv.setVisibility(View.GONE);
                    }
                    chatImageSendHolder.sendErrorIv.setVisibility(View.GONE);
                    if (downloadList.contains(bean.getLocal_path()) && bean.getLocal_path() != null) {
                        if (!bean.isUpOrDownLoad()) {
                            uploadImage(bean, chatImageSendHolder.imageIv, position, chatImageSendHolder.sendErrorIv, new ReSendMessageCallback() {
                                @Override
                                public void onSuccess() {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            chatImageSendHolder.sendErrorIv.setVisibility(View.GONE);
                                        }
                                    }, 200);

                                }

                                @Override
                                public void onError() {
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            chatImageSendHolder.sendErrorIv.setVisibility(View.VISIBLE);
                                        }
                                    }, 200);
                                }
                            });
                            Glide.with(MyApplication.getContext()).load(bean.getLocal_path()).into(chatImageSendHolder.imageIv);
                        } else {

                        }
                    } else if (bean.getLocal_path() != null) {
                        chatImageSendHolder.imageIv.setProgress(100);
                        Glide.with(MyApplication.getContext()).load(bean.getLocal_path()).into(chatImageSendHolder.imageIv);
                    } else if (bean.getLocal_path() == null) {
                        if (downloadList.contains(bean.getSourceId())) {
                            //如果正在下载，忽略

                        } else {
                            downloadList.add(bean.getSourceId());
                            //下载图片
                            downloadImageFile(bean.getSourceId(), chatImageSendHolder.imageIv, bean, position);
                        }
                    }

                    //添加点击事件
                    chatImageSendHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //点击查看大图
                            initDialog(bean.getImage_path(), bean.getSourceId(), bean, position);

                        }
                    });
                    if (onItemLongClickListener != null) {
                        chatImageSendHolder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                int pos = holder.getLayoutPosition();
                                onItemLongClickListener.onItemLongClick(chatImageSendHolder.relativeLayout, pos);
                                return true;
                            }
                        });
                    }
                }
                break;
            case CHAT_SEND_VOICE:
                //发送语音
                final ChatVoiceSendHolder chatVoiceSendHolder = (ChatVoiceSendHolder) holder;
                chatVoiceSendHolder.setMessage(bean);
                chatVoiceSendHolder.durationTv.setText(bean.getDuration() + "'");
                boolean isPlaying = bean.isPlaying();
                if (isPlaying) {
                    AnimationDrawable voiceAnimation;
                    chatVoiceSendHolder.playIv.setImageResource(R.drawable.send_voice_anim);
                    voiceAnimation = (AnimationDrawable) chatVoiceSendHolder.playIv.getDrawable();
                    voiceAnimation.start();

                } else {
                    chatVoiceSendHolder.playIv.setImageResource(R.drawable.voice_right3);
                }
//                if (mPosition == position) {
//                    chatVoiceSendHolder.playIv.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            MediaPlayerManager.stop();
//                            mPosition = -1;
//                            chatVoiceSendHolder.playIv.setBackgroundResource(R.drawable.voice_right3);
//                            notifyDataSetChanged();
//                        }
//                    });
//                } else {
//                    chatVoiceSendHolder.playIv.setBackgroundResource(R.drawable.voice_right3);
//                }
                if (bean.getSendState() == 0) {
                    //发送中
                    chatVoiceSendHolder.loadPb.setVisibility(View.VISIBLE);
                    chatVoiceSendHolder.sendErrorIv.setVisibility(View.GONE);
                } else if (bean.getSendState() == 1) {
                    //发送成功
                    chatVoiceSendHolder.loadPb.setVisibility(View.GONE);
                    chatVoiceSendHolder.sendErrorIv.setVisibility(View.GONE);
                    if (bean.isDisplayTime()) {
                        //时间间隔是否大于5分钟，
                        chatVoiceSendHolder.dateTv.setText(bean.getDescrpiton());
                        chatVoiceSendHolder.dateTv.setVisibility(View.VISIBLE);
                    } else {
                        //不显示时间分割
                        chatVoiceSendHolder.dateTv.setVisibility(View.GONE);
                    }

                } else if (bean.getSendState() == 2) {
                    //发送失败
                    chatVoiceSendHolder.loadPb.setVisibility(View.GONE);
                    chatVoiceSendHolder.sendErrorIv.setVisibility(View.VISIBLE);
                    chatVoiceSendHolder.sendErrorIv.setOnClickListener(v -> {
                        //点击重新发送语音消息
                        chatVoiceSendHolder.sendErrorIv.setVisibility(View.GONE);
                        chatVoiceSendHolder.loadPb.setVisibility(View.VISIBLE);
                        reUploadVoice(bean, position, chatVoiceSendHolder.sendErrorIv, chatVoiceSendHolder.loadPb, new ReSendMessageCallback() {
                            @Override
                            public void onSuccess() {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        chatVoiceSendHolder.sendErrorIv.setVisibility(View.GONE);
                                        chatVoiceSendHolder.loadPb.setVisibility(View.GONE);
                                    }
                                }, 200);

                            }

                            @Override
                            public void onError() {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        chatVoiceSendHolder.sendErrorIv.setVisibility(View.VISIBLE);
                                        chatVoiceSendHolder.loadPb.setVisibility(View.GONE);
                                    }
                                }, 200);

                            }
                        });
                    });
                }
                if (onItemLongClickListener != null) {
                    chatVoiceSendHolder.contentLl.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            int pos = holder.getLayoutPosition();
                            onItemLongClickListener.onItemLongClick(chatVoiceSendHolder.contentLl, pos);
                            return true;
                        }
                    });
                }
                if (mOnItemClickListener != null) {
                    //点击事件
                    chatVoiceSendHolder.contentLl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int pos = holder.getLayoutPosition();
                            mOnItemClickListener.onItemClick(chatVoiceSendHolder.contentLl, pos);
                        }
                    });
                }
//                //添加点击事件
//                chatVoiceSendHolder.contentLl.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        //播放语音
//                        if (!TextUtils.isEmpty(bean.getLocal_path())) {
//                            //播放语音
//                            mPosition = position;
//                            notifyDataSetChanged();
//                            chatVoiceSendHolder.playIv.setBackgroundResource(R.drawable.send_voice_anim);
//                            AnimationDrawable animation = (AnimationDrawable) chatVoiceSendHolder.playIv.getBackground();
//                            animation.start();
//                            MediaPlayerManager.playSound(bean.getLocal_path(), new MediaPlayer.OnCompletionListener() {
//                                @Override
//                                public void onCompletion(MediaPlayer mediaPlayer) {
//                                    //播放完成，关闭动画
//                                    chatVoiceSendHolder.playIv.setBackgroundResource(R.drawable.voice_right3);
//                                    MediaPlayerManager.stop();
//                                    mPosition = -1;
//                                    notifyDataSetChanged();
//                                }
//                            });
//                        }
////                        else {
////                            chatVoiceSendHolder.contentLl.setEnabled(false);
////                            chatVoiceSendHolder.loadPb.setVisibility(View.VISIBLE);
////                            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
////                                    .subscribe(new io.reactivex.functions.Consumer<Boolean>() {
////                                        @Override
////                                        public void accept(Boolean aBoolean) throws Exception {
////                                            //权限已经开启
////                                            if (aBoolean) {
////                                                downloadAudioFile(bean.getSourceid(), chatVoiceSendHolder, bean, position);
////                                            } else {
////                                                //未开启权限，弹出提示框
////
////                                            }
////
////
////                                        }
////                                    });
////                        }
//
//                    }
//                });
                break;
            case CHAT_RECEIVE_TXT:
                //接收文本信息
                ChatTextReceivedHolder chatTextReceivedHolder = (ChatTextReceivedHolder) holder;
                if (bean.isDisplayTime()) {
                    //时间间隔是否大于5分钟，
                    chatTextReceivedHolder.dateTv.setText(bean.getDescrpiton());
                    chatTextReceivedHolder.dateTv.setVisibility(View.VISIBLE);
                } else {
                    //不显示时间分割
                    chatTextReceivedHolder.dateTv.setVisibility(View.GONE);
                }
                if (headImageIsExist(senderAvatar))
                    Glide.with(MyApplication.getContext())
                            .load(senderImagePath(bean.getSender()))
                            .error(R.drawable.head)
                            .into(chatTextReceivedHolder.headCiv);
                else if (!TextUtils.isEmpty(senderAvatar)) {
                    downloadHeadImage(item, chatTextReceivedHolder.headCiv);
                }
//                chatTextReceivedHolder.timeTv.setText(DataUtils.times7(bean.getTimestamp()));
                chatTextReceivedHolder.nameTv.setText(item.getName());
                chatTextReceivedHolder.contentTv.setText(bean.getContent());
                if (onItemLongClickListener != null) {
                    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            int pos = holder.getLayoutPosition();
                            onItemLongClickListener.onItemLongClick(holder.itemView, pos);
                            return true;
                        }
                    });
                }
                break;
            case CHAT_RECEIVE_IMAGE:
                //接收图片
                ChatImageReceivedHolder chatImageReceivedHolder = (ChatImageReceivedHolder) holder;
                if (bean.isDisplayTime()) {
                    //时间间隔是否大于5分钟，
                    chatImageReceivedHolder.dateTv.setText(bean.getDescrpiton());
                    chatImageReceivedHolder.dateTv.setVisibility(View.VISIBLE);
                } else {
                    //不显示时间分割
                    chatImageReceivedHolder.dateTv.setVisibility(View.GONE);
                }
                chatImageReceivedHolder.timeTv.setText(DataUtils.times7(bean.getTimestamp()));
                chatImageReceivedHolder.nameTv.setText(item.getName());
                //加载图片
                if (bean.getLocal_path() != null) {
                    //判断图片是否为空
                    chatImageReceivedHolder.imageIv.setProgress(100);
                    Glide.with(MyApplication.getContext()).load(bean.getLocal_path()).into(chatImageReceivedHolder.imageIv);
                } else {
                    if (downloadList.contains(bean.getSourceId())) {
                        //如果正在下载，忽略

                    } else {
                        downloadList.add(bean.getSourceId());
                        //下载图片
                        downloadImageFile(bean.getSourceId(), chatImageReceivedHolder.imageIv, bean, position);
                    }
                }
                if (headImageIsExist(senderAvatar))
                    Glide.with(MyApplication.getContext())
                            .load(senderImagePath(bean.getSender()))
                            .error(R.drawable.head)
                            .into(chatImageReceivedHolder.headCiv);
                else if (!TextUtils.isEmpty(senderAvatar)) {
                    downloadHeadImage(item, chatImageReceivedHolder.headCiv);
                }
                //添加点击事件
                chatImageReceivedHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //点击查看大图
                        initDialog(bean.getImage_path(), bean.getSourceId(), bean, position);

                    }
                });
                //长按删除
                if (onItemLongClickListener != null) {
                    chatImageReceivedHolder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            int pos = holder.getLayoutPosition();
                            onItemLongClickListener.onItemLongClick(chatImageReceivedHolder.relativeLayout, pos);
                            return true;
                        }
                    });
                }
                break;
            case CHAT_RECEIVE_VOICE:
                //接收语音
                final ChatVoiceReceivedHolder chatVoiceReceivedHolder = (ChatVoiceReceivedHolder) holder;
                chatVoiceReceivedHolder.setMessage(bean);
                chatVoiceReceivedHolder.durationTv.setText(bean.getDuration() + "'");
                chatVoiceReceivedHolder.nameTv.setText(item.getName());
                boolean reIsPlaying = bean.isPlaying();
                if (reIsPlaying) {
                    AnimationDrawable voiceAnimation;
                    chatVoiceReceivedHolder.playIv.setImageResource(R.drawable.receive_voice_anim);
                    voiceAnimation = (AnimationDrawable) chatVoiceReceivedHolder.playIv.getDrawable();
                    voiceAnimation.start();
                } else {
                    chatVoiceReceivedHolder.playIv.setImageResource(R.drawable.voice_left3);
                }
//                if (mPosition == position) {
//                    chatVoiceReceivedHolder.playIv.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            //播放完成，关闭动画
//                            MediaPlayerManager.stop();
//                            mPosition = -1;
//                            chatVoiceReceivedHolder.playIv.setBackgroundResource(R.drawable.voice_left3);
//                            notifyDataSetChanged();
//                        }
//                    });
//                } else {
//                    chatVoiceReceivedHolder.playIv.setBackgroundResource(R.drawable.voice_left3);
//                }
                if (bean.isRead()) {
                    chatVoiceReceivedHolder.unreadIv.setVisibility(View.GONE);
                } else {
                    chatVoiceReceivedHolder.unreadIv.setVisibility(View.VISIBLE);
                }
                if (bean.getSendState() == 0) {
                    chatVoiceReceivedHolder.loadPb.setVisibility(View.VISIBLE);
                } else {
                    chatVoiceReceivedHolder.loadPb.setVisibility(View.GONE);
                    if (bean.isDisplayTime()) {
                        //时间间隔是否大于5分钟，
                        chatVoiceReceivedHolder.dateTv.setText(bean.getDescrpiton());
                        chatVoiceReceivedHolder.dateTv.setVisibility(View.VISIBLE);
                    } else {
                        //不显示时间分割
                        chatVoiceReceivedHolder.dateTv.setVisibility(View.GONE);
                    }
                }
                if (headImageIsExist(senderAvatar))
                    Glide.with(MyApplication.getContext())
                            .load(senderImagePath(bean.getSender()))
                            .error(R.drawable.head)
                            .into(chatVoiceReceivedHolder.headCiv);
                else if (!TextUtils.isEmpty(senderAvatar)) {
                    downloadHeadImage(item, chatVoiceReceivedHolder.headCiv);
                }

//                //添加点击事件
//                chatVoiceReceivedHolder.contentLl.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        ChatMessage tempMsg = new ChatMessage();
//                        tempMsg.setRead(true);
//                        bean.setRead(true);
//                        tempMsg.updateAll("message_id=?", bean.getMessage_id());
//                        //播放语音
//                        if (!TextUtils.isEmpty(bean.getLocal_path())) {
//                            //播放语音
//                            mPosition = position;
//                            notifyDataSetChanged();
//                            chatVoiceReceivedHolder.playIv.setBackgroundResource(R.drawable.receive_voice_anim);
//                            AnimationDrawable animationDrawable = (AnimationDrawable) chatVoiceReceivedHolder.playIv.getBackground();
//                            animationDrawable.start();
//                            MediaPlayerManager.playSound(bean.getLocal_path(), new MediaPlayer.OnCompletionListener() {
//                                @Override
//                                public void onCompletion(MediaPlayer mediaPlayer) {
//                                    //播放完成，关闭动画
//                                    MediaPlayerManager.stop();
//                                    chatVoiceReceivedHolder.playIv.setBackgroundResource(R.drawable.voice_left3);
//                                    mPosition = -1;
//                                    notifyDataSetChanged();
//                                }
//                            });
//                        } else {
//                            chatVoiceReceivedHolder.contentLl.setEnabled(false);
//                            chatVoiceReceivedHolder.loadPb.setVisibility(View.VISIBLE);
//                            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                                    .subscribe(new io.reactivex.functions.Consumer<Boolean>() {
//                                        @Override
//                                        public void accept(Boolean aBoolean) throws Exception {
//                                            //权限已经开启
//                                            if (aBoolean) {
//                                                if (!bean.isUpOrDownLoad())
//                                                    downloadAudioFile(bean.getSourceid(), chatVoiceReceivedHolder, bean, position);
//                                            } else {
//                                                //未开启权限，弹出提示框
//
//                                            }
//
//
//                                        }
//                                    });
//                        }
//                    }
//                });
                //长按删除
                if (onItemLongClickListener != null) {
                    chatVoiceReceivedHolder.contentLl.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            int pos = holder.getLayoutPosition();
                            onItemLongClickListener.onItemLongClick(chatVoiceReceivedHolder.contentLl, pos);
                            return true;
                        }
                    });
                }
                //点击事件
                if (mOnItemClickListener != null) {
                    //点击事件
                    chatVoiceReceivedHolder.contentLl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int pos = holder.getLayoutPosition();
                            mOnItemClickListener.onItemClick(chatVoiceReceivedHolder.contentLl, pos);
                        }
                    });
                }
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
                if (userInfo.getId().equals(mDatas.get(position).getSender()))
                    type = CHAT_SEND_TXT;
                else
                    type = CHAT_RECEIVE_TXT;

                break;
            case "image":
                //图片
                if (userInfo.getId().equals(mDatas.get(position).getSender()))
                    type = CHAT_SEND_IMAGE;
                else
                    type = CHAT_RECEIVE_IMAGE;
                break;
            case "audio":
                //语音
                if (userInfo.getId().equals(mDatas.get(position).getSender()))
                    type = CHAT_SEND_VOICE;
                else
                    type = CHAT_RECEIVE_VOICE;
                break;
            case "video":
                //视频
                if (userInfo.getId().equals(mDatas.get(position).getSender()))
                    type = CHAT_SEND_VIDEO;
                else
                    type = CHAT_RECEIVE_VIDEO;
                break;
            case "file":
                //文件
                if (userInfo.getId().equals(mDatas.get(position).getSender()))
                    type = CHAT_SEND_FILE;
                else
                    type = CHAT_RECEIVE_FILE;
                break;
            case "link":
                //链接
                if (userInfo.getId().equals(mDatas.get(position).getSender()))
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
        private ProgressBar loadPb;
        private ImageView sendErrorIv;

        public ChatTextSendHolder(View itemView) {
            super(itemView);
            dateTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_date);
            contentTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_content);
//            timeTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_time);
            contentLl = Utils.findViewsById(itemView, R.id.item_chat_ll_right_content);
            loadPb = Utils.findViewsById(itemView, R.id.item_chat_pb_right_progress);
            sendErrorIv = Utils.findViewsById(itemView, R.id.item_chat_pb_right_failed);
        }
    }

    /**
     * 接受文字 左侧
     */
    class ChatTextReceivedHolder extends RecyclerView.ViewHolder {
        private TextView dateTv, contentTv, timeTv, nameTv;
        private LinearLayout contentLl;
        private CircleImageView headCiv;

        public ChatTextReceivedHolder(View itemView) {
            super(itemView);
            dateTv = Utils.findViewsById(itemView, R.id.item_chat_txt_left_date);
            contentTv = Utils.findViewsById(itemView, R.id.item_chat_txt_left_content);
//            timeTv = Utils.findViewsById(itemView, R.id.item_chat_txt_left_time);
            headCiv = Utils.findViewsById(itemView, R.id.item_chat_civ_head);
            contentLl = Utils.findViewsById(itemView, R.id.item_chat_ll_content);
            nameTv = Utils.findViewsById(itemView, R.id.item_chat_txt_left_name);
        }
    }

    /**
     * 图片发送
     */
    class ChatImageSendHolder extends RecyclerView.ViewHolder {
        private TextView dateTv, timeTv;
        private ProcessImageView imageIv;
        private RCRelativeLayout relativeLayout;
        private ProgressBar loadPb;
        private ImageView sendErrorIv;

        public ChatImageSendHolder(View itemView) {
            super(itemView);
            dateTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_date);
            timeTv = Utils.findViewsById(itemView, R.id.item_chat_img_tv_time);
            imageIv = Utils.findViewsById(itemView, R.id.item_chat_image_piv_image);
            relativeLayout = Utils.findViewsById(itemView, R.id.item_chat_image_rc_contentrl);
            loadPb = Utils.findViewsById(itemView, R.id.item_chat_pb_right_progress);
            sendErrorIv = Utils.findViewsById(itemView, R.id.item_chat_pb_right_failed);
        }
    }

    /**
     * 图片接收
     */
    class ChatImageReceivedHolder extends RecyclerView.ViewHolder {
        private TextView dateTv, timeTv, nameTv;
        private ProcessImageView imageIv;
        private RCRelativeLayout relativeLayout;
        private CircleImageView headCiv;

        public ChatImageReceivedHolder(View itemView) {
            super(itemView);
            dateTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_date);
            timeTv = Utils.findViewsById(itemView, R.id.item_chat_img_tv_time);
            imageIv = Utils.findViewsById(itemView, R.id.item_chat_image_piv_image);
            relativeLayout = Utils.findViewsById(itemView, R.id.item_chat_image_rc_contentrl);
            headCiv = Utils.findViewsById(itemView, R.id.item_chat_civ_head);
            nameTv = Utils.findViewsById(itemView, R.id.item_chat_txt_left_name);
        }
    }

    /**
     * 语音发送
     */

    class ChatVoiceSendHolder extends RecyclerView.ViewHolder implements PropertyChangeListener {
        private TextView dateTv, durationTv;
        private LinearLayout contentLl;
        private ProgressBar loadPb;
        private ImageView playIv, sendErrorIv;
        private ChatMessage message;

        public ChatVoiceSendHolder(View itemView) {
            super(itemView);
            dateTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_date);
            contentLl = Utils.findViewsById(itemView, R.id.item_chat_right_voice_ll_content);
            durationTv = Utils.findViewsById(itemView, R.id.item_chat_right_voice_tv_duration);
            loadPb = Utils.findViewsById(itemView, R.id.item_chat_pb_right_progress);
            playIv = Utils.findViewsById(itemView, R.id.item_chat_right_voice_iv_play);
            sendErrorIv = Utils.findViewsById(itemView, R.id.item_chat_pb_right_failed);
        }

        //将message传进来，便于赋值
        public void setMessage(ChatMessage message) {
            if (this.message != null)
                this.message.removePropertyChangeListener(this);
            this.message = message;
            this.message.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            boolean isPlaying = message.isPlaying();
            if (isPlaying) {
                AnimationDrawable voiceAnimation;
                playIv.setImageResource(R.drawable.send_voice_anim);
                voiceAnimation = (AnimationDrawable) playIv.getDrawable();
                voiceAnimation.start();
            } else {
                playIv.setImageResource(R.drawable.voice_right3);
            }
        }
    }

    /**
     * 语音接收
     */
    class ChatVoiceReceivedHolder extends RecyclerView.ViewHolder implements PropertyChangeListener {
        private TextView dateTv, durationTv, nameTv;
        private LinearLayout contentLl;
        private ProgressBar loadPb;
        private ImageView playIv, unreadIv;
        private CircleImageView headCiv;
        private ChatMessage message;

        public ChatVoiceReceivedHolder(View itemView) {
            super(itemView);
            dateTv = Utils.findViewsById(itemView, R.id.item_chat_txt_right_date);
            durationTv = Utils.findViewsById(itemView, R.id.item_chat_txt_left_duration);
            contentLl = Utils.findViewsById(itemView, R.id.item_chat_left_voice_ll_content);
            loadPb = Utils.findViewsById(itemView, R.id.item_chat_pb_left_progress);
            playIv = Utils.findViewsById(itemView, R.id.item_chat_right_voice_iv_play);
            headCiv = Utils.findViewsById(itemView, R.id.item_chat_civ_head);
            nameTv = Utils.findViewsById(itemView, R.id.item_chat_txt_left_name);
            unreadIv = Utils.findViewsById(itemView, R.id.item_group_tv_unread);
        }

        //将message传进来，便于赋值
        public void setMessage(ChatMessage message) {
            if (this.message != null)
                this.message.removePropertyChangeListener(this);
            this.message = message;
            this.message.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("playing")) {
                boolean isPlaying = message.isPlaying();
                if (isPlaying) {
                    AnimationDrawable voiceAnimation;
                    playIv.setImageResource(R.drawable.receive_voice_anim);
                    voiceAnimation = (AnimationDrawable) playIv.getDrawable();
                    voiceAnimation.start();
                } else {
                    playIv.setImageResource(R.drawable.voice_left3);
                }

            } else if (evt.getPropertyName().equals("upOrDownLoad")) {
                boolean isDownload = message.isUpOrDownLoad();
                if (isDownload) {
                    loadPb.setVisibility(View.VISIBLE);
                } else {
                    loadPb.setVisibility(View.GONE);
                }
            }
        }
    }


    //下载音频文件
    public void downloadAudioFile(String url, final ChatVoiceReceivedHolder holder, final ChatMessage message, final int position) {
        final String fileName = generateFileName();
//        Constants.TSHION_URL + Constants.downloadFile + url
        OkGo.<File>get(Constants.TSHION_URL + Constants.downloadFile + url)
                .headers("Authorization", "Bearer " + userInfo.getToken())
                .execute(new FileCallback(fileForderPath, fileName) {
                    @Override
                    public void onSuccess(Response<File> response) {
                        File file = response.body();
                        //保存文件
                        ChatMessage item = new ChatMessage();
                        item.setLocal_path(file.getAbsolutePath());
                        item.setSourceId(message.getSourceId());
                        item.setTimestamp(message.getTimestamp());
                        item.setRoomid(message.getRoomid());
                        item.setSender(message.getSender());
                        item.setMessageId(message.getMessageId());
                        item.setDuration(message.getDuration());
                        item.setType(message.getType());
                        item.setContent(message.getContent());
                        message.setLocal_path(file.getAbsolutePath());
                        message.setUpOrDownLoad(false);
                        mDatas.set(position, message);
                        boolean isSave = item.saveOrUpdate("roomid=? and timestamp=?", message.getRoomid(), message.getTimestamp() + "");
                        holder.contentLl.setEnabled(true);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Response<File> response) {
                        holder.contentLl.setEnabled(true);
                        notifyDataSetChanged();

                    }
                });
    }

    //上传图片
    public void uploadImage(final ChatMessage message, ProcessImageView processImageView, final int position, ImageView errorIv, ReSendMessageCallback callback) {
        message.setUpOrDownLoad(true);
        mDatas.set(position, message);
        final File file = new File(message.getLocal_path());
        boolean hasNetWork = SharedDataTool.getBoolean(mContext, "network");
        OkGo.<String>get(Constants.BaseUrl + "/getToken")
                .headers("Authorization", userInfo.getToken())
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.e("TAG", response.body());
                        JSONObject jsonObject = JSON.parseObject(response.body());
                        JSONObject dataObject = JSON.parseObject(jsonObject.getString("data"));
                        String token = dataObject.getString("token");
                        uploadImageToQiNiu(message.getBackId()+".jpg", token, file, message, processImageView, position, errorIv, callback);
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Log.e("TAG", response.getException().getMessage());
                        errorIv.setVisibility(View.VISIBLE);
                        message.setUpOrDownLoad(false);
                        message.setSendState(2);
                        mDatas.set(position, message);
                    }
                });

    }

    //上传图片到七牛云
    public void uploadImageToQiNiu(String fileName, String qiNiuToken, File file, ChatMessage message, ProcessImageView processImageView, final int position, ImageView errorIv, ReSendMessageCallback callback) {
        UploadManager uploadManager = new UploadManager();
        uploadManager.put(file, fileName, qiNiuToken, new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, org.json.JSONObject response) {
                if (info.isOK()) {
                    Log.e("qiniu", "Upload Success" + response.toString());
                    try {
                        String hash = response.getString("hash");
                        long size = info.totalSize;
                        message.setUpOrDownLoad(false);
                        mDatas.set(position, message);
                        downloadList.remove(message.getLocal_path());
                        GroupChatActivity chatActivity = (GroupChatActivity) mContext;
                        chatActivity.sendImageMessage(position, message, key, hash, size, callback);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                    errorIv.setVisibility(View.VISIBLE);
                    message.setUpOrDownLoad(false);
                    message.setSendState(2);
                    mDatas.set(position, message);
                }

            }

        }, new UploadOptions(null, null, false, new UpProgressHandler() {
            @Override
            public void progress(String key, double percent) {
                int progress = (int) (percent * 100);
                processImageView.setProgress(progress);
            }
        }, null));
    }

    //下载缩略图
    public void downloadImageFile(String url, final ProcessImageView processImageView, final ChatMessage message, final int position) {
        final String fileName = generateImageFileName();
        message.setUpOrDownLoad(true);
        mDatas.set(position, message);
        OkGo.<File>get(Constants.FileUrl + Constants.downloadFile)
                .headers("Authorization", userInfo.getToken())
                .params("sourceId", url)
                .params("type",1)
                .execute(new FileCallback(fileForderPath, fileName) {
                    @Override
                    public void onSuccess(Response<File> response) {
                        File file = response.body();
                        //保存文件
                        ChatMessage item = new ChatMessage();
                        item.setLocal_path(file.getAbsolutePath());
                        item.setSourceId(message.getSourceId());
                        item.setTimestamp(message.getTimestamp());
                        item.setRoomid(message.getRoomid());
                        item.setSender(message.getSender());
                        item.setMessageId(message.getMessageId());
                        item.setType(message.getType());
                        item.setContent(message.getContent());
                        message.setLocal_path(file.getAbsolutePath());
                        message.setUpOrDownLoad(false);
                        mDatas.set(position, message);
                        boolean isSave = item.saveOrUpdate("roomid=? and timestamp=?", message.getRoomid(), message.getTimestamp() + "");
                        downloadList.remove(url);
                        notifyDataSetChanged();
                    }

                    @Override
                    public void downloadProgress(Progress progress) {
                        int percent = (int) (progress.currentSize / progress.totalSize) * 100;
                        processImageView.setProgress(percent);
                    }

                    @Override
                    public void onError(Response<File> response) {
                        downloadList.remove(url);
                        message.setUpOrDownLoad(false);
                        mDatas.set(position, message);
                    }
                });
    }

    //下载原图
    public void downloadBitmapImageFile(String url, final ChatMessage message, final int position, final ProgressBar progressBar, final SubsamplingScaleImageView scaleImageView) {
        final String fileName = generateImageFileName();
        message.setUpOrDownLoad(true);
        mDatas.set(position, message);
        OkGo.<File>get(Constants.FileUrl + Constants.downloadFile)
                .headers("Authorization",userInfo.getToken())
                .params("sourceId",url)
                .execute(new FileCallback(fileForderPath, fileName) {
                    @Override
                    public void onSuccess(Response<File> response) {
                        progressBar.setVisibility(View.GONE);
                        File file = response.body();
                        //保存文件
                        ChatMessage item = new ChatMessage();
                        item.setImage_path(file.getAbsolutePath());
                        item.setLocal_path(message.getLocal_path());
                        item.setSourceId(message.getSourceId());
                        item.setTimestamp(message.getTimestamp());
                        item.setRoomid(message.getRoomid());
                        item.setSender(message.getSender());
                        item.setMessageId(message.getMessageId());
                        item.setType(message.getType());
                        item.setContent(message.getContent());
                        message.setLocal_path(file.getAbsolutePath());
                        scaleImageView.setMinScale(2.0F);
                        scaleImageView.setImage(ImageSource.uri(file.getAbsolutePath()));
                        mDatas.set(position, message);
                        boolean isSave = item.saveOrUpdate("roomid=? and timestamp=?", message.getRoomid(), message.getTimestamp() + "");
                    }

                    @Override
                    public void onError(Response<File> response) {
                    }
                });
    }

    private String generateFileName() {
        return UUID.randomUUID().toString() + ".aac";
    }

    private String generateImageFileName() {
        return UUID.randomUUID().toString() + ".jpg";
    }

    //初始化dialog
    public void initDialog(String imagePath, String url, ChatMessage chatMessage, int position) {
        final Dialog dialog = new Dialog(mContext, R.style.myTransformStyle);
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.dialog_image, null);
//        //初始化控件
        //将布局设置给Dialog
        dialog.setContentView(inflate);
        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();
        //设置Dialog从窗体从中间弹出
        dialogWindow.setGravity(Gravity.CENTER);
        dialog.setCancelable(true);
        dialog.show();//显示对话框
        // 将对话框的大小按屏幕大小的百分比设置
        WindowManager windowManager = ((Activity) mContext).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = (int) display.getWidth(); //设置宽度
        lp.height = (int) display.getHeight(); //设置宽度
        dialog.getWindow().setAttributes(lp);

        ProgressBar progressBar = Utils.findViewsById(inflate, R.id.dialog_progresss);
        SubsamplingScaleImageView scaleImageView = Utils.findViewsById(inflate, R.id.dialog_siv_image);
        scaleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        if (!TextUtils.isEmpty(imagePath)) {
            //加载原图
            scaleImageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
            scaleImageView.setMinScale(2.0F);
            scaleImageView.setImage(ImageSource.uri(imagePath));
            progressBar.setVisibility(View.GONE);
        } else {
            //下载原图
            downloadBitmapImageFile(url, chatMessage, position, progressBar, scaleImageView);
        }

    }

    //下载头像
    public void downloadHeadImage(GroupMember groupMember, CircleImageView headCiv) {
        if (TextUtils.isEmpty(groupMember.getAvatar()))
            return;
        boolean makeFolderState = utils.fileutil.FileUtils.makeFolders(fileForderPath);
        File cameraFile = new File(fileForderPath, groupMember.getUserId() + ".jpg");
        OkGo.<File>get(groupMember.getAvatar())
                .execute(new FileCallback(fileForderPath, groupMember.getUserId() + ".jpg") {
                    @Override
                    public void onSuccess(Response<File> response) {
                        File file = response.body();
                        String imagePath = file.getAbsolutePath();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(MyApplication.getContext())
                                        .load(imagePath)
                                        .error(R.drawable.head)
                                        .into(headCiv);
                            }
                        }, 200);
                    }

                    @Override
                    public void onError(Response<File> response) {
                    }
                });
    }

    public boolean headImageIsExist(String targetId) {
        File headImage = new File(fileForderPath, targetId + ".jpg");
        if (headImage.exists())
            return true;
        else
            return false;
    }

    public GroupMember getSenderInfo(String targetId) {
        if (members.size() == 0)
            return null;
        else {
            for (GroupMember member : members) {
                if (member.getUserId().equals(targetId))
                    return member;
            }
        }
        return null;
    }

    public String senderImagePath(String targetId) {
        return fileForderPath + File.separator + targetId + ".jpg";
    }

    //上传语音文件
    public void reUploadVoice(ChatMessage chatMessage, final int position, ImageView errorIv, ProgressBar loadPb, ReSendMessageCallback callback) {
        final File file = new File(chatMessage.getLocal_path());
        boolean hasNetWork = SharedDataTool.getBoolean(mContext, "network");
        if (hasNetWork) {
            OkGo.<String>get(Constants.BaseUrl + "/getToken")
                    .headers("Authorization", userInfo.getToken())
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {
                            Log.e("TAG", response.body());
                            com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(response.body());
                            com.alibaba.fastjson.JSONObject dataObject = JSON.parseObject(jsonObject.getString("data"));
                            String token = dataObject.getString("token");
                            uploadAudioToQiNiu(chatMessage.getBackId(), token, file, chatMessage, position, callback);
                        }

                        @Override
                        public void onError(Response<String> response) {
                            super.onError(response);
                            Log.e("TAG", response.getException().getMessage());
                        }
                    });
        } else {
            errorIv.setVisibility(View.VISIBLE);
            loadPb.setVisibility(View.GONE);
        }
    }

    //时间相关
    private String formatTimeBase(long ts) {
        Log.e("DATE", "timestamp" + ts);
        String s = "";
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ts);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        String weeks[] = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        if (isToday(ts)) {
            s = String.format("%02d:%02d", hour, minute);
        } else if (isYesterday(ts)) {
            s = String.format("昨天 %02d:%02d", hour, minute);
        } else if (isInWeek(ts)) {
            s = String.format("%s %02d:%02d", weeks[dayOfWeek - 1], hour, minute);
        } else if (isInYear(ts)) {
            s = String.format("%02d-%02d %02d:%02d", month + 1, dayOfMonth, hour, minute);
        } else {
            s = String.format("%d-%02d-%02d %02d:%02d", year, month + 1, dayOfMonth, hour, minute);
        }
        Log.e("DATE", s);
        return s;
    }

    public int now() {
        Date date = new Date();
        long t = date.getTime();
        return (int) (t / 1000);
    }

    private boolean isToday(long ts) {
        int now = now();
        return isSameDay(now, ts);
    }

    private boolean isYesterday(long ts) {
        int now = now();
        int yesterday = now - 24 * 60 * 60;
        return isSameDay(ts, yesterday);
    }

    private boolean isInWeek(long ts) {
        int now = now();
        //6天前
        long day6 = now - 6 * 24 * 60 * 60;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(day6 * 1000);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        int zero = (int) (cal.getTimeInMillis() / 1000);
        return (ts >= zero);
    }

    private boolean isInYear(long ts) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ts * 1000);
        int year = cal.get(Calendar.YEAR);

        cal.setTime(new Date());
        int y = cal.get(Calendar.YEAR);

        return (year == y);
    }

    private boolean isSameDay(long ts1, long ts2) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ts1 * 1000);
        int year1 = cal.get(Calendar.YEAR);
        int month1 = cal.get(Calendar.MONTH);
        int day1 = cal.get(Calendar.DAY_OF_MONTH);


        cal.setTimeInMillis(ts2 * 1000);
        int year2 = cal.get(Calendar.YEAR);
        int month2 = cal.get(Calendar.MONTH);
        int day2 = cal.get(Calendar.DAY_OF_MONTH);

        return ((year1 == year2) && (month1 == month2) && (day1 == day2));
    }

    /*
       *计算time2减去time1的差值 差值只设置 几天 几个小时 或 几分钟
       * 根据差值返回多长之间前或多长时间后
       * */
    public boolean isDisplayTime(long time1, long time2) {
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        long diff;
        String flag;
        if (time1 < time2) {
            diff = time2 - time1;
            flag = "前";
        } else {
            diff = time1 - time2;
            flag = "后";
        }
        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        Log.e("DATE", "day:" + day + "hour:" + hour + "min:" + min + "sec:" + sec);
        if (day != 0)
            return true;
        if (hour != 0)
            return true;
        if (min >= 5)
            return true;
        return false;
    }

    //上传图片到七牛云
    public void uploadAudioToQiNiu(String fileName, String qiNiuToken, File file, ChatMessage message, int position, ReSendMessageCallback callback) {
        UploadManager uploadManager = new UploadManager();
        uploadManager.put(file, fileName, qiNiuToken, new UpCompletionHandler() {
            @Override
            public void complete(String key, ResponseInfo info, org.json.JSONObject response) {
                if (info.isOK()) {
                    Log.e("qiniu", "Upload Success" + response.toString());
                    try {
                        String hash = response.getString("hash");
                        long size = info.totalSize;
                        GroupChatActivity chatActivity = (GroupChatActivity) mContext;
                        chatActivity.reSendVoiceMessage(message, position, callback, hash, size);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                }

            }

        }, new UploadOptions(null, null, false, new UpProgressHandler() {
            @Override
            public void progress(String key, double percent) {
            }
        }, null));
    }

}
