package co.cxip.chrec;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;

import co.cxip.chrec.api.methods.CreateChannel;
import co.cxip.chrec.fragments.InterestsListFragment;
import co.cxip.chrec.fragments.ProfileFragment;
import co.cxip.chrec.fragments.RegisterFragment;
import me.grishka.appkit.FragmentStackActivity;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import co.cxip.chrec.api.ClubhouseSession;
import co.cxip.chrec.api.methods.CheckWaitlistStatus;
import co.cxip.chrec.api.methods.GetChannel;
import co.cxip.chrec.api.methods.GetEvent;
import co.cxip.chrec.api.methods.JoinChannel;
import co.cxip.chrec.api.model.Channel;
import co.cxip.chrec.fragments.HomeFragment;
import co.cxip.chrec.fragments.InChannelFragment;
import co.cxip.chrec.fragments.LoginFragment;
import co.cxip.chrec.fragments.WaitlistedFragment;
import me.grishka.appkit.fragments.AppKitFragment;

public class MainActivity extends FragmentStackActivity{
	private Channel channelToJoin;
	private String channelNameToJoin;
	private CreateChannel.Body channelToCreate;
	InChannelFragment inChannelFragment;
	private static final int PERMISSION_RESULT=270;
	public static final int PERMISSION_RESULT_EXTSTORAGE=280;
	private static final int PERMISSION_RESULT_CREATE_CHANNEL=290;
	private static final int PERMISSION_RESULT_JOIN_CHANNAME=300;
	public boolean create_room_panel_visible=false;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		/*SharedPreferences prefs=getPreferences(MODE_PRIVATE);
		if(!prefs.getBoolean("warningShown", false)){
			new AlertDialog.Builder(this)
					.setTitle(R.string.warning)
					.setMessage(R.string.warning_text)
					.setPositiveButton(R.string.i_understand, null)
					.setCancelable(false)
					.show();
			prefs.edit().putBoolean("warningShown", true).apply();
		}*/

