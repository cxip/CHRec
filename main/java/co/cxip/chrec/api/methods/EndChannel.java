package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class EndChannel extends ClubhouseAPIRequest<BaseResponse>{
	public EndChannel(String channelName, long channelId){
		super("POST", "end_channel", BaseResponse.class);
		requestBody=new Body(channelName, channelId);
	}

	private static class Body{
		public String channel;
		public long channelId;

		public Body(String channel, long channelId){
			this.channel=channel;
			this.channelId=channelId;
		}
	}
}
