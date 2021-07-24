package com.ltts.testgooglefit;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.ltts.testgooglefit.models.User;
import com.ltts.testgooglefit.utils.Constants;
import com.ltts.testgooglefit.views.UserServices;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoogleFitActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private TextView tvUserName;
    private boolean authInProgress = false;
    private GoogleApiClient mApiClient;
    private GoogleSignInAccount googleSignInAccount;
    private User loggedUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlefit);

        initUI();
        initGoogleApi(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }

    /**
     * Initialize UI components
     */
    private void initUI() {
        tvUserName = findViewById(R.id.tv_user_name);

        findViewById(R.id.btn_request_blood_glucose_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBloodGlucoseData();
            }
        });

        findViewById(R.id.btn_sign_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignOut();
            }
        });
    }

    /**
     * @param savedInstanceState Initialize the google api client
     */
    private void initGoogleApi(Bundle savedInstanceState) {
        googleSignInAccount = getIntent().getParcelableExtra(Constants.GOOGLE_ACCOUNT);
        tvUserName.setText(googleSignInAccount.getDisplayName());
        Log.d(Constants.TAG, "Token : " + Constants.ACCESS_TOKEN);
        Log.d(Constants.TAG, "Granted Scope : " + googleSignInAccount.getGrantedScopes());

//            loggedUser.setEmail(googleSignInAccount.getEmail());
//            loggedUser.setUser_name(googleSignInAccount.getDisplayName());
//            loggedUser.setAccess_token(googleSignInAccount.getIdToken());

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(Constants.AUTH_PENDING);
        }

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);
        builder.addApi(Fitness.HISTORY_API);
        builder.addScope(new Scope(Scopes.FITNESS_BLOOD_GLUCOSE_READ));
        builder.addConnectionCallbacks(this);
        builder.addOnConnectionFailedListener(this);
        mApiClient = builder.build();
    }

    /**
     * Request to get the blood glucose data from GoogleFit API
     */
    private void requestBloodGlucoseData() {
        try {
            String jsonData = "{\n" +
                    "  \"aggregateBy\": [\n" +
                    "    {\n" +
                    "      \"dataTypeName\": \"com.google.blood_glucose\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"bucketByTime\": {\n" +
                    "    \"durationMillis\": 86400000\n" +
                    "  },\n" +
                    "  \"startTimeMillis\": 1578718800000,\n" +
                    "  \"endTimeMillis\": 1580360400000\n" +
                    "}";
            UserServices userService = ServiceGenerator.createService(UserServices.class, Constants.ACCESS_TOKEN);
            Call<ResponseBody> call = userService.getBloodGlucoseData(jsonData);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(Constants.TAG, "onResponse of call response : " + response);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d(Constants.TAG, "onFailure of call");
                }
            });
        } catch (Exception e) {
            Log.d(Constants.TAG, "Exception in requestBloodGlucoseData : " + e.getMessage());
        }
    }

    /**
     * Signout user account
     */
    private void googleSignOut() {
        if (MainActivity.mGoogleSignInClient != null)
            MainActivity.mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    //On Succesfull signout we navigate the user back to MainActivity
                    MainActivity.mGoogleSignInClient = null;
                    Intent intent = new Intent(GoogleFitActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data        Call back method when user retries to re-connect with google api
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constants.REQUEST_OAUTH:
                    authInProgress = false;
                    if (resultCode == RESULT_OK) {
                        if (!mApiClient.isConnecting() && !mApiClient.isConnected()) {
                            mApiClient.connect();
                        }
                    } else if (resultCode == RESULT_CANCELED) {
                        Log.e(Constants.TAG, "RESULT_CANCELED");
                    }
                    break;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(Constants.TAG, "onConnected...................");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(Constants.TAG, "onConnectionSuspended...................");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (!authInProgress) {
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult(this, Constants.REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {
                Log.e(Constants.TAG, "Exception while re-connecting.");
            }
        } else {
            Log.e(Constants.TAG, "Please wait authInProgress.");
        }
    }
}
