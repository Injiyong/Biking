package cau.injiyong.biking;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import cau.injiyong.biking.Common.Common;
import cau.injiyong.biking.Model.WeatherResult;
import cau.injiyong.biking.Retrofit.IOpenWeatherMap;
import cau.injiyong.biking.Retrofit.RetrofitClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class TodayWeatherFragment extends Fragment {

    ImageView img_weather;
    TextView txt_city_name, txt_humidity, txt_sunrise, txt_sunset, txt_pressure, txt_temperature, txt_description, txt_date_time, txt_geo_coord, txt_wind;
    LinearLayout weather_panel;
    ProgressBar loading;

    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;


    static TodayWeatherFragment instance;

    public static TodayWeatherFragment getInstance() {
        if (instance == null)
            instance = new TodayWeatherFragment();
        return instance;
    }

    public TodayWeatherFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_today_weather, container, false);

        // ImageView
        img_weather = itemView.findViewById(R.id.img_weather);

        // TextView's
        txt_city_name = itemView.findViewById(R.id.txt_city_name);
        txt_humidity = itemView.findViewById(R.id.txt_humidity);
        txt_sunrise = itemView.findViewById(R.id.txt_sunrise);
        txt_sunset = itemView.findViewById(R.id.txt_sunset);
        txt_pressure = itemView.findViewById(R.id.txt_pressure);
        txt_temperature = itemView.findViewById(R.id.txt_temperature);
        txt_date_time = itemView.findViewById(R.id.txt_date_time);
        txt_geo_coord = itemView.findViewById(R.id.txt_geo_coord);
        txt_wind = itemView.findViewById(R.id.txt_wind);

        // ProgressBar
        loading = itemView.findViewById(R.id.loading);
        // LinearLayout
        weather_panel = itemView.findViewById(R.id.weather_panel);

        getWeatherInformation();

        return itemView;
    }

    private void getWeatherInformation() {

        compositeDisposable.add(mService.getWeatherByLatLng(
                String.valueOf(Common.current_location.getLatitude()),
                String.valueOf(Common.current_location.getLongitude()),
                Common.APP_ID,
                "metric"
                ).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<WeatherResult>() {
                            @Override
                            public void accept(WeatherResult weatherResult) throws Exception {

                                // Load Image
                                Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                                        .append(weatherResult.getWeather().get(0).getIcon())
                                        .append(".png").toString()
                                ).into(img_weather);
                                // Load Information
                                txt_wind.setText(new StringBuilder(String.valueOf(weatherResult.getWind().getSpeed())).append(" km/s ").append(String.valueOf(weatherResult.getWind().getDeg())).append("°"));
                                txt_city_name.setText(weatherResult.getName());
                                txt_temperature.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getTemp())).append("°C").toString());
                                txt_date_time.setText(Common.convertUnixToDate(weatherResult.getDt()));
                                txt_pressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                                txt_humidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append(" %").toString());
                                txt_sunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
                                txt_sunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
                                txt_geo_coord.setText(new StringBuilder("[").append(weatherResult.getCoord().getLat()).append(" , ").append(weatherResult.getCoord().getLon()).append("]").toString());

                                weather_panel.setVisibility(View.VISIBLE);
                                loading.setVisibility(View.GONE);


                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Toast.makeText(getActivity(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("API Connect Error", throwable.getMessage());
                            }
                        })


        );

    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}