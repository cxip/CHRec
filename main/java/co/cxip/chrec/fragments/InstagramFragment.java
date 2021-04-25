package co.cxip.chrec.fragments;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.HashMap;

import co.cxip.chrec.App;
import co.cxip.chrec.BuildConfig;
import co.cxip.chrec.R;
import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIController;
import co.cxip.chrec.api.ClubhouseSession;
import co.cxip.chrec.api.methods.CallPhoneNumberAuth;
import co.cxip.chrec.api.methods.CompletePhoneNumberAuth;
import co.cxip.chrec.api.methods.InstagramAuth;
import co.cxip.chrec.api.methods.ResendPhoneNumberAuth;
import co.cxip.chrec.api.methods.StartPhoneNumberAuth;
import co.cxip.chrec.api.methods.UpdateInstagram;
import co.cxip.chrec.api.methods.UpdateNotifications;
import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.AppKitFragment;
import me.grishka.appkit.fragments.LoaderFragment;

public class InstagramFragment extends BaseToolbarFragment {

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
	}

	@Override
	public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view=inflater.inflate(R.layout.auth_instagram, container, false);

		HashMap<String, String> headers = new HashMap<>();
		headers.put("CH-AppBuild", ClubhouseAPIController.API_BUILD_ID);
		headers.put("CH-AppVersion", ClubhouseAPIController.API_BUILD_VERSION);
		headers.put("User-Agent", ClubhouseAPIController.API_UA);
		headers.put("CH-DeviceId", ClubhouseSession.deviceID);
		headers.put("Authorization", "Token "+ClubhouseSession.userToken);
		headers.put("CH-UserID", ClubhouseSession.userID);

		String url="https://www.instagram.com/oauth/authorize/?client_id=" +
				BuildConfig.INSTAGRAM_APP_ID + "&redirect_uri="+UpdateInstagram.REDIRECT_INSTAGRAM_URL+"&response_type=code&scope=user_profile";
		WebView webView;
		webView=view.findViewById(R.id.web_view);
		webView.setWebViewClient(new CHWebViewClient());
		webView.getSettings().setLoadsImagesAutomatically(true);
		webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		webView.loadUrl(url, headers);
		return view;
	}

	private class CHWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			Boolean redirect = checkRedirect(request.getUrl().toString());
			view.loadUrl(request.getUrl().toString());
			return redirect;
		}
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Boolean redirect = checkRedirect(url);
			view.loadUrl(url);
			return redirect;
		}
		private Boolean checkRedirect(String url){
			if (url.startsWith(UpdateInstagram.REDIRECT_INSTAGRAM_URL+"?code=")) { // redirect uri!
				// last2 chars is #_ by docs https://developers.facebook.com/docs/instagram-basic-display-api/getting-started
				String code = url.substring((UpdateInstagram.REDIRECT_INSTAGRAM_URL+"?code=").length(), url.length()-2);
				new UpdateInstagram(code)
						.wrapProgress(getActivity())
						.setCallback(new Callback<BaseResponse>(){
							@Override
							public void onSuccess(BaseResponse result){
								Nav.finish(InstagramFragment.this);
							}
							@Override
							public void onError(ErrorResponse error){
								error.showToast(App.applicationContext);
								Nav.finish(InstagramFragment.this);
							}
						}).exec();
				return false; // don’t load the page
			}
			return true;
		}
	}
}
