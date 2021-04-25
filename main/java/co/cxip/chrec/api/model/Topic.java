package co.cxip.chrec.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Topic implements Parcelable {
    public String title;
    public int id;
    public String abbreviated_title;
    public List<Topic> topics;

    public Topic(String title, int i, String abbreviated_title, List<Topic> topics) {
        this.title=title;
        this.id=i;
        this.abbreviated_title=abbreviated_title;
        this.topics=topics;
    }

    protected Topic(Parcel in) {
        title = in.readString();
        id = in.readInt();
        abbreviated_title = in.readString();
        topics = in.createTypedArrayList(Topic.CREATOR);
    }

    public static final Creator<Topic> CREATOR = new Creator<Topic>() {
        @Override
        public Topic createFromParcel(Parcel in) {
            return new Topic(in);
        }

        @Override
        public Topic[] newArray(int size) {
            return new Topic[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeInt(id);
        parcel.writeString(abbreviated_title);
        parcel.writeList(topics);
    }

    public void readFromParcel(Parcel source) {
        this.title=source.readString();
        this.id=source.readInt();
        this.abbreviated_title=source.readString();
        this.topics=new ArrayList<>();
        source.readList(this.topics, Topic.class.getClassLoader());
    }
}