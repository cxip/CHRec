package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class FollowClub extends ClubhouseAPIRequest<BaseResponse>{
	public FollowClub(int clubID, String sourceTopicId){
		super("POST", "follow_club", BaseResponse.class);
		requestBody=new Body(clubID, sourceTopicId);
	}

	private static class Body{
		public int clubId;
		String sourceTopicId;

		public Body(int clubId, String sourceTopicId){
			this.clubId=clubId;
			this.sourceTopicId=sourceTopicId;
		}
	}
}
