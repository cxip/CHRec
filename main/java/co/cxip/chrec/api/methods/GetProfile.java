package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.FullUser;

public class GetProfile extends ClubhouseAPIRequest<GetProfile.Response>{
	public GetProfile(int id){
		super("POST", "get_profile", Response.class);
		requestBody=new Body(id);
	}

	private static class Body{
		public int userId;

		public Body(int userId){
			this.userId=userId;
		}
	}

	public static class Response{
		public FullUser userProfile;
	}
}
