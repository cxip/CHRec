package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class AcceptClubMemberInvite extends ClubhouseAPIRequest<BaseResponse>{
	public AcceptClubMemberInvite(int clubId, int sourceTopicId){
		super("POST", "accept_club_member_invite", BaseResponse.class);
		requestBody=new Body(clubId, sourceTopicId);
	}

	private static class Body{
		public int clubId;
		public int sourceTopicId;

		public Body(int clubId, int sourceTopicId){
			this.clubId=clubId;
			this.sourceTopicId=sourceTopicId;
		}
	}
}
