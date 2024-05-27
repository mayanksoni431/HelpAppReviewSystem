package com.infostreamz.help;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.infostreamz.help.ViewModel.MainViewModel;
import com.infostreamz.help.adapter.MoviesAdapter;
import com.infostreamz.help.api.Client;
import com.infostreamz.help.api.Service;
import com.infostreamz.help.database.FavoriteEntry;
import com.infostreamz.help.model.Movie;
import com.infostreamz.help.model.MoviesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ToobarFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // limiting to 5
    private int TOTAL_PAGES = 5;
    private int currentPage = PAGE_START;

    private static final String TAG = "ToolbarFragment";
    private static final int STANDARD_APPBAR = 0;
    private static final int SEARCH_APPBAR = 1;
    private int mAppBarState;
    private RecyclerView recyclerView;

    EditText mSearchContacts;
    private static String LIST_STATE = "list_state";
    private Parcelable savedRecyclerLayoutState;
    private static final String BUNDLE_RECYCLER_LAYOUT = "recycler_layout";

    private Toolbar viewContactsBar, searchBar;
    private MoviesAdapter adapter;
    private ArrayList<Movie> moviesInstance = new ArrayList<>();
    private Context context;
    private boolean flag=false;
    private ImageView searchIconbtn,ivBackArrow,settingsiconbtn;
    private Button gobtn;

    private TextView toolbartitle;

    //refresh
    private SwipeRefreshLayout swipeLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_toobar, container, false);
        viewContactsBar = view.findViewById(R.id.viewContactsToolbar);
        searchBar = view.findViewById(R.id.searchToolbar);
        recyclerView = view.findViewById(R.id.recycler_view);
        mSearchContacts = view.findViewById(R.id.etSearchContacts);

        this.context = getActivity();
        toolbartitle=view.findViewById(R.id.toolbartitle);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);

        Log.d(TAG, "onCreateView: started");

        setAppBaeState(STANDARD_APPBAR);


        ImageView ivFlterMovies = view.findViewById(R.id.filtericon);
        ivFlterMovies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked fitered icon");
                toggleToolBarState();
            }
        });

        ivBackArrow = view.findViewById(R.id.ivBackArrow);
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked back arrow.");
                toggleToolBarState();
                flag=false;

            }
        });

        searchIconbtn = view.findViewById(R.id.searchIcon);
        searchIconbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=true;
                toggleToolBarState();
            }
        });

        settingsiconbtn = view.findViewById(R.id.settingsicon);
        settingsiconbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go on settings fragment
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new SettingsFragment(context)).addToBackStack(null)
                        .commit();
            }
        });



        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        adapter = new MoviesAdapter(context, moviesInstance);
        recyclerView.setAdapter(adapter);
        setSearchTextFilerMethod(adapter);
        GridLayoutManager gridLayoutManager;
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(context, 2);
            recyclerView.setLayoutManager(gridLayoutManager);
        } else {
            gridLayoutManager = new GridLayoutManager(context, 4);
            recyclerView.setLayoutManager(gridLayoutManager);
        }


        recyclerView.addOnScrollListener(new PaginationScrollListener(gridLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;
                // mocking network delay for API call
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextPage();
                    }
                }, 1000);

            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });


        if (savedInstanceState != null) {
            moviesInstance = savedInstanceState.getParcelableArrayList(LIST_STATE);
            savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            displayData();
        } else {
            loadJSON();
        }

        gobtn= view.findViewById(R.id.gobtnforsearch);
        gobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag)
                {
                    final String text = mSearchContacts.getText().toString().toLowerCase(Locale.getDefault());
                    searchMoviewithTitle(text);
                }
            }
        });

        return view;
    }




    private void setAppBaeState(int state) {

        Log.d(TAG, "setAppBaeState: changing app bar state to: " + state);

        mAppBarState = state;
        if (mAppBarState == STANDARD_APPBAR) {
            searchBar.setVisibility(View.GONE);
            viewContactsBar.setVisibility(View.VISIBLE);

            View view = getView();
            InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                im.hideSoftInputFromWindow(view.getWindowToken(), 0); // make keyboard hide
            } catch (NullPointerException e) {
                Log.d(TAG, "setAppBaeState: NullPointerException: " + e);
            }
        } else if (mAppBarState == SEARCH_APPBAR) {
            viewContactsBar.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);
            InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0); // make keyboard popup

        }
    }


    private void toggleToolBarState() {
        Log.d(TAG, "toggleToolBarState: toggling AppBarState.");
        if (mAppBarState == STANDARD_APPBAR) {
            setAppBaeState(SEARCH_APPBAR);
        } else {
            setAppBaeState(STANDARD_APPBAR);
        }
    }


    private void displayData(){
        adapter = new MoviesAdapter(context, moviesInstance);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        setSearchTextFilerMethod(adapter);
        recyclerView.setAdapter(adapter);
        //restoreLayoutManagerPosition();
        adapter.notifyDataSetChanged();
    }

