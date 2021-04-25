package co.cxip.chrec.api.methods;

import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.Club;
import co.cxip.chrec.api.model.User;
import co.cxip.chrec.api.model.UserOrClub;

public class SearchUsers extends ClubhouseAPIRequest<SearchUsers.Response>{
	public SearchUsers(boolean cofollows_only, boolean following_only, boolean followers_only, String query){
		super("POST", "search_users", Response.class);
		requestBody=new SearchUsers.Body(cofollows_only, following_only, followers_only, query);
	}

	public static class Response{
		public List<UserOrClub> users;
		public int count;
	}

	public static class Body{
		public boolean cofollows_only;
		public boolean following_only;
		public boolean followers_only;
		public String query;

		public Body(boolean cofollows_only, boolean following_only, boolean followers_only, String query){
			this.cofollows_only=cofollows_only;
			this.following_only=following_only;
			this.followers_only=followers_only;
			this.query=query;
		}
	}
}
