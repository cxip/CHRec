package co.cxip.chrec.api.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ChannelUser implements Parcelable {
	public int userId;
	public String name;
	public String photoUrl;
	public String username;
	public int lastActiveMinutes;
	public String channel;
	public boolean isSpeaker;
	public String topic;
	public boolean isModerator;
	public boolean isFollowedBySpeaker;
	public boolean isInvitedAsSpeaker;
	public boolean isNew;
	public String timeJoinedAsSpeaker;
	public String firstName;
	//public transient boolean isMuted;
	public boolean isMuted;


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
		dest.writeByte(this.isSpeaker ? (byte) 1 : (byte) 0);
		dest.writeByte(this.isModerator ? (byte) 1 : (byte) 0);
		dest.writeByte(this.isFollowedBySpeaker ? (byte) 1 : (byte) 0);
		dest.writeByte(this.isInvitedAsSpeaker ? (byte) 1 : (byte) 0);
		dest.writeByte(this.isNew ? (byte) 1 : (byte) 0);
		dest.writeString(this.timeJoinedAsSpeaker);
		dest.writeString(this.firstName);
	}

	public void readFromParcel(Parcel source){
		this.userId=source.readInt();
		this.name=source.readString();
		this.photoUrl=source.readString();
		this.username=source.readString();
		this.isSpeaker=source.readByte()!=0;
		this.isModerator=source.readByte()!=0;
		this.isFollowedBySpeaker=source.readByte()!=0;
		this.isInvitedAsSpeaker=source.readByte()!=0;
		this.isNew=source.readByte()!=0;
		this.timeJoinedAsSpeaker=source.readString();
		this.firstName=source.readString();
	}

	public ChannelUser(){
	}

	protected ChannelUser(Parcel in){
		this.userId=in.readInt();
		this.name=in.readString();
		this.photoUrl=in.readString();
		this.username=in.readString();
		this.isSpeaker=in.readByte()!=0;
		this.isModerator=in.readByte()!=0;
		this.isFollowedBySpeaker=in.readByte()!=0;
		this.isInvitedAsSpeaker=in.readByte()!=0;
		this.isNew=in.readByte()!=0;
		this.timeJoinedAsSpeaker=in.readString();
		this.firstName=in.readString();
	}

	public static final Creator<ChannelUser> CREATOR=new Creator<ChannelUser>(){
		@Override
		public ChannelUser createFromParcel(Parcel source){
			return new ChannelUser(source);
		}

		@Override
		public ChannelUser[] newArray(int size){
			return new ChannelUser[size];
		}
	};
}
