package co.cxip.chrec.api.methods;

import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.Notification;

public class GetSettings extends ClubhouseAPIRequest<GetSettings.Response>{
	public GetSettings(){
		super("GET", "get_settings", Response.class);
	}

	public static class Response{
		public boolean notificationsEnableTrending;
		public int notificationsFrequency;
		public boolean notificationsIsPaused;
		public boolean success;
	}
}
