package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class UpdateTwitter extends ClubhouseAPIRequest<BaseResponse> {
    public static String REDIRECT_TWITTER_URL = "https://www.joinclubhouse.com/callback/twitter";

    public UpdateTwitter(String username, String twitter_token, String twitter_secret) {
        super("POST", "update_twitter_username", BaseResponse.class);
        requestBody = new UpdateTwitter.Body(username, twitter_token, twitter_secret);
    }

    private static class Body {
        public String username;
        public String twitterToken;
        public String twitterSecret;

        public Body(String username, String twitter_token, String twitter_secret) {
            this.username = username;
            this.twitterToken = twitter_token;
            this.twitterSecret = twitter_secret;
        }
    }

}