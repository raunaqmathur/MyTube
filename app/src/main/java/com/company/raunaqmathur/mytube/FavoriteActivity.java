package com.company.raunaqmathur.mytube;

import android.accounts.AccountManager;

import android.app.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.support.v4.view.ViewPager;


import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;



public class FavoriteActivity extends AppCompatActivity{
    private YouTube youtube = null;
    private YouTube.Search.List query;


    private static final int REQUEST_AUTHORIZATION = 1;






    ///////////////////




    private ListView videosFound;



    private Handler handler;

    private List<SearchItem> searchResults;
    private List<String> favVideoIds  = new ArrayList<String>();

    private String videoID;

    private String playListName = "CMPESJSU277";

    public FavoriteActivity() {
    }

    /////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);




        youtube = YouTubeClass.getYouTube();

        ////////get Data of Playlist

        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(FavoriteActivity.this);
                searchResults = yc.searchPlaylist(playListName);
                handler.post(new Runnable(){
                    public void run(){
                        getAllFavIds("Add", "");
                        updateVideosFound();
                    }
                });
            }
        }.start();




        videosFound = (ListView)findViewById(R.id.favvideos_found);


        handler = new Handler();


    }


    private void searchOnYoutube(final String keywords){
        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(FavoriteActivity.this);
                searchResults = yc.search(keywords);
                handler.post(new Runnable(){
                    public void run(){
                        updateVideosFound();
                    }
                });
            }
        }.start();
    }


    private void updateVideosFound(){
        ArrayAdapter<SearchItem> adapter = new ArrayAdapter<SearchItem>(getApplicationContext(), R.layout.video_result, searchResults){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.video_result, parent, false);
                }
                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView description = (TextView)convertView.findViewById(R.id.video_description);

                ImageView playListPlay = (ImageView)convertView.findViewById(R.id.video_play);
                SearchItem searchResult = searchResults.get(position);
                videoID = searchResult.getId();
                Picasso.with(getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                description.setText(searchResult.getDescription());
                ((ImageView)convertView.findViewById(R.id.video_addPlayList)).setVisibility(View.INVISIBLE);
                ((ImageView)convertView.findViewById(R.id.video_removePlayList)).setVisibility(View.INVISIBLE);

                playListPlay.setClickable(true);
                playListPlay.setOnClickListener(new OnClickListener() {


                    @Override
                    public void onClick(View v ) {

                        Log.i("Going to play VideoID:","" + videoID);
                        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                        intent.putExtra("VIDEO_ID", videoID);
                        startActivity(intent);


                    }
                });
                return convertView;
            }
        };

        videosFound.setAdapter(adapter);


        //addClickListener();
    }

    private void getAllFavIds(String action, String video)
    {
        if(action.equals("Add"))
            for(SearchItem fav : searchResults)
            {

                if(!favVideoIds.contains(fav.getId()))
                    favVideoIds.add(fav.getId());

            }

        else
        {
            if(favVideoIds.contains(video))
                favVideoIds.remove(video);
        }

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
        getMenuInflater().inflate(R.menu.menu_favorite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.putExtra("signOut", "1");
            startActivity(intent);
            finish();
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
