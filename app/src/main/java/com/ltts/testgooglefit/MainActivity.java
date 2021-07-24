package com.ltts.testgooglefit;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.ltts.testgooglefit.utils.Constants;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static GoogleSignInClient mGoogleSignInClient = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        initGoogleSignInClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAlreadyLoggedIn();
    }

    /**
     * Initialize UI components
     */
    private void initUI() {
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });
    }

    /**
     * Setup google sign in client with client id
     */
    private void initGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(Constants.GOOGLE_FIT_ANDROID_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    /**
     * Check whether the user is already logged in or not. If already logged in navigate to GoogleFitActivity by passing the user account
     */
    private void checkAlreadyLoggedIn() {
        GoogleSignInAccount alreadyloggedAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (alreadyloggedAccount != null) {
            Log.d(Constants.TAG, "Already Logged In");
            Toast.makeText(this, "Already Logged In", Toast.LENGTH_SHORT).show();
            onLoggedIn(alreadyloggedAccount);
        } else {
            Log.d(Constants.TAG, "Not logged in");
        }
    }

    /**
     * @param alreadyloggedAccount Call GoogleFitActivity by passing the user account if user is already logged in
     */
    private void onLoggedIn(GoogleSignInAccount alreadyloggedAccount) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String scope = "oauth2:"+ Scopes.EMAIL+" "+ Scopes.PROFILE + Scopes.FITNESS_BLOOD_GLUCOSE_READ;
                    String accessToken = GoogleAuthUtil.getToken(getApplicationContext(), alreadyloggedAccount.getAccount(), scope, new Bundle());
                    Log.d(Constants.TAG, "accessToken:"+accessToken); //accessToken:ya29.Gl...
                    Constants.ACCESS_TOKEN = accessToken;

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GoogleAuthException e) {
                    e.printStackTrace();
                }
            }
        };
        AsyncTask.execute(runnable);

        Constants.ACCESS_TOKEN = alreadyloggedAccount.getIdToken();

        Intent intent = new Intent(MainActivity.this, GoogleFitActivity.class);
        intent.putExtra(Constants.GOOGLE_ACCOUNT, alreadyloggedAccount);
        startActivity(intent);
        finish();
    }

    /**
     * Allow user to sign in with google account
     */
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, Constants.RC_SIGN_IN);
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data        Call back method after user sign in
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.RC_SIGN_IN:
                    try {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        onLoggedIn(account);
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
