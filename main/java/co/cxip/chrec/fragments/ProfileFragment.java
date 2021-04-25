package co.cxip.chrec.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.cxip.chrec.App;
import co.cxip.chrec.BuildConfig;
import co.cxip.chrec.MainActivity;
import co.cxip.chrec.api.ClubhouseAPIController;
import co.cxip.chrec.api.methods.GetAllTopics;
import co.cxip.chrec.api.methods.GetChannels;
import co.cxip.chrec.api.methods.GetClubs;
import co.cxip.chrec.api.methods.GetMutualFollowers;
import co.cxip.chrec.api.methods.GetSettings;
import co.cxip.chrec.api.methods.UpdateInstagram;
import co.cxip.chrec.api.methods.UpdateNotifications;
import co.cxip.chrec.api.methods.UpdateTwitter;
import co.cxip.chrec.api.methods.UpdateUsername;
import co.cxip.chrec.api.model.Club;
import co.cxip.chrec.api.model.Topic;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.AppKitFragment;
import me.grishka.appkit.fragments.LoaderFragment;
import me.grishka.appkit.imageloader.ViewImageLoader;
import co.cxip.chrec.R;
import co.cxip.chrec.VoiceService;
import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseSession;
import co.cxip.chrec.api.methods.Follow;
import co.cxip.chrec.api.methods.GetProfile;
import co.cxip.chrec.api.methods.Unfollow;
import co.cxip.chrec.api.methods.UpdateBio;
import co.cxip.chrec.api.methods.UpdatePhoto;
import co.cxip.chrec.api.methods.UpdateName;
import co.cxip.chrec.api.model.FullUser;
import me.grishka.appkit.views.UsableRecyclerView;
import retrofit2.Call;

public class ProfileFragment extends LoaderFragment {

	private static final int PICK_PHOTO_RESULT=468;

	private FullUser user;

