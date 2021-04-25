package co.cxip.chrec.api.methods;

import java.util.HashMap;

import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIRequest;

public class CheckForUpdate extends ClubhouseAPIRequest<BaseResponse>{
	public CheckForUpdate(){
		super("GET", "check_for_update", BaseResponse.class);
		queryParams=new HashMap<>();
		queryParams.put("is_testflight", "0");
	}
}
