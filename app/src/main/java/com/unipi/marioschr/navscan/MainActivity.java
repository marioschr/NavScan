package com.unipi.marioschr.navscan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void SignOut(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this,AuthActivity.class));
        finish();
    }
}