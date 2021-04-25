package co.cxip.chrec.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import co.cxip.chrec.App;
import co.cxip.chrec.VoiceService;
import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseErrorResponse;
import co.cxip.chrec.api.methods.AcceptSpeakerInvite;
import co.cxip.chrec.api.methods.CreateChannel;
import co.cxip.chrec.api.methods.GetProfile;
import co.cxip.chrec.api.methods.Me;
import co.cxip.chrec.api.model.ChannelUser;
import co.cxip.chrec.api.model.FullUser;
import co.cxip.chrec.api.model.User;
import co.cxip.chrec.views.RoundedImageButton;
import co.cxip.chrec.views.TextDrawable;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.APIRequest;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.AppKitFragment;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.fragments.LoaderFragment;
import me.grishka.appkit.imageloader.ImageCache;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.ViewImageLoader;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.utils.V;
import co.cxip.chrec.MainActivity;
import co.cxip.chrec.R;
import co.cxip.chrec.api.ClubhouseSession;
import co.cxip.chrec.api.methods.GetChannels;
import co.cxip.chrec.api.model.Channel;

public class HomeFragment extends BaseRecyclerFragment<Channel> implements VoiceService.ChannelEventListener {

	private ChannelAdapter adapter;
	private Button openroomBtn, socialroomBtn, closedroomBtn, startARoomBtn, invitesBtn, speakerBtn;
	private ImageButton raiseBtn, muteBtn, eventsBtn;
	private RoundedImageButton profileBtn;
	private LinearLayout home_club;
	private RelativeLayout create_room;
	private TextView club_name,start_a_room, unreadNotifications;
	private String new_room_topic=null;
	private int create_room_button=0,numInvites=0;
	private String user_photo_url=null;
	private ImageView pic1,pic2;
	public boolean amIvisible;

