package com.example.giniwale.cinescape;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {

    public MovieDetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        Intent intent = getActivity().getIntent();
        Bundle extras = intent.getExtras();
        if (intent != null){
            //String posterStr = intent.getStringExtra("EXTRA_MOVIE_POSTER");
            String posterStr = extras.getString(rootView.getContext().getString(R.string.EXTRA_moviePoster));
            String backdropStr = extras.getString(rootView.getContext().getString(R.string.EXTRA_movieBackdrop));
            //(ImageView)rootView.findViewById(R.id.movie_poster)).setText(posterStr);
            Picasso.with(getContext()).load(posterStr).into((ImageView)rootView.findViewById(R.id.movie_poster));
            Picasso.with(getContext()).load(backdropStr).into((ImageView)rootView.findViewById(R.id.movie_backdrop));

            final TextView movieTitleTextView = (TextView)rootView.findViewById(R.id.movie_title);
            movieTitleTextView.setText(extras.getString(rootView.getContext().getString(R.string.EXTRA_movieTitle)));

            final TextView movieOverviewTextView = (TextView)rootView.findViewById(R.id.movie_overview);
            movieOverviewTextView.setText(extras.getString(rootView.getContext().getString(R.string.EXTRA_movieOverview)));

            final TextView movieReleaseDateTextView = (TextView)rootView.findViewById(R.id.movie_release_date);
            movieReleaseDateTextView.setText(extras.getString(rootView.getContext().getString(R.string.EXTRA_movieReleaseDate)));

            final TextView movieRatingsTextView = (TextView)rootView.findViewById(R.id.movie_rating);
            movieRatingsTextView.setText(extras.getString(rootView.getContext().getString(R.string.EXTRA_movieRating)));
        }
        return rootView;
    }
}
