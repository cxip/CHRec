package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class AcceptSpeakerInvite extends ClubhouseAPIRequest<BaseResponse>{
	public AcceptSpeakerInvite(String channel, int userID){
		super("POST", "accept_speaker_invite", BaseResponse.class);
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
