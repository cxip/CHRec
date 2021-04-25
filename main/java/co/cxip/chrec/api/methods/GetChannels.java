package co.cxip.chrec.api.methods;

import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.Channel;

public class GetChannels extends ClubhouseAPIRequest<GetChannels.Response>{
	public GetChannels(){
		super("GET", "get_channels", Response.class);
	}

	public static class Response{
		public List<Channel> channels;
	}
}
