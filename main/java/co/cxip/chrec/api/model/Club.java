package co.cxip.chrec.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Club implements Parcelable{
	public int clubId;
	public String name;
	public String description;
	public String photoUrl;
	public int numMembers;
	public int numFollowers;
	public boolean enablePrivate;
	public boolean isFollowAllowed;
	public boolean isMembershipPrivate;
	public boolean isCommunity;
	public List<Rule> rules;
	public String url;
	public long num_online;

	@Override
	public int describeContents(){
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags){
		dest.writeInt(this.clubId);
		dest.writeString(this.name);
		dest.writeString(this.photoUrl);
		dest.writeString(this.description);
	}

	public void readFromParcel(Parcel source){
		this.clubId=source.readInt();
		this.name=source.readString();
		this.photoUrl=source.readString();
		this.description=source.readString();
	}

	public Club(){
	}

	protected Club(Parcel in){
		this.clubId=in.readInt();
		this.name=in.readString();
		this.photoUrl=in.readString();
		this.description=in.readString();
	}

	public static final Creator<Club> CREATOR=new Creator<Club>(){
		@Override
		public Club createFromParcel(Parcel source){
			return new Club(source);
		}

		@Override
		public Club[] newArray(int size){
			return new Club[size];
		}
	};
}
