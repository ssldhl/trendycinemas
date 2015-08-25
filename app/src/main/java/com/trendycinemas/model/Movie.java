package com.trendycinemas.model;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

public class Movie {

    public static final String EXTRA_MOVIE = "io.maritimus.sofaexpert.EXTRA_MOVIE";
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_OVERVIEW = "overview";
    public static final String KEY_POSTER_PATH = "poster_path";
    public static final String KEY_VOTE_AVERAGE = "vote_average";
    public static final String KEY_VOTE_COUNT = "vote_count";
    public static final String KEY_RELEASE_DATE = "release_date";
    public static final String IMAGE_SIZE = "w185";

    public final long id;
    public final String title;
    public final String overview;
    public final String poster_path;
    public final double vote_average;
    public final long vote_count;
    public final String release_date;


    public Movie(long id, String title, String overview, String poster_path, double vote_average,
                 long vote_count, String release_date){
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.poster_path = poster_path;
        this.vote_average = vote_average;
        this.vote_count = vote_count;
        this.release_date = release_date;
    }

    public static Movie fromJson(JSONObject jsonObject) throws JSONException {
        return new Movie(
                jsonObject.getLong(KEY_ID),
                jsonObject.getString(KEY_TITLE),
                jsonObject.getString(KEY_OVERVIEW),
                jsonObject.getString(KEY_POSTER_PATH),
                jsonObject.getDouble(KEY_VOTE_AVERAGE),
                jsonObject.getLong(KEY_VOTE_COUNT),
                jsonObject.getString(KEY_RELEASE_DATE)
        );
    }

    public Uri buildPosterUri() {
        final String BASE_URL = "http://image.tmdb.org/t/p/";

        return Uri.parse(BASE_URL).buildUpon()
                .appendPath(IMAGE_SIZE)
                .appendEncodedPath(poster_path)
                .build();
    }
}
