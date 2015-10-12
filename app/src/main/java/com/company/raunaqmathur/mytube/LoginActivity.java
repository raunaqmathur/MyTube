package com.company.raunaqmathur.mytube;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

public class LoginActivity extends Activity implements OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;

    // Google client to communicate with Google
    private GoogleApiClient mGoogleApiClient;

    private boolean mIntentInProgress;
    private boolean signedInUser;
    private ConnectionResult mConnectionResult;
    private SignInButton signinButton;
    private ImageView image;
    private TextView username, emailLabel;
    private LinearLayout profileFrame, signinFrame;
    public String                 accessToken        = "";
    private YouTube youtube;
    private GoogleAccountCredential credential;
    private YouTube.Search.List query;
    public static final String[] SCOPES = {Scopes.PROFILE, YouTubeScopes.YOUTUBE, YouTubeScopes.YOUTUBE_UPLOAD};
    public String accName="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signinButton = (SignInButton) findViewById(R.id.signin);
        signinButton.setOnClickListener(this);

        image = (ImageView) findViewById(R.id.image);
        username = (TextView) findViewById(R.id.username);
        emailLabel = (TextView) findViewById(R.id.email);

        profileFrame = (LinearLayout) findViewById(R.id.profileFrame);
        signinFrame = (LinearLayout) findViewById(R.id.signinFrame);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Plus.API, Plus.PlusOptions.builder().build()).addScope(Plus.SCOPE_PLUS_LOGIN).build();
    }



    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        if (!mIntentInProgress) {
            // store mConnectionResult
            mConnectionResult = result;

            if (signedInUser) {
                resolveSignInError();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        switch (requestCode) {
            case RC_SIGN_IN:
                if (responseCode == RESULT_OK) {
                    signedInUser = false;

                }
                mIntentInProgress = false;
                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
                break;
            case 1:
                if (responseCode == 1) {


                }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        signedInUser = false;
        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
        //Intent it = new Intent(this, SearchActivity.class);

        getProfileInformation();
        //startActivity(it);
    }

    private void updateProfile(boolean isSignedIn) {
        if (isSignedIn) {
            signinFrame.setVisibility(View.GONE);
            profileFrame.setVisibility(View.VISIBLE);

        } else {
            signinFrame.setVisibility(View.VISIBLE);
            profileFrame.setVisibility(View.GONE);
        }
    }

    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
                accName = email;
                //username.setText(personName);
                emailLabel.setText(email);

                new LoadProfileImage(image).execute(personPhotoUrl);

                // update profile frame with new info about Google Account
                // profile
                updateProfile(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            username.setText("Alrwady error: " + e);
        }
    }
    class RetrieveFeedTask extends AsyncTask<String, Void, YouTube> {

        private Exception exception;

        protected YouTube doInBackground(String... urls) {
            try {
                credential = GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Arrays.asList(SCOPES));
                SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                credential.setSelectedAccountName(settings.getString(Plus.AccountApi.getAccountName(mGoogleApiClient), null));

                youtube =
                        new com.google.api.services.youtube.YouTube.Builder(new NetHttpTransport(),
                                new JacksonFactory(), new HttpRequestInitializer(){
                            @Override
                            public void initialize(HttpRequest hr) throws IOException {}
                        }).setApplicationName("MyTube").build();

        return youtube;


            } catch (Exception e) {
                this.exception = e;

                return null;
            }
        }

        protected void onPostExecute(YouTube feed) {
            // TODO: check this.exception
            // TODO: do something with the feed
        }
    }
    public void searchClicked(View view)
    {


       // StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        try{
            credential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Arrays.asList(SCOPES));
            //SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
            credential.setSelectedAccountName(accName);

            new AsyncTask<Void, Void, Void>() {

                protected Void doInBackground (Void... voids) {
                    youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).

                            setApplicationName(getString(R.string.app_name)

                            ).

                            build();

                    try

                    {
                        ChannelListResponse results = youtube.channels().list("contentDetails").setMine(true).execute();
                    } catch (
                            UserRecoverableAuthIOException e
                            )

                    {


                        startActivityForResult(e.getIntent(), 1);
                    } catch (
                            IOException e
                            )

                    {

                    }
                    return null;
                }
            }.execute((Void)null);

        }catch(Exception e){
            Log.d("YC", "Could not initialize: " + e);
            ((TextView) findViewById(R.id.textViewVDO)).setText("Could not initialize: " + e);
            Toast.makeText(this, "" + e, Toast.LENGTH_LONG).show();
        }

    }
    public List<SearchItem> search(String keywords){
        query.setQ("dogs");

        try{
            SearchListResponse response = query.execute();

            List<SearchResult> results = response.getItems();
            Toast.makeText(this, "size is:" + results.size(), Toast.LENGTH_LONG).show();
            List<SearchItem> items = new ArrayList<SearchItem>();
            for(SearchResult result:results){
                SearchItem item = new SearchItem();
                item.setTitle(result.getSnippet().getTitle());
                item.setDescription(result.getSnippet().getDescription());
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(result.getId().getVideoId());
                items.add(item);
            }
            if(results.size() > 0)
                username.setText(items.get(0).toString());
            else
                username.setText("no item");
            return items;
        }catch(IOException e){
            Log.d("YC", "Could not search: " + e);
           // ((TextView) findViewById(R.id.textViewVDO)).setText("Could not initialize: " + e);
            username.setText("Could not initialize: " + e);
            Toast.makeText(this, "" + e, Toast.LENGTH_LONG).show();
            return null;
        }
    }
    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
        updateProfile(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signin:
                googlePlusLogin();
                break;
        }
    }

    public void signIn(View v) {
        googlePlusLogin();
    }

    public void logout(View v) {
        googlePlusLogout();
    }

    private void googlePlusLogin() {
        if (!mGoogleApiClient.isConnecting()) {
            signedInUser = true;
            resolveSignInError();
        }
    }

    private void googlePlusLogout() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            updateProfile(false);
        }
    }

    // download Google Account profile image, to complete profile
    private class LoadProfileImage extends AsyncTask {
        ImageView downloadedImage;

        public LoadProfileImage(ImageView image) {
            this.downloadedImage = image;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap icon = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                icon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return icon;
        }

        protected void onPostExecute(Bitmap result) {
            downloadedImage.setImageBitmap(result);
        }

        @Override
        protected Object doInBackground(Object[] params) {
            return null;
        }
    }
}