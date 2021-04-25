package co.cxip.chrec.api.methods;

import co.cxip.chrec.api.ClubhouseAPIRequest;

public class CheckWaitlistStatus extends ClubhouseAPIRequest<CheckWaitlistStatus.Response>{
	public CheckWaitlistStatus(){
		super("POST", "check_waitlist_status", Response.class);
	}

	public static class Response{
		public boolean isWaitlisted, isOnboarding;
	}
}
