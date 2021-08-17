package com.unipi.marioschr.navscan;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class RegisterFragment extends Fragment implements View.OnClickListener {
	private TextInputEditText tietFullName,tietBirthday,tietEmail,tietPassword,tietConfirmPassword;
	private TextInputLayout tilFullName,tilBirthday,tilEmail,tilPassword,tilConfirmPassword;
	private TextView tvSignInRegisterFrag;
	private Button btnSignUp;
	private String fullName,birthday,email,password,confirmPassword;
	private LocalDate birthdayLD;
	private DatePickerDialog datePickerDialog;
	private boolean foundError = false, passOk = true;
	private FirebaseAuth mAuth;
	private FirebaseFirestore db;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_register, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		findViewsAndSetListeners(view);
		mAuth = FirebaseAuth.getInstance();
		db = FirebaseFirestore.getInstance();
	}

	private void findViewsAndSetListeners(View view) {
		//region FindViewById
		tietFullName = view.findViewById(R.id.tietFullName);
		tietBirthday = view.findViewById(R.id.tietBirthday);
		tietEmail = view.findViewById(R.id.tietEmail);
		tietPassword = view.findViewById(R.id.tietPassword);
		tietConfirmPassword = view.findViewById(R.id.tietConfirmPassword);

		tilFullName = view.findViewById(R.id.tilFullName);
		tilBirthday = view.findViewById(R.id.tilBirthday);
		tilEmail = view.findViewById(R.id.tilEmail);
		tilPassword = view.findViewById(R.id.tilPassword);
		tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);

		tvSignInRegisterFrag = view.findViewById(R.id.tvSignInRegisterFrag);

		btnSignUp = view.findViewById(R.id.btnSignUp);
		//endregion

		//region TextChangedListeners
		tietFullName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) {
					setErrorFullName();
				} else {
					tilFullName.setError(null);
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
		tvSignInRegisterFrag.setOnClickListener(this);
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
			case R.id.tvSignInRegisterFrag:
				navigateBack();
				break;
		}
	}

	private void signUp() {
		if(!validateData()) return;
		register(email,password);
	}

	private void navigateBack() {
		NavHostFragment.findNavController(this).popBackStack();
	}

	private void navigateToMain() {
		startActivity(new Intent(getActivity(), MainActivity.class));
		requireActivity().finish();
	}

	private boolean validateData() {
		fullName = String.valueOf(tietFullName.getText());
		birthday = String.valueOf(tietBirthday.getText());
		email = String.valueOf(tietEmail.getText());
		password = String.valueOf(tietPassword.getText());
		confirmPassword = String.valueOf(tietConfirmPassword.getText());

		if (fullName.trim().isEmpty()) {
			setErrorFullName();
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
			return false;
		}
		foundError = false;
		return true;
	}

	private void register(String email, String password) {
		mAuth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(requireActivity(), task -> {
					if (task.isSuccessful()) {
						// Sign in success, update UI with the signed-in user's information
						Log.d("Firebase Register", "createUserWithEmail:success");
						FirebaseUser user = mAuth.getCurrentUser();
						Map<String, Object> userData = new HashMap<>();
						userData.put("fullName", fullName);
						userData.put("birthday", new Timestamp(new Date(birthdayLD.getMonth().toString() + " " + birthdayLD.getDayOfMonth() + ", " + birthdayLD.getYear())));
						userData.put("email", email);

						db.collection("users").document(user.getUid())
								.set(userData)
								.addOnSuccessListener(l -> {
									Log.d("Firestore DB", "DocumentSnapshot successfully written!");
									Toast.makeText(getContext(), "Successfully created user", Toast.LENGTH_SHORT).show();
									navigateToMain();
								})
								.addOnFailureListener(e -> {
									Log.w("Firestore DB", "Error writing document", e);
									mAuth.getCurrentUser().delete().addOnSuccessListener(l -> {
										Toast.makeText(getContext(), "Error creating user. Try again", Toast.LENGTH_SHORT).show();
									});
								});
					} else {
						// If sign in fails, display a message to the user.
						Log.w("Firebase Register", "createUserWithEmail:failure", task.getException());
						Toast.makeText(getContext(), "Authentication failed.",
								Toast.LENGTH_SHORT).show();
					}
				});
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
				birthdayLD = LocalDate.parse(String.valueOf(tietBirthday.getText()), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
				tietBirthday.clearFocus();
			}, mYear, mMonth, mDay);
		} else {
			LocalDate localDate = LocalDate.parse(String.valueOf(tietBirthday.getText()), DateTimeFormatter.ofPattern("d-M-yyyy"));
			datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
				tietBirthday.setText(String.format("%02d", dayOfMonth)
						+ "-" + String.format("%02d", monthOfYear + 1)
						+ "-" + String.format("%02d", year));
				birthdayLD = LocalDate.parse(String.valueOf(tietBirthday.getText()), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
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
	private void setErrorFullName() {
		tilFullName.setError("You have to fill in your name");
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