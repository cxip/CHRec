package co.cxip.chrec.api.methods;

import java.util.HashMap;
import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.FullUser;

public class GetFollowers extends ClubhouseAPIRequest<GetFollowers.Response>{
	public GetFollowers(int userID, int pageSize, int page){
		super("GET", "get_followers", Response.class);
		queryParams=new HashMap<>();
		queryParams.put("user_id", userID+"");
		queryParams.put("page_size", pageSize+"");
		queryParams.put("page", page+"");
	}

	public static class Response{
		public List<FullUser> users;
		public int count;
	}
}
