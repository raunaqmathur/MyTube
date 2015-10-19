package com.company.raunaqmathur.mytube;

import android.content.Context;
import android.util.Log;

import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by raunaqmathur on 10/18/15.
 */
public class YoutubeConnector {
    private YouTube youtube;
    private YouTube.Search.List query;
    private YouTube.Playlists.List playlistQuery;

    private static final String Info = "YoutubeConnector";
    private static String playListId = null;

    Playlist playlist = null;

    public YoutubeConnector(Context context) {
        youtube = YouTubeClass.getYouTube();
        try{
            query = youtube.search().list("id,snippet");
            //query.setKey(KEY);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)");
            query.setMaxResults(10L);
        }catch(IOException e){
            Log.d("YC", "Could not initialize: " + e);
        }
    }

    public List<SearchItem> search(String keywords){


        query.setQ(keywords);

        List<String> videoIds = new ArrayList<String>();
        try{
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            for (SearchResult result : results) {
                videoIds.add(result.getId().getVideoId());
            }
            Log.i("doinbak loop", "end");
            return getVideoInformation(videoIds);
        }catch(IOException e){
            Log.d("YC", "Could not search: "+e);
            return null;
        }
    }



    private List<SearchItem> getVideoInformation(List<String> videoIds) {
        List<SearchItem> items = new ArrayList<SearchItem>();
        Joiner stringJoiner = Joiner.on(',');
        String videoId = stringJoiner.join(videoIds);
        YouTube.Videos.List listVideoRequest;
        try {
            listVideoRequest = youtube.videos()
                    .list("id, snippet, statistics")
                    .setFields("items(id,snippet,statistics)")
                    .setId(videoId);

            VideoListResponse listResponse = listVideoRequest.execute();
            List<Video> videos = listResponse.getItems();
            for (Video result : videos) {
                SearchItem item = new SearchItem();
                item.setTitle(result.getSnippet().getTitle());
                item.setDescription(result.getSnippet().getDescription());
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setNumberOfViews(result.getStatistics().getViewCount().toString());
                item.setPublishedDate(result.getSnippet().getPublishedAt().toString());
                item.setId(result.getId());
                items.add(item);
            }
            return items;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public String insertPlaylistItem( String videoId) {

        if (!playListId.isEmpty()) {

            ResourceId resourceId = new ResourceId();
            resourceId.setKind("youtube#video");
            resourceId.setVideoId(videoId);


            PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
            playlistItemSnippet.setTitle("First video in the  playlist");
            playlistItemSnippet.setPlaylistId(playListId);
            playlistItemSnippet.setResourceId(resourceId);


            PlaylistItem playlistItem = new PlaylistItem();
            playlistItem.setSnippet(playlistItemSnippet);

            try {

                YouTube.PlaylistItems.Insert playlistItemsInsertCommand =
                        youtube.playlistItems().insert("snippet,contentDetails", playlistItem);
                PlaylistItem returnedPlaylistItem = playlistItemsInsertCommand.execute();



                Log.i("New PlaylistItem name: ", returnedPlaylistItem.getSnippet().getTitle());
                Log.i(" - Video id: ", returnedPlaylistItem.getSnippet().getResourceId().getVideoId());
                Log.i(" - Posted: " , returnedPlaylistItem.getSnippet().getPublishedAt().toString());
                Log.i(" - Channel: " , returnedPlaylistItem.getSnippet().getChannelId());
                return returnedPlaylistItem.getId();
            } catch (IOException e) {
                Log.e(Info, e.getMessage());
                return null;
            }
        } else {
            Log.e("Play list error:" , "not created");

            return null;
        }

    }


    public String removePlaylistItem( String videoId) {

        if (!playListId.isEmpty()) {

            ResourceId resourceId = new ResourceId();
            resourceId.setKind("youtube#video");
            resourceId.setVideoId(videoId);

            try {

                YouTube.PlaylistItems.Delete deleteVideosRequest = youtube.playlistItems().delete(videoId);
                deleteVideosRequest.execute();






                return "Deleted";
            } catch (IOException e) {
                Log.e(Info, e.getMessage());
                return null;
            }
        } else {
            Log.e("Play list error:" , "not created");

            return null;
        }

    }

    public List<SearchItem> searchPlaylist(String playlistName) {
        try {



            List<Playlist> results = getAllPlaylists();
            if (results != null) {
                for (Playlist result : results) {


                    Log.i("User Playlists:", "" + result.getSnippet().getTitle());
                    if (result.getSnippet().getTitle().equals(playlistName)) {
                        playListId = result.getId();
                        playlist = result;
                        break;
                    }
                }
                if (playlist != null) {
                    //To get List of Videos of a Particular Playlist
                    PlaylistItemListResponse playlistItemListResponse = youtube.playlistItems()
                            .list("id, snippet, contentDetails")
                            .setPlaylistId(playListId)
                            .setFields("items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo")
                            .setMaxResults((long) 10)
                            .execute();
                    List<PlaylistItem> playlistItemListResponseItems = playlistItemListResponse.getItems();
                    List<String> videoIds = new ArrayList<String>();
                    for (PlaylistItem result : playlistItemListResponseItems) {
                        videoIds.add(result.getContentDetails().getVideoId());
                    }
                    return getVideoInformation(videoIds);
                }
                else
                {
                    Log.i(Info, "User had playlist but not the one req");
                    createNewPlaylist(playlistName);
                }

            }
            else
            {
                Log.i(Info, "User had no playlist" );
                createNewPlaylist(playlistName);
            }

            return null;
        } catch (IOException e) {
            Log.e(Info, "Could not search playlist: " + e);

            return null;
        }
    }



    public List<Playlist> getAllPlaylists() {
        try {
            playlistQuery = youtube.playlists()
                    .list("id, snippet")
                    .setFields("items(id, snippet)")
                    .setMine(true);

            PlaylistListResponse response = playlistQuery.execute();
            List<Playlist> results = response.getItems();
            return results;
        } catch (IOException e) {
            Log.e(Info, "Could not retrieve all playlist: " + e);
            e.printStackTrace();
            return null;
        }
    }
    private void createNewPlaylist(String playListName)
    {
        PlaylistSnippet playlistSnippet = new PlaylistSnippet();
        playlistSnippet.setTitle(playListName);
        playlistSnippet.setDescription("Playlist created for CMPE-277");
        PlaylistStatus playlistStatus = new PlaylistStatus();
        playlistStatus.setPrivacyStatus("private");

        Playlist youTubePlaylist = new Playlist();
        youTubePlaylist.setSnippet(playlistSnippet);
        youTubePlaylist.setStatus(playlistStatus);


        YouTube.Playlists.Insert playlistInsertCommand =
                null;
        try {
            playlistInsertCommand = youtube.playlists().insert("snippet,status", youTubePlaylist);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            playlist = playlistInsertCommand.execute();
             playListId = playlist.getId();
        } catch (IOException e1) {
            e1.printStackTrace();
        }


    }


}
