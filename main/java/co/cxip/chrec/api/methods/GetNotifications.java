package co.cxip.chrec.api.methods;

import java.util.HashMap;
import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.Notification;

public class GetNotifications extends ClubhouseAPIRequest<GetNotifications.Response>{
	public GetNotifications(int userID, int pageSize, int page){
		super("GET", "get_notifications", Response.class);
		queryParams=new HashMap<>();
		queryParams.put("user_id", userID+"");
		queryParams.put("page_size", pageSize+"");
		queryParams.put("page", page+"");
	}

	public static class Response{
		public List<Notification> notifications;
		public int count;
	}
}
