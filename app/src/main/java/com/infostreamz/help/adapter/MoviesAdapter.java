package com.infostreamz.help.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.infostreamz.help.DetailActivity;
import com.infostreamz.help.R;
import com.infostreamz.help.model.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private boolean isLoadingAdded = false;

    private Context mContext;
    private List<Movie> movieList;
    private ArrayList<Movie> movieFilterd;

    public MoviesAdapter(Context mContext, List<Movie> movieList){

        this.mContext = mContext;
        this.movieList = movieList;
        movieFilterd=new ArrayList<>();
        this.movieFilterd.addAll(movieList);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                viewHolder = getViewHolder(parent, inflater);
                break;
            case LOADING:
                View v2 = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(v2);
                break;
        }
        return viewHolder;

    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        RecyclerView.ViewHolder viewHolder;
        View v1 = inflater.inflate(R.layout.movie_card, parent, false);
        viewHolder = new MoviesAdapter.MyViewHolder(v1);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int i){
        switch (getItemViewType(i)) {
            case ITEM:
                final MoviesAdapter.MyViewHolder viewHolder1 = (MoviesAdapter.MyViewHolder)viewHolder;
                viewHolder1.title.setText(movieList.get(i).getOriginalTitle());
                String vote = Double.toString(movieList.get(i).getVoteAverage());
                viewHolder1.userrating.setText(vote);

                String poster = "https://image.tmdb.org/t/p/w500" + movieList.get(i).getPosterPath();

                Glide.with(mContext)
                        .load(poster)
                        .placeholder(R.drawable.load)
                        .into(viewHolder1.thumbnail);

                break;
            case LOADING:
                //notrhind
                break;

        }

    }

    public void setMovies(List<Movie> movie) {
        movieList = movie;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount(){
        return movieList==null ? 0 : movieList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == movieList.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    public void filter(String text) {
        movieFilterd.clear();
        movieFilterd.addAll(movieList);
        text = text.toLowerCase(Locale.getDefault());
        if (text.length() == 0) {

        } else {
            movieList.clear();
            for (Movie mv: movieFilterd) {
                if(mv.getTitle()==null){}
                else if (mv.getTitle().toLowerCase(Locale.getDefault()).contains(text)){
                    movieList.add(mv);
                }
            }
        }
        notifyDataSetChanged();
    }

    //add revove methods
    public void add(Movie r) {
        movieList.add(r);
        notifyItemInserted(movieList.size() - 1);
    }

    public void addAll(List<Movie> moveResults) {
        for (Movie result : moveResults) {
            add(result);
        }
    }


    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public void remove(Movie r) {
        int position = movieList.indexOf(r);
        if (position > -1) {
            movieList.remove(position);
            notifyItemRemoved(position);
        }
    }



    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Movie());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        if(movieList.size()==0){
            return;
        }
        else {
            int position = movieList.size() - 1;
            Movie result = getItem(position);

            if (result != null) {
                movieList.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    public Movie getItem(int position) {
        return movieList.get(position);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView title, userrating;
        public ImageView thumbnail;

        public MyViewHolder(View view){
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            userrating = (TextView) view.findViewById(R.id.userrating);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);

            view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        Movie clickedDataItem = movieList.get(pos);
                        Intent intent = new Intent(mContext, DetailActivity.class);
                        intent.putExtra("movies", clickedDataItem );
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        Toast.makeText(v.getContext(), "You clicked " + clickedDataItem.getOriginalTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    protected class LoadingVH extends RecyclerView.ViewHolder {

        public LoadingVH(View itemView) {
            super(itemView);
        }
    }


}