	private final ViewOutlineProvider roundedCornersOutline=new ViewOutlineProvider(){
		@Override
		public void getOutline(View view, Outline outline){
			outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), V.dp(8));
		}
	};

	public HomeFragment(){
		super(20);
		setListLayoutId(R.layout.home);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		loadData();
		//setHasOptionsMenu(true);
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetChannels()
				.setCallback(new SimpleCallback<GetChannels.Response>(this){
					@Override
					public void onSuccess(GetChannels.Response result){
						currentRequest=null;
						onDataLoaded(result.channels, false);
					}
					@Override
					public void onError(ErrorResponse error) {
						if(getActivity()!=null) {
							new AlertDialog.Builder(getActivity())
									.setMessage("Error receiving information. Retry or reinstall.")
									.setPositiveButton(R.string.ok, null)
									.show();
						}
					}
				}).exec();
		new Me().setCallback(new Callback<Me.Response>() {
			@Override
			public void onSuccess(Me.Response result) {
				numInvites=result.num_invites;
				invitesBtn.setText(String.valueOf(numInvites));
				if(result.has_unread_notifications || result.actionable_notifications_count>0) unreadNotifications.setVisibility(View.VISIBLE); else unreadNotifications.setVisibility(View.GONE);
				User user=result.userProfile;
				if(user.photoUrl!=null && !user.photoUrl.equals(user_photo_url) && getActivity()!=null) {
					user_photo_url=user.photoUrl;
					Drawable d=getResources().getDrawable(R.drawable.ic_baseline_person_24);
					ViewImageLoader.load(profileBtn, d, user.photoUrl);
				}
			}
			@Override
			public void onError(ErrorResponse error) {
				invitesBtn.setText("");
				/*try {
					ClubhouseErrorResponse err = (ClubhouseErrorResponse) error;
					if (err.code == 400) {
						new AlertDialog.Builder(getActivity())
								.setTitle(R.string.app_name)
								.setMessage("Your account is waitlisted. Please go back and login again and enter all the required information.")
								.setCancelable(false)
								.setNeutralButton(R.string.ok, (dialog, id) -> {
									ClubhouseSession.userID = ClubhouseSession.userToken = null;
									ClubhouseSession.write();
									Nav.goClearingStack(getActivity(), LoginFragment.class, null);
								}).create().show();
					}
				}catch (Exception e) {};*/
			}
		}).exec();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		list.addItemDecoration(new RecyclerView.ItemDecoration(){
			@Override
			public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state){
				outRect.bottom=outRect.top=V.dp(8);
				outRect.left=outRect.right=V.dp(16);
			}
		});
		//getToolbar().setElevation(0);
		home_club=view.findViewById(R.id.home_club);
		home_club.setVisibility(View.GONE);
		Button start_room = view.findViewById(R.id.start_room);
		club_name=view.findViewById(R.id.club_name);
		ImageButton friendsBtn = view.findViewById(R.id.friends);
		create_room=view.findViewById(R.id.create_room);
		Button closeCreateRoomBtn = view.findViewById(R.id.close_create_room);
		openroomBtn=view.findViewById(R.id.openroom);
		socialroomBtn=view.findViewById(R.id.socialroom);
		closedroomBtn=view.findViewById(R.id.closedroom);
		start_a_room=view.findViewById(R.id.start_a_room);
		Button addTopicBtn = view.findViewById(R.id.addtopic);
		startARoomBtn=view.findViewById(R.id.start_a_room_btn);
		Button searchBtn = view.findViewById(R.id.seatch_button);
		ImageButton notificationBtn = view.findViewById(R.id.notification_button);
		profileBtn=view.findViewById(R.id.profile_button);
		invitesBtn=view.findViewById(R.id.invite_button);
		unreadNotifications=view.findViewById(R.id.unread_notifications);
		eventsBtn=view.findViewById(R.id.events);
		start_room.setOnClickListener(this::onStartaroomClick);
		club_name.setOnClickListener(this::onClubnameClick);
		friendsBtn.setOnClickListener(this::onFriendsClick);
		closeCreateRoomBtn.setOnClickListener(this::onCloseCreateRoomBtnClick);
		openroomBtn.setOnClickListener(this::onOpenroomBtnClick);
		socialroomBtn.setOnClickListener(this::onSocialroomBtnClick);
		closedroomBtn.setOnClickListener(this::onClosedroomBtnClick);
		addTopicBtn.setOnClickListener(this::onAddTopicBtnClick);
		startARoomBtn.setOnClickListener(this::onStartARoomBtnClick);
		searchBtn.setOnClickListener(this::onSearchBtnClick);
		invitesBtn.setOnClickListener(this::onInvitesBtnClick);
		notificationBtn.setOnClickListener(this::onNotificationBtnClick);
		profileBtn.setOnClickListener(this::onProfileBtnClick);
		view.findViewById(R.id.back_view).setOnClickListener(this::onBackViewClick);
		eventsBtn.setOnClickListener(this::onEventsClick);

		view.findViewById(R.id.leave).setOnClickListener(this::onLeaveClick);
		speakerBtn=view.findViewById(R.id.speaker);
		speakerBtn.setOnClickListener(this::onSpeakerClick);
		view.findViewById(R.id.ping).setOnClickListener(this::onPingClick);
		raiseBtn=view.findViewById(R.id.raise);
		raiseBtn.setOnClickListener(this::onRaiseClick);
		muteBtn=view.findViewById(R.id.mute);
		muteBtn.setOnClickListener(this::onMuteClick);
		pic1=view.findViewById(R.id.pic1);
		pic2=view.findViewById(R.id.pic2);
		VoiceService.addListener(this);
		amIvisible=true;
		initButtons();
	}

	private void initButtons() {
		if(!amIvisible) return;
		if(VoiceService.getInstance()!=null) {
			Channel channel = VoiceService.getInstance().getChannel();
			if(channel==null) return;
			raiseBtn.setEnabled(channel.isHandraiseEnabled);
			raiseBtn.setVisibility(VoiceService.getInstance().isSelfSpeaker() ? View.GONE : View.VISIBLE);
			muteBtn.setVisibility(VoiceService.getInstance().isSelfSpeaker() ? View.VISIBLE : View.GONE);
			raiseBtn.setImageResource(VoiceService.getInstance().isHandRaised() ? R.drawable.ic_raised_hand_yellow : R.drawable.ic_raised_hand_transparent);
			muteBtn.setVisibility(VoiceService.getInstance().isSelfSpeaker() ? View.VISIBLE : View.GONE);
			muteBtn.setImageResource(VoiceService.getInstance().isMuted() ? R.drawable.ic_mic_off : R.drawable.ic_mic);
			speakerBtn.setText(VoiceService.getInstance().isSpeakerOn() ? R.string.speaker_on : R.string.speaker_off);
			for(ChannelUser user:channel.users) {
				if(user.isModerator) {
					if(user.photoUrl!=null) {
						Drawable d;
						if(getActivity()==null)
							d= App.applicationContext.getResources().getDrawable(R.drawable.ic_baseline_person_24);
						else
							d = getResources().getDrawable(R.drawable.ic_baseline_person_24);
						ViewImageLoader.load(pic1, d, user.photoUrl);
					}
				}
			}
			//Drawable numUsers=new TextDrawable(Integer.toString(channel.users.size()), getResources());
			int users=channel.users.size()-1;
			String nuserStr;
			if(users>1000) {
				nuserStr=String.format("%.1f", (float)(users/1000))+"k";
			}else{
				nuserStr=Integer.toString(users);
			}
			String num="+" + nuserStr;
			Drawable numUsers;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
				numUsers = new TextDrawable(App.applicationContext, num);
			}else{
				numUsers = new TextDrawable(getActivity(), num);
			}
			pic2.setImageDrawable(numUsers);
		}
	}

	private void onLeaveClick(View v){
		if(VoiceService.getInstance()==null) return;
		VoiceService.getInstance().leaveChannel();
		home_club.setVisibility(View.GONE);
	}

	private void onSpeakerClick(View v) {
		VoiceService svc=VoiceService.getInstance();
		if(svc==null) return;
		svc.setSpeakerOn(!svc.isSpeakerOn());
		speakerBtn.setText(svc.isSpeakerOn() ? R.string.speaker_on : R.string.speaker_off);
	}

	private void onPingClick(View v) {
		VoiceService svc=VoiceService.getInstance();
		if(svc==null) return;
		Channel channel = svc.getChannel();
		if(channel==null) return;
		Bundle args=new Bundle();
		args.putString("channel", channel.channel);
		Nav.go(getActivity(), PingListFragment.class, args);
	}

	private void onRaiseClick(View v) {
		VoiceService svc=VoiceService.getInstance();
		if(svc.isHandRaised()) {
			svc.unraiseHand();
			raiseBtn.setImageResource(R.drawable.ic_raised_hand_transparent);
		}else {
			svc.raiseHand();
			raiseBtn.setImageResource(R.drawable.ic_raised_hand_yellow);
		}
	}

	private void onMuteClick(View v) {
		VoiceService svc=VoiceService.getInstance();
		if(svc==null) return;
		svc.setMuted(!svc.isMuted());
		muteBtn.setImageResource(svc.isMuted() ? R.drawable.ic_mic_off : R.drawable.ic_mic);
	}

	@Override
	public void onHiddenChanged (boolean hidden) {
		if(!hidden) {
			amIvisible=true;
			VoiceService.addListener(this);
			initButtons();
			if(VoiceService.getInstance()!=null) {
				Channel current = VoiceService.getInstance().getChannel();
				if(current==null) {
					home_club.setVisibility(View.GONE);
				}else{
					home_club.setVisibility(View.VISIBLE);
					club_name.setSelected(true);
					club_name.setText(current.topic);
				}
			}
			MainActivity mainActivity=(MainActivity)getActivity();
			if(mainActivity.create_room_panel_visible) {
				create_room.setVisibility(View.VISIBLE);
			}else{
				create_room.setVisibility(View.GONE);
			}
			loadData(0,40);
			new Me().setCallback(new Callback<Me.Response>() {
				@Override
				public void onSuccess(Me.Response result) {
					numInvites=result.num_invites;
					invitesBtn.setText(String.valueOf(numInvites));
					if(result.has_unread_notifications) unreadNotifications.setVisibility(View.VISIBLE); else unreadNotifications.setVisibility(View.GONE);
					User user=result.userProfile;
					if(user.photoUrl!=null && !user.photoUrl.equals(user_photo_url)) {
						user_photo_url=user.photoUrl;
						Drawable d=getResources().getDrawable(R.drawable.ic_baseline_person_24);
						ViewImageLoader.load(profileBtn, d, user.photoUrl);
					}
				}
				@Override
				public void onError(ErrorResponse error) {
					invitesBtn.setText("");
				}
			}).exec();
		}else{
			amIvisible=false;
		}
	}

	private void onBackViewClick(View v) {
		onCloseCreateRoomBtnClick(v);
	}

	private void onSearchBtnClick(View v) {
		Bundle args=new Bundle();
		args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
		Nav.go(getActivity(), SearchFragment.class, args);
	}

	private void onInvitesBtnClick(View v) {
		if(numInvites>0) {
			Bundle args=new Bundle();
			args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
			Nav.go(getActivity(), InviteFragment.class, args);
		}else{
			new AlertDialog.Builder(getActivity())
				.setMessage(R.string.no_invites)
				.setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
				.setCancelable(false)
				.show();
		}
	}

	private void onEventsClick(View v) {
		Bundle args=new Bundle();
		args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
		Nav.go(getActivity(), EventsFragment.class, args);
	}

	private void onNotificationBtnClick(View v) {
		Bundle args=new Bundle();
		args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
		Nav.go(getActivity(), NotificationListFragment.class, args);
	}

	private void onProfileBtnClick(View v) {
		Bundle args=new Bundle();
		args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
		Nav.go(getActivity(), ProfileFragment.class, args);
	}

	private void onStartARoomBtnClick(View v) {
		boolean is_social_media= create_room_button == 1;
		boolean is_private= create_room_button == 2;
		String club_id=null;
		String event_id=null;
		if(create_room_button==2) {
			Bundle args=new Bundle();
			args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
			args.putString("topic", new_room_topic);
			args.putBoolean("is_social_media", is_social_media);
			args.putBoolean("is_private", is_private);
			args.putString("club_id", club_id);
			args.putString("event_id", event_id);
			Nav.go(getActivity(), SelectUsersListFragment.class, args);
		}else{
			MainActivity mainActivity=(MainActivity)getActivity();
			mainActivity.create_room_panel_visible=false;
			create_room.setVisibility(View.GONE);
			List<Integer> user_ids=new ArrayList<>();
			CreateChannel.Body body=new CreateChannel.Body(is_social_media, is_private, club_id, user_ids, event_id, new_room_topic);
			mainActivity.createChannel(body);
		}
	}

	private void onAddTopicBtnClick(View v) {
		final EditText txtUrl = new EditText(getActivity());
		txtUrl.setHint(getString(R.string.add_a_topic_hint));
		if(new_room_topic!=null) txtUrl.setText(new_room_topic);
		new AlertDialog.Builder(getActivity())
				.setTitle(getString(R.string.add_a_topic))
				.setMessage(getString(R.string.add_a_topic_2))
				.setView(txtUrl)
				.setPositiveButton(getString(R.string.set_topic), (dialog, whichButton) -> {
					new_room_topic = txtUrl.getText().toString();
					if(create_room_button==0) {
						start_a_room.setText(getString(R.string.start_room_with_topic_text, getString(R.string.open_to_all), new_room_topic));
					}else if(create_room_button==1) {
						start_a_room.setText(getString(R.string.start_room_with_topic_text, getString(R.string.open_to_follow), new_room_topic));
					}else if(create_room_button==2) {
						start_a_room.setText(getString(R.string.start_room_with_topic_text, getString(R.string.open_to_choose), new_room_topic));
					}
				})
				.setNegativeButton(getString(R.string.cancel), (dialog, whichButton) -> {
				})
				.show();
	}

	private void onOpenroomBtnClick(View v) {
		create_room_button=0;
		if(new_room_topic==null) {
			start_a_room.setText(getString(R.string.start_room_text, getString(R.string.open_to_all)));
		}else{
			start_a_room.setText(getString(R.string.start_room_with_topic_text, getString(R.string.open_to_all), new_room_topic));
		}
		startARoomBtn.setText(R.string.lets_go);
		openroomBtn.setBackgroundResource(R.drawable.round_corners_gray);
		socialroomBtn.setBackgroundResource(R.drawable.round_corners);
		closedroomBtn.setBackgroundResource(R.drawable.round_corners);
		create_room.setVisibility(View.VISIBLE);
	}

	private void onSocialroomBtnClick(View v) {
		create_room_button=1;
		if(new_room_topic==null) {
			start_a_room.setText(getString(R.string.start_room_text, getString(R.string.open_to_follow)));
		}else{
			start_a_room.setText(getString(R.string.start_room_with_topic_text, getString(R.string.open_to_follow), new_room_topic));
		}
		startARoomBtn.setText(R.string.lets_go);
		openroomBtn.setBackgroundResource(R.drawable.round_corners);
		socialroomBtn.setBackgroundResource(R.drawable.round_corners_gray);
		closedroomBtn.setBackgroundResource(R.drawable.round_corners);
		create_room.setVisibility(View.VISIBLE);
	}

	private void onClosedroomBtnClick(View v) {
		create_room_button=2;
		if(new_room_topic==null) {
			start_a_room.setText(getString(R.string.start_room_text, getString(R.string.open_to_choose)));
		}else{
			start_a_room.setText(getString(R.string.start_room_with_topic_text, getString(R.string.open_to_choose), new_room_topic));
		}
		startARoomBtn.setText(R.string.choose_people);
		openroomBtn.setBackgroundResource(R.drawable.round_corners);
		socialroomBtn.setBackgroundResource(R.drawable.round_corners);
		closedroomBtn.setBackgroundResource(R.drawable.round_corners_gray);
		create_room.setVisibility(View.VISIBLE);
	}

	private void onCloseCreateRoomBtnClick(View v) {
		MainActivity mainActivity=(MainActivity)getActivity();
		mainActivity.create_room_panel_visible=false;
		create_room.setVisibility(View.GONE);
	}

	private void onStartaroomClick(View v) {
		MainActivity mainActivity=(MainActivity)getActivity();
		mainActivity.create_room_panel_visible=true;
		start_a_room.setText(getString(R.string.start_room_text, getString(R.string.open_to_all)));
		startARoomBtn.setText(R.string.lets_go);
		openroomBtn.setBackgroundResource(R.drawable.round_corners_gray);
		socialroomBtn.setBackgroundResource(R.drawable.round_corners);
		closedroomBtn.setBackgroundResource(R.drawable.round_corners);
		create_room.setVisibility(View.VISIBLE);
	}

	private void onClubnameClick(View v) {
		if(VoiceService.getInstance()!=null) {
			Channel current = VoiceService.getInstance().getChannel();
			((MainActivity) getActivity()).joinChannel(current);
		}
	}

	private void onFriendsClick(View v) {
		Bundle args=new Bundle();
		args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
		Nav.go(getActivity(), FriendsListFragment.class, args);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		//getToolbar().setElevation(0);
	}

	@Override
	protected RecyclerView.Adapter<ChannelViewHolder> getAdapter(){
		if(adapter==null){
			adapter=new ChannelAdapter();
			adapter.setHasStableIds(true);
		}
		return adapter;
	}

	@Override
	public boolean wantsLightNavigationBar(){
		return true;
	}

	@Override
	public boolean wantsLightStatusBar(){
		return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		//menu.add(0,0,1,"").setIcon(R.drawable.ic_notifications).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		//menu.add(0,1,2,"").setIcon(R.drawable.ic_baseline_person_24).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		/*new GetProfile(Integer.parseInt(ClubhouseSession.userID))
				.setCallback(new SimpleCallback<GetProfile.Response>(this){
					@Override
					public void onSuccess(GetProfile.Response result){
						FullUser user=result.userProfile;
						if(user.photoUrl!=null) {
							ImageCache.RequestWrapper reqWrapper = new ImageCache.RequestWrapper();
							final Bitmap bmp = ImageCache.getInstance(getContext()).get(user.photoUrl, null, reqWrapper, null, true);
							if(bmp!=null) {
								menu.getItem(1).setIcon(new BitmapDrawable(getResources(), bmp));
							}
						}
					}
				})
				.exec();*/
		//menu.add(0,-1,0,"").setIcon(R.drawable.ic_files).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		//menu.add(0,-2,3,"").setIcon(R.drawable.ic_files).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		int mid=item.getItemId();
		Bundle args=new Bundle();
		args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
		if(mid==0) {
			Nav.go(getActivity(), NotificationListFragment.class, args);
		} else if(mid==1){
			Nav.go(getActivity(), ProfileFragment.class, args);
		}
		return true;
	}

	@Override
	public void onUserMuteChanged(int id, boolean muted) {
	}

	@Override
	public void onUserJoined(ChannelUser user) {
	}

	@Override
	public void onUserLeft(int id) {
	}

	@Override
	public void onCanSpeak(String inviterName, int inviterID) {
		if(!amIvisible) return;
		if(getActivity()==null) return;
		Channel channel = VoiceService.getInstance().getChannel();
		new AlertDialog.Builder(getActivity())
				.setMessage(getString(R.string.confirm_join_as_speaker, inviterName))
				.setPositiveButton(R.string.join, (dialogInterface, i) -> new AcceptSpeakerInvite(channel.channel, inviterID)
						.wrapProgress(getActivity())
						.setCallback(new Callback<BaseResponse>(){
							@Override
							public void onSuccess(BaseResponse result){
								VoiceService.getInstance().rejoinChannel();
							}
							@Override
							public void onError(ErrorResponse error){
								error.showToast(getActivity());
							}
						})
						.exec())
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	@Override
	public void onChannelUpdated(Channel channel) {
		if(channel==null) return;
		VoiceService svc=VoiceService.getInstance();
		if(svc==null) return;
		raiseBtn.setEnabled(channel.isHandraiseEnabled);
		raiseBtn.setVisibility(svc.isSelfSpeaker() ? View.GONE : View.VISIBLE);
		muteBtn.setVisibility(svc.isSelfSpeaker() ? View.VISIBLE : View.GONE);
		if(svc.isSelfSpeaker()){
			onUserMuteChanged(Integer.parseInt(ClubhouseSession.userID), svc.isMuted());
		}
		initButtons();
	}

	@Override
	public void onSpeakingUsersChanged(List<Integer> ids) {
	}

	@Override
	public void onChannelEnded() {
		home_club.setVisibility(View.GONE);
	}

	@Override
	public void onMadeModerator(int id) {
	}

	@Override
	public void onUninvitedAsSpeaker() {
		if(!amIvisible) return;
		VoiceService svc=VoiceService.getInstance();
		if(svc==null) return;
		VoiceService.getInstance().rejoinChannel();
		//svc.updateChannel(svc.getChannel());
		raiseBtn.setVisibility(svc.isSelfSpeaker() ? View.GONE : View.VISIBLE);
		muteBtn.setVisibility(svc.isSelfSpeaker() ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onRemoveAsSpeaker(int id) {
	}

	@Override
	public void onSpeakerMuted(boolean speakerOn) {
		speakerBtn.setText(speakerOn ? R.string.speaker_on : R.string.speaker_off);
	}

	private class ChannelAdapter extends RecyclerView.Adapter<ChannelViewHolder> implements ImageLoaderRecyclerAdapter{

		@NonNull
		@Override
		public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new ChannelViewHolder();
		}

		@Override
		public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position){
			holder.bind(data.get(position));
		}

		@Override
		public int getItemCount(){
			return data.size();
		}

		@Override
		public long getItemId(int position){
			return data.get(position).channelId;
		}

		@Override
		public int getImageCountForItem(int position){
			Channel chan=data.get(position);
			int count=0;
			for(int i=0;i<Math.min(2, chan.users.size());i++){
				if(chan.users.get(i).photoUrl!=null)
					count++;
			}
			return count;
		}

		@Override
		public String getImageURL(int position, int image){
			Channel chan=data.get(position);
			for(int i=0;i<Math.min(2, chan.users.size());i++){
				if(chan.users.get(i).photoUrl!=null){
					if(image==0)
						return chan.users.get(i).photoUrl;
					else
						image--;
				}
			}
			return null;
		}
	}

	private class ChannelViewHolder extends BindableViewHolder<Channel> implements View.OnClickListener, ImageLoaderViewHolder{

		private final TextView topic, speakers, numMembers, numSpeakers;
		private final ImageView pic1, pic2;
		private final Drawable placeholder=new ColorDrawable(getResources().getColor(R.color.grey));

		public ChannelViewHolder(){
			super(getActivity(), R.layout.channel_row);
			topic=findViewById(R.id.topic);
			speakers=findViewById(R.id.speakers);
			numSpeakers=findViewById(R.id.num_speakers);
			numMembers=findViewById(R.id.num_members);
			pic1=findViewById(R.id.pic1);
			pic2=findViewById(R.id.pic2);

			itemView.setOutlineProvider(roundedCornersOutline);
			itemView.setClipToOutline(true);
			itemView.setElevation(V.dp(2));
			itemView.setOnClickListener(this);
		}

		@Override
		public void onBind(Channel item){
			topic.setText(item.topic);
			numMembers.setText(getString(R.string.num,item.numAll));
			numSpeakers.setText(getString(R.string.num,item.numSpeakers));
			speakers.setText(item.users.stream().map(user->user.isSpeaker ? (user.name+" 💬") : user.name).collect(Collectors.joining("\n")));

			imgLoader.bindViewHolder(adapter, this, getAdapterPosition());
		}

		@Override
		public void onClick(View view){
			((MainActivity)getActivity()).joinChannel(item);
		}

		private ImageView imgForIndex(int index){
			if(index==0)
				return pic1;
			return pic2;
		}

		@Override
		public void setImage(int index, Bitmap bitmap){
			if(index==0 && item.users.get(0).photoUrl==null)
				index=1;
			imgForIndex(index).setImageBitmap(bitmap);
		}

		@Override
		public void clearImage(int index){
			imgForIndex(index).setImageDrawable(placeholder);
		}
	}
}