	private TextView name, username, followers, following, followsYou, bio, inviteInfo, twitter, instagram, mutuals;
	private ImageView photo, inviterPhoto, imgView, mutualpic1, mutualpic2;
	private Button followBtn;
	private ImageButton filesBtn;
	private View socialButtons;
	private boolean self;
	private UsableRecyclerView usableRecyclerView;
	private LinearLayout clubsLayout,mutualLayout;
	private List<Topic> topics;
	private TwitterAuthClient twitterAuthClient;

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		loadData();
		self=getArguments().getInt("id")==Integer.parseInt(ClubhouseSession.userID);
		if(self)
			setHasOptionsMenu(true);
	}

	@Override
	public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v=inflater.inflate(R.layout.profile, container, false);
		name=v.findViewById(R.id.name);
		username=v.findViewById(R.id.username);
		followers=v.findViewById(R.id.followers);
		following=v.findViewById(R.id.following);
		followsYou=v.findViewById(R.id.follows_you);
		bio=v.findViewById(R.id.status);
		inviteInfo=v.findViewById(R.id.invite_info);
		photo=v.findViewById(R.id.photo);
		inviterPhoto=v.findViewById(R.id.inviter_photo);
		followBtn=v.findViewById(R.id.follow_btn);
		twitter=v.findViewById(R.id.twitter);
		instagram=v.findViewById(R.id.instagram);
		socialButtons=v.findViewById(R.id.social);
		filesBtn = v.findViewById(R.id.files);
		imgView = v.findViewById(R.id.imgView);
		imgView.setOnClickListener(vv -> {
			imgView.setVisibility(View.GONE);
		});
		followBtn.setOnClickListener(this::onFollowClick);
		followers.setOnClickListener(this::onFollowersClick);
		following.setOnClickListener(this::onFollowingClick);
		v.findViewById(R.id.inviter_btn).setOnClickListener(this::onInviterClick);
		mutualLayout=v.findViewById(R.id.mutuals_layout);
		mutualpic1=v.findViewById(R.id.mutualpic1);
		mutualpic2=v.findViewById(R.id.mutualpic2);
		mutuals=v.findViewById(R.id.mutuals);
		if(self){
			bio.setOnClickListener(this::onBioClick);
			photo.setOnClickListener(this::onPhotoClick);
			name.setOnClickListener(this::onNameClick);
			username.setOnClickListener(this::onUsernameClick);
			filesBtn.setOnClickListener(this::onFilesClick);
		}else{
			photo.setOnClickListener(this::onOthersPhotoClick);
		}
		twitter.setOnClickListener(this::onTwitterClick);
		instagram.setOnClickListener(this::onInstagramClick);
		usableRecyclerView=v.findViewById(R.id.recyclerView);
		usableRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
		clubsLayout=v.findViewById(R.id.club_layout);
		return v;
	}

	@Override
	protected void doLoadData(){
		currentRequest=new GetProfile(getArguments().getInt("id"))
				.setCallback(new SimpleCallback<GetProfile.Response>(this){
					@Override
					public void onSuccess(GetProfile.Response result){
						currentRequest=null;
						if(getActivity()==null) return;
						user=result.userProfile;
						name.setText(user.name);
						username.setText(String.format("@%s", user.username));
						ColorDrawable d=new ColorDrawable(getResources().getColor(R.color.grey));
						if(user.photoUrl!=null)
							ViewImageLoader.load(photo, d, user.photoUrl);
						else
							photo.setImageDrawable(d);

						followsYou.setVisibility(user.followsMe ? View.VISIBLE : View.GONE);
						followers.setText(getResources().getQuantityString(R.plurals.followers, user.numFollowers, user.numFollowers));
						following.setText(getResources().getQuantityString(R.plurals.following, user.numFollowing, user.numFollowing));
						bio.setText(user.bio);
						if(TextUtils.isEmpty(user.bio) && self)
							bio.setText(R.string.update_bio);

						if(self) {
							followBtn.setVisibility(View.GONE);
							filesBtn.setVisibility(View.VISIBLE);
						}else {
							followBtn.setText(user.isFollowed() ? R.string.following : R.string.follow);
							filesBtn.setVisibility(View.GONE);
						}

						if(!self && user.twitter==null && user.instagram==null){
							socialButtons.setVisibility(View.GONE);
						}else{
							socialButtons.setVisibility(View.VISIBLE);
							twitter.setVisibility(user.twitter==null ? View.GONE : View.VISIBLE);
							//twitter.setVisibility(user.twitter==null && !self ? View.GONE : View.VISIBLE);
							instagram.setVisibility(user.instagram==null && !self ? View.GONE : View.VISIBLE);
							if(user.twitter!=null)
								twitter.setText(user.twitter);
							//if(self && user.twitter==null)
							//	twitter.setText(R.string.add_twitter);
							if(user.instagram!=null)
								instagram.setText(user.instagram);
							if(self && user.instagram==null)
								instagram.setText(R.string.add_instagram);
						}

						if(self) {
							mutualLayout.setVisibility(View.GONE);
						}else{
							currentRequest=new GetMutualFollowers(user.userId, 4, 1)
									.setCallback(new SimpleCallback<GetMutualFollowers.Response>(ProfileFragment.this){
										@Override
										public void onSuccess(GetMutualFollowers.Response result){
											currentRequest=null;
											if(result.count>2) {
												mutualLayout.setVisibility(View.VISIBLE);
												ColorDrawable d2=new ColorDrawable(getResources().getColor(R.color.grey));
												if(result.users.get(0).photoUrl!=null)
													ViewImageLoader.load(mutualpic1, d2, result.users.get(0).photoUrl);
												if(result.users.get(1).photoUrl!=null)
													ViewImageLoader.load(mutualpic2, d2, result.users.get(1).photoUrl);
												String s=getResources().getString(R.string.mutual_follows_other, result.users.get(0).name, result.users.get(1).name, result.count-2);
												mutuals.setText(s);
											}else if(result.count>1) {
												mutualLayout.setVisibility(View.VISIBLE);
												ColorDrawable d2=new ColorDrawable(getResources().getColor(R.color.grey));
												if(result.users.get(0).photoUrl!=null)
													ViewImageLoader.load(mutualpic1, d2, result.users.get(0).photoUrl);
												if(result.users.get(1).photoUrl!=null)
													ViewImageLoader.load(mutualpic2, d2, result.users.get(1).photoUrl);
												String s=getResources().getString(R.string.mutual_follows_two, result.users.get(0).name, result.users.get(1).name);
												mutuals.setText(s);
											}else if(result.count>0) {
												mutualpic2.setVisibility(View.GONE);
												mutualLayout.setVisibility(View.VISIBLE);
												if(result.users.get(0).photoUrl!=null) {
													ColorDrawable d2=new ColorDrawable(getResources().getColor(R.color.grey));
													ViewImageLoader.load(mutualpic1, d2, result.users.get(0).photoUrl);
												}
												String s=getResources().getString(R.string.mutual_follows_one, result.users.get(0).name);
												mutuals.setText(s);
											}else{
												mutualLayout.setVisibility(View.GONE);
											}
										}
									})
									.exec();
						}

						String joined=getString(R.string.joined_date, DateFormat.getDateInstance().format(user.timeCreated));
						if(user.invitedByUserProfile!=null){
							ColorDrawable d2=new ColorDrawable(getResources().getColor(R.color.grey));
							joined+="\n"+getString(R.string.invited_by, user.invitedByUserProfile.name);
							if(user.invitedByUserProfile.photoUrl!=null)
								ViewImageLoader.load(inviterPhoto, d2, user.invitedByUserProfile.photoUrl);
							else
								inviterPhoto.setImageDrawable(d2);
						}else{
							inviterPhoto.setVisibility(View.GONE);
						}
						inviteInfo.setText(joined);
						if(result.userProfile.clubs!=null && result.userProfile.clubs.size()>0) {
							clubsLayout.setVisibility(View.VISIBLE);
							ClubsAdapter mAdapter = new ClubsAdapter(result.userProfile.clubs, item -> {
								Bundle args=new Bundle();
								args.putInt("id", item.clubId);
								Nav.go(getActivity(), ClubFragment.class, args);
							});
							usableRecyclerView.setAdapter(mAdapter);
						}else{
							clubsLayout.setVisibility(View.GONE);
							usableRecyclerView.setAdapter(null);
						}
						topics=result.userProfile.topics;
						/*new UpdateNotifications(1,-1,false,null,2,3)
								.wrapProgress(getActivity())
								.setCallback(new SimpleCallback<BaseResponse>(fragment){
									@Override
									public void onSuccess(BaseResponse result){
									}
								}).exec();
						new GetSettings().wrapProgress(getActivity()).setCallback(new SimpleCallback<GetSettings.Response>(fragment){
							@Override
							public void onSuccess(GetSettings.Response result){
								boolean s=result.success;
							}
						}).exec();*/
						dataLoaded();
					}
				})
				.exec();
		/*if(self) {
			new GetClubs(false)
					.setCallback(new SimpleCallback<GetClubs.Response>(this){
						@Override
						public void onSuccess(GetClubs.Response result){
							dataLoaded();
							if(result.clubs!=null) {
								clubsLayout.setVisibility(View.VISIBLE);
								ClubsAdapter mAdapter = new ClubsAdapter(result.clubs, item -> {
									Bundle args=new Bundle();
									args.putInt("id", item.clubId);
									Nav.go(getActivity(), ClubFragment.class, args);
								});
								usableRecyclerView.setAdapter(mAdapter);
							}else{
								clubsLayout.setVisibility(View.GONE);
								usableRecyclerView.setAdapter(null);
							}
						}
					}).exec();
		}*/
	}

	@Override
	public void onHiddenChanged (boolean hidden) {
		if(!hidden) doLoadData();
	}

	@Override
	public void onRefresh(){
		loadData();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		if(!self) {
			ImageButton b1 = new ImageButton(getActivity());
			b1.setImageResource(R.drawable.ic_home_icon);
			b1.setBackgroundColor(Color.TRANSPARENT);
			Toolbar.LayoutParams l3 = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
			l3.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			l3.setMarginEnd(25);
			b1.setLayoutParams(l3);
			b1.setOnClickListener(vv -> {
				Bundle extras=new Bundle();
				extras.putBoolean(AppKitFragment.EXTRA_IS_TAB, true);
				Nav.goClearingStack(getActivity(), HomeFragment.class, extras);
			});
			getToolbar().addView(b1);
		}
		getToolbar().setElevation(0);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		getToolbar().setElevation(0);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		menu.add(0,0,0, R.string.interests);
		menu.add(0,2,1, R.string.about);
		menu.add(0,1,2, R.string.log_out);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		int mid=item.getItemId();
		if(mid==0) {
			Bundle args=new Bundle();
			args.putParcelableArrayList("topics", (ArrayList<Topic>) topics);
			Nav.go(getActivity(), InterestsListFragment.class, args);
		}else if(mid==1) {
			if (VoiceService.getInstance() != null) {
				VoiceService.getInstance().leaveChannel();
			}
			ClubhouseSession.userID = ClubhouseSession.userToken = null;
			ClubhouseSession.write();
			Nav.goClearingStack(getActivity(), LoginFragment.class, null);
		}else if(mid==2) {
			final String about=getResources().getString(R.string.about_text, BuildConfig.VERSION_NAME);
			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.app_name)
					.setMessage(about)
					.setCancelable(false)
					.setNeutralButton(R.string.ok, (dialog, id) -> {
					}).create().show();
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode==PICK_PHOTO_RESULT && resultCode==Activity.RESULT_OK){
			new UpdatePhoto(data.getData())
					.wrapProgress(getActivity())
					.setCallback(new Callback<Bitmap>(){
						@Override
						public void onSuccess(Bitmap result){
							photo.setImageBitmap(result);
						}
						@Override
						public void onError(ErrorResponse error){
							error.showToast(getActivity());
						}
					})
					.exec();
		}else{
			if (twitterAuthClient != null) {
				twitterAuthClient.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	private void onFollowClick(View v){
		if(user.isFollowed()){
			new AlertDialog.Builder(getActivity())
					.setMessage(getString(R.string.confirm_unfollow, user.name))
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialogInterface, int i){
							new Unfollow(user.userId)
									.wrapProgress(getActivity())
									.setCallback(new Callback<BaseResponse>(){
										@Override
										public void onSuccess(BaseResponse result){
											user.notificationType=0;
											followBtn.setText(R.string.follow);
										}

										@Override
										public void onError(ErrorResponse error){
											error.showToast(getActivity());
										}
									})
									.exec();
						}
					})
					.setNegativeButton(R.string.no, null)
					.show();
		}else{
			new Follow(user.userId)
					.wrapProgress(getActivity())
					.setCallback(new Callback<BaseResponse>(){
						@Override
						public void onSuccess(BaseResponse result){
							user.notificationType=2;
							followBtn.setText(R.string.following);
						}

						@Override
						public void onError(ErrorResponse error){
							error.showToast(getActivity());
						}
					})
					.exec();
		}
	}

	private void onFilesClick(View v) {
		Nav.go(getActivity(), FilesListFragment.class, null);
	}

	/*private void onInstagramClick(View v){
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/"+user.instagram)));
	}

	private void onTwitterClick(View v){
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"+user.twitter)));
	}*/

	private void onFollowersClick(View v){
		Bundle args=new Bundle();
		args.putInt("id", user.userId);
		Nav.go(getActivity(), FollowersFragment.class, args);
	}

	private void onFollowingClick(View v){
		Bundle args=new Bundle();
		args.putInt("id", user.userId);
		Nav.go(getActivity(), FollowingFragment.class, args);
	}

	private void onInviterClick(View v){
		if(user.invitedByUserProfile==null)
			return;
		Bundle args=new Bundle();
		args.putInt("id", user.invitedByUserProfile.userId);
		Nav.go(getActivity(), ProfileFragment.class, args);
	}

	private void onNameClick(View v) {
		final EditText edit = new EditText(getActivity());
		edit.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
		edit.setText(user.name);
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.update_name)
				.setView(edit)
				.setPositiveButton(R.string.save, (dialog, which) -> {
					final String newName = edit.getText().toString();
					new UpdateName(newName)
							.wrapProgress(getActivity())
							.setCallback(new Callback<BaseResponse>() {
								@Override
								public void onSuccess(BaseResponse result) {
									user.name = newName;
									if (TextUtils.isEmpty((newName)))
										name.setText(R.string.update_name);
									else
										name.setText(newName);
								}

								@Override
								public void onError(ErrorResponse error) {
									error.showToast(getActivity());
								}
							})
							.exec();
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private void onUsernameClick(View v) {
		final EditText edit = new EditText(getActivity());
		edit.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
		edit.setText(user.username);
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.update_username)
				.setView(edit)
				.setPositiveButton(R.string.save, (dialog, which) -> {
					final String newName = edit.getText().toString();
					new UpdateUsername(newName)
							.wrapProgress(getActivity())
							.setCallback(new Callback<BaseResponse>() {
								@Override
								public void onSuccess(BaseResponse result) {
									user.username = newName;
									if (TextUtils.isEmpty((newName)))
										name.setText(R.string.update_username);
									else
										name.setText("@"+newName);
								}

								@Override
								public void onError(ErrorResponse error) {
									error.showToast(getActivity());
								}
							})
							.exec();
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private void onBioClick(View v){
		final EditText edit=new EditText(getActivity());
		edit.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE | edit.getInputType());
		edit.setSingleLine(false);
		edit.setMinLines(3);
		edit.setMaxLines(6);
		edit.setGravity(Gravity.TOP);
		edit.setText(user.bio);
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.update_bio)
				.setView(edit)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialogInterface, int i){
						final String newBio=edit.getText().toString();
						new UpdateBio(newBio)
								.wrapProgress(getActivity())
								.setCallback(new Callback<BaseResponse>(){
									@Override
									public void onSuccess(BaseResponse result){
										user.bio=newBio;
										if(TextUtils.isEmpty(newBio))
											bio.setText(R.string.update_bio);
										else
											bio.setText(newBio);
									}

									@Override
									public void onError(ErrorResponse error){
										error.showToast(getActivity());
									}
								})
								.exec();
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private void onPhotoClick(View v){
		Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, PICK_PHOTO_RESULT);
	}

	private void onOthersPhotoClick(View v) {
		if(user.photoUrl!=null) {
			ColorDrawable d=new ColorDrawable(getResources().getColor(R.color.lightBeige));
			ViewImageLoader.load(imgView, d, user.photoUrl);
			imgView.setVisibility(View.VISIBLE);
		}
	}

	private void updateTwitterData(final TwitterSession twitterSession) {
		new UpdateTwitter(twitterSession.getUserName(), twitterSession.getAuthToken().token, twitterSession.getAuthToken().secret)
				.wrapProgress(getActivity())
				.setCallback(new Callback<BaseResponse>(){
					@Override
					public void onSuccess(BaseResponse result){
						user.twitter=null;
					}
					@Override
					public void onError(ErrorResponse error){
						error.showToast(getActivity());
					}
				})
				.exec();
	}


	private void onTwitterClick(View v) {
		if (self) {
			/*if (user.twitter == null) {
				TwitterConfig config = new TwitterConfig.Builder(getActivity())
						.logger(new DefaultLogger(Log.DEBUG))
						.twitterAuthConfig(new TwitterAuthConfig(getResources().getString(R.string.twitter_api_key), getResources().getString(R.string.twitter_api_secret)))
						.debug(true)
						.build();
				Twitter.initialize(config);
				twitterAuthClient = new TwitterAuthClient();
				TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
				if (twitterSession == null) {
					twitterAuthClient.authorize(getActivity(), new com.twitter.sdk.android.core.Callback<TwitterSession>() {
						@Override
						public void success(Result<TwitterSession> result) {
							TwitterSession twitterSession = result.data;
							updateTwitterData(twitterSession);
						}
						@Override
						public void failure(TwitterException e) {
							Log.e("Twitter", "Failed to authenticate user " + e.getMessage());
						}
					});
				} else {
					updateTwitterData(twitterSession);
				}
			} else {
				//unlink twitter account
			}*/
		}else
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"+user.twitter)));
	}

	private void onInstagramClick(View v){
		if (self){
			if (user.instagram == null) {
				Nav.go(getActivity(), InstagramFragment.class, null);
			} else {
				new AlertDialog.Builder(getActivity())
					.setMessage(getString(R.string.confirm_unlink_instagram_title))
					.setMessage(getString(R.string.confirm_unlink_instagram))
					.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialogInterface, int i){
								new UpdateInstagram(null)
									.wrapProgress(getActivity())
									.setCallback(new Callback<BaseResponse>(){
										@Override
										public void onSuccess(BaseResponse result){
											instagram.setText(R.string.add_instagram);
											user.instagram=null;
										}
										@Override
										public void onError(ErrorResponse error){
											error.showToast(getActivity());
										}
									})
								.exec();
							}
						})
					.setNegativeButton(R.string.no, null)
					.show();
			}
		} else
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/"+user.instagram)));
	}

	public static class ClubsAdapter extends RecyclerView.Adapter<ClubsAdapter.ViewHolder> {

		private final List<Club> clubList;
		private final OnItemClickListener listener;

		public interface OnItemClickListener {
			void onItemClick(Club item);
		}

		public static class ViewHolder extends UsableRecyclerView.ViewHolder {
			private final ImageView img;

			public ViewHolder(View view) {
				super(view);
				img = view.findViewById(R.id.club_photo);
			}

			public ImageView getImageView() {
				return img;
			}

			public void bind(final Club item, final OnItemClickListener listener) {
				itemView.setOnClickListener(v -> listener.onItemClick(item));
			}
		}

		public ClubsAdapter(List<Club> clubs, OnItemClickListener listener) {
			this.clubList = clubs;
			this.listener=listener;
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.profile_clubs_row, viewGroup, false);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(ViewHolder viewHolder, final int position) {
			viewHolder.bind(clubList.get(position), listener);
			Drawable d= App.applicationContext.getDrawable(R.drawable.ic_friends);
			ViewImageLoader.load(viewHolder.getImageView(), d, clubList.get(position).photoUrl);
		}

		@Override
		public int getItemCount() {
			return clubList.size();
		}
	}
}
