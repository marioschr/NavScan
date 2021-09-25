package com.unipi.marioschr.navscan;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.unipi.marioschr.navscan.databinding.FragmentEditProfileBinding;
import com.unipi.marioschr.navscan.viewmodels.UserDataViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import es.dmoral.toasty.Toasty;

public class EditProfileFragment extends Fragment implements View.OnClickListener {
	UserDataViewModel viewModel;
	FragmentEditProfileBinding binding;
	LocalDate birthdayLD;
	DatePickerDialog datePickerDialog;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		binding = FragmentEditProfileBinding.inflate(inflater, container, false);
		SetOnClickListeners();
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(UserDataViewModel.class);
		viewModel.getUserData(FirebaseAuth.getInstance().getUid()).observe(requireActivity(), user -> {
			binding.tvEditName.setText(user.getFullName());
			binding.tvEditBirthday.setText(user.getBirthday());
		});
	}

	private void SetOnClickListeners() {
		binding.btnEditName.setOnClickListener(this);
		binding.btnEditBirthday.setOnClickListener(this);
		binding.btnEditPass.setOnClickListener(this);
	}

	private void EditName() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getContext()); // Η δημιουργία του alert dialog
		LayoutInflater inflater = LayoutInflater.from(getContext());
		final View customLayout = inflater.inflate(R.layout.alert_dialog_change_name, null);
		alert.setView(customLayout);
		Dialog dialog = alert.create();
		TextInputEditText tietEditName;
		Button btnApply, btnCancel;
		btnApply = customLayout.findViewById(R.id.alertBtnChangeNameApply);
		btnCancel = customLayout.findViewById(R.id.alertBtnChangeNameCancel);
		tietEditName = customLayout.findViewById(R.id.alertTietEditName);
		tietEditName.setText(binding.tvEditName.getText());
		btnApply.setOnClickListener(view -> {
			if (tietEditName.getText().toString().trim().isEmpty()) {
				tietEditName.setError("You have to fill in your full name");
			} else {
				binding.tvEditName.setText(tietEditName.getText());
				viewModel.editName(FirebaseAuth.getInstance().getUid(), tietEditName.getText().toString(), getContext());
				dialog.dismiss();
			}
		});
		btnCancel.setOnClickListener(view -> dialog.dismiss());
		dialog.show();
	}

	private void EditBirthday() {
		final Calendar c = Calendar.getInstance();
		LocalDate localDate = LocalDate.parse(String.valueOf(binding.tvEditBirthday.getText()), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
			String formattedDate = String.format("%02d", dayOfMonth)
					+ "/" + String.format("%02d", monthOfYear + 1)
					+ "/" + year;
			binding.tvEditBirthday.setText(formattedDate);
			birthdayLD = LocalDate.parse(formattedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			viewModel.editBirthday(FirebaseAuth.getInstance().getUid(), birthdayLD, getContext());
		}, localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());

		datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
		Calendar minYear = Calendar.getInstance();
		minYear.add(Calendar.YEAR, -110);
		datePickerDialog.getDatePicker().setMinDate(minYear.getTimeInMillis());
		datePickerDialog.setOnCancelListener(l -> binding.tvEditBirthday.clearFocus());
		datePickerDialog.show();
	}

	private void EditPassword() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getContext()); // Η δημιουργία του alert dialog
		LayoutInflater inflater = LayoutInflater.from(getContext());
		final View customLayout = inflater.inflate(R.layout.alert_dialog_change_pass, null);
		alert.setView(customLayout);
		AlertDialog dialog = alert.create();
		FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		TextInputEditText tietCurrentPass,tietNewPass,tietConfirmNewPass;
		TextInputLayout tilCurrentPass,tilNewPass,tilConfirmNewPass;
		Button btnApply,btnCancel;
		//region findViewById
		tietCurrentPass = customLayout.findViewById(R.id.alertTietCurrentPass);
		tietNewPass = customLayout.findViewById(R.id.alertTietNewPass);
		tietConfirmNewPass = customLayout.findViewById(R.id.alertTietConfirmNewPass);
		tilCurrentPass = customLayout.findViewById(R.id.alertTilCurrentPass);
		tilNewPass= customLayout.findViewById(R.id.alertTilNewPass);
		tilConfirmNewPass = customLayout.findViewById(R.id.alertTilConfirmNewPass);
		btnApply = customLayout.findViewById(R.id.alertBtnChangePassApply);
		btnCancel = customLayout.findViewById(R.id.alertBtnChangePassCancel);
		//endregion
		tietCurrentPass.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) {
					tilCurrentPass.setError("You have to fill in your current password");
				} else {
					tilCurrentPass.setError(null);
				}
			}
		});
		tietNewPass.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) {
					tilNewPass.setError("You have to fill in your new password");
				} else {
					tilNewPass.setError(null);
				}
			}
		});
		tietConfirmNewPass.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) {
					tilConfirmNewPass.setError("You have to fill in your new password");
				} else if (!s.toString().equals(String.valueOf(tietNewPass.getText())))
					tilConfirmNewPass.setError("Passwords don't match");
				else tilConfirmNewPass.setError(null);
			}
		});
		btnApply.setOnClickListener(view -> {
			String str_current_pass = String.valueOf(tietCurrentPass.getText());
			String str_new_pass = String.valueOf(tietNewPass.getText());
			String str_confirm_new_pass = String.valueOf(tietConfirmNewPass.getText());
			if (str_current_pass.trim().isEmpty() || str_new_pass.trim().isEmpty() || str_confirm_new_pass.trim().isEmpty()) {
				if (str_current_pass.trim().isEmpty()) {
					tilCurrentPass.setError("You have to fill in your current password");
				}
				if (str_new_pass.trim().isEmpty()) {
					tilNewPass.setError("You have to fill in your new password");
				}
				if (str_confirm_new_pass.trim().isEmpty()) {
					tilConfirmNewPass.setError("You have to fill in your new password");
				}
			} else if (!str_new_pass.equals(str_confirm_new_pass)) {
				tilNewPass.setError("Passwords don't match");
				tilConfirmNewPass.setError("Passwords don't match");
			} else { // Πρέπει πρώτα να γίνει reauthenticate για να μπορεί να γίνει αλλαγή του κωδικού
				AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), str_current_pass);
				firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
					if (task.isSuccessful()) {
						firebaseUser.updatePassword(str_new_pass).addOnCompleteListener(task1 -> {
							if (!task1.isSuccessful()) {
								try {
									throw task1.getException();
								} catch (FirebaseAuthWeakPasswordException e) {
									Toasty.error(requireContext(), "The new password is weak", Toasty.LENGTH_SHORT).show();
								} catch (Exception ex) {
									Toasty.error(requireContext(), "Password change failed", Toasty.LENGTH_SHORT).show();
								}
							} else {
								Toasty.success(requireContext(), "Password changed successfully", Toasty.LENGTH_SHORT).show();
								dialog.dismiss();
							}
						});
					} else {
						tilCurrentPass.setError("Wrong Password");
					}
				});
			}
		});
		btnCancel.setOnClickListener(view -> dialog.dismiss());
		dialog.show();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btnEditName) {
			EditName();
		} else if (view.getId() == R.id.btnEditBirthday) {
			EditBirthday();
		} else if (view.getId() == R.id.btnEditPass) {
			EditPassword();
		}
	}

	//TODO: Edit Profile Picture
}