package co.cxip.chrec.api.methods;

import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.Club;
import co.cxip.chrec.api.model.FullUser;
import co.cxip.chrec.api.model.Topic;

public class GetClub extends ClubhouseAPIRequest<GetClub.Response>{
	public GetClub(int id, String topicid){
		super("POST", "get_club", Response.class);
		requestBody=new Body(id, topicid);
	}

	private static class Body{
		public int clubId;
		public String source_topic_id;

		public Body(int clubId, String sourceTopicId){
			this.clubId=clubId;
			this.source_topic_id=sourceTopicId;
		}
	}

	public static class Response{
		public Club club;
		public boolean isAdmin;
		public boolean isMember;
		public boolean isFollower;
		public boolean isPendingAccept;
		public boolean isPendingApproval;
		public List<Topic> topics;
	}
}
