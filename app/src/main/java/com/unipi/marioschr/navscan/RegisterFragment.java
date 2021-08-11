package com.unipi.marioschr.navscan;

import android.app.DatePickerDialog;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;


public class RegisterFragment extends Fragment implements View.OnClickListener {
	TextInputEditText tietFirstName,tietLastName,tietBirthday,tietEmail,tietPassword,tietConfirmPassword;
	TextInputLayout tilFirstName,tilLastName,tilBirthday,tilEmail,tilPassword,tilConfirmPassword;
	Button btnSignUp;
	DatePickerDialog datePickerDialog;
	boolean foundError = false, passOk = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_register, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		findViewsAndSetListeners(view);
	}

	private void findViewsAndSetListeners(View view) {
		//region FindViewById
		tietFirstName = view.findViewById(R.id.tietFirstName);
		tietLastName = view.findViewById(R.id.tietLastName);
		tietBirthday = view.findViewById(R.id.tietBirthday);
		tietEmail = view.findViewById(R.id.tietEmail);
		tietPassword = view.findViewById(R.id.tietPassword);
		tietConfirmPassword = view.findViewById(R.id.tietConfirmPassword);

		tilFirstName = view.findViewById(R.id.tilFirstName);
		tilLastName = view.findViewById(R.id.tilLastName);
		tilBirthday = view.findViewById(R.id.tilBirthday);
		tilEmail = view.findViewById(R.id.tilEmail);
		tilPassword = view.findViewById(R.id.tilPassword);
		tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);

		btnSignUp = view.findViewById(R.id.btnSignUp);
		//endregion

		//region TextChangedListeners
		tietFirstName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) {
					setErrorFirstName();
				} else {
					tilFirstName.setError(null);
				}
			}
		});
		tietLastName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) {
					setErrorLastName();
				} else {
					tilLastName.setError(null);
				}
			}
		});
		tietBirthday.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) {
					setErrorBirthday();
				} else {
					tilBirthday.setError(null);
				}
			}
		});
		tietEmail.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) {
					setErrorEmail();
				} else {
					tilEmail.setError(null);
				}
			}
		});
		tietPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) setErrorPassword();
				else if (s.toString().trim().length()<6)
					tilPassword.setError("You need a stronger password (Minimum length: 6)");
				else tilPassword.setError(null);
			}
		});
		tietConfirmPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) setErrorConfirmPassword();
				else if (!s.toString().equals(String.valueOf(tietPassword.getText())))
					tilConfirmPassword.setError("Passwords don't match");
				else tilConfirmPassword.setError(null);
			}
		});
		//endregion

		btnSignUp.setOnClickListener(this);

		tietBirthday.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) openDatePicker();
			else hideDatePicker();
		});
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnSignUp:
				signUp();
				break;
		}
	}

	private void signUp() {
		validateData();
		//TODO:Create account with firebase
	}

	private void validateData() {
		String firstName = String.valueOf(tietFirstName.getText());
		String lastName = String.valueOf(tietLastName.getText());
		String birthday = String.valueOf(tietBirthday.getText());
		String email = String.valueOf(tietEmail.getText());
		String password = String.valueOf(tietPassword.getText());
		String confirmPassword = String.valueOf(tietConfirmPassword.getText());

		if (firstName.trim().isEmpty()) {
			setErrorFirstName();
			foundError = true;
		}
		if (lastName.trim().isEmpty()) {
			setErrorLastName();
			foundError = true;
		}
		if (birthday.trim().isEmpty())  {
			setErrorBirthday();
			foundError = true;
		}
		if (email.trim().isEmpty()) {
			setErrorEmail();
			foundError = true;
		}
		if (password.trim().isEmpty()) {
			setErrorPassword();
			foundError = true;
			passOk = false;
		}
		if (confirmPassword.trim().isEmpty()) {
			setErrorConfirmPassword();
			foundError = true;
			passOk = false;
		}
		if (passOk) {
			if (!password.equals(confirmPassword)) {
				tilConfirmPassword.setError("Passwords don't match");
				foundError = true;
			}
		}
		passOk = true;
		if (foundError) {
			foundError = false;
			return;
		}
		foundError = false;
		Toast.makeText(getContext(), "OK", Toast.LENGTH_SHORT).show();
	}

	//region Datepicker
	private void openDatePicker() {

		// Get Current Date
		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH);


		if (String.valueOf(tietBirthday.getText()).equals("")) {
			datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
				tietBirthday.setText(String.format("%02d", dayOfMonth)
						+ "-" + String.format("%02d", monthOfYear + 1)
						+ "-" + String.format("%02d", year));
				tietBirthday.clearFocus();
			}, mYear, mMonth, mDay);
		} else {
			LocalDate localDate = LocalDate.parse(String.valueOf(tietBirthday.getText()), DateTimeFormatter.ofPattern("d-M-yyyy"));
			datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
				tietBirthday.setText(String.format("%02d", dayOfMonth)
						+ "-" + String.format("%02d", monthOfYear + 1)
						+ "-" + String.format("%02d", year));
				tietBirthday.clearFocus();
			}, localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
		}
		datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
		Calendar minYear = Calendar.getInstance();
		minYear.add(Calendar.YEAR, -110);
		datePickerDialog.getDatePicker().setMinDate(minYear.getTimeInMillis());
		datePickerDialog.setOnCancelListener(l -> tietBirthday.clearFocus());
		datePickerDialog.show();
	}
	private void hideDatePicker() {
		datePickerDialog.hide();
	}
	//endregion

	//region Set Errors
	private void setErrorFirstName() {
		tilFirstName.setError("You have to fill in your first name");
	}
	private void setErrorLastName() {
		tilLastName.setError("You have to fill in your last name");
	}
	private void setErrorBirthday() {
		tilBirthday.setError("You have to fill in your birthday");
	}
	private void setErrorEmail() {
		tilEmail.setError("You have to fill in your email");
	}
	private void setErrorPassword() {
		tilPassword.setError("You have to fill in your password");
	}
	private void setErrorConfirmPassword() {
		tilConfirmPassword.setError("You have to fill in your password");
	}
	//endregion
}