package co.cxip.chrec.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.cxip.chrec.R;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.utils.BindableViewHolder;

public class FilesListFragment extends BaseRecyclerFragment<String> {

	//private final int selfID=Integer.parseInt(ClubhouseSession.userID);
	private FilesListAdapter adapter;

	public FilesListFragment(){
		super(50);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		setTitle("Files");
		loadData();
	}

	@Override
	protected void doLoadData(int offset, int count){
		File mainDir = new File(getActivity().getExternalFilesDir(null) + File.separator + "CHRec");
		ArrayList<String> mFileNames = new ArrayList<>();
		File[] filesInDirectory = mainDir.listFiles();
		if(filesInDirectory==null || filesInDirectory.length==0) {
			onDataLoaded(mFileNames, false);
		}else{
			Arrays.sort(filesInDirectory, (Comparator<File>) (File o1, File o2) -> Long.compare(((File) o2).lastModified(), ((File) o1).lastModified()));
			for (File file : filesInDirectory) {
				mFileNames.add(file.getName());
			}
			List<String> subList=mFileNames.subList(Math.min(mFileNames.size(), offset), Math.min(mFileNames.size(), offset+count));
			onDataLoaded(subList, false);
		}
	}

	@Override
	protected RecyclerView.Adapter<FilesViewHolder> getAdapter(){
		if(adapter==null){
			adapter=new FilesListAdapter();
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

	private class FilesListAdapter extends RecyclerView.Adapter<FilesViewHolder> {

		@NonNull
		@Override
		public FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new FilesViewHolder();
		}

		@Override
		public void onBindViewHolder(@NonNull FilesViewHolder holder, int position){
			holder.bind(data.get(position));
		}

		@Override
		public int getItemCount(){
			return data.size();
		}
	}

	private class FilesViewHolder extends BindableViewHolder<String> {

		public TextView name;
		Button playBtn;
		final  private ImageButton shareBtn, infoBtn, deleteBtn;

		public FilesViewHolder(){
			super(getActivity(), R.layout.files_list_row);
			name=findViewById(R.id.name);
			playBtn=findViewById(R.id.play);
			shareBtn=findViewById(R.id.share);
			infoBtn=findViewById(R.id.info);
			deleteBtn=findViewById(R.id.delete);
		}

		@Override
		public void onBind(String item){
			name.setText(item);
			playBtn.setOnClickListener(view -> {
				Context context=itemView.getContext();
				Intent viewIntent = new Intent(Intent.ACTION_VIEW);
				File file = new File(getActivity().getExternalFilesDir(null) + File.separator + "CHRec" + File.separator + item);
				viewIntent.setDataAndType(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file), "audio/*");
				viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				startActivity(Intent.createChooser(viewIntent, null));
			});
			shareBtn.setOnClickListener(view -> {
				Context context=itemView.getContext();
				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				File file = new File(getActivity().getExternalFilesDir(null) + File.separator + "CHRec" + File.separator + item);
				shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context,context.getApplicationContext().getPackageName() + ".provider", file));
				shareIntent.setType("audio/*");
				startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
			});
			infoBtn.setOnClickListener(view -> {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
				String datestr="";
				try {
					Date filedate=formatter.parse(item);
					SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.filedatestr));
					if(filedate!=null) datestr=sdf.format(filedate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				SharedPreferences pref;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
					pref = getContext().getSharedPreferences("CHRecFiles", Context.MODE_PRIVATE);
				}else{
					pref = getActivity().getSharedPreferences("CHRecFiles", Context.MODE_PRIVATE);
				}
				String channelName=pref.getString(item,"");
				new AlertDialog.Builder(getActivity())
					.setTitle("Information")
					.setMessage(datestr+"\nRoom: "+channelName+"\n")
					.setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss())
					.setCancelable(false)
					.show();
			});
			deleteBtn.setOnClickListener(view -> {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Confirm");
				builder.setMessage("Are you sure you want to delete this file?");
				builder.setPositiveButton("YES", (dialog, which) -> {
					File file = new File(getActivity().getExternalFilesDir(null) + File.separator + "CHRec" + File.separator + item);
					file.delete();
					reload();
					dialog.dismiss();
				});
				builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
				AlertDialog alert = builder.create();
				alert.show();
			});
		}
	}
}
