package com.trendycinemas;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.trendycinemas.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class PosterFragment extends Fragment {

    PosterAdapter<Movie> mPosterAdapter;
    TextView mLoading;
    boolean mIsLoading = false;
    int mPagesLoaded = 1;
    String mAPIKey;

    public PosterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.posterfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            resetView();
            updatePoster();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        mPosterAdapter =
                new PosterAdapter<>(
                        getActivity(),
                        R.layout.grid_item_poster,
                        R.id.grid_item_poster_imageview,
                        new ArrayList<Movie>()
                );

        mLoading = (TextView) rootView.findViewById(R.id.loading);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_poster);
        gridView.setAdapter(mPosterAdapter);

        gridView.setOnScrollListener(
                new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {

                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        int lastInScreen = firstVisibleItem + visibleItemCount;
                        if (lastInScreen == totalItemCount) {
                            updatePoster();
                        }
                    }
                }
        );

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mAPIKey = getString(R.string.theMovieDBAPI);

        updatePoster();
    }

    public void updatePoster(){
        if(mIsLoading){
            return;
        }

        mIsLoading = true;

        if (mLoading != null) {
            mLoading.setVisibility(View.VISIBLE);
        }

        FetchPosterTask posterTask = new FetchPosterTask();
        posterTask.execute("popularity");
    }

    public void stopLoading() {
        if (!mIsLoading) {
            return;
        }

        mIsLoading = false;

        if (mLoading != null) {
            mLoading.setVisibility(View.GONE);
        }
    }

    public void resetView(){
        mPosterAdapter.clear();
        mPagesLoaded = 1;
    }

    public class FetchPosterTask extends AsyncTask<String, Void, ArrayList<Movie>>{
        private final String LOG_TAG = FetchPosterTask.class.getSimpleName();

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String posterJsonStr = null;
            String api_key = mAPIKey;
            String sort_by = params[0]+".desc";
            int page = mPagesLoaded;

            try {
                // Construct the URL for the TheMovieDB query
                // Possible parameters are available at TMDB's configuration API page, at
                // https://www.themoviedb.org/documentation/api

                final String API_BASE_URL ="http://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY = "sort_by";
                final String API_KEY = "api_key";
                final String PAGE_KEY = "page";
                Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY, sort_by)
                        .appendQueryParameter(PAGE_KEY, String.valueOf(page))
                        .appendQueryParameter(API_KEY, api_key)
                        .build();
                URL url = new URL(builtUri.toString());

                Log.d(LOG_TAG, url.toString());

                // Create the request to TheMovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                posterJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(posterJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        private ArrayList<Movie> getMovieDataFromJson(String posterJsonStr) throws JSONException{
            final String KEY_MOVIES = "results";

            JSONObject posterJson  = new JSONObject(posterJsonStr);
            JSONArray movies = posterJson.getJSONArray(KEY_MOVIES);
            ArrayList<Movie> result = new ArrayList<>();

            for (int i = 0; i < movies.length(); i++) {
                result.add(Movie.fromJson(movies.getJSONObject(i)));
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> result) {
            if(result != null){
                mPagesLoaded++;
                stopLoading();
                for(Movie movie : result){
                    mPosterAdapter.add(movie);
                }
                //new data is back from server. Hooray!
            }else{
                Toast.makeText(
                        getActivity(),
                        getString(R.string.msg_server_error),
                        Toast.LENGTH_SHORT
                ).show();

                stopLoading();
            }
        }
    }
}
