package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class UpdateBio extends ClubhouseAPIRequest<BaseResponse>{
	public UpdateBio(String bio){
		super("POST", "update_bio", BaseResponse.class);
		requestBody=new Body(bio);
	}

	private static class Body{
		public String bio;

		public Body(String bio){
			this.bio=bio;
		}
	}
}
