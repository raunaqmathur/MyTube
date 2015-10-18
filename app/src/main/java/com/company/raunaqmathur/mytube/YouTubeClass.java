package com.company.raunaqmathur.mytube;

import android.content.Context;
import android.os.StrictMode;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.Arrays;

/**
 * Created by raunaqmathur on 10/18/15.
 */
public class YouTubeClass {

    private GoogleAccountCredential credential;
    private static YouTube youtube = null;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new GsonFactory();

    private String[] youtubeScopes = {YouTubeScopes.YOUTUBE, YouTubeScopes.YOUTUBE_UPLOAD, YouTubeScopes.YOUTUBE_READONLY, YouTubeScopes.YOUTUBEPARTNER, YouTubeScopes.YOUTUBEPARTNER_CHANNEL_AUDIT};

    public YouTubeClass(String mName, Context appContext){


            if(youtube == null) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                credential = GoogleAccountCredential.usingOAuth2(appContext, Arrays.asList(youtubeScopes));
                // set exponential backoff policy
                credential.setBackOff(new ExponentialBackOff());
                credential.setSelectedAccountName(mName);
                youtube = new YouTube.Builder(transport, jsonFactory,
                        credential).setApplicationName("MyTube")
                        .build();
            }

        }

    public static YouTube getYouTube()
    {
        return youtube;
    }

}
