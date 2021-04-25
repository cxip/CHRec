package co.cxip.chrec.api.methods;

import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.Channel;
import co.cxip.chrec.api.model.FullUser;

public class CreateChannel extends ClubhouseAPIRequest<Channel>{
	public CreateChannel(boolean is_social_media, boolean is_private, String club_id, List<Integer> user_ids, String event_id, String topic){
		super("POST", "create_channel", Channel.class);
		requestBody=new Body(is_social_media, is_private, club_id, user_ids, event_id, topic);
	}

	public static class Body{
		public boolean is_social_media;
		public boolean is_private;
		public String club_id;
		public List<Integer> user_ids;
		public String event_id;
		public String topic;

		public Body(boolean is_social_media, boolean is_private, String club_id, List<Integer> user_ids, String event_id, String topic){
			this.is_social_media=is_social_media;
			this.is_private=is_private;
			this.club_id=club_id;
			this.user_ids=user_ids;
			this.event_id=event_id;
			this.topic=topic;
		}
	}
}