//    private void restoreLayoutManagerPosition() {
//        if (savedRecyclerLayoutState != null) {
//            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
//        }
//    }

    private void initViews2(){
        getAllFavorite();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList(LIST_STATE, moviesInstance);
        savedInstanceState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        moviesInstance = savedInstanceState.getParcelableArrayList(LIST_STATE);
//        savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
//        super.onRestoreInstanceState(savedInstanceState);
//    }

    private void loadJSON(){
        adapter.clear();
        String sortOrder = checkSortOrder();
        if (sortOrder.equals(context.getString(R.string.pref_most_popular))) {

            try {
                Client Client = new Client();
                Service apiService = Client.getClient().create(Service.class);
                Call<MoviesResponse> call = apiService.getPopularMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN,currentPage);
                call.enqueue(new Callback<MoviesResponse>() {
                    @Override
                    public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, "+" + moviesInstance.size(), Toast.LENGTH_SHORT).show();

                            if (response.body() != null) {
                                List<Movie> movies = response.body().getResults();
                                //moviesInstance.clear();
                                moviesInstance.addAll(movies);
                                adapter.addAll(movies);
                                adapter.addLoadingFooter();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MoviesResponse> call, Throwable t) {
                        Log.d("Error", t.getMessage());
                        Toast.makeText(context, "Error Fetching Data!", Toast.LENGTH_SHORT).show();

                    }
                });

            } catch (Exception e) {
                Log.d("Error", e.getMessage());
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }else if (sortOrder.equals(getString(R.string.favorite))){
            initViews2();
        }else {

            try {
                Client Client = new Client();
                Service apiService =
                        Client.getClient().create(Service.class);
                Call<MoviesResponse> call = apiService.getTopRatedMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN,currentPage);
                call.enqueue(new Callback<MoviesResponse>() {
                    @Override
                    public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                        List<Movie> movies = response.body().getResults();
                        //moviesInstance.clear();
                        adapter.removeLoadingFooter();
                        isLoading=false;
                        adapter.addAll(movies);
                        adapter.addLoadingFooter();
                    }

                    @Override
                    public void onFailure(Call<MoviesResponse> call, Throwable t) {
                        Log.d("Error", t.getMessage());
                        Toast.makeText(context, "Error Fetching Data!", Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (Exception e) {
                Log.d("Error", e.getMessage());
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void setSearchTextFilerMethod(final MoviesAdapter adapter) {
        mSearchContacts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                final String text = mSearchContacts.getText().toString().toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private String checkSortOrder(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String sortOrder = preferences.getString(
                this.getString(R.string.pref_sort_order_key),
                this.getString(R.string.pref_most_popular)
        );

        return sortOrder;
    }

    private void getAllFavorite(){
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getFavorite().observe(this, new Observer<List<FavoriteEntry>>() {
            @Override
            public void onChanged(@Nullable List<FavoriteEntry> imageEntries) {
                List<Movie> movies = new ArrayList<>();
                for (FavoriteEntry entry : imageEntries){
                    Movie movie = new Movie();
                    movie.setId(entry.getMovieid());
                    movie.setOverview(entry.getOverview());
                    movie.setOriginalTitle(entry.getTitle());
                    movie.setPosterPath(entry.getPosterpath());
                    movie.setVoteAverage(entry.getUserrating());

                    movies.add(movie);
                }

                adapter =new MoviesAdapter(context,movies);
                recyclerView.setAdapter(adapter);

            }
        });
    }


    public void loadNextPage(){
        String sortOrder = checkSortOrder();

        if (sortOrder.equals(this.getString(R.string.pref_most_popular))) {

            try {
                Client Client = new Client();
                Service apiService =
                        Client.getClient().create(Service.class);
                Call<MoviesResponse> call = apiService.getPopularMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN,currentPage);
                call.enqueue(new Callback<MoviesResponse>() {
                    @Override
                    public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                        if (response.isSuccessful()) {Toast.makeText(context, "the value is " + moviesInstance.size(), Toast.LENGTH_SHORT).show();

                            if (response.body() != null) {
                                List<Movie> movies = response.body().getResults();
                                //moviesInstance.clear();
                                adapter.removeLoadingFooter();
                                isLoading = false;
                                adapter.addAll(movies);

                                adapter.addLoadingFooter();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MoviesResponse> call, Throwable t) {
                        Log.d("Error", t.getMessage());
                        Toast.makeText(context, "Error Fetching Data!", Toast.LENGTH_SHORT).show();

                    }
                });

            } catch (Exception e) {
                Log.d("Error", e.getMessage());
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }else if (sortOrder.equals(getString(R.string.favorite))){
            initViews2();
        }else {

            try {
                Client Client = new Client();
                Service apiService =
                        Client.getClient().create(Service.class);
                Call<MoviesResponse> call = apiService.getTopRatedMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN,currentPage);
                call.enqueue(new Callback<MoviesResponse>() {
                    @Override
                    public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                        List<Movie> movies = response.body().getResults();
                        //moviesInstance.clear();
                        adapter.removeLoadingFooter();
                        isLoading = false;
                        adapter.addAll(movies);

                        adapter.addLoadingFooter();

                    }

                    @Override
                    public void onFailure(Call<MoviesResponse> call, Throwable t) {
                        Log.d("Error", t.getMessage());
                        Toast.makeText(context, "Error Fetching Data!", Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (Exception e) {
                Log.d("Error", e.getMessage());
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }


    }

    private Call<MoviesResponse> callgetMovieDetailApi(String s) {
        Client Client = new Client();
        Service apiService =
                Client.getClient().create(Service.class);
        return apiService.getItemSearch(s);
    }

    private void searchMoviewithTitle(String input_movie) {
        callgetMovieDetailApi(input_movie).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                if(response.body()!=null) {
                    List<Movie> results = response.body().getResults();
                    adapter.clear();
                    adapter.addAll(results);
                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {
                t.printStackTrace();
                // TODO: 08/11/16 handle failure
            }
        });

    }

    @Override
    public void onRefresh() {
        String sortOrder = checkSortOrder();
        toolbartitle.setText(sortOrder);
        if(!isNetworkAvailable()){
            Toast.makeText(context,"Network Not Available.",Toast.LENGTH_SHORT);
        }
        else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadJSON();
                }
            }, 1000);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
            }
        }, 2000);

    }



}
