package co.cxip.chrec.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import co.cxip.chrec.MainActivity;
import co.cxip.chrec.R;
import co.cxip.chrec.api.methods.GetOnlineFriends;
import co.cxip.chrec.api.model.User;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.views.UsableRecyclerView;

public class FriendsListFragment extends BaseRecyclerFragment<User>{

	//private int selfID=Integer.parseInt(ClubhouseSession.userID);
	private UserListAdapter adapter;

	public FriendsListFragment(){
		super(50);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		setTitle(R.string.friends_title);
		loadData();
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetOnlineFriends()
				.setCallback(new SimpleCallback<GetOnlineFriends.Response>(this){
					@Override
					public void onSuccess(GetOnlineFriends.Response result){
						currentRequest=null;
						//result.clubs?!
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

		public TextView name, status, isSpeaker;
		public ImageView photo, onlineStatus;
		public Button join;
		private final Drawable placeholder=new ColorDrawable(getResources().getColor(R.color.grey));

		public UserViewHolder(){
			super(getActivity(), R.layout.friend_list_row);

			name=findViewById(R.id.name);
			status=findViewById(R.id.status);
			photo=findViewById(R.id.photo);
			onlineStatus=findViewById(R.id.online_status);
			isSpeaker=findViewById(R.id.isSpeaker);
			join=findViewById(R.id.join);
		}

		@Override
		public void onBind(User item){
			name.setText(item.name);
			status.setSelected(true);
			if(item.channel!=null) {
				onlineStatus.setImageDrawable(getActivity().getDrawable(R.drawable.ic_green_circle));
				status.setText(item.topic);
				if(item.isSpeaker) isSpeaker.setVisibility(View.VISIBLE); else isSpeaker.setVisibility(View.GONE);
				join.setVisibility(View.VISIBLE);
				join.setOnClickListener(view -> ((MainActivity)getActivity()).joinChannelByName(item.channel));
			}else{
				isSpeaker.setVisibility(View.GONE);
				join.setVisibility(View.GONE);
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
			Bundle args=new Bundle();
			args.putInt("id", item.userId);
			Nav.go(getActivity(), ProfileFragment.class, args);
		}
	}
}
