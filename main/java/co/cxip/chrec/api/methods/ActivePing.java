package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.ClubhouseAPIRequest;

public class ActivePing extends ClubhouseAPIRequest<ActivePing.Response>{
	public ActivePing(String channel){
		super("POST", "active_ping", Response.class);
		requestBody=new Body(channel);
	}

	public static class Response{
		public boolean shouldLeave;
	}

	private static class Body{
		public String channel;

		public Body(String channel){
			this.channel=channel;
		}
	}
}
