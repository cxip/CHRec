package co.cxip.chrec.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import co.cxip.chrec.R;
import co.cxip.chrec.api.methods.GetOnlineFriends;
import co.cxip.chrec.api.methods.InviteToChannel;
import co.cxip.chrec.api.model.User;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.views.UsableRecyclerView;

public class PingListFragment extends BaseRecyclerFragment<User> {

	//private int selfID=Integer.parseInt(ClubhouseSession.userID);
	private String channel;
	private UserListAdapter adapter;

	public PingListFragment(){
		super(50);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		setTitle(R.string.ping_to_room);
		channel=getArguments().getString("channel");
		loadData();
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetOnlineFriends()
				.setCallback(new SimpleCallback<GetOnlineFriends.Response>(this){
					@Override
					public void onSuccess(GetOnlineFriends.Response result){
						currentRequest=null;
						onDataLoaded(result.users, false);
					}
				})
				.exec();
	}

	@Override
	protected RecyclerView.Adapter<UserViewHolder> getAdapter(){
		if(adapter==null){
			adapter=new UserListAdapter();
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

	private class UserListAdapter extends RecyclerView.Adapter<UserViewHolder> implements ImageLoaderRecyclerAdapter{

		@NonNull
		@Override
		public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new UserViewHolder();
		}

		@Override
		public void onBindViewHolder(@NonNull UserViewHolder holder, int position){
			holder.bind(data.get(position));
		}

		@Override
		public int getItemCount(){
			return data.size();
		}

		@Override
		public int getImageCountForItem(int position){
			return data.get(position).photoUrl!=null ? 1 : 0;
		}

		@Override
		public String getImageURL(int position, int image){
			return data.get(position).photoUrl;
		}
	}

	private class UserViewHolder extends BindableViewHolder<User> implements ImageLoaderViewHolder, UsableRecyclerView.Clickable{

		public TextView name, status;
		public ImageView photo, onlineStatus;
		private final Drawable placeholder=new ColorDrawable(getResources().getColor(R.color.grey));

		public UserViewHolder(){
			super(getActivity(), R.layout.ping_list_row);

			name=findViewById(R.id.name);
			status=findViewById(R.id.status);
			photo=findViewById(R.id.photo);
			onlineStatus=findViewById(R.id.online_status);
			//findViewById(R.id.ping).setOnClickListener(view -> {
			//});
		}

		@Override
		public void onBind(User item){
			name.setText(item.name);
			status.setVisibility(View.VISIBLE);
			if (item.lastActiveMinutes == 0) {
				status.setText(getString(R.string.online));
				onlineStatus.setImageDrawable(getActivity().getDrawable(R.drawable.ic_green_circle));
				onlineStatus.setVisibility(View.VISIBLE);
			} else {
				if (item.lastActiveMinutes < 60) {
					status.setText(getString(R.string.last_active_m, item.lastActiveMinutes));
					onlineStatus.setImageDrawable(getActivity().getDrawable(R.drawable.ic_green_white_circle));
					onlineStatus.setVisibility(View.VISIBLE);
				} else if (item.lastActiveMinutes < 1440) {
					status.setText(getString(R.string.last_active_h, Math.round(item.lastActiveMinutes / 60.0)));
					onlineStatus.setImageDrawable(getActivity().getDrawable(R.drawable.ic_green_white_circle));
					onlineStatus.setVisibility(View.VISIBLE);
				}else{
					status.setText(getString(R.string.last_active_d, Math.round(item.lastActiveMinutes / 1440.0)));
					onlineStatus.setVisibility(View.GONE);
				}
			}

			if(item.photoUrl!=null)
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
			new InviteToChannel(channel, item.userId)
					.wrapProgress(getActivity())
					.setCallback(new Callback<InviteToChannel.Response>(){
						@Override
						public void onSuccess(InviteToChannel.Response result){
							if(getActivity()==null) return;
							if(result.notifications_enabled) {
								Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.pinged, item.name), Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.pinged_notifsoff, item.name), Toast.LENGTH_SHORT).show();
							}
						}
						@Override
						public void onError(ErrorResponse error){
							error.showToast(getActivity());
						}
					})
					.exec();
		}
	}
}
