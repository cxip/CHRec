package co.cxip.chrec.api.methods;

import java.util.HashMap;
import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.Notification;

public class GetActionableNotifications extends ClubhouseAPIRequest<GetActionableNotifications.Response>{
	public GetActionableNotifications(){
		super("POST", "get_actionable_notifications", Response.class);
	}

	public static class Response{
		public List<Notification> notifications;
		public int count;
	}
}
