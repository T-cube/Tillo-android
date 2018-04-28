package bean;

/**
 * messageEvent eventBus传递对象
 */

public class MessageEvent {
    private String messageType;
    private String message;

    public MessageEvent(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
