package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class UpdateNotifications extends ClubhouseAPIRequest<BaseResponse>{
	public UpdateNotifications(int enableTrending, int pauseTill, boolean isSandbox, String apnToken, int systemEnabled, int frequency){
		super("POST", "update_notifications", BaseResponse.class);
		requestBody=new Body(enableTrending, pauseTill, isSandbox, apnToken, systemEnabled, frequency);
	}

	public static class Response{
	}

	private static class Body{
		public int enableTrending;
		public int pauseTill;
		public boolean isSandbox;
		public String apnToken;
		public int systemEnabled;
		public int frequency;

		public Body(int enableTrending, int pauseTill, boolean isSandbox, String apnToken, int systemEnabled, int frequency){
			this.enableTrending=enableTrending;
			this.pauseTill=pauseTill;
			this.isSandbox=isSandbox;
			this.apnToken=apnToken;
			this.systemEnabled=systemEnabled;
			this.frequency=frequency;
		}
	}
}
