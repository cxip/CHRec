package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class UpdateInstagram extends ClubhouseAPIRequest<BaseResponse> {
    public static String REDIRECT_INSTAGRAM_URL = "https://www.joinclubhouse.com/callback/instagram";

    public UpdateInstagram(String code) {
        super("POST", "update_instagram_username", BaseResponse.class);
        requestBody = new UpdateInstagram.Body(code);
    }

    private static class Body {
        public String code;

        public Body(String code) {
            this.code = code;
        }
    }

}