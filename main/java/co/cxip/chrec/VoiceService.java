package co.cxip.chrec;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import co.cxip.chrec.api.methods.EndChannel;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseAPIController;
import co.cxip.chrec.api.ClubhouseSession;
import co.cxip.chrec.api.methods.ActivePing;
import co.cxip.chrec.api.methods.AudienceReply;
import co.cxip.chrec.api.methods.JoinChannel;
import co.cxip.chrec.api.methods.LeaveChannel;
import co.cxip.chrec.api.model.Channel;
import co.cxip.chrec.api.model.ChannelUser;
import co.cxip.chrec.notification.NotificationHandlerBroadcastReceiver;

import static io.agora.rtc.Constants.AUDIO_RECORDING_QUALITY_HIGH;
import static io.agora.rtc.Constants.AUDIO_RECORDING_QUALITY_MEDIUM;

public class VoiceService extends Service{

	private RtcEngine engine;
	private Channel channel;
	private String notificationContentText;
	private boolean muted=true;
	private boolean recording=false;
	private boolean speakerOn=true;
	private final Handler uiHandler=new Handler(Looper.getMainLooper());
	private final Runnable pinger=new Runnable(){
		@Override
		public void run(){
			new ActivePing(channel.channel)
								//.wrapProgress(App.applicationContext)
								.setCallback(new Callback<ActivePing.Response>() {
									@Override
									public void onSuccess(ActivePing.Response result) {
										if(result.shouldLeave) leaveChannel();
									}
									@Override
									public void onError(ErrorResponse error) {
									}
								})
								.exec();
			uiHandler.postDelayed(this, 30000);
		}
	};
	private boolean raisedHand=false;
	private PubNub pubnub;
	//private final ArrayList<Integer> mutedUserIds=new ArrayList<>();
	private boolean isSelfSpeaker, isSelfModerator;

	private static final ArrayList<ChannelEventListener> listeners=new ArrayList<>();

	private static VoiceService instance;
	private static final String TAG="VoiceService";

	public static VoiceService getInstance(){
		return instance;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent){
		return null;
	}

	@Override
	public void onCreate(){
		super.onCreate();

		try{
			engine=RtcEngine.create(getBaseContext(), ClubhouseAPIController.AGORA_KEY, new RtcEngineEventHandler());
		}catch(Exception x){
			Log.e(TAG, "Error initializing agora", x);
			stopSelf();
			return;
		}

		engine.setDefaultAudioRoutetoSpeakerphone(true);
		engine.enableAudioVolumeIndication(500, 3, false);
		engine.muteLocalAudioStream(true);
		instance=this;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		RtcEngine.destroy();
		instance=null;
	}

	private Notification getNotification() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
			Intent snoozeIntent = new Intent(this, NotificationHandlerBroadcastReceiver.class);
			snoozeIntent.setAction(NotificationHandlerBroadcastReceiver.ACTION_LEAVE_ROOM);
			PendingIntent leaveRoomPendingIntent = PendingIntent.getBroadcast(this, 0, snoozeIntent, 0);
			Notification.Action leaveRoomAction = null;
			leaveRoomAction = new Notification.Action.Builder(
					Icon.createWithResource(this, R.drawable.ic_leave),
					getString(R.string.leave_room),
					leaveRoomPendingIntent
			).build();

