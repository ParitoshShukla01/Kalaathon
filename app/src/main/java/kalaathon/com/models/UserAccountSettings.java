package kalaathon.com.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserAccountSettings implements Parcelable {

    private String description="def";
    private String display_name;
    private String profile_photo;
    private String username;
    private String user_id;
    private String email_id;

    @Override
    public String toString() {
        return "UserAccountSettings{" +
                "description='" + description + '\'' +
                ", display_name='" + display_name + '\'' +
                ", profile_photo='" + profile_photo + '\'' +
                ", username='" + username + '\'' +
                ", user_id='" + user_id + '\'' +
                ", email_id='" + email_id + '\'' +
                '}';
    }
    public UserAccountSettings() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail_id() {
        return email_id;
    }

    public void setEmail_id(String email_id) {
        this.email_id = email_id;
    }

    public static Creator<UserAccountSettings> getCREATOR() {
        return CREATOR;
    }

    public UserAccountSettings(String description, String display_name, String profile_photo, String username, String user_id, String email_id) {
        this.description = description;
        this.display_name = display_name;
        this.profile_photo = profile_photo;
        this.username = username;
        this.user_id = user_id;
        this.email_id = email_id;
    }

    protected UserAccountSettings(Parcel in) {
        description = in.readString();
        display_name = in.readString();
        profile_photo = in.readString();
        username = in.readString();
        user_id = in.readString();
        email_id = in.readString();
    }

    public static final Creator<UserAccountSettings> CREATOR = new Creator<UserAccountSettings>() {
        @Override
        public UserAccountSettings createFromParcel(Parcel in) {
            return new UserAccountSettings(in);
        }

        @Override
        public UserAccountSettings[] newArray(int size) {
            return new UserAccountSettings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(description);
        parcel.writeString(display_name);
        parcel.writeString(profile_photo);
        parcel.writeString(username);
        parcel.writeString(user_id);
        parcel.writeString(email_id);
    }
}
