package co.cxip.chrec.api.methods;

import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.Topic;

public class GetAllTopics extends ClubhouseAPIRequest<GetAllTopics.Response>{
	public GetAllTopics(){
		super("GET", "get_all_topics", Response.class);
	}

	public static class Response{
		public List<Topic> topics;
		public boolean success;
	}
}
