package co.cxip.chrec.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import co.cxip.chrec.App;
import co.cxip.chrec.MainActivity;
import co.cxip.chrec.api.methods.ChangeHandraiseSettings;
import co.cxip.chrec.api.methods.Follow;
import co.cxip.chrec.api.methods.GetChannels;
import co.cxip.chrec.api.methods.InviteSpeaker;
import co.cxip.chrec.api.methods.UninviteSpeaker;
import co.cxip.chrec.api.model.User;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.utils.MergeRecyclerAdapter;
import me.grishka.appkit.utils.SingleViewRecyclerAdapter;
import me.grishka.appkit.utils.V;
import me.grishka.appkit.views.UsableRecyclerView;
import co.cxip.chrec.R;
import co.cxip.chrec.VoiceService;
import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseSession;
import co.cxip.chrec.api.methods.AcceptSpeakerInvite;
import co.cxip.chrec.api.methods.GetChannel;
import co.cxip.chrec.api.model.Channel;
import co.cxip.chrec.api.model.ChannelUser;

public class InChannelFragment extends BaseRecyclerFragment<ChannelUser> implements VoiceService.ChannelEventListener {

	private MergeRecyclerAdapter adapter;
	private UserListAdapter speakersAdapter, followedAdapter, othersAdapter;
	private ImageButton muteBtn, raiseCtrlBtn, raiseBtn;
	//private Button raiseBtn;
	private Button recordBtn, speakerBtn, pingBtn;
	private Channel channel;
	private Menu options_menu;
	private TextView recordTimer, raiseHandsEdit, handraiseSetting, handraiseBackground;
	private Timer timer;
	private int timercounter;
	private PopupMenu raiseCtrlMenu;
	private FrameLayout raiseHandsLayout;
	private GridView raisedHandsGrid;
	final private ArrayList<ChannelUser> speakers=new ArrayList<>(), followedBySpeakers=new ArrayList<>(), otherUsers=new ArrayList<>();
	final private ArrayList<Integer> mutedUsers=new ArrayList<>(), speakingUsers=new ArrayList<>();

	public InChannelFragment(){
		super(10);
		setListLayoutId(R.layout.in_channel);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.leave).setOnClickListener(this::onLeaveClick);

		raiseBtn=view.findViewById(R.id.raise);
		muteBtn=view.findViewById(R.id.mute);
		recordBtn=view.findViewById(R.id.record);
		speakerBtn=view.findViewById(R.id.speaker);
		recordTimer=view.findViewById(R.id.recordTimer);
		pingBtn=view.findViewById(R.id.ping);
		raiseCtrlBtn=view.findViewById(R.id.raiseCtrl);
		raiseHandsLayout=view.findViewById(R.id.raise_hands_layout);
		raisedHandsGrid=view.findViewById(R.id.raised_hands_grid);
		raiseHandsEdit=view.findViewById(R.id.raise_hands_edit);
		handraiseSetting=view.findViewById(R.id.hand_raise_setting);
		handraiseBackground=view.findViewById(R.id.hand_raise_background);

		raiseBtn.setOnClickListener(this::onRaiseClick);
		muteBtn.setOnClickListener(this::onMuteClick);
		recordBtn.setOnClickListener(this::onRecordClick);
		speakerBtn.setOnClickListener(this::onSpeakerClick);
		pingBtn.setOnClickListener(this::onPingClick);
		raiseCtrlBtn.setOnClickListener(this::onRaiseCtrlClick);
		raiseHandsEdit.setOnClickListener(this::onRaiseEditClick);
		view.findViewById(R.id.back_view).setOnClickListener(this::onBackViewClick);

