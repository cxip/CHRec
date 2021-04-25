package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class RemoveUserTopic extends ClubhouseAPIRequest<BaseResponse>{
	public RemoveUserTopic(int clubId, int topicId){
		super("POST", "remove_user_topic", BaseResponse.class);
		requestBody=new Body(clubId, topicId);
	}

	private static class Body{
		public int clubId;
		public int topicId;

		public Body(int clubId, int sourceTopicId){
			this.clubId=clubId;
			this.topicId=sourceTopicId;
		}
	}
}
