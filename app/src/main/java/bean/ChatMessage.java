package bean;


import com.alibaba.fastjson.annotation.JSONField;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * 聊天消息 实体类
 */

public class ChatMessage extends DataSupport implements Serializable {
    @JSONField(name = "messageId")
    private String messageId;
    private String content;
    @Column(nullable = false)
    private String roomId;
    private long timestamp;
    private String type;
    private String local_path;
    private String sourceId;
    private String duration;
    @Column(ignore = true)
    private boolean isUpOrDownLoad = false;
    private String image_path;//大图
    private int sendState = 1;//0、发送中 1、发送成功 2、发送失败
    private String backId;//标识消息
    @JSONField(name = "filename")
    private String fileName;
    private boolean isRead;
    @Column(ignore = true)
    private boolean isDisplayTime = false;
    @Column(ignore = true)
    private String descrpiton;
    @Column(ignore = true)
    private boolean playing;
    private String sender;
    private String receiver;

    public String getDescrpiton() {
        return descrpiton;
    }

    public void setDescrpiton(String descrpiton) {
        this.descrpiton = descrpiton;
    }

    public boolean isDisplayTime() {
        return isDisplayTime;
    }

    public void setDisplayTime(boolean displayTime) {
        isDisplayTime = displayTime;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public boolean isUpOrDownLoad() {
        return isUpOrDownLoad;
    }

    public void setUpOrDownLoad(boolean upOrDownLoad) {
        boolean old = this.isUpOrDownLoad;
        isUpOrDownLoad = upOrDownLoad;
        changeSupport.firePropertyChange("upOrDownLoad", old, this.isUpOrDownLoad);
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public String getRoomid() {
        return roomId;
    }

    public void setRoomid(String roomId) {
        this.roomId = roomId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocal_path() {
        return local_path;
    }

    public void setLocal_path(String local_path) {
        this.local_path = local_path;
    }


    public int getSendState() {
        return sendState;
    }

    public void setSendState(int sendState) {
        this.sendState = sendState;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getBackId() {
        return backId;
    }

    public void setBackId(String backId) {
        this.backId = backId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public PropertyChangeSupport getChangeSupport() {
        return changeSupport;
    }

    public void setChangeSupport(PropertyChangeSupport changeSupport) {
        this.changeSupport = changeSupport;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        boolean old = this.playing;
        this.playing = playing;
        changeSupport.firePropertyChange("playing", old, this.playing);
    }

    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(
            this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public String messageToString() {
        return "messageId" + messageId + "sender" + sender + "roomId" + roomId + "receiver" + receiver;
    }
}
