
package com.company.raunaqmathur.mytube;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

@SuppressWarnings("serial")
public class LoginActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,View.OnClickListener

{
    YouTube youtube;
    private String mEmailID;
    private String mPassword;
    private SignInButton mButton;
    private GoogleApiClient mGoogleApiClient;
    private Button mYoutube;
    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "Activity";

    private boolean mIsResolving = false;
    private boolean mShouldResolve = false;



    private GoogleAccountCredential credential;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new GsonFactory();
    private String mChosenAccountName = "raunaq.mathur22@gmail.com";


    private static final int REQUEST_AUTHORIZATION = 1;

    private String[] youtubeScopes = {YouTubeScopes.YOUTUBE, YouTubeScopes.YOUTUBE_UPLOAD};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //mEmailID =  ((EditText) findViewById(R.id.email)).getText().toString();
        //mPassword =  ((EditText) findViewById(R.id.password)).getText().toString();
        mButton =  (SignInButton)findViewById(R.id.buttonSignIn);
        //mYoutube = (Button) findViewById(R.id.youtube);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Plus.API).addScope(new Scope(Scopes.PROFILE)).addScope(new Scope(Scopes.EMAIL)).build();
       // mButton.setOnClickListener()
       mButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {

                if (v.getId() == R.id.buttonSignIn) {
                    onSignInClicked();

                   /*updateUI();*/
                    /*Intent intent = new Intent(getBaseContext(), YoutubeActivity.class);
                    intent.putExtra("youtubeObject", (Serializable) youtube);
                    startActivity(intent);*/
                }
            }

        });


    }
    public void signInClick()
    {

            onSignInClicked();

            //updateUI();

            Intent intent = new Intent(getBaseContext(), YoutubeActivity.class);
            intent.putExtra("youtubeObject", (Serializable) youtube);
            startActivity(intent);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onSignInClicked() {
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        mShouldResolve = true;
        mGoogleApiClient.connect();

        // Show a message to the user that we are signing in.
        Toast.makeText(LoginActivity.this, "Signing in", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        switch (requestCode){
            case RC_SIGN_IN:
                if (resultCode != RESULT_OK) {
                    mShouldResolve = false;
                }
                mIsResolving = false;
                mGoogleApiClient.connect();
                break;
            case REQUEST_AUTHORIZATION:
                if(resultCode == Activity.RESULT_OK){
                    String accountName = data.getExtras().getString(
                            AccountManager.KEY_ACCOUNT_NAME);
                }


        }

    }

    @Override
    public void onConnected(Bundle bundle) {

        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + bundle);
        mShouldResolve = false;

        // Show the signed-in UI
        Toast.makeText(LoginActivity.this, "Signed in", Toast.LENGTH_SHORT).show();
        //SharedPreferences sp = PreferenceManager
        //       .getDefaultSharedPreferences(this);

        //mChosenAccountName = sp.getString(ACCOUNT_KEY, null);
        Toast.makeText(LoginActivity.this, mChosenAccountName, Toast.LENGTH_SHORT).show();



        YouTubeClass yt = new YouTubeClass(mChosenAccountName, getApplicationContext());
        youtube = YouTubeClass.getYouTube();
        new Thread() {
            public void run(){
                 try

                {
                    ChannelListResponse clr = youtube.channels()
                            .list("contentDetails").setMine(true).execute();
                } catch (UserRecoverableAuthIOException e)

                {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {//Log.e(TAG, e.getMessage());
                }

            }
        }.start();





        Toast.makeText(LoginActivity.this, "update UI called", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getBaseContext(), YoutubeActivity.class);

        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an1
                // error dialog.
                String conn = connectionResult.getErrorMessage();

                Toast.makeText(LoginActivity.this, conn, Toast.LENGTH_SHORT).show();
            }
        } //else {
        // Show the signed-out UI
        //showSignedOutUI();
        //}

    }
/*
    private void updateUI() {


        credential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), Arrays.asList(youtubeScopes));
        credential.setSelectedAccountName(mChosenAccountName);
         youtube= new YouTube.Builder(transport, jsonFactory,
                credential).setApplicationName("MyTube")
                .build();
        new AsyncTask<Void, Void, Void>() {
            @Override

            protected Void doInBackground(Void... voids){



                try

                {
                    ChannelListResponse clr = youtube.channels()
                            .list("contentDetails").setMine(true).execute();
                }

                catch(UserRecoverableAuthIOException e)

                {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                }

                catch(IOException e)
                {Log.e(TAG, e.getMessage());
                }
                return null;
            }
        }.execute((Void) null);
    }
*/
    @Override
    public void onClick(View v) {

    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        //mGoogleApiClient.disconnect();
    }
}
