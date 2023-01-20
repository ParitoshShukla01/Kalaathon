package kalaathon.com.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Contest implements Parcelable{

    private String value;
    private String text;
    private String code;

    public Contest() {
    }

    protected Contest(Parcel in) {
        value = in.readString();
        text = in.readString();
        code = in.readString();
    }

    public static final Creator<Contest> CREATOR = new Creator<Contest>() {
        @Override
        public Contest createFromParcel(Parcel in) {
            return new Contest(in);
        }

        @Override
        public Contest[] newArray(int size) {
            return new Contest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(value);
        parcel.writeString(text);
        parcel.writeString(code);
    }

    public Contest(String value, String text, String code) {
        this.value = value;
        this.text = text;
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static Creator<Contest> getCREATOR() {
        return CREATOR;
    }

    @Override
    public String toString() {
        return "Contest{" +
                "value='" + value + '\'' +
                ", text='" + text + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
