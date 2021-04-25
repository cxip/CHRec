package co.cxip.chrec.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
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
import android.widget.TextView;

import java.text.DateFormat;

import co.cxip.chrec.R;
import co.cxip.chrec.VoiceService;
import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseSession;
import co.cxip.chrec.api.methods.Follow;
import co.cxip.chrec.api.methods.FollowClub;
import co.cxip.chrec.api.methods.GetClub;
import co.cxip.chrec.api.methods.GetProfile;
import co.cxip.chrec.api.methods.Unfollow;
import co.cxip.chrec.api.methods.UnfollowClub;
import co.cxip.chrec.api.methods.UpdateBio;
import co.cxip.chrec.api.methods.UpdateName;
import co.cxip.chrec.api.methods.UpdatePhoto;
import co.cxip.chrec.api.model.Club;
import co.cxip.chrec.api.model.FullUser;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.LoaderFragment;
import me.grishka.appkit.imageloader.ViewImageLoader;

public class ClubFragment extends LoaderFragment {

	private static final int PICK_PHOTO_RESULT=468;

	private Club user;
	private boolean isAdmin,isMember,isFollower,isPendingAccept,isPendingApproval;
	private TextView name, invitelink, followers, members, description;
	private ImageView photo, imgView;
	private Button followBtn;

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		loadData();
	}

	@Override
	public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v=inflater.inflate(R.layout.club, container, false);
		name=v.findViewById(R.id.name);
		invitelink=v.findViewById(R.id.url);
		followers=v.findViewById(R.id.followers);
		members=v.findViewById(R.id.members);
		description=v.findViewById(R.id.status);
		photo=v.findViewById(R.id.photo);
		followBtn=v.findViewById(R.id.follow_btn);
		imgView = v.findViewById(R.id.imgView);
		imgView.setOnClickListener(vv -> {
			imgView.setVisibility(View.GONE);
		});
		followBtn.setOnClickListener(this::onFollowClick);
		followers.setOnClickListener(this::onFollowersClick);
		members.setOnClickListener(this::onMembersClick);
		photo.setOnClickListener(this::onOthersPhotoClick);
		return v;
	}

	@Override
	protected void doLoadData(){
		currentRequest=new GetClub(getArguments().getInt("id"), null)
				.setCallback(new SimpleCallback<GetClub.Response>(this){
					@Override
					public void onSuccess(GetClub.Response result){
						currentRequest=null;
						user=result.club;
						isAdmin=result.isAdmin;
						isFollower=result.isFollower;
						isMember=result.isMember;
						name.setText(user.name);
						invitelink.setText(user.url);
						ColorDrawable d=new ColorDrawable(getResources().getColor(R.color.grey));
						if(user.photoUrl!=null)
							ViewImageLoader.load(photo, d, user.photoUrl);
						else
							photo.setImageDrawable(d);

						followers.setText(getResources().getQuantityString(R.plurals.followers, user.numFollowers, user.numFollowers));
						members.setText(getResources().getQuantityString(R.plurals.members, user.numMembers, user.numMembers));
						followBtn.setText(result.isFollower ? R.string.following : R.string.follow);
						description.setText(user.description);
						dataLoaded();
					}
				})
				.exec();
	}

	@Override
	public void onRefresh(){
		loadData();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		getToolbar().setElevation(0);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		getToolbar().setElevation(0);
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		menu.add(R.string.log_out);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if(VoiceService.getInstance()!=null){
			VoiceService.getInstance().leaveChannel();
		}
		ClubhouseSession.userID=ClubhouseSession.userToken=null;
		ClubhouseSession.write();
		Nav.goClearingStack(getActivity(), LoginFragment.class, null);
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
		}
	}

	private void onFollowClick(View v){
		if(isFollower){
			new AlertDialog.Builder(getActivity())
					.setMessage(getString(R.string.confirm_unfollow, user.name))
					.setPositiveButton(R.string.yes, (dialogInterface, i) -> new UnfollowClub(user.clubId, null)
							.wrapProgress(getActivity())
							.setCallback(new Callback<BaseResponse>(){
								@Override
								public void onSuccess(BaseResponse result){
									followBtn.setText(R.string.follow);
								}

								@Override
								public void onError(ErrorResponse error){
									error.showToast(getActivity());
								}
							})
							.exec())
					.setNegativeButton(R.string.no, null)
					.show();
		}else{
			new FollowClub(user.clubId, null)
					.wrapProgress(getActivity())
					.setCallback(new Callback<BaseResponse>(){
						@Override
						public void onSuccess(BaseResponse result){
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

	private void onFollowersClick(View v){
		Bundle args=new Bundle();
		args.putInt("id", user.clubId);
		Nav.go(getActivity(), FollowersFragment.class, args);
	}

	private void onMembersClick(View v){
		Bundle args=new Bundle();
		args.putInt("id", user.clubId);
		Nav.go(getActivity(), FollowingFragment.class, args);
	}

	private void onNameClick(View v) {
		final EditText edit = new EditText(getActivity());
		edit.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
		edit.setText(user.name);
		new AlertDialog.Builder(getActivity())
				.setTitle(R.string.update_name)
				.setView(edit)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
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
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private void onOthersPhotoClick(View v) {
		if(user.photoUrl!=null) {
			ColorDrawable d=new ColorDrawable(getResources().getColor(R.color.lightBeige));
			ViewImageLoader.load(imgView, d, user.photoUrl);
			imgView.setVisibility(View.VISIBLE);
		}
	}
}
