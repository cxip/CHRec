package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.ClubhouseAPIRequest;

public class InstagramAuth extends ClubhouseAPIRequest<InstagramAuth.Response> {

    public InstagramAuth(String client_id, String client_secret, String grant_type, String redirect_uri, String code) {
        super("POST", "access_token", "https://api.instagram.com/oauth/", Response.class);
        requestBody = new InstagramAuth.Body(client_id, client_secret, grant_type, redirect_uri, code);
    }

    public static class Body {
        public String client_id;
        public String client_secret;
        public String grant_type;
        public String redirect_uri;
        public String code;

        public Body(String client_id, String client_secret, String grant_type, String redirect_uri, String code) {
            this.client_id = client_id;
            this.client_secret = client_secret;
            this.grant_type = grant_type;
            this.redirect_uri = redirect_uri;
            this.code = code;
        }
    }

    public static class Response {
        public String access_token;
        public String user_id;
    }
}