package cau.injiyong.biking;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import cau.injiyong.biking.Adapter.FindPathAdapter;
import cau.injiyong.biking.autosearch.Poi;
import cau.injiyong.biking.autosearch.TMapSearchInfo;

public class AutoCompleteParse extends AsyncTask<String, Void, ArrayList<SearchEntity>>
{
    private final String TMAP_API_KEY = "l7xx3ce387d7e7764c70ba53c4cddb6391eb";
    private final int SEARCH_COUNT = 20;  // minimum is 20
    private ArrayList<SearchEntity> mListData;
    private FindPathAdapter mAdapter;

    public AutoCompleteParse(FindPathAdapter adapter) {
        this.mAdapter = adapter;
        mListData = new ArrayList<SearchEntity>();
    }

    @Override
    protected ArrayList<SearchEntity> doInBackground(String... word) {
        return getAutoComplete(word[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<SearchEntity> autoCompleteItems) {
        mAdapter.setData(autoCompleteItems);
        mAdapter.notifyDataSetChanged();
    }

    public ArrayList<SearchEntity> getAutoComplete(String word){


        try{
            String encodeWord = URLEncoder.encode(word, "UTF-8");
            URL acUrl = new URL(
                    "https://apis.openapi.sk.com/tmap/pois?version=1&page=&"+
                            "count="+SEARCH_COUNT+"&searchKeyword="+encodeWord+
                            "&areaLLCode=&areaLMCode=&resCoordType=WGS84GEO&searchType=&searchtypCd=&radius=&reqCoordType=&centerLon=&centerLat=&multiPoint="
                      //      "&callback=&appKey=l7xx3ce387d7e7764c70ba53c4cddb6391eb"
//                    "https://apis.openapi.sk.com/tmap/pois?areaLMCode=&centerLon=&centerLat=&" +
//                            "count=" + SEARCH_COUNT + "&page=&reqCoordType=&" + "" +
//                            "searchKeyword=" + encodeWord + "&callback=&areaLLCode=&multiPoint=&searchtypCd=&radius=&searchType=&resCoordType=WGS84GEO&version=1"
            );

            HttpURLConnection acConn = (HttpURLConnection)acUrl.openConnection();
            acConn.setRequestProperty("Accept", "application/json");
            acConn.setRequestProperty("appKey", TMAP_API_KEY);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    acConn.getInputStream()));

            String line = reader.readLine();
            if(line == null){
                mListData.clear();
                return mListData;
            }

            reader.close();

            mListData.clear();

            TMapSearchInfo searchPoiInfo = new Gson().fromJson(line, TMapSearchInfo.class);

            ArrayList<Poi> poi =  searchPoiInfo.getSearchPoiInfo().getPois().getPoi();
            for(int i =0; i < poi.size(); i++){
                String fullAddr = poi.get(i).getUpperAddrName() + " " + poi.get(i).getMiddleAddrName() +
                        " " + poi.get(i).getLowerAddrName() + " " + poi.get(i).getDetailAddrName();

                mListData.add(new SearchEntity(poi.get(i).getName(), fullAddr));
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return mListData;
    }
}