		GridLayoutManager lm=new GridLayoutManager(getActivity(), 12);
		lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){
			@Override
			public int getSpanSize(int position){
				RecyclerView.Adapter a=adapter.getAdapterForPosition(position);
				if(a instanceof UserListAdapter){
					if(((UserListAdapter) a).users==speakers)
						return 4;
					return 3;
				}
				return 12;
			}
		});
		list.setLayoutManager(lm);
		list.setPadding(0, V.dp(16), 0, V.dp(16));
		list.setClipToPadding(false);

		raiseCtrlMenu = new PopupMenu(getActivity(), raiseHandsEdit);
		raiseCtrlMenu.getMenu().add(Menu.NONE, 0, 0, getResources().getString(R.string.off)); //parm 2 is menu id, param 3 is position of this menu item in menu items list, param 4 is title of the menu
		raiseCtrlMenu.getMenu().add(Menu.NONE, 1, 1, getResources().getString(R.string.limited_to_followed));
		raiseCtrlMenu.getMenu().add(Menu.NONE, 2, 2, getResources().getString(R.string.open_to_everyone));
		raiseCtrlMenu.setOnMenuItemClickListener(menuItem -> {
			int id = menuItem.getItemId();
			if (id==0){
				new ChangeHandraiseSettings(channel.channel, false, 1)
						.setCallback(new Callback<BaseResponse>(){
							@Override
							public void onSuccess(BaseResponse result){
								handraiseSetting.setText(R.string.off);
								handraiseBackground.setText(R.string.hand_raises_are_off);
							}
							@Override
							public void onError(ErrorResponse error){
								error.showToast(getActivity());
							}
						})
						.exec();
			}else if (id==1){
				new ChangeHandraiseSettings(channel.channel, true, 2)
						.setCallback(new Callback<BaseResponse>(){
							@Override
							public void onSuccess(BaseResponse result){
								handraiseSetting.setText(R.string.limited_to_followed);
								handraiseBackground.setText(R.string.no_one_raised_hands);
							}
							@Override
							public void onError(ErrorResponse error){
								error.showToast(getActivity());
							}
						})
						.exec();
			}else if (id==2){
				new ChangeHandraiseSettings(channel.channel, true, 1)
						.setCallback(new Callback<BaseResponse>(){
							@Override
							public void onSuccess(BaseResponse result){
								handraiseSetting.setText(R.string.open_to_everyone);
								handraiseBackground.setText(R.string.no_one_raised_hands);
							}
							@Override
							public void onError(ErrorResponse error){
								error.showToast(getActivity());
							}
						})
						.exec();
			}
			return false;
		});

		VoiceService.addListener(this);
		getToolbar().setElevation(0);

		VoiceService svc=VoiceService.getInstance();
		if(svc!=null){
			initButtons(svc);
			onUserMuteChanged(Integer.parseInt(ClubhouseSession.userID), svc.isMuted());
		}
	}

	private void initButtons(VoiceService svc) {
		raiseBtn.setImageResource(svc.isHandRaised() ? R.drawable.ic_raised_hand_yellow : R.drawable.ic_raised_hand_transparent);
		muteBtn.setImageResource(svc.isMuted() ? R.drawable.ic_mic_off : R.drawable.ic_mic);
		speakerBtn.setText(svc.isSpeakerOn() ? R.string.speaker_on : R.string.speaker_off);
		recordBtn.setText(svc.isRecording() ? R.string.stop : R.string.record);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		getToolbar().setElevation(0);
	}

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		VoiceService.removeListener(this);
	}

	@Override
	protected void doLoadData(int offset, int count){
//		channel=VoiceService.getInstance().getChannel();
//		setTitle(channel.topic);
//		onDataLoaded(channel.users, false);
		new GetChannel(channel.channel)
				.setCallback(new SimpleCallback<Channel>(this){
					@Override
					public void onSuccess(Channel result){
						if(VoiceService.getInstance()!=null) {
							VoiceService.getInstance().updateChannel(result);
							onChannelUpdated(result);
						}
					}
				})
				.exec();
	}

	private View makeSectionHeader(@StringRes int text){
		TextView view=(TextView) View.inflate(getActivity(), R.layout.category_header, null);
		view.setText(text);
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		options_menu=menu;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		int remainingusers=speakers.size()+followedBySpeakers.size()+otherUsers.size()-1;
		if(remainingusers>0) {
			new AlertDialog.Builder(getActivity())
					.setMessage(getResources().getQuantityString(R.plurals.end_are_you_sure, (speakers.size()+followedBySpeakers.size()+otherUsers.size()-1)))
					.setTitle(R.string.are_you_sure)
					.setPositiveButton(R.string.end_room, (dialog, which) -> {
						endChannel();
					})
					.setNegativeButton(R.string.cancel, null)
					.show();
		}else{
			endChannel();
		}
		return true;
	}

	private void endChannel() {
		if(VoiceService.getInstance()!=null){
			VoiceService.getInstance().endChannel();
			Nav.finish(this);
		}
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
		if(adapter==null){
			adapter=new MergeRecyclerAdapter();
			adapter.addAdapter(speakersAdapter=new UserListAdapter(speakers, View.generateViewId()));
			adapter.addAdapter(new SingleViewRecyclerAdapter(makeSectionHeader(R.string.followed_by_speakers)));
			adapter.addAdapter(followedAdapter=new UserListAdapter(followedBySpeakers, View.generateViewId()));
			adapter.addAdapter(new SingleViewRecyclerAdapter(makeSectionHeader(R.string.others_in_room)));
			adapter.addAdapter(othersAdapter=new UserListAdapter(otherUsers, View.generateViewId()));
		}
		return adapter;
	}

	private void onBackViewClick(View v) {
		raiseHandsLayout.setVisibility(View.GONE);
	}

	private void onPingClick(View v) {
		Bundle args=new Bundle();
		args.putString("channel", channel.channel);
		Nav.go(getActivity(), PingListFragment.class, args);
	}

	private void onLeaveClick(View v){
		if(VoiceService.getInstance()==null) return;
		VoiceService.getInstance().leaveChannel();
		Nav.finish(this);
	}

	private void onRaiseCtrlClick(View v){
		raiseHandsLayout.setVisibility(View.VISIBLE);
		ArrayList<ChannelUser> raisedHands=new ArrayList<>();
		RaisedHandsAdapter raisedHandsAdapter=new RaisedHandsAdapter(App.applicationContext, R.layout.raised_hand_cell, raisedHands);
		raisedHandsGrid.setAdapter(raisedHandsAdapter);
	}

	private void onRaiseEditClick(View v){
		raiseCtrlMenu.show();
	}

	private void onRaiseClick(View v){
		VoiceService svc=VoiceService.getInstance();
		if(svc.isHandRaised()) {
			svc.unraiseHand();
			raiseBtn.setImageResource(R.drawable.ic_raised_hand_transparent);
		}else {
			svc.raiseHand();
			raiseBtn.setImageResource(R.drawable.ic_raised_hand_yellow);
		}
	}

	private void onMuteClick(View v){
		VoiceService svc=VoiceService.getInstance();
		if(svc==null) return;
		svc.setMuted(!svc.isMuted());
		muteBtn.setImageResource(svc.isMuted() ? R.drawable.ic_mic_off : R.drawable.ic_mic);
		onUserMuteChanged(Integer.parseInt(ClubhouseSession.userID), svc.isMuted());
	}

	private void onRecordClick(View v) {
		MainActivity mainActivity=(MainActivity)getActivity();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(mainActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
				doRecordClick(mainActivity);
			}else{
				mainActivity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.PERMISSION_RESULT_EXTSTORAGE);
			}
		}else{
			doRecordClick(mainActivity);
		}
	}

	public void doRecordClick(Activity activity) {
		VoiceService svc=VoiceService.getInstance();
		if(svc==null) return;
		svc.setRecording(activity, !svc.isRecording(), channel.topic);
		recordBtn.setText(svc.isRecording() ? R.string.stop : R.string.record);
		if(svc.isRecording()) {
			recordTimer.setVisibility(View.VISIBLE);
			timer=new Timer();
			timercounter=0;
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					Activity activity=getActivity();
					if(activity!=null)
						activity.runOnUiThread(() -> {
							int seconds=timercounter%60;
							int minutes=(timercounter/60)%60;
							String tstr=String.format("%02d:%02d",minutes,seconds);
							recordTimer.setText(tstr);
						});
					timercounter++;
				}
			}, 1000, 1000);
		}else{
			if(timer!=null) timer.cancel();
			recordTimer.setText("00:00");
			recordTimer.setVisibility(View.GONE);
		}
	}

	private void onSpeakerClick(View v) {
		VoiceService svc=VoiceService.getInstance();
		if(svc==null) return;
		svc.setSpeakerOn(!svc.isSpeakerOn());
		speakerBtn.setText(svc.isSpeakerOn() ? R.string.speaker_on : R.string.speaker_off);
	}

	@Override
	public void onUserMuteChanged(int id, boolean muted){
		int i=0;
		if(muted){
			if(!mutedUsers.contains(id))
				mutedUsers.add(id);
		}else{
			mutedUsers.remove((Integer)id);
		}
		if(list==null) return;
		for(ChannelUser user:speakers){
			if(user.userId==id){
				user.isMuted=muted;
				RecyclerView.ViewHolder h=list.findViewHolderForAdapterPosition(i);
				if(h instanceof UserViewHolder){
					((UserViewHolder) h).muted.setVisibility(muted ? View.VISIBLE : View.INVISIBLE);
				}
			}
			i++;
		}
	}

	@Override
	public void onUserJoined(ChannelUser user){
		if(user.isSpeaker){
			speakers.add(user);
			speakersAdapter.notifyItemInserted(speakers.size()-1);
		}else if(user.isFollowedBySpeaker){
			followedBySpeakers.add(user);
			followedAdapter.notifyItemInserted(followedBySpeakers.size()-1);
		}else{
			otherUsers.add(user);
			othersAdapter.notifyItemInserted(otherUsers.size()-1);
		}
	}

	@Override
	public void onUserLeft(int id){
		int i=0;
		for(ChannelUser user:speakers){
			if(user.userId==id){
				speakers.remove(user);
				speakersAdapter.notifyItemRemoved(i);
				return;
			}
			i++;
		}
		i=0;
		for(ChannelUser user:followedBySpeakers){
			if(user.userId==id){
				followedBySpeakers.remove(user);
				followedAdapter.notifyItemRemoved(i);
				return;
			}
			i++;
		}
		i=0;
		for(ChannelUser user:otherUsers){
			if(user.userId==id){
				otherUsers.remove(user);
				othersAdapter.notifyItemRemoved(i);
				return;
			}
			i++;
		}
	}

	@Override
	public void onCanSpeak(String inviterName, int inviterID){
		new AlertDialog.Builder(getActivity())
				.setMessage(getString(R.string.confirm_join_as_speaker, inviterName))
				.setPositiveButton(R.string.join, (dialogInterface, i) -> new AcceptSpeakerInvite(channel.channel, inviterID)
						.wrapProgress(getActivity())
						.setCallback(new Callback<BaseResponse>(){
							@Override
							public void onSuccess(BaseResponse result){
								if(VoiceService.getInstance()!=null)
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
	public void onChannelUpdated(Channel channel){
		this.channel=channel;
		if(channel==null) return;
		setTitle(channel.topic);
		speakers.clear();
		followedBySpeakers.clear();
		otherUsers.clear();
		for(ChannelUser user:channel.users){
			if(user.isMuted && !mutedUsers.contains(user.userId))
				mutedUsers.add(user.userId);
			if(user.isSpeaker)
				speakers.add(user);
			else if(user.isFollowedBySpeaker)
				followedBySpeakers.add(user);
			else
				otherUsers.add(user);
		}
		onDataLoaded(channel.users, false);

		VoiceService svc=VoiceService.getInstance();
		if(svc==null) return;
		raiseBtn.setEnabled(channel.isHandraiseEnabled);
		raiseBtn.setVisibility(svc.isSelfSpeaker() ? View.GONE : View.VISIBLE);
		muteBtn.setVisibility(svc.isSelfSpeaker() ? View.VISIBLE : View.GONE);
		raiseCtrlBtn.setVisibility(svc.isSelfModerator() ? View.VISIBLE : View.GONE);
		if(svc.isSelfSpeaker()){
			onUserMuteChanged(Integer.parseInt(ClubhouseSession.userID), svc.isMuted());
		}
		initButtons(svc);
		options_menu.removeItem(0);
		for(ChannelUser user:channel.users) {
			if(user.userId==Integer.parseInt(ClubhouseSession.userID)) {
				if(user.isModerator) {
					options_menu.add(0,0,0, R.string.end_room);
				}
			}
		}
	}

	@Override
	public void onSpeakingUsersChanged(List<Integer> ids){
		speakingUsers.clear();
		speakingUsers.addAll(ids);

		int i=0;
		for(ChannelUser user:speakers){
			RecyclerView.ViewHolder h=list.findViewHolderForAdapterPosition(i);
			if(h instanceof UserViewHolder){
				((UserViewHolder) h).speakerBorder.setAlpha(speakingUsers.contains(user.userId) ? 1 : 0);
			}
			i++;
		}
	}

	@Override
	public void onChannelEnded(){
		Nav.finish(this);
	}

	@Override
	public void onMadeModerator(int id) {
		int i=0;
		for(ChannelUser user:speakers){
			if(user.userId==id){
				user.isModerator=true;
				speakersAdapter.notifyItemChanged(i);
				return;
			}
			i++;
		}
		i=0;
		for(ChannelUser user:followedBySpeakers){
			if(user.userId==id){
				user.isModerator=true;
				followedAdapter.notifyItemChanged(i);
				return;
			}
			i++;
		}
		i=0;
		for(ChannelUser user:otherUsers){
			if(user.userId==id){
				user.isModerator=true;
				othersAdapter.notifyItemChanged(i);
				return;
			}
			i++;
		}
	}

	@Override
	public void onUninvitedAsSpeaker() {
		VoiceService svc=VoiceService.getInstance();
		if(svc==null) return;
		VoiceService.getInstance().rejoinChannel();
		/*svc.updateChannel(svc.getChannel());
		raiseBtn.setVisibility(svc.isSelfSpeaker() ? View.GONE : View.VISIBLE);
		muteBtn.setVisibility(svc.isSelfSpeaker() ? View.VISIBLE : View.GONE);
		int i=0;
		for(ChannelUser user:speakers){
			if(user.userId==Integer.parseInt(ClubhouseSession.userID)){
				user.isSpeaker=false;
				user.isModerator=false;
				speakers.remove(user);
				speakersAdapter.notifyItemRemoved(i);
				if(user.isFollowedBySpeaker){
					followedBySpeakers.add(user);
					followedAdapter.notifyItemInserted(followedBySpeakers.size()-1);
				}else{
					otherUsers.add(user);
					othersAdapter.notifyItemInserted(otherUsers.size()-1);
				}
				return;
			}
			i++;
		}*/
	}

	@Override
	public void onRemoveAsSpeaker(int id) {
		int i=0;
		for(ChannelUser user:speakers){
			if(user.userId==Integer.parseInt(ClubhouseSession.userID)){
				user.isModerator=false;
				speakersAdapter.notifyItemChanged(i);
				return;
			}
			i++;
		}
	}

	@Override
	public void onSpeakerMuted(boolean speakerOn) {
		speakerBtn.setText(speakerOn ? R.string.speaker_on : R.string.speaker_off);
	}

	private class UserListAdapter extends RecyclerView.Adapter<UserViewHolder> implements ImageLoaderRecyclerAdapter{

		private final List<ChannelUser> users;
		private final int type;

		public UserListAdapter(List<ChannelUser> users, int type){
			this.users=users;
			this.type=type;
		}

		@NonNull
		@Override
		public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new UserViewHolder(users==speakers);
		}

		@Override
		public void onBindViewHolder(@NonNull UserViewHolder holder, int position){
			holder.bind(users.get(position));
		}

		@Override
		public int getItemViewType(int position){
			return type;
		}

		@Override
		public int getItemCount(){
			return users.size();
		}

		@Override
		public int getImageCountForItem(int position){
			return users.get(position).photoUrl!=null ? 1 : 0;
		}

		@Override
		public String getImageURL(int position, int image){
			return users.get(position).photoUrl;
		}
	}

	private class UserViewHolder extends BindableViewHolder<ChannelUser> implements ImageLoaderViewHolder, UsableRecyclerView.Clickable, UsableRecyclerView.LongClickable {

		private ImageView photo, muted;
		private TextView name;
		private View speakerBorder;
		private Drawable placeholder=new ColorDrawable(getResources().getColor(R.color.grey));

		public UserViewHolder(boolean large){
			super(getActivity(), R.layout.channel_user_cell, list);

			photo=findViewById(R.id.photo);
			name=findViewById(R.id.name);
			muted=findViewById(R.id.muted);
			speakerBorder=findViewById(R.id.speaker_border);

			ViewGroup.LayoutParams lp=photo.getLayoutParams();
			lp.width=lp.height=V.dp(large ? 72 : 48);
			muted.setVisibility(View.INVISIBLE);
			if(!large)
				speakerBorder.setVisibility(View.GONE);
			else
				speakerBorder.setAlpha(0);
		}

		@Override
		public void onBind(ChannelUser item){
			if(item.isModerator)
				name.setText(getString(R.string.moderator, item.firstName));
			else
				name.setText(item.firstName);
			muted.setVisibility(mutedUsers.contains(item.userId) ? View.VISIBLE : View.INVISIBLE);
			speakerBorder.setAlpha(speakingUsers.contains(item.userId) ? 1 : 0);

			if(item.photoUrl==null)
				photo.setImageDrawable(placeholder);
			else
				imgLoader.bindViewHolder(adapter, this, getAdapterPosition());
		}

		@Override
		public void setImage(int index, Bitmap bitmap){
			photo.setImageBitmap(bitmap);
		}

		@Override
		public void clearImage(int index){
			photo.setImageDrawable(placeholder);
		}

		@Override
		public void onClick(){
			Bundle args=new Bundle();
			args.putInt("id", item.userId);
			Nav.go(getActivity(), ProfileFragment.class, args);
		}

		@Override
		public boolean onLongClick() {
			PopupMenu longClickMenu = new PopupMenu(getActivity(), photo);
			if(item.isSpeaker) {
				boolean mefound= speakers.stream().anyMatch(o -> o.userId==Integer.parseInt(ClubhouseSession.userID));
				if(mefound) {
					ChannelUser me=speakers.stream().filter(o -> o.userId==Integer.parseInt(ClubhouseSession.userID)).findFirst().get();
					if(me.isModerator || item.userId==Integer.parseInt(ClubhouseSession.userID)) longClickMenu.getMenu().add(Menu.NONE, 0, 0, getResources().getString(R.string.move_to_audience));
				}
			}else{
				ChannelUser me=null;
				boolean mefound= followedBySpeakers.stream().anyMatch(o -> o.userId==Integer.parseInt(ClubhouseSession.userID));
				if(mefound) {
					me = followedBySpeakers.stream().filter(o -> o.userId == Integer.parseInt(ClubhouseSession.userID)).findFirst().get();
				}
				mefound= otherUsers.stream().anyMatch(o -> o.userId==Integer.parseInt(ClubhouseSession.userID));
				if(mefound) {
					me = otherUsers.stream().filter(o -> o.userId == Integer.parseInt(ClubhouseSession.userID)).findFirst().get();
				}
				if (me!=null && me.isModerator)
					longClickMenu.getMenu().add(Menu.NONE, 0, 0, getResources().getString(R.string.invite_to_speak));
			}
			longClickMenu.setOnMenuItemClickListener(menuItem -> {
				if(item.isSpeaker) {
					new UninviteSpeaker(channel.channel, item.userId)
							.exec();
					item.isSpeaker=false;
					int i = 0;
					for (ChannelUser user : speakers) {
						if (user.userId == item.userId) {
							speakers.remove(user);
							speakersAdapter.notifyItemRemoved(i);
							break;
						}
						i++;
					}
					if (item.isFollowedBySpeaker) {
						followedBySpeakers.add(item);
						followedAdapter.notifyItemInserted(followedBySpeakers.size() - 1);
					} else {
						otherUsers.add(item);
						othersAdapter.notifyItemInserted(otherUsers.size() - 1);
					}
				}else{
					new InviteSpeaker(channel.channel, item.userId).exec();
					item.isSpeaker=true;
					speakers.add(item);
					speakersAdapter.notifyItemInserted(speakers.size() - 1);
					if (item.isFollowedBySpeaker) {
						int i = 0;
						for (ChannelUser user : followedBySpeakers) {
							if (user.userId == item.userId) {
								followedBySpeakers.remove(user);
								followedAdapter.notifyItemRemoved(i);
								break;
							}
							i++;
						}
					}else{
						int i = 0;
						for (ChannelUser user : otherUsers) {
							if (user.userId == item.userId) {
								otherUsers.remove(user);
								othersAdapter.notifyItemRemoved(i);
								break;
							}
							i++;
						}
					}
				}
				return false;
			});
			longClickMenu.show();
			return true;
		}
	}

	public static class RaisedHandsAdapter extends ArrayAdapter<ChannelUser> {
		Context context;
		LayoutInflater inflter;
		ArrayList<ChannelUser> users;

		public RaisedHandsAdapter(Context applicationContext, int cell_id, ArrayList<ChannelUser> objects) {
			super(applicationContext, cell_id, objects);
			this.context = applicationContext;
			this.users = objects;
			inflter = (LayoutInflater.from(applicationContext));
		}

		@Override
		public int getCount() {
			return super.getCount();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;

			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.raised_hand_cell, parent);
				viewHolder = new ViewHolder(convertView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.item_text.setText(users.get(position).firstName);
			//viewHolder.item_check.setChecked(users.get(position).getLoremCheck());

			return convertView;
		}

		private static class ViewHolder {
			public TextView item_text;
			public ImageView item_icon;

			public ViewHolder(View convertView) {
				item_text  = (TextView) convertView.findViewById(R.id.name);
				item_icon = (ImageView) convertView.findViewById(R.id.icon);
			}
		}
	}
}
