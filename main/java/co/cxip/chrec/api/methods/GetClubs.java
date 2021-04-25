package co.cxip.chrec.api.methods;

import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.Channel;
import co.cxip.chrec.api.model.Club;

public class GetClubs extends ClubhouseAPIRequest<GetClubs.Response>{
	public GetClubs(boolean isStartableOnly){
		super("POST", "get_clubs", Response.class);
		requestBody=new Body(isStartableOnly);
	}

	private static class Body {
		public boolean isStartableOnly;

		public Body(boolean isStartableOnly) {
			this.isStartableOnly=isStartableOnly;
		}
	}

	public static class Response{
		public List<Club> clubs;
	}
}
