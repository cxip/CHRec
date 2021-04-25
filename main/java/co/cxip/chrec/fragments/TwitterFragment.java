package co.cxip.chrec.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import java.util.HashMap;

import co.cxip.chrec.App;
import co.cxip.chrec.BuildConfig;
import co.cxip.chrec.R;
import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIController;
import co.cxip.chrec.api.ClubhouseSession;
import co.cxip.chrec.api.methods.UpdateInstagram;
import co.cxip.chrec.api.methods.UpdateTwitter;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.APIRequest;
import me.grishka.appkit.api.ErrorResponse;
import retrofit2.Call;

public class TwitterFragment extends BaseToolbarFragment {
	private TwitterAuthClient twitterAuthClient;

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
	}

	@Override
	public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view=inflater.inflate(R.layout.auth_instagram, container, false);

		TwitterConfig config = new TwitterConfig.Builder(getActivity())
				.logger(new DefaultLogger(Log.DEBUG))
				.twitterAuthConfig(new TwitterAuthConfig(getResources().getString(R.string.twitter_api_key), getResources().getString(R.string.twitter_api_secret)))
				.debug(true)
				.build();
		Twitter.initialize(config);

		twitterAuthClient = new TwitterAuthClient();

		TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();

		if (twitterSession == null) {
			twitterAuthClient.authorize(getActivity(), new Callback<TwitterSession>() {
				@Override
				public void success(Result<TwitterSession> result) {
					Log.d("Twitter", "here");
					TwitterSession twitterSession = result.data;
					getTwitterData(twitterSession);
				}
				@Override
				public void failure(TwitterException e) {
					Log.e("Twitter", "Failed to authenticate user " + e.getMessage());
				}
			});
		} else {
			getTwitterData(twitterSession);
		}
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
			if (url.startsWith(UpdateInstagram.REDIRECT_INSTAGRAM_URL)) { // redirect uri!
				// last2 chars is #_ by docs https://developers.facebook.com/docs/instagram-basic-display-api/getting-started
				String code = url.substring((UpdateInstagram.REDIRECT_INSTAGRAM_URL+ "?code=").length(), url.length()-2);
				new UpdateInstagram(code)
						.wrapProgress(getActivity())
						.setCallback(new me.grishka.appkit.api.Callback<BaseResponse>(){
							@Override
							public void onSuccess(BaseResponse result){
								Nav.finish(TwitterFragment.this);
							}
							@Override
							public void onError(ErrorResponse error){
								error.showToast(App.applicationContext);
								Nav.finish(TwitterFragment.this);
							}
						}).exec();
				return false; // don’t load the page
			}
			return true;
		}
	}

	private void getTwitterData(final TwitterSession twitterSession) {
		TwitterApiClient twitterApiClient = new TwitterApiClient(twitterSession);
		final Call<User> getUserCall = twitterApiClient.getAccountService().verifyCredentials(true, false, true);
		getUserCall.enqueue(new Callback<User>() {
			@Override
			public void success(Result<User> result) {

				String socialId = "", firstName = "", lastName = "", gender = "", birthday = "", email = "", picture = "";

				User user = result.data;
				socialId = user.idStr;
				email = user.email;
            /*picture = user.profileImageUrlHttps.replace("_normal", "");
            firstName = user.name;
            lastName = user.screenName;*/

				try {
					firstName = user.name.split(" ")[0];
					lastName = user.name.split(" ")[1];
				} catch (Exception e) {
					firstName = user.name;
					lastName = "";
				}

				Log.e("Twitter", "SocialId: " + socialId + "\tFirstName: " + firstName + "\tLastName: " + lastName + "\tEmail: " + email);
			}

			@Override
			public void failure(TwitterException exception) {
				Log.e("Twitter", "Failed to get user data " + exception.getMessage());
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (twitterAuthClient != null) {
			twitterAuthClient.onActivityResult(requestCode, resultCode, data);
		}
	}
}
