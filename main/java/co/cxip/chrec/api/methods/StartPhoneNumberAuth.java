package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class StartPhoneNumberAuth extends ClubhouseAPIRequest<BaseResponse>{
	public StartPhoneNumberAuth(String phoneNumber){
		super("POST", "start_phone_number_auth", BaseResponse.class);
		requestBody=new Body(phoneNumber);
	}

	private static class Body{
		public String phoneNumber;

		public Body(String phoneNumber){
			this.phoneNumber=phoneNumber;
		}
	}
}
