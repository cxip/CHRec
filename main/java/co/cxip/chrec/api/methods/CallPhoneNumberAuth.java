package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class CallPhoneNumberAuth extends ClubhouseAPIRequest<BaseResponse>{
	public CallPhoneNumberAuth(String phoneNumber){
		super("POST", "call_phone_number_auth", BaseResponse.class);
		requestBody=new Body(phoneNumber);
	}

	private static class Body{
		public String phoneNumber;

		public Body(String phoneNumber){
			this.phoneNumber=phoneNumber;
		}
	}
}
