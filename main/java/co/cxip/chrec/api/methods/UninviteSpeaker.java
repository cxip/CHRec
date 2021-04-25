package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class UninviteSpeaker extends ClubhouseAPIRequest<BaseResponse>{
	public UninviteSpeaker(String channel, int userID){
		super("POST", "uninvite_speaker", BaseResponse.class);
		requestBody=new Body(channel, userID);
	}

	private static class Body{
		public String channel;
		public int userId;

		public Body(String channel, int userId){
			this.channel=channel;
			this.userId=userId;
		}
	}
}
