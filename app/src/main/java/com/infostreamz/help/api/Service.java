package com.infostreamz.help.api;

import com.infostreamz.help.BuildConfig;
import com.infostreamz.help.model.MoviesResponse;
import com.infostreamz.help.model.Review;
import com.infostreamz.help.model.TrailerResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by delaroy on 5/18/17.
 */
public interface Service {

    @GET("movie/popular")
    Call<MoviesResponse> getPopularMovies(@Query("api_key") String apiKey, @Query("page") int pageIndex);

    @GET("movie/top_rated")
    Call<MoviesResponse> getTopRatedMovies(@Query("api_key") String apiKey, @Query("page") int pageIndex);

    @GET("movie/{movie_id}/videos")
    Call<TrailerResponse> getMovieTrailer(@Path("movie_id") int id, @Query("api_key") String apiKey);

    //Reviews
    @GET("movie/{movie_id}/reviews")
    Call<Review> getReview(@Path("movie_id") int id, @Query("api_key") String apiKey);
//
//    //seach
    @GET("search/movie?api_key="+ BuildConfig.THE_MOVIE_DB_API_TOKEN)
    Call<MoviesResponse> getItemSearch(@Query("query") String movie_name);

}
