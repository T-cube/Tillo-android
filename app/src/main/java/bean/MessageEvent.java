package bean;

/**
 * messageEvent eventBus传递对象
 */

public class MessageEvent {
    private String target;//接收对象
    private String behavior;//行为
    private ChatMessage chatMessage;//消息对象

    public MessageEvent() {
    }

    public MessageEvent(String target, String behavior, ChatMessage chatMessage) {
        this.target = target;
        this.behavior = behavior;
        this.chatMessage = chatMessage;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getBehavior() {
        return behavior;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
