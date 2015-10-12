package com.company.raunaqmathur.mytube;

import android.app.LauncherActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.Scopes;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class SearchActivity extends AppCompatActivity {

    private static final String PROPERTIES_FILENAME = "youtube.properties";
    public static final String ACCOUNT_KEY = "accountName";

    private YouTube youtube;
    private YouTube.Search.List query;
    public static final String KEY= "";

    private String mChosenAccountName;

    GoogleAccountCredential credential;
    public static final String[] SCOPES = {Scopes.PROFILE, YouTubeScopes.YOUTUBE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
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

    public void searchClicked(View view)
    {


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try{
        youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {}
        }).setApplicationName(getString(R.string.app_name)).build();

            credential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Arrays.asList(SCOPES));
            // set exponential backoff policy
            credential.setBackOff(new ExponentialBackOff());



            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(this);
            mChosenAccountName = sp.getString(ACCOUNT_KEY, null);

            Toast.makeText(this, "AccName:" + mChosenAccountName, Toast.LENGTH_LONG).show();

            /*query = youtube.search().list("id,snippet");
            query.setKey(KEY);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)");
            */
            ((TextView) findViewById(R.id.textViewVDO)).setText("AccName:" + mChosenAccountName);



        }catch(Exception e){
            Log.d("YC", "Could not initialize: " + e);
           // ((TextView) findViewById(R.id.textViewVDO)).setText("Could not initialize: " + e);
            //Toast.makeText(this, "" + e, Toast.LENGTH_LONG).show();
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
            return items;
        }catch(IOException e){
            Log.d("YC", "Could not search: "+e);
            ((TextView) findViewById(R.id.textViewVDO)).setText("Could not initialize: " + e);
            Toast.makeText(this, "" + e, Toast.LENGTH_LONG).show();
            return null;
        }
    }
}
