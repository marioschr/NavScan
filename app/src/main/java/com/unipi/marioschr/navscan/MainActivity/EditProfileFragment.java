package com.unipi.marioschr.navscan.MainActivity;

import android.app.Activity;
import androidx.appcompat.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.databinding.FragmentEditProfileBinding;
import com.unipi.marioschr.navscan.utils.LoadingDialog;
import com.unipi.marioschr.navscan.viewmodels.UserDataViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import es.dmoral.toasty.Toasty;

public class EditProfileFragment extends Fragment implements View.OnClickListener {
	UserDataViewModel viewModel;
	FragmentEditProfileBinding binding;
	LocalDate birthdayLD;
	DatePickerDialog datePickerDialog;
	StorageReference profileRef;
	FirebaseFirestore db;
	DocumentReference documentRef;
	String userID = FirebaseAuth.getInstance().getUid();

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		binding = FragmentEditProfileBinding.inflate(inflater, container, false);
		profileRef = FirebaseStorage.getInstance().getReference().child("users").child(userID).child("profile.jpg");
		db = FirebaseFirestore.getInstance();
		documentRef = db.collection("users").document(userID);
		SetOnClickListeners();
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(UserDataViewModel.class);
		viewModel.getUserData(userID).observe(requireActivity(), user -> {
			binding.tvEditName.setText(user.getFullName());
			binding.tvEditBirthday.setText(user.getBirthday());
			if (user.getPicture() != null) {
				Glide.with(requireActivity()).load(user.getPicture())
						.circleCrop()
						.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
						.error(R.drawable.default_profile)
						.into(binding.imgEditProfile);
			} else {
				profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
					Glide.with(requireActivity()).load(uri)
							.circleCrop()
							.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
							.error(R.drawable.default_profile)
							.into(binding.imgEditProfile);
				}).addOnFailureListener(e -> {
					Glide.with(requireActivity()).load(R.drawable.default_profile)
							.circleCrop()
							.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
							.into(binding.imgEditProfile);
				});
			}
		});
	}

	private void SetOnClickListeners() {
		binding.btnEditName.setOnClickListener(this);
		binding.btnEditBirthday.setOnClickListener(this);
		binding.btnEditPass.setOnClickListener(this);
		binding.fabEditProfilePicture.setOnClickListener(this);
	}

	private void EditName() {
		AlertDialog.Builder alert = new AlertDialog.Builder(requireActivity()); // Η δημιουργία του alert dialog
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
				tietEditName.setError(getString(R.string.you_have_to_fill_in_your_full_name));
			} else {
				binding.tvEditName.setText(tietEditName.getText());
				viewModel.editName(userID, tietEditName.getText().toString(), getContext());
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
			viewModel.editBirthday(userID, birthdayLD, getContext());
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
		//region textChangedListeners
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
		//endregion
		btnApply.setOnClickListener(view -> {
			String str_current_pass = String.valueOf(tietCurrentPass.getText());
			String str_new_pass = String.valueOf(tietNewPass.getText());
			String str_confirm_new_pass = String.valueOf(tietConfirmNewPass.getText());
			if (str_current_pass.trim().isEmpty() || str_new_pass.trim().isEmpty() || str_confirm_new_pass.trim().isEmpty()) {
				if (str_current_pass.trim().isEmpty()) {
					tilCurrentPass.setError(getString(R.string.you_have_to_fill_in_your_current_password));
				}
				if (str_new_pass.trim().isEmpty()) {
					tilNewPass.setError(getString(R.string.you_have_to_fill_in_your_new_password));
				}
				if (str_confirm_new_pass.trim().isEmpty()) {
					tilConfirmNewPass.setError(getString(R.string.you_have_to_fill_in_your_new_password));
				}
			} else if (!str_new_pass.equals(str_confirm_new_pass)) {
				tilNewPass.setError(getString(R.string.passwords_dont_match));
				tilConfirmNewPass.setError(getString(R.string.passwords_dont_match));
			} else { // Πρέπει πρώτα να γίνει reauthenticate για να μπορεί να γίνει αλλαγή του κωδικού
				AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), str_current_pass);
				firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
					if (task.isSuccessful()) {
						firebaseUser.updatePassword(str_new_pass).addOnCompleteListener(task1 -> {
							if (!task1.isSuccessful()) {
								try {
									throw task1.getException();
								} catch (FirebaseAuthWeakPasswordException e) {
									Toasty.error(requireContext(), getString(R.string.the_new_password_is_weak), Toasty.LENGTH_SHORT).show();
								} catch (Exception ex) {
									Toasty.error(requireContext(), getString(R.string.password_change_failed), Toasty.LENGTH_SHORT).show();
								}
							} else {
								Toasty.success(requireContext(), getString(R.string.password_changed_successfully), Toasty.LENGTH_SHORT).show();
								dialog.dismiss();
							}
						});
					} else {
						tilCurrentPass.setError(getString(R.string.wrong_password));
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
		} else if (view.getId() == R.id.fabEditProfilePicture) {
			Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(openGalleryIntent, 6969);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // Όταν γίνει επιλογή εικόνας από το τηλέφωνο
		super.onActivityResult( requestCode, resultCode, data);
		if (requestCode == 6969) {
			if(resultCode == Activity.RESULT_OK) {
				Uri imageUri = data.getData();
				uploadImageToFirebase(imageUri);
			}
		}
	}

	private void uploadImageToFirebase(Uri imageUri) { // Επεξεργασία και αποστολή της εικόνας στο Firebase Storage
		LoadingDialog loadingDialog = new LoadingDialog(getActivity());
		loadingDialog.startLoadingDialog();
		Bitmap bmp = null;
		try {
			bmp = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Bitmap rotatedBitmap = rotateImageIfRequired(getContext(), bmp, imageUri); // Έλεγχος αν χριάζεται να περιστραφεί
			rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos); // Συμπίεση της εικόνας
			byte[] data = baos.toByteArray();
			UploadTask uploadTask = profileRef.putBytes(data);
			uploadTask.addOnSuccessListener(taskSnapshot -> {
				profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
					viewModel.updateUserData(userID);
				});
				loadingDialog.dismissDialog();
				Toasty.success(getContext(), getString(R.string.profile_picture_changed_successfully), Toasty.LENGTH_LONG).show();
			}).addOnFailureListener(e -> {
				loadingDialog.dismissDialog();
				Toasty.error(getContext(), getString(R.string.failed_to_change_profile_picture), Toasty.LENGTH_LONG).show();
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {
		// Μέθοδος που ελέγχει τα Exif data αν χριάζεται rotate η εικόνα
		InputStream input = context.getContentResolver().openInputStream(selectedImage);
		ExifInterface ei;
		ei = new ExifInterface(input);

		int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

		switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				return rotateImage(img, 90);
			case ExifInterface.ORIENTATION_ROTATE_180:
				return rotateImage(img, 180);
			case ExifInterface.ORIENTATION_ROTATE_270:
				return rotateImage(img, 270);
			default:
				return img;
		}
	}

	private static Bitmap rotateImage(Bitmap img, int degree) {
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
		img.recycle();
		return rotatedImg;
	}
}