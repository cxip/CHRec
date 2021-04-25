package co.cxip.chrec.fragments;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import co.cxip.chrec.api.methods.CallPhoneNumberAuth;
import co.cxip.chrec.api.methods.UpdateNotifications;
import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import co.cxip.chrec.R;
import co.cxip.chrec.api.BaseResponse;
import co.cxip.chrec.api.ClubhouseSession;
import co.cxip.chrec.api.methods.CompletePhoneNumberAuth;
import co.cxip.chrec.api.methods.ResendPhoneNumberAuth;
import co.cxip.chrec.api.methods.StartPhoneNumberAuth;
import me.grishka.appkit.fragments.AppKitFragment;

public class LoginFragment extends BaseToolbarFragment {

	private EditText phoneInput, codeInput;
	private CountryCodePicker countryCodePicker;
	private LinearLayout resendCodeLayout;
	private boolean sentCode=false;
	private PhoneNumberUtil phoneNumberUtil;

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
//		setTitle(R.string.login);
	}

	@Override
	public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view=inflater.inflate(R.layout.login, container, false);

		phoneNumberUtil=PhoneNumberUtil.createInstance(getActivity());
		phoneInput=view.findViewById(R.id.phone_input);
		codeInput=view.findViewById(R.id.code_input);
		Button resendBtn = view.findViewById(R.id.resend_code);
		Button nextBtn = view.findViewById(R.id.next);
		Button callBtn = view.findViewById(R.id.call_code);
		countryCodePicker=view.findViewById(R.id.country_code_picker);
		resendCodeLayout=view.findViewById(R.id.resend_code_layout);

		codeInput.setVisibility(View.GONE);
		resendCodeLayout.setVisibility(View.GONE);

		countryCodePicker.registerPhoneNumberTextView(phoneInput);
		nextBtn.setOnClickListener(this::onNextClick);
		resendBtn.setOnClickListener(this::onResendClick);
		callBtn.setOnClickListener(this::onCallClick);
		phoneInput.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){

			}

			@Override
			public void afterTextChanged(Editable editable){
				try{
					Phonenumber.PhoneNumber number=phoneNumberUtil.parse(phoneInput.getText().toString(), countryCodePicker.getSelectedCountryNameCode());
					String country=phoneNumberUtil.getRegionCodeForNumber(number);
					if(country!=null)
						countryCodePicker.setCountryForNameCode(country);
				}catch(NumberParseException igonre){}
			}
		});
		phoneInput.requestFocus();
		return view;
	}

	private String getCleanPhoneNumber(){
		return countryCodePicker.getNumber();
	}

	private void onNextClick(View v){
		if(sentCode){
			new CompletePhoneNumberAuth(getCleanPhoneNumber(), codeInput.getText().toString())
					.wrapProgress(getActivity())
					.setCallback(new SimpleCallback<CompletePhoneNumberAuth.Response>(this){
						@Override
						public void onSuccess(CompletePhoneNumberAuth.Response result){
							if(result.userProfile!=null) {
								ClubhouseSession.userToken = result.authToken;
								ClubhouseSession.userID = result.userProfile.userId + "";
								ClubhouseSession.isWaitlisted = result.isWaitlisted;
								ClubhouseSession.write();
							}
							if(getActivity()==null) return;
							InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
							View view = getActivity().getCurrentFocus();
							if (view == null) {
								view = new View(getActivity());
							}
							imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
							if(result.userProfile==null) {
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
									Toast.makeText(LoginFragment.this.getContext(), "Login failed", Toast.LENGTH_SHORT).show();
								}else{
									Toast.makeText(LoginFragment.this.getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
								}
								Nav.goClearingStack(getActivity(), RegisterFragment.class, null);
								return;
							}
							if(result.isWaitlisted){
								Nav.goClearingStack(getActivity(), WaitlistedFragment.class, null);
							}else if(result.userProfile.username==null){
								Nav.goClearingStack(getActivity(), RegisterFragment.class, null);
							}else{
								new UpdateNotifications(-1,-1,false,null,2,3)
										.wrapProgress(getActivity())
										.setCallback(new SimpleCallback<BaseResponse>(fragment){
											@Override
											public void onSuccess(BaseResponse result){
											}
										}).exec();
								Bundle extras=new Bundle();
								extras.putBoolean(AppKitFragment.EXTRA_IS_TAB, true);
								Nav.goClearingStack(getActivity(), HomeFragment.class, extras);
							}
						}
					})
					.exec();
		}else{
			new StartPhoneNumberAuth(getCleanPhoneNumber())
					.wrapProgress(getActivity())
					.setCallback(new SimpleCallback<BaseResponse>(this){
						@Override
						public void onSuccess(BaseResponse result){
							sentCode=true;
							phoneInput.setEnabled(false);
							countryCodePicker.setClickable(false);
							codeInput.setVisibility(View.VISIBLE);
							resendCodeLayout.setVisibility(View.VISIBLE);
							codeInput.requestFocus();
						}

						@Override
						public void onError(ErrorResponse error){
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
								Toast.makeText(LoginFragment.this.getContext(), "Login failed", Toast.LENGTH_SHORT).show();
							}else{
								Toast.makeText(LoginFragment.this.getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
							}
						}
					})
					.exec();
		}
	}

	private void onResendClick(View v){
		new ResendPhoneNumberAuth(getCleanPhoneNumber())
				.wrapProgress(getActivity())
				.exec();
	}

	private void onCallClick(View v){
		new CallPhoneNumberAuth(getCleanPhoneNumber())
				.wrapProgress(getActivity())
				.exec();
	}
}
