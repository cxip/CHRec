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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.cxip.chrec.MainActivity;
import co.cxip.chrec.R;
import co.cxip.chrec.api.ClubhouseSession;
import co.cxip.chrec.api.methods.CreateChannel;
import co.cxip.chrec.api.methods.GetFollowers;
import co.cxip.chrec.api.methods.GetOnlineFriends;
import co.cxip.chrec.api.model.FullUser;
import co.cxip.chrec.api.model.User;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.views.UsableRecyclerView;

public class SelectUsersListFragment extends BaseRecyclerFragment<FullUser>{

	//private int selfID=Integer.parseInt(ClubhouseSession.userID);
	private UserListAdapter adapter;
	private Button createClosedRoomBtn;
	boolean is_social_media;
	boolean is_private;
	String club_id;
	String event_id;
	String topic;
	List<Integer> user_ids=new ArrayList<>();

	public SelectUsersListFragment(){
		super(50);
		setListLayoutId(R.layout.select_user);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		setTitle(R.string.start_a_room_title);
		is_social_media=getArguments().getBoolean("is_social_media");
		is_private=getArguments().getBoolean("is_social_media");
		club_id=getArguments().getString("club_id");
		event_id=getArguments().getString("event_id");
		topic=getArguments().getString("topic");
		loadData();
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetFollowers(getArguments().getInt("id"), 50, offset/50+1)
				.setCallback(new SimpleCallback<GetFollowers.Response>(this){
					@Override
					public void onSuccess(GetFollowers.Response result){
						currentRequest=null;
						onDataLoaded(result.users, data.size()+preloadedData.size()+result.users.size()<result.count);
					}
				})
				.exec();
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
		if(adapter==null){
			adapter=new UserListAdapter();
		}
		return adapter;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		getToolbar().setElevation(0);
		createClosedRoomBtn=view.findViewById(R.id.create_closed_room);
		createClosedRoomBtn.setOnClickListener(this::onCreateClosedRoomBtnClick);
	}

	private void onCreateClosedRoomBtnClick(View v) {
		Nav.finish(this);
		MainActivity mainActivity=(MainActivity)getActivity();
		mainActivity.create_room_panel_visible=false;
		CreateChannel.Body body=new CreateChannel.Body(is_social_media, is_private, club_id, user_ids, event_id, topic);
		mainActivity.createChannel(body);
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

		public TextView name, username;
		public CheckBox userSelected;
		public ImageView photo;
		private final Drawable placeholder=new ColorDrawable(getResources().getColor(R.color.grey));

		public UserViewHolder(){
			super(getActivity(), R.layout.user_select_row);

			name=findViewById(R.id.name);
			username=findViewById(R.id.username);
			photo=findViewById(R.id.photo);
			userSelected=findViewById(R.id.userSelected);
			userSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
				if (userSelected.isChecked()) {
					user_ids.add(item.userId);
				}else{
					user_ids.remove(Integer.valueOf(item.userId));
				}
			});
		}

		@Override
		public void onBind(User item){
			name.setText(item.name);
			username.setText("@"+item.username);
			if(item.photoUrl!=null)
				imgLoader.bindViewHolder(adapter, this, getAdapterPosition());
			else
				photo.setImageDrawable(placeholder);
			userSelected.setChecked(user_ids.contains(item.userId));
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
		}
	}
}
