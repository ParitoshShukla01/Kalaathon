package kalaathon.com.models;

public class Trending {

    private int like;
    private String time;
    private String type;
    private String category;

    public Trending() {
    }

    public Trending(int like, String time, String type, String category) {
        this.like = like;
        this.time = time;
        this.type = type;
        this.category = category;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Trending{" +
                "like=" + like +
                ", time='" + time + '\'' +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
