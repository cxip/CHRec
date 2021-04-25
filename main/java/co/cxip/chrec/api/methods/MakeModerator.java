package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class MakeModerator extends ClubhouseAPIRequest<BaseResponse>{
	public MakeModerator(String channel, int userID){
		super("POST", "make_moderator", BaseResponse.class);
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
