package co.cxip.chrec.api;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import me.grishka.appkit.api.ErrorResponse;
import co.cxip.chrec.BuildConfig;
import co.cxip.chrec.R;

public class ClubhouseErrorResponse extends ErrorResponse{

	public String message;
	public int code;

	public ClubhouseErrorResponse(int code, String message){
		this.code=code;
		this.message=message;
	}

	public ClubhouseErrorResponse(BaseResponse br){
		message=br.errorMessage;
	}

	@Override
	public void bindErrorView(View view){
		TextView txt=view.findViewById(R.id.error_text);
		if(code>0) {
			if(message!=null && message.length()>0) {
				txt.setText(view.getContext().getString(R.string.error_loading_code_reason, code, message));
			}else{
				txt.setText(view.getContext().getString(R.string.error_loading_code, code));
			}
		}else{
			txt.setText(R.string.error_loading);
		}
		/*if(BuildConfig.DEBUG)
			txt.setText(view.getContext().getString(R.string.error_loading)+":\n"+message);
		else
			txt.setText(R.string.error_loading);*/
	}

	@Override
	public void showToast(Context context){
		if(BuildConfig.DEBUG)
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		else
			Toast.makeText(context, R.string.error_loading, Toast.LENGTH_SHORT).show();
	}
}
