package com.unipi.marioschr.navscan.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.marioschr.navscan.MainActivity.MainActivity;
import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.databinding.FragmentLoginBinding;
import com.unipi.marioschr.navscan.utils.LoadingDialog;

import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import es.dmoral.toasty.Toasty;

public class LoginFragment extends Fragment implements View.OnClickListener {
	private static final String TAG = "GoogleActivity";
	private static final int RC_SIGN_IN = 9001;
	private FirebaseAuth mAuth;
	private FirebaseFirestore db;
	private GoogleSignInClient mGoogleSignInClient;

	private String loginEmail, loginPassword;
	private boolean foundError = false;
	private CallbackManager callbackManager;
	private FragmentLoginBinding binding;
	LoadingDialog loadingDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		callbackManager = CallbackManager.Factory.create();

		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id)) // Token is created on build
				.requestEmail()
				.build();

		mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
		mAuth = FirebaseAuth.getInstance();
		db = FirebaseFirestore.getInstance();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		binding = FragmentLoginBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@Nonnull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		findViewsAndSetListeners(view);
		loadingDialog = new LoadingDialog(getActivity());
		if (getActivity().getIntent().getBooleanExtra("HaveToGoogleRegister",false)) {
			getActivity().getIntent().removeExtra("HaveToGoogleRegister");
			navigateToGoogleSignUp();
		}
	}

	private void findViewsAndSetListeners(View view) {
		binding.tvSignUpLoginFrag.setOnClickListener(this);
		binding.btnSignIn.setOnClickListener(this);
		binding.btnGoogleSignIn.setOnClickListener(this);
		binding.btnFacebookSignIn.setOnClickListener(this);
		LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
				handleFacebookAccessToken(loginResult.getAccessToken());
			}
			@Override
			public void onCancel() { }
			@Override
			public void onError(FacebookException error) { }
		});
		//region TextChanged Listeners
		binding.tietLoginEmail.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (binding.tietLoginEmail.hasFocus() && s.toString().trim().isEmpty()) {
					setErrorLoginEmail();
				} else {
					binding.tilLoginEmail.setError(null);
				}
			}
		});
		binding.tietLoginPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (binding.tietLoginEmail.hasFocus() && s.toString().trim().isEmpty()) {
					setErrorLoginPassword();
				} else {
					binding.tilLoginPassword.setError(null);
				}
			}
		});
		//endregion
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnGoogleSignIn:
				googleSignIn();
				break;
			case R.id.btnFacebookSignIn:
				facebookSignIn();
				break;
			case R.id.tvSignUpLoginFrag:
				navigateToSignUp();
				break;
			case R.id.btnSignIn:
				emailSignIn();
				break;
		}
	}

	//region Google Sign In
	private void googleSignIn() {
		Intent signInIntent = mGoogleSignInClient.getSignInIntent();
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RC_SIGN_IN) {
			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
			try {
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = task.getResult(ApiException.class);
				Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
				firebaseAuthWithGoogle(account.getIdToken());
			} catch (ApiException e) {
				if (e.getStatusCode() != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
					Log.w(TAG, "signInWithCredential:failure" + e.getStatusCode(), e.getCause());
					// Google Sign In failed, update UI appropriately
					Toasty.error(requireActivity(), getString(R.string.sign_in_failed_try_again), Toasty.LENGTH_SHORT).show();
				}
			}
		} else {
			callbackManager.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void firebaseAuthWithGoogle(String idToken) {
		AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
		mAuth.signInWithCredential(credential)
				.addOnCompleteListener(requireActivity(), task -> {
					if (task.isSuccessful()) {
						// Sign in success, update UI with the signed-in user's information
						Log.d(TAG, "signInWithCredential:success");
						FirebaseUser user = mAuth.getCurrentUser();
						DocumentReference docIdRef = db.collection("users").document(user.getUid());
						docIdRef.get().addOnCompleteListener(task1 -> {
							if (task1.isSuccessful()) {
								DocumentSnapshot document = task1.getResult();
								if (document.exists()) {
									Log.d(TAG, "Document exists!");
									navigateToMain();
								} else {
									Log.d(TAG, "Document does not exist!");
									navigateToGoogleSignUp();
								}
							} else {
								Log.d(TAG, "Failed with: ", task.getException());
							}
						});
					} else {
						// If sign in fails, display a message to the user.
						Log.w(TAG, "signInWithCredential:failure", task.getException());
						Toasty.error(requireActivity(), getString(R.string.sign_in_failed_try_again), Toasty.LENGTH_SHORT).show();
					}
				});
	}
	//endregion

	//region Facebook Sign In
	private void facebookSignIn() {
		LoginManager.getInstance().logInWithReadPermissions(
				this,
				Arrays.asList("email", "user_birthday", "public_profile")
		);
	}

	private void handleFacebookAccessToken(AccessToken token) {
		Log.d(TAG, "handleFacebookAccessToken:" + token);

		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
		loadingDialog.startLoadingDialog();
		mAuth.signInWithCredential(credential)
				.addOnCompleteListener(requireActivity(), task -> {
					if (task.isSuccessful()) {
						loadingDialog.dismissDialog();
						// Sign in success, update UI with the signed-in user's information
						Log.d(TAG, "signInWithCredential:success");
						DocumentReference docIdRef = db.collection("users").document(mAuth.getUid());
						docIdRef.get().addOnCompleteListener(firestoreTask -> {
							if (task.isSuccessful()) {
								DocumentSnapshot document = firestoreTask.getResult();
								if (document.exists()) {
									Log.d("TAG", "Document exists!");
									navigateToMain();
								} else {
									Log.d("TAG", "Document doesn't exist!");
									getUserDetailFromFB(token);
								}
							} else {
								Log.d("TAG", "Failed with: ", task.getException());
							}
						});
					} else {
						// If sign in fails, display a message to the user.
						Log.w(TAG, "signInWithCredential:failure", task.getException());
						Toasty.error(getContext(), getString(R.string.authentication_failed),
								Toasty.LENGTH_SHORT).show();
					}
				});
	}

	private void getUserDetailFromFB(AccessToken accessToken) {
		GraphRequest request = GraphRequest.newMeRequest(
				accessToken, (object, response) -> {
					if (object != null) {
						try {
							String name = object.getString("name");
							String email = object.getString("email");
							String birthday = object.getString("birthday");
							Map<String, Object> userData = new HashMap<>();
							userData.put("fullName", name);
							SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
							try {
								Date date= formatter.parse(birthday);
								userData.put("birthday", new Timestamp(date));
							} catch (ParseException e) {
								e.printStackTrace();
							}
							userData.put("email", email);
							userData.put("exp",0);

							db.collection("users").document(mAuth.getUid())
									.set(userData)
									.addOnSuccessListener(l -> {
										Log.d("Firestore DB", "DocumentSnapshot successfully written!");
										Toasty.success(getContext(), getString(R.string.registration_successful), Toasty.LENGTH_SHORT).show();
										navigateToMain();
									})
									.addOnFailureListener(e -> {
										Log.w("Firestore DB", "Error writing document", e);
										mAuth.getCurrentUser().delete().addOnSuccessListener(l ->
												Toasty.error(getContext(), getString(R.string.error_registering_try_again), Toasty.LENGTH_SHORT).show());
									});
						}
						catch (JSONException | NullPointerException e) {
							e.printStackTrace();
						}
					}
				});

		Bundle parameters = new Bundle();
		parameters.putString(
				"fields",
				"id, name, email, birthday");
		request.setParameters(parameters);
		request.executeAsync();
	}
	//endregion

	//region Email/Password Sign In
	private void emailSignIn() {
		if(!validateData()) return;
		loadingDialog.startLoadingDialog();
		mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(requireActivity(), task -> {
			if (task.isSuccessful()) {
				loadingDialog.dismissDialog();
				// Sign in success, update UI with the signed-in user's information
				Log.d(TAG, "signInWithEmail:success");
				navigateToMain();
			} else {
				// If sign in fails, display a message to the user.
				Log.w(TAG, "signInWithEmail:failure", task.getException());
				Toasty.error(getContext(), getString(R.string.sign_in_failed_try_again),
						Toasty.LENGTH_SHORT).show();
			}

		});
	}
	//endregion

	private boolean validateData() {
		loginEmail = String.valueOf(binding.tietLoginEmail.getText()).trim();
		loginPassword = String.valueOf(binding.tietLoginPassword.getText());
		if (loginEmail.isEmpty()) {
			setErrorLoginEmail();
			foundError = true;
		}
		if (loginPassword.trim().isEmpty()) {
			setErrorLoginPassword();
			foundError = true;
		}
		if (foundError) {
			foundError = false;
			return false;
		}
		foundError = false;
		return true;
	}

	private void setErrorLoginEmail() {
		binding.tilLoginEmail.setError(getString(R.string.you_have_to_fill_in_your_email));
	}
	private void setErrorLoginPassword() {
		binding.tilLoginPassword.setError(getString(R.string.you_have_to_fill_in_your_password));
	}

	private void navigateToMain() {
		startActivity(new Intent(getActivity(), MainActivity.class));
		requireActivity().finish();
	}

	private void navigateToSignUp() {
		NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_registerFragment);
	}

	private void navigateToGoogleSignUp() {
		NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_googleRegisterFragment);
	}
}