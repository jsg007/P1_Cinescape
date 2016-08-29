package com.example.giniwale.cinescape;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

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
 * Created by GINIWALE on 2/18/2016.
 */
public class GridFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private GridView mGridView;
    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private Context mContext;

    public GridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_gridfragment, menu);
        MenuItem spinnerItem = menu.findItem(R.id.spinner);
        //View view = spinnerItem.getActionView();

        Spinner spinner = (Spinner) MenuItemCompat.getActionView(spinnerItem);


        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sort_options, android.R.layout.simple_spinner_item);
        // specify the layout to use when the list of choices appear
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(pos).toString();
        Resources res = getResources();
        String[] options = res.getStringArray(R.array.sort_options);

        if (item.equals(options[0])){
            FetchMovieTask movieTask=new FetchMovieTask(mContext);
            //movieTask.execute("popularity.desc");
            movieTask.execute("movie/popular");
        }
        else if (item.equals(options[1])){
            FetchMovieTask movieTask=new FetchMovieTask(mContext);
            //movieTask.execute("vote_average.desc");
            movieTask.execute("movie/top_rated");
        }
        else{
            Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
        }
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchMovieTask movieTask=new FetchMovieTask(mContext);
            //movieTask.execute("popularity.desc");
            movieTask.execute("movie/popular");
            return true;
        }
        else if (id == R.id.action_settings){

            }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mContext=rootView.getContext();

        mGridView = (GridView) rootView.findViewById(R.id.gridview);
        //mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(rootView.getContext(), R.layout.image_main, mGridData);
        mGridView.setAdapter(mGridAdapter);
        //Start download
       // new FetchMovieTask(rootView.getContext()).execute("popularity.desc");
        //mProgressBar.setVisibility(View.VISIBLE);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridItem movieItem=mGridAdapter.getItem(position);
                Bundle extras = new Bundle();
                extras.putString(mContext.getString(R.string.EXTRA_moviePoster),movieItem.getImage());
                extras.putString(mContext.getString(R.string.EXTRA_movieTitle),movieItem.getTitle());
                extras.putString(mContext.getString(R.string.EXTRA_movieBackdrop),movieItem.getBackdrop());
                extras.putString(mContext.getString(R.string.EXTRA_movieOverview),movieItem.getOverview());
                extras.putString(mContext.getString(R.string.EXTRA_movieReleaseDate),mContext.getString(R.string.ReleasedOn)+movieItem.getReleaseDate());
                extras.putString(mContext.getString(R.string.EXTRA_movieRating),mContext.getString(R.string.Rating)+movieItem.getVoteAverage());
                //Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                Intent detailIntent = new Intent(getActivity(), MovieDetailActivity.class);
                detailIntent.putExtras(extras);
                //downloadIntent.setData(Uri.parse(fileUrl));
                startActivity(detailIntent);
            }
        });
/*
        GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(mImageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(getActivity(), "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });
*/
        return rootView;
    }


    public class FetchMovieTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        private Context mContext;

        public FetchMovieTask(Context context){
            mContext=context;
        }

        private String[] getMovieDataFromJson(String movieJsonStr) throws JSONException{
            // These are the names of the JSON objects that need to be extracted.
            final String OMD_RESULTS = "results";
            final String OMD_POSTER_PATH = "poster_path";
            final String OMD_MOVIE_TITLE = "original_title";
            final String OMD_MOVIE_OVERVIEW = "overview";
            final String OMD_MOVIE_RELEASE_DATE = "release_date";
            final String OMD_MOVIE_VOTE_AVERAGE = "vote_average";
            final String OMD_BACKDROP = "backdrop_path";
            final int numMovies=20;

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OMD_RESULTS);


            String[] resultStrs = new String[numMovies];
            //String[] titleStrs = new String[numMovies];
            GridItem item;

            for(int i = 0; i < movieArray.length(); i++){
                String posterUrl;
                String movieTitle;
                String backdropUrl;

                // Get the JSON object representing the day
                JSONObject movieData = movieArray.getJSONObject(i);
                posterUrl = movieData.getString(OMD_POSTER_PATH);
                backdropUrl = movieData.getString(OMD_BACKDROP);

                item = new GridItem();
                if (Connectivity.isConnectedFast(mContext)) {
                    item.setImage("http://image.tmdb.org/t/p/" + "w342" + posterUrl);
                    item.setBackdrop("http://image.tmdb.org/t/p/" + "w342" + backdropUrl);
                }
                else{
                    item.setImage("http://image.tmdb.org/t/p/" + "w185" + posterUrl);
                    item.setBackdrop("http://image.tmdb.org/t/p/" + "w185" + backdropUrl);
                }
                mGridData.add(item);
                Log.v(LOG_TAG, "posterURL== " + posterUrl + " ");


                movieTitle = movieData.getString(OMD_MOVIE_TITLE);
                item.setTitle(movieTitle);

                item.setOverview(movieData.getString(OMD_MOVIE_OVERVIEW));
                item.setReleaseDate(movieData.getString(OMD_MOVIE_RELEASE_DATE));
                item.setVoteAverage(movieData.getString(OMD_MOVIE_VOTE_AVERAGE));

                resultStrs[i] = posterUrl;
                //titleStrs[i] = movieTitle;
            }

            return resultStrs;
        }


        @Override
        protected String[] doInBackground(String... params) {
            // If there is no zip code, there's nothing to look up.
            if(params.length==0){
                return null;
            }
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                // Construct the URL for the OpenMovieDB query
                // Possible parameters are available at OMD's forecast API page, at
                // http://api.themoviedb.org
                final String MOVIE_BASE_URL="http://api.themoviedb.org/3";
                //final String SORT_PARAM="sort_by";
                final String APPID_PARAM="api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendEncodedPath(params[0])
                        //.appendQueryParameter(SORT_PARAM, params[0])
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_MOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenMovieDb, and open the connection
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
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();


            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
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

            try{
                return getMovieDataFromJson(movieJsonStr);
            }catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null){
                /*mImageAdapter.clear();
                for ( String moviePosterStr : result){
                    mImageAdapter.add(moviePosterStr);
                }
                */
                mGridAdapter.setGridData(mGridData);
            }
            else {
                Toast.makeText(getActivity(), "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute(){
            mGridData.clear();
        }
    }
}
