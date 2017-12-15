package hangxu.finalproject.cs5520.hikerplus.model;

/**
 * Model class representing chatting message.
 */

public class Message {

    // The sender's user id
    private String userId;
    // The text content of the message

    private String receiverId;

    private String text;

    // The sender's user name
    private String userName;

    public Message() {

    }

    public Message(String userId, String receiverId, String text, String userName) {
        this.userId = userId;
        this.receiverId = receiverId;
        this.text = text;
        this.userName = userName;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
