package com.company.raunaqmathur.mytube;
import android.app.Activity;
import android.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import com.google.api.services.youtube.YouTubeScopes;


public class FavoriteActivity extends Fragment {



    GoogleAccountCredential credential;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_favorite, container, false);

        return rootView;

    }





}