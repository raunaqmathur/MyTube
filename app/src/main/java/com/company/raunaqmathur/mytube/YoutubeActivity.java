package com.company.raunaqmathur.mytube;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class YoutubeActivity extends AppCompatActivity {
    private YouTube youtube = null;
    private YouTube.Search.List query;
    private String mChosenAccountName = "";
    private TextView textViewData;
    private GoogleAccountCredential credential;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new GsonFactory();
    private String[] youtubeScopes = {YouTubeScopes.YOUTUBE, YouTubeScopes.YOUTUBE_UPLOAD, YouTubeScopes.YOUTUBE_READONLY, YouTubeScopes.YOUTUBEPARTNER, YouTubeScopes.YOUTUBEPARTNER_CHANNEL_AUDIT};
    private static final int REQUEST_AUTHORIZATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube);
        textViewData = (TextView) findViewById(R.id.textViewData);
        youtube = YouTubeClass.getYouTube();
        search("");


    }


    private void updateUI() {

        youtube = YouTubeClass.getYouTube();
        new AsyncTask<Void, Void, Void>() {
            @Override

            protected Void doInBackground(Void... voids) {


                try

                {
                    ChannelListResponse clr = youtube.channels()
                            .list("contentDetails").setMine(true).execute();
                } catch (UserRecoverableAuthIOException e)

                {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {//Log.e(TAG, e.getMessage());
                }
                return null;
            }
        }.execute((Void) null);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        switch (requestCode) {

            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    String accountName = data.getExtras().getString(
                            AccountManager.KEY_ACCOUNT_NAME);
                }


        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_youtube, menu);
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


    public void search(String keywords) {
        try {
            new LoadSearchData().execute("teri deewani");


        } catch (Exception e) {
            Log.d("YC", "Could not initialize: " + e);
            //((TextView) findViewById(R.id.textViewVDO)).setText("Could not initialize: " + e);
            Toast.makeText(this, "" + e, Toast.LENGTH_LONG).show();
        }

    }


    private class LoadSearchData extends AsyncTask<String, Void, List<SearchItem>> {
        TextView textViewData;
        List<SearchResult> results;
        List<SearchItem> items;
        /*public LoadSearchData( TextView textViewData) {
            this.textViewData = textViewData;
        }
*/
        @Override
        protected void onPostExecute(List<SearchItem> result) {
            super.onPostExecute(result);
            if (result.size() > 0) {
                    for (SearchItem res : result) {

                    Log.i("postres: ", res.getThumbnailURL());
                }
            } else
                textViewData.setText("no item");
        }


        @Override
        protected List<SearchItem> doInBackground(String... params) {
            try {
                query = youtube.search().list("id,snippet");
                query.setMaxResults(10L);
                query.setType("video");
                query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)");
                query.setQ(params[0]);
            } catch (IOException e) {
                Log.d("YC", "Could not initialize: " + e);
            }

            Log.i("keyword", params[0]);


            try {
                SearchListResponse response = query.execute();

                results = response.getItems();

                items = new ArrayList<SearchItem>();
                for (SearchResult result : results) {
                    SearchItem item = new SearchItem();
                    item.setTitle(result.getSnippet().getTitle());
                    item.setDescription(result.getSnippet().getDescription());
                    item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                    item.setId(result.getId().getVideoId());
                    items.add(item);

                    Log.i("Result", "" + item.getTitle() + " - " + item.getDescription());


                }
                Log.i("doinbak loop", "end");
                return items;
            }
            catch (UserRecoverableAuthIOException e)

            {
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            }
            catch (Exception e) {
                Log.i("doInBak err", e.getMessage());
                return null;
            }
            return null;
        }
    }



}
