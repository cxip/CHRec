package co.cxip.chrec.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class UserOrClub implements Parcelable{
	public int userId;
	public String name;
	public String photoUrl;
	public String username;
	public int lastActiveMinutes;

	public int clubId;
	public String description;
	public int num_members;
	public int num_followers;
	public boolean enable_private;
	public boolean is_follow_allowed;
	public boolean is_membership_private;
	public boolean is_community;
	public List<Rule> rules;
	public String url;
	public long num_online;

	public boolean isUser;

	@Override
	public int describeContents(){
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags){
		dest.writeInt(this.userId);
		dest.writeString(this.name);
		dest.writeString(this.photoUrl);
		dest.writeString(this.username);
	}

	public void readFromParcel(Parcel source){
		this.userId=source.readInt();
		this.name=source.readString();
		this.photoUrl=source.readString();
		this.username=source.readString();
	}

	public UserOrClub(){
	}

	protected UserOrClub(Parcel in){
		this.userId=in.readInt();
		this.name=in.readString();
		this.photoUrl=in.readString();
		this.username=in.readString();
	}

	public static final Creator<UserOrClub> CREATOR=new Creator<UserOrClub>(){
		@Override
		public UserOrClub createFromParcel(Parcel source){
			return new UserOrClub(source);
		}

		@Override
		public UserOrClub[] newArray(int size){
			return new UserOrClub[size];
		}
	};
}
