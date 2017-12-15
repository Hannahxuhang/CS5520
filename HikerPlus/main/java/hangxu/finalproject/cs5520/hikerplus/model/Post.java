package hangxu.finalproject.cs5520.hikerplus.model;

/**
 * Model class representing a user's post.
 */

public class Post {

    private String title;
    private String desc;
    private String image;
    private String timeStamp;
    private String userId;

    public Post() {

    }

    public Post(String title, String desc, String image, String timeStamp, String userId) {
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.timeStamp = timeStamp;
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
