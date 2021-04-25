package co.cxip.chrec.api.methods;

import java.util.List;
import java.util.TimeZone;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.Club;
import co.cxip.chrec.api.model.User;

public class Me extends ClubhouseAPIRequest<Me.Response> {
    public Me() {
        super("POST", "me", Response.class);
        requestBody = new Body();
    }

    private static class Body {
        public boolean return_blocked_ids;
        public String timezone_identifier;
        public boolean return_following_ids;

        public Body() {
            this.return_blocked_ids = true;
            this.return_following_ids = true;
            this.timezone_identifier = TimeZone.getDefault().getDisplayName();
        }
    }

    public static class Response {
        public int num_invites;
        public boolean has_unread_notifications;
        public int actionable_notifications_count;
        public boolean notifications_enabled;
        public User userProfile;
        public List<Integer> following_ids;
        public List<Integer> blocked_ids;
        public boolean is_admin;
        public String email;
        public List<String> feature_flags;
    }
}
