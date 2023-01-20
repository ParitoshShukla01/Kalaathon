package kalaathon.com.models;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Video implements Parcelable {

    private String caption;
    private String date_created;
    private String video_path;
    private String video_id;
    private String user_id;
    private String category;
    private List<Like> likes;
    private String contest;
    private String thumbnail;


    public Video() {

    }

    public Video(String caption, String date_created, String video_path, String video_id,
                 String user_id, String category, List<Like> likes, String contest,String thumbnail) {
        this.caption = caption;
        this.date_created = date_created;
        this.video_path = video_path;
        this.video_id = video_id;
        this.user_id = user_id;
        this.category = category;
        this.likes = likes;
        this.contest = contest;
        this.thumbnail=thumbnail;
    }

    protected Video(Parcel in) {
        caption = in.readString();
        date_created = in.readString();
        video_path = in.readString();
        video_id = in.readString();
        user_id = in.readString();
        category = in.readString();
        thumbnail=in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(caption);
        dest.writeString(date_created);
        dest.writeString(video_path);
        dest.writeString(video_id);
        dest.writeString(user_id);
        dest.writeString(category);
        dest.writeString(thumbnail);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    public String getContest() {
        return contest;
    }

    public void setContest(String contest) {
        this.contest = contest;
    }

    public static Creator<Video> getCREATOR() {
        return CREATOR;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getVideo_path() {
        return video_path;
    }

    public void setVideo_path(String video_path) {
        this.video_path = video_path;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String photo_id) {
        this.video_id = photo_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public String toString() {
        return "Video{" +
                "caption='" + caption + '\'' +
                ", date_created='" + date_created + '\'' +
                ", video_path='" + video_path + '\'' +
                ", video_id='" + video_id + '\'' +
                ", user_id='" + user_id + '\'' +
                ", category='" + category + '\'' +
                ", likes=" + likes +
                ", contest='" + contest + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                '}';
    }
}
