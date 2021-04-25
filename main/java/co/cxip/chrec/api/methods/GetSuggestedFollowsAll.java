package co.cxip.chrec.api.methods;

import java.util.HashMap;
import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.FullUser;
import co.cxip.chrec.api.model.UserOrClub;

public class GetSuggestedFollowsAll extends ClubhouseAPIRequest<GetSuggestedFollowsAll.Response>{
	public GetSuggestedFollowsAll(boolean in_onboarding, int pageSize, int page){
		super("GET", "get_suggested_follows_all", Response.class);
		queryParams=new HashMap<>();
		queryParams.put("in_onboarding", in_onboarding+"");
		queryParams.put("page_size", pageSize+"");
		queryParams.put("page", page+"");
	}

	public static class Response{
		public List<UserOrClub> users;
		public int count;
	}
}
