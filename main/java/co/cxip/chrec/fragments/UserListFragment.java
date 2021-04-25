package co.cxip.chrec.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.Nav;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.views.UsableRecyclerView;
import co.cxip.chrec.R;
import co.cxip.chrec.api.model.FullUser;

public abstract class UserListFragment extends BaseRecyclerFragment<FullUser>{

	//private int selfID=Integer.parseInt(ClubhouseSession.userID);
	private UserListAdapter adapter;
	private ImageView imgView;

	public UserListFragment(){
		super(50);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		loadData();
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
		imgView=view.findViewById(R.id.imgView);
		if(imgView!=null)
			imgView.setOnClickListener(v -> imgView.setVisibility(View.GONE));
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

	private class UserViewHolder extends BindableViewHolder<FullUser> implements ImageLoaderViewHolder, UsableRecyclerView.Clickable{

		public TextView name, bio;
		public Button followBtn;
		public ImageView photo;
		private final Drawable placeholder=new ColorDrawable(getResources().getColor(R.color.grey));

		public UserViewHolder(){
			super(getActivity(), R.layout.user_list_row);

			name=findViewById(R.id.name);
			bio=findViewById(R.id.status);
			followBtn=findViewById(R.id.follow_btn);
			photo=findViewById(R.id.photo);
		}

		@Override
		public void onBind(FullUser item){
			name.setText(item.name);
			if(TextUtils.isEmpty(item.bio)){
				bio.setVisibility(View.GONE);
			}else{
				bio.setVisibility(View.VISIBLE);
				bio.setText(item.bio);
			}
//			if(item.userId==selfID){
				followBtn.setVisibility(View.GONE);
//			}else{
//				followBtn.setVisibility(View.VISIBLE);
//				followBtn.setText(item.isFollowed() ? R.string.following : R.string.follow);
//			}
			if(item.photoUrl!=null) {
				imgLoader.bindViewHolder(adapter, this, getAdapterPosition());
				photo.setOnClickListener(view -> {
					if(imgView!=null) {
						imgView.setVisibility(View.VISIBLE);
						imgLoader.bindImageView(imgView, placeholder, item.photoUrl);
					}
				});
			}else {
				photo.setImageDrawable(placeholder);
			}
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
