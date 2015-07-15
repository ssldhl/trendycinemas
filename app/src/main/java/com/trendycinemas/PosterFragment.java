package com.trendycinemas;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.json.JSONException;

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

    ArrayAdapter<String> mPosterAdapter;

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
                new ArrayAdapter<String>(
                        getActivity(),
                        R.layout.grid_item_poster,
                        R.id.grid_item_poster_imageview,
                        new ArrayList<String>()
                );
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_poster);
        gridView.setAdapter(mPosterAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updatePoster();
    }

    public void updatePoster(){
        FetchPosterTask posterTask = new FetchPosterTask();
        posterTask.execute("popularity");
    }

    public class FetchPosterTask extends AsyncTask<String, Void, String[]>{
        private final String LOG_TAG = FetchPosterTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
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
            String api_key="";
            String sort_by = params[0]+".desc";
            GetMetaData metaData = new GetMetaData();
            try{
                api_key = metaData.getMetaDataFromManifest(getActivity(), "theMovieDBAPI");
            }catch(PackageManager.NameNotFoundException e){
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the api key, there's no point in attempting
                // to make the request.
                return null;
            }

            String[] weatherData;

            try {
                // Construct the URL for the TheMovieDB query
                // Possible parameters are available at TMDB's configuration API page, at
                // https://www.themoviedb.org/documentation/api

                final String POSTER_BASE_URL ="http://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY = "sort_by";
                final String API_KEY = "api_key";
                Uri builtUri = Uri.parse(POSTER_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY, sort_by)
                        .appendQueryParameter(API_KEY, api_key)
                        .build();
                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, url.toString());

                // Create the request to TheMovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
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
                    buffer.append(line +"\n");
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

            Log.v(LOG_TAG, posterJsonStr);
            /*try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }*/

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }
    }

    public final class GetMetaData {

        private GetMetaData() {
//            throw new AssertionError();
        }

        /**
         * Returns the metadata value with the metadata name
         *
         * @param context
         * @param metadataName
         * @return value of the metadata
         * @throws PackageManager.NameNotFoundException when the MetaData Name is not in the Manifest
         */
        protected String getMetaDataFromManifest(Context context, String metadataName) throws PackageManager.NameNotFoundException {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);

            Bundle bundle = appInfo.metaData;
            return bundle.getString(metadataName);
        }
    }

}
