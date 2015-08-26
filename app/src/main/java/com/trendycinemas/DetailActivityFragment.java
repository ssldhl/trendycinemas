package com.trendycinemas;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.trendycinemas.model.Movie;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    Movie movie;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        if(intent != null && intent.hasExtra(Movie.EXTRA_TEXT)){
            movie = new Movie(intent.getBundleExtra(Movie.EXTRA_TEXT));
            ((TextView) rootView.findViewById(R.id.movie_title))
                    .setText(movie.title);
            ((TextView) rootView.findViewById(R.id.movie_release_date))
                    .setText(movie.release_date);
            ((TextView) rootView.findViewById(R.id.movie_user_rating))
                    .setText(movie.getRating());
            ((TextView) rootView.findViewById(R.id.movie_synopsis))
                    .setText(movie.overview);

            ImageView movie_image = (ImageView) rootView.findViewById(R.id.movie_image);
            Uri posterUri = movie.buildPosterUri();
            Picasso.with(getActivity()).load(posterUri).into(movie_image);
        }
        return rootView;
    }
}