		if(ClubhouseSession.isLoggedIn()){
			if(ClubhouseSession.isWaitlisted) {
				showFragment(new WaitlistedFragment());
			}else {
				HomeFragment homeFragment=new HomeFragment();
				Bundle extras=new Bundle();
				extras.putBoolean(AppKitFragment.EXTRA_IS_TAB, true);
				homeFragment.setArguments(extras);
				showFragment(homeFragment);
			}
			if(ClubhouseSession.isWaitlisted){
				new CheckWaitlistStatus()
						.setCallback(new Callback<CheckWaitlistStatus.Response>(){
							@Override
							public void onSuccess(CheckWaitlistStatus.Response result){
								if(!result.isWaitlisted){
									ClubhouseSession.isWaitlisted=false;
									ClubhouseSession.write();
									if(result.isOnboarding){
//										showFragmentClearingBackStack(new RegisterFragment());
										ClubhouseSession.userID=ClubhouseSession.userToken=null;
										ClubhouseSession.write();
										if(!isFinishing()) {
											new AlertDialog.Builder(MainActivity.this)
													.setMessage(R.string.log_in_to_activate)
													.setPositiveButton(R.string.ok, null)
													.show();
											showFragmentClearingBackStack(new LoginFragment());
										}
									}else{
										HomeFragment homeFragment=new HomeFragment();
										Bundle extras=new Bundle();
										extras.putBoolean(AppKitFragment.EXTRA_IS_TAB, true);
										homeFragment.setArguments(extras);
										showFragmentClearingBackStack(homeFragment);
									}
//									if(Intent.ACTION_VIEW.equals(getIntent().getAction())){
//										joinChannelFromIntent();
//									}
								}
							}

							@Override
							public void onError(ErrorResponse error){

							}
						})
						.exec();
			}else{
				if(Intent.ACTION_VIEW.equals(getIntent().getAction())){
					joinChannelFromIntent();
				}
			}
		}else{
			showFragment(new LoginFragment());
		}
	}

	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		setIntent(intent);
		if(Intent.ACTION_VIEW.equals(intent.getAction())){
			joinChannelFromIntent();
		}else if(intent.hasExtra("openCurrentChannel")){
			if(VoiceService.getInstance()!=null){
				Bundle extras=new Bundle();
				extras.putBoolean("_can_go_back", true);
				InChannelFragment fragment=new InChannelFragment();
				fragment.setArguments(extras);
				showFragment(fragment);
			}
		}
	}

	private void joinChannelFromIntent(){
		Uri data=getIntent().getData();
		List<String> path=data.getPathSegments();
		String id=path.get(path.size()-1);
		if(path.get(0).equals("room")){
			joinChannelById(id);
//		}else if(path.get(0).equals("club")){
		}else if(path.get(0).equals("event")){
			new GetEvent(id)
					.wrapProgress(this)
					.setCallback(new Callback<GetEvent.Response>(){
						@Override
						public void onSuccess(GetEvent.Response result){
							if(result.event.channel!=null){
								joinChannelById(result.event.channel);
							}else{
								if(result.event.isExpired)
									Toast.makeText(MainActivity.this, R.string.event_expired, Toast.LENGTH_SHORT).show();
								else if(result.event.timeStart.after(new Date()))
									Toast.makeText(MainActivity.this, R.string.event_not_started, Toast.LENGTH_SHORT).show();
							}
						}

						@Override
						public void onError(ErrorResponse error){
							error.showToast(MainActivity.this);
						}
					})
					.exec();
		}
	}

	private void joinChannelById(String id){
		new GetChannel(id)
				.wrapProgress(this)
				.setCallback(new Callback<Channel>(){
					@Override
					public void onSuccess(final Channel result){
						new AlertDialog.Builder(MainActivity.this)
								.setTitle(R.string.join_this_room)
								.setMessage(result.topic)
								.setPositiveButton(R.string.join, (dialogInterface, i) -> joinChannel(result))
								.setNegativeButton(R.string.cancel, null)
								.show();
					}

					@Override
					public void onError(ErrorResponse error){
						error.showToast(MainActivity.this);
					}
				})
				.exec();
	}

	public void joinChannel(Channel chan){
		if(chan==null) return;
		if(VoiceService.getInstance()!=null){
			Channel current=VoiceService.getInstance().getChannel();
			if(current!=null) {
				if (current.channel.equals(chan.channel)) {
					Bundle extras = new Bundle();
					extras.putBoolean("_can_go_back", true);
					inChannelFragment = new InChannelFragment();
					inChannelFragment.setArguments(extras);
					showFragment(inChannelFragment);
					return;
				}
				VoiceService.getInstance().leaveChannel();
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){
				new JoinChannel(chan.channel)
						.wrapProgress(this)
						.setCallback(new Callback<Channel>(){
							@Override
							public void onSuccess(Channel result){
								Intent intent=new Intent(MainActivity.this, VoiceService.class);
								intent.putExtra("channel", result.channel);
								DataProvider.saveChannel(result);
								if(Build.VERSION.SDK_INT>=26)
									startForegroundService(intent);
								else
									startService(intent);

								Bundle extras=new Bundle();
								extras.putBoolean("_can_go_back", true);
								inChannelFragment=new InChannelFragment();
								inChannelFragment.setArguments(extras);
								showFragment(inChannelFragment);
							}
							@Override
							public void onError(ErrorResponse error){
								error.showToast(MainActivity.this);
							}
						})
						.exec();
			}else{
				channelToJoin=chan;
				requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_RESULT);
			}
		}else{
			new JoinChannel(chan.channel)
					.wrapProgress(this)
					.setCallback(new Callback<Channel>(){
						@Override
						public void onSuccess(Channel result){
							Intent intent=new Intent(MainActivity.this, VoiceService.class);
							intent.putExtra("channel", result.channel);
							DataProvider.saveChannel(result);
							startService(intent);
							Bundle extras=new Bundle();
							extras.putBoolean("_can_go_back", true);
							inChannelFragment=new InChannelFragment();
							inChannelFragment.setArguments(extras);
							showFragment(inChannelFragment);
						}
						@Override
						public void onError(ErrorResponse error){
							error.showToast(MainActivity.this);
						}
					})
					.exec();
		}
	}

	public void joinChannelByName(String chanName){
		if(VoiceService.getInstance()!=null){
			Channel current=VoiceService.getInstance().getChannel();
			if(current.channel.equals(chanName)){
				Bundle extras=new Bundle();
				extras.putBoolean("_can_go_back", true);
				inChannelFragment=new InChannelFragment();
				inChannelFragment.setArguments(extras);
				showFragment(inChannelFragment);
				return;
			}
			VoiceService.getInstance().leaveChannel();
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){
				new JoinChannel(chanName)
						.wrapProgress(this)
						.setCallback(new Callback<Channel>(){
							@Override
							public void onSuccess(Channel result){
								Intent intent=new Intent(MainActivity.this, VoiceService.class);
								intent.putExtra("channel", result.channel);
								DataProvider.saveChannel(result);
								if(Build.VERSION.SDK_INT>=26)
									startForegroundService(intent);
								else
									startService(intent);
								Bundle extras=new Bundle();
								extras.putBoolean("_can_go_back", true);
								inChannelFragment=new InChannelFragment();
								inChannelFragment.setArguments(extras);
								showFragment(inChannelFragment);
							}
							@Override
							public void onError(ErrorResponse error){
								error.showToast(MainActivity.this);
							}
						})
						.exec();
			}else{
				channelNameToJoin=chanName;
				requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_RESULT_JOIN_CHANNAME);
			}
		}else{
			new JoinChannel(chanName)
					.wrapProgress(this)
					.setCallback(new Callback<Channel>(){
						@Override
						public void onSuccess(Channel result){
							Intent intent=new Intent(MainActivity.this, VoiceService.class);
							intent.putExtra("channel", result.channel);
							DataProvider.saveChannel(result);
							startService(intent);
							Bundle extras=new Bundle();
							extras.putBoolean("_can_go_back", true);
							inChannelFragment=new InChannelFragment();
							inChannelFragment.setArguments(extras);
							showFragment(inChannelFragment);
						}
						@Override
						public void onError(ErrorResponse error){
							error.showToast(MainActivity.this);
						}
					})
					.exec();
		}
	}

	public void createChannel(CreateChannel.Body chan){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){
				new CreateChannel(chan.is_social_media, chan.is_private, chan.club_id, chan.user_ids, chan.event_id, chan.topic)
						.wrapProgress(this)
						.setCallback(new Callback<Channel>(){
							@Override
							public void onSuccess(Channel result){
								Intent intent=new Intent(MainActivity.this, VoiceService.class);
								intent.putExtra("channel", result.channel);
								DataProvider.saveChannel(result);
								if(Build.VERSION.SDK_INT>=26)
									startForegroundService(intent);
								else
									startService(intent);
								Bundle extras=new Bundle();
								extras.putBoolean("_can_go_back", true);
								inChannelFragment=new InChannelFragment();
								inChannelFragment.setArguments(extras);
								showFragment(inChannelFragment);
							}
							@Override
							public void onError(ErrorResponse error){
								error.showToast(MainActivity.this);
							}
						})
						.exec();
			}else{
				channelToCreate=chan;
				requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_RESULT_CREATE_CHANNEL);
			}
		}else{
			new CreateChannel(chan.is_social_media, chan.is_private, chan.club_id, chan.user_ids, chan.event_id, chan.topic)
					.wrapProgress(this)
					.setCallback(new Callback<Channel>(){
						@Override
						public void onSuccess(Channel result){
							Intent intent=new Intent(MainActivity.this, VoiceService.class);
							intent.putExtra("channel", result.channel);
							DataProvider.saveChannel(result);
							startService(intent);
							Bundle extras=new Bundle();
							extras.putBoolean("_can_go_back", true);
							inChannelFragment=new InChannelFragment();
							inChannelFragment.setArguments(extras);
							showFragment(inChannelFragment);
						}
						@Override
						public void onError(ErrorResponse error){
							error.showToast(MainActivity.this);
						}
					})
					.exec();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode==PERMISSION_RESULT && grantResults[0]==PackageManager.PERMISSION_GRANTED){
			if(channelToJoin!=null){
				joinChannel(channelToJoin);
			}
		}else if(requestCode==PERMISSION_RESULT_CREATE_CHANNEL && grantResults[0]==PackageManager.PERMISSION_GRANTED){
			if(channelToCreate!=null){
				createChannel(channelToCreate);
			}
		}else if(requestCode==PERMISSION_RESULT_EXTSTORAGE && grantResults[0]==PackageManager.PERMISSION_GRANTED){
			boolean success=false;
			//File externalStorageDirectory = Environment.getExternalStorageDirectory();
			File externalStorageDirectory = getExternalFilesDir(null);
			if (externalStorageDirectory != null) {
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {
					File mainDir = new File(externalStorageDirectory, "CHRec");
					if (mainDir.exists()) {
						success = true;
					}else{
						success=mainDir.mkdirs();
					}
				}
			}
			if(success) inChannelFragment.doRecordClick(this);
		}if(requestCode==PERMISSION_RESULT_JOIN_CHANNAME && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
			if (channelNameToJoin != null) {
				joinChannelByName(channelNameToJoin);
			}
		}
		channelToJoin=null;
		channelNameToJoin=null;
	}

	/*@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		List<Fragment> allFragments;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			allFragments = getFragmentManager().getFragments();
		}else{
			int index = 0;
			Bundle tempBundle = new Bundle();
			allFragments=new ArrayList<>();
			while (true) {
				tempBundle.putInt("key", index++);
				android.app.Fragment fragment = null;
				try {
					fragment = getFragmentManager().getFragment(tempBundle, "key");
				} catch (Exception e) {
					// This generates log spam from FragmentManager anyway.
				}
				if (fragment == null)
					break;
				else
					allFragments.add(fragment);
			}
		}
		for (Fragment fragmento : allFragments) {
			if (fragmento instanceof ProfileFragment) {
				((ProfileFragment) fragmento).onActivityResult(requestCode, resultCode, data);
			}
		}
	}*/
}
