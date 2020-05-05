package cau.injiyong.biking.Retrofit;


import cau.injiyong.biking.Model.FiveDays.WeatherForecastResult;
import cau.injiyong.biking.Model.WeatherResult;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * API ' den talep edebileceğimiz Yöntemler burada tanımlanacak
 */
public interface IOpenWeatherMap {

    /*
     weather endpoint'ine aşağıda yer alan query ' leri göndererek json result alacağız.
     */
    @GET("weather")
    Observable<WeatherResult> getWeatherByLatLng(@Query("lat") String lat,
                                                 @Query("lon") String lng,
                                                 @Query("appid") String appid,
                                                 @Query("units") String unit);

    @GET("forecast")
    Observable<WeatherForecastResult> getForecastWeatherByLatLng(@Query("lat") String lat,
                                                                 @Query("lon") String lng,
                                                                 @Query("appid") String appid,
                                                                 @Query("units") String unit);


    @GET("weather")
    Observable<WeatherResult> getWeatherByCityName(@Query("q") String cityName,
                                                   @Query("appid") String appid,
                                                   @Query("units") String unit);


}
