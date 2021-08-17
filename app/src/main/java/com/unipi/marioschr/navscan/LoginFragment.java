package com.unipi.marioschr.navscan;

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

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LoginFragment extends Fragment implements View.OnClickListener {
	private static final String TAG = "GoogleActivity";
	private static final int RC_SIGN_IN = 9001;

	private FirebaseAuth mAuth;
	private FirebaseFirestore db;
	private GoogleSignInClient mGoogleSignInClient;

	private TextView tvSignUp;
	private Button btnSignIn, btnGoogleSignIn, btnFacebookSignIn;

	private TextInputLayout tilLoginEmail, tilLoginPassword;
	private TextInputEditText tietLoginEmail, tietLoginPassword;
	private String loginEmail, loginPassword;
	private boolean foundError = false;
	CallbackManager callbackManager;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		callbackManager = CallbackManager.Factory.create();
		// Configure Google Sign In

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
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_login, container, false);
	}

	@Override
	public void onViewCreated(@Nonnull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		findViewsAndSetListeners(view);

		if (getActivity().getIntent().getBooleanExtra("HaveToGoogleRegister",false)) {
			getActivity().getIntent().removeExtra("HaveToGoogleRegister");
			navigateToGoogleSignUp();
		}
	}

	private void findViewsAndSetListeners(View view) {
		tvSignUp = view.findViewById(R.id.tvSignUpLoginFrag);
		btnSignIn = view.findViewById(R.id.btnSignIn);
		btnFacebookSignIn = view.findViewById(R.id.btnFacebookSignIn);
		btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn);

		tietLoginEmail = view.findViewById(R.id.tietLoginEmail);
		tietLoginPassword = view.findViewById(R.id.tietLoginPassword);
		tilLoginEmail = view.findViewById(R.id.tilLoginEmail);
		tilLoginPassword = view.findViewById(R.id.tilLoginPassword);

		tvSignUp.setOnClickListener(this);
		btnSignIn.setOnClickListener(this);
		btnGoogleSignIn.setOnClickListener(this);
		btnFacebookSignIn.setOnClickListener(this);
		LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
				Toast.makeText(getContext(), loginResult.getAccessToken().getUserId(), Toast.LENGTH_SHORT).show();
				handleFacebookAccessToken(loginResult.getAccessToken());
			}

			@Override
			public void onCancel() {
				Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(FacebookException error) {
				Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
			}
		});
		//region TextChanged Listeners
		tietLoginEmail.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (tietLoginEmail.hasFocus() && s.toString().trim().isEmpty()) {
					setErrorLoginEmail();
				} else {
					tilLoginEmail.setError(null);
				}
			}
		});
		tietLoginPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (tietLoginEmail.hasFocus() && s.toString().trim().isEmpty()) {
					setErrorLoginPassword();
				} else {
					tilLoginPassword.setError(null);
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
					Toast.makeText(requireActivity(), "Sign in failed. Try again.", Toast.LENGTH_SHORT).show();
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
							if (task.isSuccessful()) {
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
						Toast.makeText(requireActivity(), "Sign in failed. Try again.", Toast.LENGTH_SHORT).show();
					}
				});
	}
	//endregion

	private void facebookSignIn() {
		LoginManager.getInstance().logInWithReadPermissions(
				this,
				Arrays.asList("email", "user_birthday", "public_profile")
		);
	}

	private void handleFacebookAccessToken(AccessToken token) {
		Log.d(TAG, "handleFacebookAccessToken:" + token);

		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
		mAuth.signInWithCredential(credential)
				.addOnCompleteListener(requireActivity(), task -> {
					if (task.isSuccessful()) {
						// Sign in success, update UI with the signed-in user's information
						Log.d(TAG, "signInWithCredential:success");
						FirebaseUser user = mAuth.getCurrentUser();
						navigateToMain();
					} else {
						// If sign in fails, display a message to the user.
						Log.w(TAG, "signInWithCredential:failure", task.getException());
						Toast.makeText(getContext(), "Authentication failed.",
								Toast.LENGTH_SHORT).show();
					}
				});
	}

	private void emailSignIn() {
		if(!validateData()) return;
		mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(requireActivity(), task -> {
			if (task.isSuccessful()) {
				// Sign in success, update UI with the signed-in user's information
				Log.d(TAG, "signInWithEmail:success");
				FirebaseUser user = mAuth.getCurrentUser();
				navigateToMain();
			} else {
				// If sign in fails, display a message to the user.
				Log.w(TAG, "signInWithEmail:failure", task.getException());
				Toast.makeText(getContext(), "Authentication failed.",
						Toast.LENGTH_SHORT).show();
			}

		});
	}

	private boolean validateData() {
		loginEmail = tietLoginEmail.getText().toString().trim();
		loginPassword = tietLoginPassword.getText().toString();
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
		Toast.makeText(getContext(), "OK", Toast.LENGTH_SHORT).show();
		return true;
	}

	private void setErrorLoginEmail() {
		tilLoginEmail.setError("You have to fill in your email");
	}
	private void setErrorLoginPassword() {
		tilLoginPassword.setError("You have to fill in your password");
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