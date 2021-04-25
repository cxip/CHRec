package co.cxip.chrec.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.cxip.chrec.R;
import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.methods.AddUserTopic;
import co.cxip.chrec.api.methods.GetAllTopics;
import co.cxip.chrec.api.methods.RemoveUserTopic;
import co.cxip.chrec.api.model.Topic;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.AppKitFragment;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.views.DividerItemDecoration;
import me.grishka.appkit.views.UsableRecyclerView;

import static co.cxip.chrec.fragments.RegisterFragment.IS_ONBOARDING;

public class InterestsListFragment extends BaseRecyclerFragment<Topic>{

	private InterestsListAdapter adapter;
	private List<Topic> srcTopics;

	public InterestsListFragment(){
		super(10);
		setListLayoutId(R.layout.interests);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		srcTopics=getArguments().getParcelableArrayList("topics");
		if(srcTopics==null) srcTopics=new ArrayList<>();
		setTitle(getResources().getString(R.string.interests));
		loadData();
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetAllTopics()
				.setCallback(new SimpleCallback<GetAllTopics.Response>(this){
					@Override
					public void onSuccess(GetAllTopics.Response result){
						currentRequest=null;
						onDataLoaded(result.topics, false);
					}
				})
				.exec();
	}

	@Override
	protected RecyclerView.Adapter<InterestsViewHolder> getAdapter(){
		if(adapter==null){
			adapter=new InterestsListAdapter();
		}
		return adapter;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);
		if(getArguments()!=null && getArguments().getBoolean(IS_ONBOARDING)) {
			Button b1 = new Button(getActivity());
			b1.setText(R.string.next);
			b1.setBackgroundResource(R.drawable.round_corners_chblue);
			Toolbar.LayoutParams l3 = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
			l3.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			l3.setMarginEnd(25);
			b1.setLayoutParams(l3);
			b1.setOnClickListener(vv -> {
				Bundle extras=new Bundle();
				extras.putBoolean(IS_ONBOARDING, true);
				Nav.goClearingStack(getActivity(), SearchFragment.class, extras);
			});
			getToolbar().addView(b1);
		}
		getToolbar().setElevation(0);
		UsableRecyclerView usableRecyclerView=view.findViewById(R.id.list);
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getResources().getDrawable(R.drawable.flag_transparent), 20);
		usableRecyclerView.addItemDecoration(dividerItemDecoration);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		getToolbar().setElevation(0);
	}

	private class InterestsListAdapter extends RecyclerView.Adapter<InterestsViewHolder> {

		@NonNull
		@Override
		public InterestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new InterestsViewHolder();
		}

		@Override
		public void onBindViewHolder(@NonNull InterestsViewHolder holder, int position){
			holder.bind(data.get(position));
		}

		@Override
		public int getItemCount(){
			return data.size();
		}
	}

	private class InterestsViewHolder extends BindableViewHolder<Topic> {

		public UsableRecyclerView usableRecyclerView;
		public TextView topicHeader;

		public InterestsViewHolder(){
			super(getActivity(), R.layout.interests_row);
			topicHeader=findViewById(R.id.topicHeader);
			usableRecyclerView=findViewById(R.id.usableRecyclerView);
			GridLayoutManager layoutManager=new GridLayoutManager(getActivity(), 2);
			usableRecyclerView.setLayoutManager(layoutManager);
		}

		@Override
		public void onBind(Topic item){
			topicHeader.setText(item.title);
			TopicsAdapter mAdapter = new TopicsAdapter(item.topics, srcTopics, (item1, selected, interestBtn) -> {
				if(selected) {
					new AddUserTopic(0, item1.id)
							.setCallback(new Callback<BaseResponse>(){
								@Override
								public void onSuccess(BaseResponse result){
									interestBtn.setBackgroundResource(R.drawable.round_corners_chblue);
									interestBtn.setTextColor(Color.WHITE);
									srcTopics.add(new Topic(item1.title, item1.id, item1.abbreviated_title, item1.topics));
									TopicsAdapter adapter=(TopicsAdapter) usableRecyclerView.getAdapter();
									if(adapter!=null) {
										adapter.setTopics(srcTopics);
										usableRecyclerView.getAdapter().notifyDataSetChanged();
									}
								}
								@Override
								public void onError(ErrorResponse error){
								}
							})
							.exec();
				}else{
					new RemoveUserTopic(0, item1.id)
							.setCallback(new Callback<BaseResponse>(){
								@Override
								public void onSuccess(BaseResponse result){
									interestBtn.setBackgroundResource(R.drawable.button_grey_background);
									interestBtn.setTextColor(Color.BLACK);
									if(srcTopics.stream().filter(o -> o.id == item1.id).findFirst().orElse(null)!=null)
										srcTopics.remove(srcTopics.stream().filter(o -> o.id == item1.id).findFirst().orElse(null));
									TopicsAdapter adapter=(TopicsAdapter) usableRecyclerView.getAdapter();
									if(adapter!=null) {
										adapter.setTopics(srcTopics);
										usableRecyclerView.getAdapter().notifyDataSetChanged();
									}
								}
								@Override
								public void onError(ErrorResponse error){
								}
							})
							.exec();
				}
			});
			usableRecyclerView.setAdapter(mAdapter);
		}
	}

	public static class TopicsAdapter extends RecyclerView.Adapter<TopicsAdapter.ViewHolder> {

		public interface OnItemClickListener {
			void onItemClick(Topic item, boolean selected, final Button button);
		}
		private final List<Topic> topics;
		private List<Topic> myTopics;
		private final OnItemClickListener listener;

		public static class ViewHolder extends UsableRecyclerView.ViewHolder {
			private final Button interestBtn;

			public ViewHolder(View view) {
				super(view);
				interestBtn = view.findViewById(R.id.interest_btn);
			}

			public void bind(final Topic item, boolean selected, final OnItemClickListener listener) {
				interestBtn.setText(item.title);
				if(selected) {
					interestBtn.setBackgroundResource(R.drawable.round_corners_chblue);
					interestBtn.setTextColor(Color.WHITE);
				}else{
					interestBtn.setBackgroundResource(R.drawable.button_grey_background);
					interestBtn.setTextColor(Color.BLACK);
				}
				interestBtn.setOnClickListener(view -> listener.onItemClick(item, !selected, interestBtn));
			}
		}

		public TopicsAdapter(List<Topic> topics, List<Topic> myTopics, OnItemClickListener listener) {
			this.topics = topics;
			this.myTopics = myTopics;
			this.listener = listener;
		}

		@NonNull
		@Override
		public TopicsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.interests_row_cell, viewGroup, false);
			return new TopicsAdapter.ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(TopicsAdapter.ViewHolder viewHolder, final int position) {
			Topic item=topics.get(position);
			boolean exists=myTopics.stream().anyMatch(o -> o.id==item.id);
			viewHolder.bind(item, exists, listener);
		}

		@Override
		public int getItemCount() {
			return topics.size();
		}

		public void setTopics(List<Topic> topics) {
			this.myTopics=topics;
		}
	}
}
