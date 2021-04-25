package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class InviteToChannel extends ClubhouseAPIRequest<InviteToChannel.Response> {

    public InviteToChannel(String channel, int user_id) {
        super("POST", "invite_to_existing_channel", Response.class);
        requestBody = new InviteToChannel.Body(channel, user_id);
    }

    private static class Body {
        public String channel;
        public int user_id;

        public Body(String channel, int user_id) {
            this.channel = channel;
            this.user_id = user_id;
        }
    }

    public static class Response {
        public boolean success;
        public boolean notifications_enabled;
        public String fallback_number_hash;
        public String fallback_message;
    }
}