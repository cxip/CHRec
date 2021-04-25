package co.cxip.chrec.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.text.DateFormat;

import co.cxip.chrec.R;
import co.cxip.chrec.VoiceService;
import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseSession;
import co.cxip.chrec.api.methods.Follow;
import co.cxip.chrec.api.methods.GetProfile;
import co.cxip.chrec.api.methods.InviteToApp;
import co.cxip.chrec.api.methods.Me;
import co.cxip.chrec.api.methods.Unfollow;
import co.cxip.chrec.api.methods.UpdateBio;
import co.cxip.chrec.api.methods.UpdateName;
import co.cxip.chrec.api.methods.UpdatePhoto;
import co.cxip.chrec.api.model.FullUser;
import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.LoaderFragment;
import me.grishka.appkit.imageloader.ViewImageLoader;

public class InviteFragment extends LoaderFragment {

	private static final int PERMISSION_RESULT_READ_CONTACT=260;
	private static final int REQUEST_PICK_CONTACT=469;

	private FullUser user;
	private TextView invites,finalPhoneNumber;
	private Button abBtn, inviteButton;
	private EditText phoneInput;
	private PhoneNumberUtil phoneNumberUtil;
	private CountryCodePicker countryCodePicker;
	private LinearLayout finalPhoneLayout;

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		loadData();
		setHasOptionsMenu(false);
	}

	@Override
	public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v=inflater.inflate(R.layout.invites, container, false);
		phoneNumberUtil= PhoneNumberUtil.createInstance(getActivity());
		inviteButton = v.findViewById(R.id.invite_button);
		invites = v.findViewById(R.id.num_of_invites);
		countryCodePicker=v.findViewById(R.id.country_code_picker);
		phoneInput = v.findViewById(R.id.invite_phone_num);
		countryCodePicker.registerPhoneNumberTextView(phoneInput);
		finalPhoneLayout=v.findViewById(R.id.final_phone_layout);
		finalPhoneNumber=v.findViewById(R.id.final_phone_number);
		abBtn=v.findViewById(R.id.addressbook);
		abBtn.setOnClickListener(this::onABBtnClick);
		inviteButton.setOnClickListener(this::onInviteClick);
		phoneInput.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

			}
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){

			}
			@Override
			public void afterTextChanged(Editable editable){
				if(phoneInput.getText().length()>0)
					finalPhoneLayout.setVisibility(View.VISIBLE);
				else
					finalPhoneLayout.setVisibility(View.GONE);
				try{
					Phonenumber.PhoneNumber number=phoneNumberUtil.parse(phoneInput.getText().toString(), countryCodePicker.getSelectedCountryNameCode());
					String country=phoneNumberUtil.getRegionCodeForNumber(number);
					if(country!=null)
						countryCodePicker.setCountryForNameCode(country);
					finalPhoneNumber.setText(countryCodePicker.getNumber());
				}catch(NumberParseException igonre){}
			}
		});
		countryCodePicker.setPhoneNumberInputValidityListener((ccp, isValid) -> {
			String countryCode="+"+countryCodePicker.getSelectedCountryCode();
			String phoneNo=phoneInput.getText().toString();
			if(phoneNo.startsWith(countryCode)){
				phoneNo=phoneNo.substring(countryCode.length());
				phoneInput.setText(phoneNo);
			}
		});
		phoneInput.requestFocus();
		return v;
	}

	@Override
	protected void doLoadData(){
		currentRequest=new GetProfile(getArguments().getInt("id"))
				.setCallback(new SimpleCallback<GetProfile.Response>(this){
					@Override
					public void onSuccess(GetProfile.Response result){
						currentRequest=null;
						user=result.userProfile;
						dataLoaded();
					}
				})
				.exec();
		loadInvites();
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

	private void loadInvites() {
		new Me().setCallback(new Callback<Me.Response>() {
			@Override
			public void onSuccess(Me.Response result) {
				if(getActivity()==null) return;
				if (result.num_invites > 0) {
					invites.setText(getResources().getQuantityString(R.plurals.invites, result.num_invites, result.num_invites));
					inviteButton.setEnabled(true);
				}else{
					inviteButton.setEnabled(false);
				}
			}

			@Override
			public void onError(ErrorResponse error) {
				inviteButton.setEnabled(false);
			}
		}).exec();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_PICK_CONTACT && resultCode == Activity.RESULT_OK) {
			Uri uri = data.getData();
			Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
			if (cursor.moveToFirst()) {
				int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				String phoneNo = cursor.getString(phoneIndex);
				String countryCode="+"+countryCodePicker.getSelectedCountryCode();
				if(phoneNo.startsWith(countryCode)) phoneNo=phoneNo.substring(countryCode.length());
				if(phoneNo.length()>0) phoneInput.setText(phoneNo);
			}
		}
	}

			@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PERMISSION_RESULT_READ_CONTACT && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
			startActivityForResult(intent, REQUEST_PICK_CONTACT);
		}
	}

	private void onABBtnClick(View v) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS)== PackageManager.PERMISSION_GRANTED){
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
				startActivityForResult(intent, REQUEST_PICK_CONTACT);
			} else {
				requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_RESULT_READ_CONTACT);
			}
		}else{
			Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
			startActivityForResult(intent, REQUEST_PICK_CONTACT);
		}
	}

	private void onInviteClick(View v) {
		final String numberToInvite = countryCodePicker.getNumber();
		final String message="Hi \uD83D\uDC4B - I just invited you to use Clubhouse! Here is the link:\n\n"+
				"https://play.google.com/store/apps/details?id=co.cxip.chrec";
		new InviteToApp("", numberToInvite, "")
				.wrapProgress(getActivity())
				.setCallback(new Callback<BaseResponse>() {
					@Override
					public void onSuccess(BaseResponse result) {
						Toast.makeText(getActivity(), "success", Toast.LENGTH_SHORT).show();
						loadInvites();
						if(getActivity()!=null && Telephony.Sms.getDefaultSmsPackage(getActivity())!=null) {
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + numberToInvite));
							intent.putExtra("sms_body", message);
							startActivity(intent);
						}
					}
					@Override
					public void onError(ErrorResponse error) {
						Toast.makeText(getActivity(), "failed", Toast.LENGTH_SHORT).show();
						loadInvites();
					}
				})
				.exec();
	}
}
