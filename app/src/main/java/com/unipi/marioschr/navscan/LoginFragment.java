package com.unipi.marioschr.navscan;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private TextView tvSignUp;
    private Button btnSignIn,btnFacebookSignIn,btnGoogleSignIn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Configure Google Sign In

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            navigateToMain();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        tvSignUp.setOnClickListener(this);
        btnGoogleSignIn.setOnClickListener(this);
        btnFacebookSignIn.setOnClickListener(this);
    }

    private void findViews(View view){
        tvSignUp = view.findViewById(R.id.tvSignUpLoginFrag);
        btnSignIn = view.findViewById(R.id.btnSignIn);
        btnFacebookSignIn = view.findViewById(R.id.btnFacebookSignIn);
        btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn);
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
                    Log.w(TAG, "signInWithCredential:failure"+e.getStatusCode(), e.getCause());
                    // Google Sign In failed, update UI appropriately
                    Toast.makeText(requireActivity(), "Sign in failed. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
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
                        navigateToMain();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(requireActivity(), "Sign in failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    //endregion

    private void facebookSignIn() {

    }

    private void navigateToMain(){
        startActivity(new Intent(getActivity(),MainActivity.class));
        requireActivity().finish();
    }

    private void navigateToSignUp(){
        NavHostFragment.findNavController(this).navigate(R.id.action_loginFragment_to_registerFragment);
    }
}