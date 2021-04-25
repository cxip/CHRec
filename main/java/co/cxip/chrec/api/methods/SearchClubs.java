package co.cxip.chrec.api.methods;

import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.UserOrClub;

public class SearchClubs extends ClubhouseAPIRequest<SearchClubs.Response>{
	public SearchClubs(boolean cofollows_only, boolean following_only, boolean followers_only, String query){
		super("POST", "search_clubs", Response.class);
		requestBody=new SearchClubs.Body(cofollows_only, following_only, followers_only, query);
	}

	public static class Response{
		public List<UserOrClub> clubs;
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