			Intent muteIntent = new Intent(this, NotificationHandlerBroadcastReceiver.class);
			muteIntent.setAction(NotificationHandlerBroadcastReceiver.ACTION_MUTE);
			PendingIntent mutePendingIntent = PendingIntent.getBroadcast(this, 0, muteIntent, 0);
			Notification.Action muteAction;
			if(speakerOn) {
				muteAction = new Notification.Action.Builder(
						Icon.createWithResource(this, R.drawable.ic_speaker_with_cancellation_stroke),
						getString(R.string.mute_room),
						mutePendingIntent
				).build();
			}else{
				muteAction = new Notification.Action.Builder(
						Icon.createWithResource(this, R.drawable.ic_speaker_with_three_sound_waves),
						getString(R.string.unmte_room),
						mutePendingIntent
				).build();
			}
			Notification.Builder n=new Notification.Builder(this)
					.setSmallIcon(R.drawable.ic_wavinghandwhite)
					.setContentTitle(getString(R.string.ongoing_call))
					.setContentText(notificationContentText)
					.setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class).putExtra("openCurrentChannel", true), PendingIntent.FLAG_UPDATE_CURRENT))
					.addAction(leaveRoomAction)
					.addAction(muteAction);

			if(Build.VERSION.SDK_INT>=26){
				NotificationManager nm=getSystemService(NotificationManager.class);
				if(nm.getNotificationChannel("ongoing")==null){
					NotificationChannel nc=new NotificationChannel("ongoing", "Ongoing conversation", NotificationManager.IMPORTANCE_LOW);
					nm.createNotificationChannel(nc);
				}
				n.setChannelId("ongoing");
			}
			return n.build();
		}else{
			Intent snoozeIntent = new Intent(this, NotificationHandlerBroadcastReceiver.class);
			snoozeIntent.setAction(NotificationHandlerBroadcastReceiver.ACTION_LEAVE_ROOM);
			PendingIntent leaveRoomPendingIntent = PendingIntent.getBroadcast(this, 0, snoozeIntent, 0);
			Intent muteIntent = new Intent(this, NotificationHandlerBroadcastReceiver.class);
			muteIntent.setAction(NotificationHandlerBroadcastReceiver.ACTION_MUTE);
			PendingIntent mutePendingIntent = PendingIntent.getBroadcast(this, 0, muteIntent, 0);
			if(speakerOn) {
				Notification.Builder n=new Notification.Builder(this)
						.setSmallIcon(R.drawable.ic_wavinghandwhite)
						.setContentTitle(getString(R.string.ongoing_call))
						.setContentText(notificationContentText)
						.setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class).putExtra("openCurrentChannel", true), PendingIntent.FLAG_UPDATE_CURRENT))
						.addAction(R.drawable.ic_speaker_with_cancellation_stroke, getString(R.string.mute_room), mutePendingIntent)
						.addAction(R.drawable.ic_leave, getString(R.string.leave_room), leaveRoomPendingIntent);
				return n.build();
			}else{
				Notification.Builder n=new Notification.Builder(this)
						.setSmallIcon(R.drawable.ic_wavinghandwhite)
						.setContentTitle(getString(R.string.ongoing_call))
						.setContentText(notificationContentText)
						.setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class).putExtra("openCurrentChannel", true), PendingIntent.FLAG_UPDATE_CURRENT))
						.addAction(R.drawable.ic_speaker_with_three_sound_waves, getString(R.string.unmte_room), mutePendingIntent)
						.addAction(R.drawable.ic_leave, getString(R.string.leave_room), leaveRoomPendingIntent);
				return n.build();
			}
		}
	}

	public void updateNotification() {
		Notification notification = getNotification();
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(10, notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		if(engine!=null){
			String id = intent.getStringExtra("channel");
			channel=DataProvider.getChannel(id);
			if(channel!=null) {
				updateChannel(channel);
				notificationContentText = intent.getStringExtra("topic");
				Notification n = getNotification();
				startForeground(10, n);
				doJoinChannel();
			}
		}
		return START_NOT_STICKY;
	}

	private void doJoinChannel(){
		//engine.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT, Constants.AUDIO_SCENARIO_CHATROOM_ENTERTAINMENT);
		engine.setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT, Constants.AUDIO_SCENARIO_DEFAULT);
		engine.setChannelProfile(isSelfSpeaker ? Constants.CHANNEL_PROFILE_COMMUNICATION : Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
		engine.joinChannel(channel.token, channel.channel, "", Integer.parseInt(ClubhouseSession.userID));
		uiHandler.postDelayed(pinger, 30000);
		for(ChannelEventListener l:listeners)
			l.onChannelUpdated(channel);

		PNConfiguration pnConf=new PNConfiguration();
		pnConf.setSubscribeKey(ClubhouseAPIController.PUBNUB_SUB_KEY);
		pnConf.setPublishKey(ClubhouseAPIController.PUBNUB_PUB_KEY);
		//pnConf.setUuid(UUID.randomUUID().toString());
		pnConf.setOrigin("clubhouse.pubnub.com");
		pnConf.setUuid(ClubhouseSession.userID);
		pnConf.setPresenceTimeoutWithCustomInterval(channel.pubnubHeartbeatValue, channel.pubnubHeartbeatInterval);
		pnConf.setAuthKey(channel.pubnubToken);

		pubnub=new PubNub(pnConf);
		pubnub.addListener(new SubscribeCallback(){
			@Override
			public void status(@NonNull PubNub pubnub, @NonNull PNStatus pnStatus){
				Log.d(TAG, "status() called with: pubnub = ["+pubnub+"], pnStatus = ["+pnStatus+"]");
			}

			@Override
			public void message(@NonNull PubNub pubnub, @NonNull PNMessageResult pnMessageResult){
				Log.d(TAG, "message() called with: pubnub = ["+pubnub+"], pnMessageResult = ["+pnMessageResult+"]");
				JsonObject msg=pnMessageResult.getMessage().getAsJsonObject();
				String act=msg.get("action").getAsString();
				switch(act){
					case "uninvite_speaker":
						onUninvitedAsSpeaker(msg);
						break;
					case "invite_speaker":
						onInvitedAsSpeaker(msg);
						break;
					case "remove_speaker":
						onRemoveAsSpeaker(msg);
						break;
					case "join_channel":
						onUserJoined(msg);
						break;
					case "leave_channel":
						onUserLeft(msg);
						break;
					case "end_channel":
						onEndChannel(msg);
						break;
					case "make_moderator":
						onMakeModerator(msg);
				}
			}

			@Override
			public void presence(@NonNull PubNub pubnub, @NonNull PNPresenceEventResult pnPresenceEventResult){
				Log.d(TAG, "presence() called with: pubnub = ["+pubnub+"], pnPresenceEventResult = ["+pnPresenceEventResult+"]");
			}

			@Override
			public void signal(@NonNull PubNub pubnub, @NonNull PNSignalResult pnSignalResult){
				Log.d(TAG, "signal() called with: pubnub = ["+pubnub+"], pnSignalResult = ["+pnSignalResult+"]");
			}

			@Override
			public void uuid(@NonNull PubNub pubnub, @NonNull PNUUIDMetadataResult pnUUIDMetadataResult){

			}

			@Override
			public void channel(@NonNull PubNub pubnub, @NonNull PNChannelMetadataResult pnChannelMetadataResult){
				Log.d(TAG, "channel() called with: pubnub = ["+pubnub+"], pnChannelMetadataResult = ["+pnChannelMetadataResult+"]");
			}

			@Override
			public void membership(@NonNull PubNub pubnub, @NonNull PNMembershipResult pnMembershipResult){
				Log.d(TAG, "membership() called with: pubnub = ["+pubnub+"], pnMembershipResult = ["+pnMembershipResult+"]");
			}

			@Override
			public void messageAction(@NonNull PubNub pubnub, @NonNull PNMessageActionResult pnMessageActionResult){
				Log.d(TAG, "messageAction() called with: pubnub = ["+pubnub+"], pnMessageActionResult = ["+pnMessageActionResult+"]");
			}

			@Override
			public void file(@NonNull PubNub pubnub, @NonNull PNFileEventResult pnFileEventResult){

			}
		});
		pubnub.subscribe().channels(Arrays.asList(
				"users."+ClubhouseSession.userID,
				"channel_user."+channel.channel+"."+ClubhouseSession.userID,
//				"channel_speakers."+channel.channel,
				"channel_all."+channel.channel
		)).execute();
	}

	public void leaveChannel(){
		engine.leaveChannel();
		if(channel!=null) new LeaveChannel(channel.channel).exec();
		stopSelf();
		uiHandler.removeCallbacks(pinger);
		pubnub.unsubscribeAll();
		pubnub.destroy();
		channel=null;
	}

	public void endChannel(){
		engine.leaveChannel();
		new EndChannel(channel.channel, channel.channelId)
				.exec();
		stopSelf();
		uiHandler.removeCallbacks(pinger);
		pubnub.unsubscribeAll();
		pubnub.destroy();
		channel=null;
	}

	public void leaveCurrentChannel(){
		uiHandler.post(() -> {
			for(ChannelEventListener l:listeners)
				l.onChannelEnded();
		});
		leaveChannel();
	}

	public void rejoinChannel(){
		engine.leaveChannel();
		pubnub.unsubscribeAll();
		new LeaveChannel(channel.channel)
				.setCallback(new Callback<BaseResponse>(){
					@Override
					public void onSuccess(BaseResponse result){
						if(channel==null) return;
						new JoinChannel(channel.channel)
								.setCallback(new Callback<Channel>(){
									@Override
									public void onSuccess(Channel result){
										updateChannel(result);
										doJoinChannel();
									}

									@Override
									public void onError(ErrorResponse error){

									}
								})
								.exec();
					}

					@Override
					public void onError(ErrorResponse error){

					}
				})
				.exec();
	}

	public void raiseHand(){
		if(!raisedHand){
			raisedHand=true;
			new AudienceReply(channel.channel, true)
					.exec();
		}
	}

	public void unraiseHand(){
		if(raisedHand){
			raisedHand=false;
			new AudienceReply(channel.channel, false)
					.exec();
		}
	}

	public boolean isHandRaised(){
		return raisedHand;
	}

	public void setMuted(boolean muted){
		this.muted=muted;
		engine.muteLocalAudioStream(muted);
	}

	public boolean isMuted(){
		return muted;
	}

	public void setRecording(Activity activity, boolean recording, String channelName){
		this.recording=recording;
		if(recording) {
			//File externalStorageDirectory = Environment.getExternalStorageDirectory();
			//File mainDir = new File(externalStorageDirectory + File.separator + "CHRec");
			File mainDir = new File(activity.getExternalFilesDir(null) + File.separator + "CHRec");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
			Date now = new Date();
			String filename=formatter.format(now) + ".wav";
			String filepath = mainDir.toString() + File.separator + filename;
			SharedPreferences pref= activity.getSharedPreferences("CHRecFiles", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor= pref.edit();
			editor.putString(filename, channelName);
			editor.apply();
			engine.startAudioRecording(filepath, 32000, AUDIO_RECORDING_QUALITY_MEDIUM);
		}else{
			engine.stopAudioRecording();
		}
	}

	public boolean isRecording(){
		return recording;
	}

	public void setSpeakerOn(boolean speakerOn) {
		this.speakerOn=speakerOn;
		engine.muteAllRemoteAudioStreams(!speakerOn);
		uiHandler.post(() -> {
			for(ChannelEventListener l:listeners)
				l.onSpeakerMuted(speakerOn);
		});
		/*if(speakerOn) {
			engine.adjustPlaybackSignalVolume(100);
			//engine.adjustAudioMixingVolume(100);
		}else{
			engine.adjustPlaybackSignalVolume(0);
			//engine.adjustAudioMixingVolume(0);
		}*/
	}

	public boolean isSpeakerOn() {
		return speakerOn;
	}

	public Channel getChannel(){
		return channel;
	}

	public void updateChannel(Channel chan){
		channel=chan;
		isSelfModerator=false;
		isSelfSpeaker=false;
		if(channel==null) return;
		int id=Integer.parseInt(ClubhouseSession.userID);
		for(ChannelUser user:channel.users){
			if(user.userId==id){
				isSelfModerator=user.isModerator;
				isSelfSpeaker=user.isSpeaker;
				break;
			}
		}
		if(isSelfSpeaker) raisedHand=false;
	}

	private void callAddedListener(ChannelEventListener l){
		l.onChannelUpdated(channel);
	}

	public static void addListener(ChannelEventListener l){
		if(!listeners.contains(l))
			listeners.add(l);
		if(getInstance()!=null){
			getInstance().callAddedListener(l);
		}
	}

	public static void removeListener(ChannelEventListener l){
		listeners.remove(l);
	}

	public boolean isSelfSpeaker(){
		return isSelfSpeaker;
	}

	public boolean isSelfModerator(){
		return isSelfModerator;
	}

	private void onInvitedAsSpeaker(JsonObject msg){
		if(channel==null) return;
		String ch=msg.get("channel").getAsString();
		if(!ch.equals(channel.channel))
			return;
		uiHandler.post(() -> {
			for(ChannelEventListener l:listeners)
				l.onCanSpeak(msg.get("from_name").getAsString(), msg.get("from_user_id").getAsInt());
		});
	}

	private void onUserJoined(JsonObject msg){
		if(channel==null) return;
		String ch=msg.get("channel").getAsString();
		if(!ch.equals(channel.channel))
			return;
		JsonObject profile=msg.getAsJsonObject("user_profile");
		ChannelUser user=ClubhouseAPIController.getInstance().getGson().fromJson(profile, ChannelUser.class);
		uiHandler.post(() -> {
			if(channel==null) return;
			channel.users.add(user);
			for(ChannelEventListener l:listeners)
				l.onUserJoined(user);
		});
	}

	private void onUserLeft(JsonObject msg){
		if(channel==null) return;
		String ch=msg.get("channel").getAsString();
		if(!ch.equals(channel.channel))
			return;
		int id=msg.get("user_id").getAsInt();
		uiHandler.post(() -> {
			if(channel==null) return;
			for(ChannelUser user:channel.users){
				if(user.userId==id){
					channel.users.remove(user);
					break;
				}
			}
			for(ChannelEventListener l:listeners)
				l.onUserLeft(id);
		});
	}

	private void onEndChannel(JsonObject msg){
		if(channel==null) return;
		String ch=msg.get("channel").getAsString();
		if(!ch.equals(channel.channel))
			return;
		uiHandler.post(() -> {
			for(ChannelEventListener l:listeners)
				l.onChannelEnded();
		});
		leaveChannel();
	}

	private void onMakeModerator(JsonObject msg) {
		if(channel==null) return;
		String ch=msg.get("channel").getAsString();
		if(!ch.equals(channel.channel))
			return;
		int id=msg.get("user_id").getAsInt();
		uiHandler.post(() -> {
			for(ChannelEventListener l:listeners)
				l.onMadeModerator(id);
		});
	}

	private void onUninvitedAsSpeaker(JsonObject msg) {
		if(channel==null) return;
		String ch=msg.get("channel").getAsString();
		if(!ch.equals(channel.channel))
			return;
		uiHandler.post(() -> {
			for(ChannelEventListener l:listeners)
				l.onUninvitedAsSpeaker();
		});
	}

	private void onRemoveAsSpeaker(JsonObject msg) {
		if(channel==null) return;
		String ch=msg.get("channel").getAsString();
		if(!ch.equals(channel.channel))
			return;
		int id=msg.get("user_id").getAsInt();
		uiHandler.post(() -> {
			for(ChannelEventListener l:listeners)
				l.onRemoveAsSpeaker(id);
		});
	}

	public interface ChannelEventListener{
		void onUserMuteChanged(int id, boolean muted);
		void onUserJoined(ChannelUser user);
		void onUserLeft(int id);
		void onCanSpeak(String inviterName, int inviterID);
		void onChannelUpdated(Channel channel);
		void onSpeakingUsersChanged(List<Integer> ids);
		void onChannelEnded();
		void onMadeModerator(int id);
		void onUninvitedAsSpeaker();
		void onRemoveAsSpeaker(int id);
		void onSpeakerMuted(boolean speakerOn);
	}

	private class RtcEngineEventHandler extends IRtcEngineEventHandler{
		@Override
		public void onJoinChannelSuccess(String channel, int uid, int elapsed){
			Log.d(TAG, "onJoinChannelSuccess() called with: channel = ["+channel+"], uid = ["+uid+"], elapsed = ["+elapsed+"]");
		}

		@Override
		public void onError(int err){
			Log.d(TAG, "onError() called with: err = ["+err+"]");
		}

		@Override
		public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume){
//			Log.d(TAG, "onAudioVolumeIndication() called with: speakers = ["+Arrays.toString(speakers)+"], totalVolume = ["+totalVolume+"]");
			if(ClubhouseSession.userID==null || ClubhouseSession.userID.length()<1) return;
			uiHandler.post(() -> {
				int selfID=Integer.parseInt(ClubhouseSession.userID);
				List<Integer> uids=Arrays.stream(speakers).map(s -> s.uid==0 ? selfID : s.uid).collect(Collectors.toList());
				for(ChannelEventListener l:listeners)
					l.onSpeakingUsersChanged(uids);
			});
		}

		@Override
		public void onUserMuteAudio(int uid, boolean muted){
//			Log.d(TAG, "onUserMuteAudio() called with: uid = ["+uid+"], muted = ["+muted+"]");
			uiHandler.post(() -> {
				if(channel==null) return;
				for(ChannelUser u:channel.users){
					if(u.userId==uid){
						u.isMuted=muted;
						break;
					}
				}
				for(ChannelEventListener l:listeners)
					l.onUserMuteChanged(uid, muted);
			});
		}
	}
}
