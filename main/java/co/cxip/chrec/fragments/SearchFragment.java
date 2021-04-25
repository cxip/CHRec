package co.cxip.chrec.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import co.cxip.chrec.R;
import co.cxip.chrec.api.methods.GetSuggestedFollowsAll;
import co.cxip.chrec.api.methods.SearchClubs;
import co.cxip.chrec.api.methods.SearchUsers;
import co.cxip.chrec.api.model.UserOrClub;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.AppKitFragment;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.views.UsableRecyclerView;

public class SearchFragment extends BaseRecyclerFragment<UserOrClub> {

	private SearchView searchView;
	boolean switchIsPeople, isOnboarding=false;
	private SearchFragment.UserOrClubListAdapter adapter;
	private ImageView imgView;
	private TextView suggestedToFollow;

	public SearchFragment(){
		super(10);
		setListLayoutId(R.layout.search);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		setEmptyText("");
		setTitle(R.string.explore);
		setHasOptionsMenu(false);
		loadData();
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState){
		super.onViewCreated(v, savedInstanceState);
		suggestedToFollow=v.findViewById(R.id.suggested_to_follow);
		searchView=v.findViewById(R.id.search);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}
			@Override
			public boolean onQueryTextChange(String newText) {
				onRefresh();
				return false;
			}
		});
		switchIsPeople=true;
		Switch searchSwitch = v.findViewById(R.id.search_switch);
		//searchSwitch.setTrackDrawable(new SwitchTrackTextDrawable(getActivity(), R.string.people, R.string.clubs));
		searchSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
			switchIsPeople= !isChecked;
			searchView.setQuery("", false);
		});
		imgView=v.findViewById(R.id.imgView);
		if(imgView!=null) imgView.setOnClickListener(view -> imgView.setVisibility(View.GONE));
		RelativeLayout switchLayout=v.findViewById(R.id.switchLayout);
		if(getArguments()!=null && getArguments().getBoolean(RegisterFragment.IS_ONBOARDING)) {
			isOnboarding=true;
			switchLayout.setVisibility(View.GONE);
			searchView.setVisibility(View.GONE);
			Button b1 = new Button(getActivity());
			b1.setText(R.string.next);
			b1.setBackgroundResource(R.drawable.round_corners_chblue);
			Toolbar.LayoutParams l3 = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
			l3.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			l3.setMarginEnd(25);
			b1.setLayoutParams(l3);
			b1.setOnClickListener(vv -> {
				Bundle extras=new Bundle();
				extras.putBoolean(AppKitFragment.EXTRA_IS_TAB, true);
				Nav.goClearingStack(getActivity(), HomeFragment.class, extras);
			});
			getToolbar().addView(b1);
		}else{
			switchLayout.setVisibility(View.VISIBLE);
			searchView.setVisibility(View.VISIBLE);
		}
		loadData();
	}

	@Override
	protected void doLoadData(int offset, int count){
		if(searchView==null) {
			onDataLoaded(new ArrayList<>(), false);
			return;
		}
		String qry=searchView.getQuery().toString();
		if(qry.length()<1) {
			suggestedToFollow.setVisibility(View.VISIBLE);
			currentRequest=new GetSuggestedFollowsAll(isOnboarding, 50, offset/50+1)
					.setCallback(new SimpleCallback<GetSuggestedFollowsAll.Response>(this){
						@Override
						public void onSuccess(GetSuggestedFollowsAll.Response result){
							currentRequest=null;
							onDataLoaded(result.users, data.size()+preloadedData.size()+result.users.size()<result.count);
						}
					})
					.exec();
			return;
		}
		suggestedToFollow.setVisibility(View.GONE);
		if(switchIsPeople)
			currentRequest=new SearchUsers(false, false, false, qry)
				.setCallback(new SimpleCallback<SearchUsers.Response>(this){
					@Override
					public void onSuccess(SearchUsers.Response result){
						currentRequest=null;
						onDataLoaded(result.users, false);
					}
				})
				.exec();
		else
			currentRequest=new SearchClubs(false, false, false, qry)
					.setCallback(new SimpleCallback<SearchClubs.Response>(this){
						@Override
						public void onSuccess(SearchClubs.Response result){
							currentRequest=null;
							onDataLoaded(result.clubs, false);
						}
					})
					.exec();
	}

	@Override
	protected RecyclerView.Adapter<SearchFragment.UserOrClubViewHolder> getAdapter(){
		if(adapter==null){
			adapter=new UserOrClubListAdapter();
		}
		return adapter;
	}

	//@Override
	//public void onViewCreated(View view, Bundle savedInstanceState){
	//	super.onViewCreated(view, savedInstanceState);
	//	getToolbar().setElevation(0);
	//}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		getToolbar().setElevation(0);
	}

	private class UserOrClubListAdapter extends RecyclerView.Adapter<SearchFragment.UserOrClubViewHolder> implements ImageLoaderRecyclerAdapter {

		@NonNull
		@Override
		public UserOrClubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new UserOrClubViewHolder();
		}

		@Override
		public void onBindViewHolder(@NonNull SearchFragment.UserOrClubViewHolder holder, int position){
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
			return data.size()>0 ? (position<data.size() ? data.get(position).photoUrl : null) : null;
		}
	}

	private class UserOrClubViewHolder extends BindableViewHolder<UserOrClub> implements ImageLoaderViewHolder, UsableRecyclerView.Clickable{

		public TextView name, status;
		public ImageView photo;
		//private Button followBtn;
		private final Drawable placeholder=new ColorDrawable(getResources().getColor(R.color.grey));

		public UserOrClubViewHolder(){
			super(getActivity(), R.layout.friend_list_row);

			name=findViewById(R.id.name);
			status=findViewById(R.id.status);
			photo=findViewById(R.id.photo);
			//followBtn=findViewById(R.id.join);
			findViewById(R.id.online_status).setVisibility(View.GONE);
		}

		@Override
		public void onBind(UserOrClub item){
			name.setText(item.name);
			status.setText(item.username);
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
			Bundle args = new Bundle();
			if(switchIsPeople) {
				args.putInt("id", item.userId);
				Nav.go(getActivity(), ProfileFragment.class, args);
			}else {
				args.putInt("id", item.clubId);
				Nav.go(getActivity(), ClubFragment.class, args);
			}
		}
	}
}
