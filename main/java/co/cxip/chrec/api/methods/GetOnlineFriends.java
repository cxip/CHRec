package co.cxip.chrec.api.methods;

import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.Club;
import co.cxip.chrec.api.model.User;

public class GetOnlineFriends extends ClubhouseAPIRequest<GetOnlineFriends.Response>{
	public GetOnlineFriends(){
		super("POST", "get_online_friends", Response.class);
	}

	public static class Response{
		public List<Club> clubs;
		public List<User> users;
	}
}
