package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class UnfollowClub extends ClubhouseAPIRequest<BaseResponse>{
	public UnfollowClub(int clubID, String sourceTopicId){
		super("POST", "unfollow_club", BaseResponse.class);
		requestBody=new UnfollowClub.Body(clubID, sourceTopicId);
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
