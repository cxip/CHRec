package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class ChangeHandraiseSettings extends ClubhouseAPIRequest<BaseResponse>{
	public ChangeHandraiseSettings(String channelName, boolean isEnabled, int handraisePermission){
		super("POST", "change_handraise_settings", BaseResponse.class);
		requestBody=new Body(channelName, isEnabled, handraisePermission);
	}

	private static class Body{
		public String channel;
		public boolean isEnabled;
		public int handraisePermission;

		public Body(String channel, boolean isEnabled, int handraisePermission) {
			this.channel=channel;
			this.isEnabled=isEnabled;
			this.handraisePermission=handraisePermission;
		}
	}
}
