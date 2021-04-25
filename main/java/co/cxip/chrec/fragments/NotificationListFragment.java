package co.cxip.chrec.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import co.cxip.chrec.MainActivity;
import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.methods.AcceptClubMemberInvite;
import co.cxip.chrec.api.methods.Follow;
import co.cxip.chrec.api.methods.GetActionableNotifications;
import co.cxip.chrec.api.methods.GetChannel;
import co.cxip.chrec.api.model.Channel;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.views.UsableRecyclerView;
import co.cxip.chrec.R;
import me.grishka.appkit.api.SimpleCallback;
import co.cxip.chrec.api.methods.GetNotifications;
import co.cxip.chrec.api.model.Notification;

public class NotificationListFragment extends BaseRecyclerFragment<Notification>{
	private NotificationListAdapter adapter;
	private NotificationListFragment instance;
	SharedPreferences notifPref, actionableNotifPref;

	public NotificationListFragment(){
		super(50);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		instance=this;
		notifPref= getActivity().getSharedPreferences("Notifs", Context.MODE_PRIVATE);
		actionableNotifPref= getActivity().getSharedPreferences("ActNotifs", Context.MODE_PRIVATE);
		loadData();
		setTitle(R.string.notifications_title);
	}

	@Override
	protected RecyclerView.Adapter<NotificationViewHolder> getAdapter(){
		if(adapter==null){
			adapter=new NotificationListAdapter();
		}
		return adapter;
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
	protected void doLoadData(int offset, int count){
		currentRequest=new GetActionableNotifications()
				.setCallback(new SimpleCallback<GetActionableNotifications.Response>(this){
					@Override
					public void onSuccess(GetActionableNotifications.Response result){
						currentRequest=null;
						new GetNotifications(getArguments().getInt("id"), 50, offset/50+1)
								.setCallback(new SimpleCallback<GetNotifications.Response>(fragment){
									@Override
									public void onSuccess(GetNotifications.Response result2){
										int totalCount=result.count+result2.count;
										List<Notification> newList = Stream.concat(result.notifications.stream(), result2.notifications.stream()).collect(Collectors.toList());
										Collections.sort(newList, Collections.reverseOrder());
										onDataLoaded(newList, data.size()+preloadedData.size()+newList.size()<totalCount);
									}
								})
								.exec();
					}
				})
				.exec();
	}

	private class NotificationListAdapter extends RecyclerView.Adapter<NotificationViewHolder> implements ImageLoaderRecyclerAdapter{

		@NonNull
		@Override
		public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new NotificationViewHolder();
		}

		@Override
		public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position){
			holder.bind(data.get(position));
		}

		@Override
		public int getItemCount(){
			return data.size();
		}

		@Override
		public int getImageCountForItem(int position){
			return data.get(position).userProfile.photoUrl!=null ? 1 : 0;
		}

		@Override
		public String getImageURL(int position, int image){
			return data.get(position).userProfile.photoUrl;
		}
	}

	private class NotificationViewHolder extends BindableViewHolder<Notification> implements ImageLoaderViewHolder, UsableRecyclerView.Clickable {

		public TextView name, message, time;
		public ImageView photo;
		private Drawable placeholder=new ColorDrawable(getResources().getColor(R.color.grey));

		public NotificationViewHolder(){
			super(getActivity(), R.layout.notification_list_row);

			name=findViewById(R.id.name);
			message=findViewById(R.id.message);
			time=findViewById(R.id.time);
			photo=findViewById(R.id.photo);
		}

		@Override
		public void onBind(Notification item){
			if(item.userProfile!=null) name.setText(item.userProfile.name);
			if(item.notificationId>0) {
				message.setText(item.message);
				boolean unread=notifPref.getBoolean(Long.toString(item.notificationId),true);
				if(unread) {
					notifPref.edit().putBoolean(Long.toString(item.notificationId), false).apply();
				}
				itemView.setAlpha(unread ? 1F : 0.5F);
			}else{
				if(item.club!=null) {
					message.setText(getResources().getString(R.string.accept_club_membership, item.club.name));
					boolean unread = actionableNotifPref.getBoolean(Long.toString(item.actionableNotificationId), true);
					if (unread) {
						actionableNotifPref.edit().putBoolean(Long.toString(item.actionableNotificationId), false).apply();
					}
					itemView.setAlpha(unread ? 1F : 0.5F);
				}
			}
			time.setText(DateUtils.getRelativeTimeSpanString(item.timeCreated.getTime()));

			if(item.userProfile!=null && item.userProfile.photoUrl!=null)
				imgLoader.bindViewHolder(adapter, this, getAdapterPosition());
			else
				photo.setImageDrawable(placeholder);
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
			if(item.notificationId>0) {
				if(item.type==9) {
					((MainActivity)getActivity()).joinChannelByName(item.channel);
					/*new GetChannel(item.channel)
							.wrapProgress(getActivity())
							.setCallback(new SimpleCallback<Channel>(instance){
								@Override
								public void onSuccess(Channel result){
									//Nav.finish(fragment);
									((MainActivity)getActivity()).joinChannel(result);
									Nav.finish(instance);
								}
								@Override
								public void onError(ErrorResponse error) {
								}
							})
							.exec();*/
				}else{
					if(item.userProfile!=null) {
						Bundle args = new Bundle();
						args.putInt("id", item.userProfile.userId);
						Nav.go(getActivity(), ProfileFragment.class, args);
					}
				}
			}else{
				if(item.type==3) { // did what? created a room?
					if(item.userProfile!=null) {
						Bundle args = new Bundle();
						args.putInt("id", item.userProfile.userId);
						Nav.go(getActivity(), ProfileFragment.class, args);
					}
				}else{
					if(item.club!=null)
						new AlertDialog.Builder(getActivity())
								.setTitle(getResources().getString(R.string.accept_club_membership, item.club.name))
								.setPositiveButton(R.string.yes, (dialog, which) -> new AcceptClubMemberInvite(item.club.clubId, 0)
										.wrapProgress(getActivity())
										.setCallback(new Callback<BaseResponse>() {
											@Override
											public void onSuccess(BaseResponse result) {
											}
											@Override
											public void onError(ErrorResponse error) {
												error.showToast(getActivity());
											}
										})
										.exec())
								.setNegativeButton(R.string.cancel, null)
								.show();
				}
			}
		}
	}
}
