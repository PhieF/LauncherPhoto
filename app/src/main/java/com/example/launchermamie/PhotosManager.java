package com.example.launchermamie;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PhotosManager {

    private final Context mContext;
    private JSONArray photoJsonObj;
    private HashMap<String, String> mPhotos = new HashMap<>();
    private List<String> mWeighList;

    public PhotosManager(Context context) {
        mContext = context;
        String photos = PreferenceManager.getDefaultSharedPreferences(context).getString("photos", "[]");
        try {
            photoJsonObj = new JSONArray(photos);
            for(int i = 0; i< photoJsonObj.length(); i++){
                mPhotos.put(photoJsonObj.getJSONObject(i).getString("url"), photoJsonObj.getJSONObject(i).getString("path"));
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }

    }

    public static PhotosManager getInstance(Context context){
        return new PhotosManager(context);
    }

    public List<String> getPhotosUrlsList() {
        return new ArrayList<>(mPhotos.keySet());
    }

    public void addPhoto(String url, String path) {
        try {
            JSONObject photo = new JSONObject();
            photo.put("url", url);
            photo.put("path",path);
            photoJsonObj.put(photo);

        } catch (JSONException e) {
            e.printStackTrace();

        }
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("photos", photoJsonObj.toString()).apply();
        mPhotos.put(url, path);
        renewList();

    }


    public List<String> getPhotos() {
        if(mWeighList == null)
            renewList();
        return mWeighList;

    }

    public void renewList() {

        String photoListJson = PreferenceManager.getDefaultSharedPreferences(mContext).getString("photos_list_json", "[]");
        if(photoListJson.equals("[]")){
            List<String> list = new ArrayList<>(mPhotos.values());
            Collections.reverse(list);
            mWeighList = list;
            return;
        }
        List<String> list = new ArrayList<>();
        JSONArray photos = null;
        try {
            photos = new JSONArray(photoListJson);
            for(int i = photos.length()-1; i >=0; i--){
                String url = mPhotos.get(photos.getJSONObject(i).getString("url"));
                list.add(url);
                if(i> photos.length()- 20){
                    list.add(url);
                    list.add(url);
                    list.add(url);
                    list.add(url);
                    list.add(url);
                    list.add(url);
                    list.add(url);
                }

                if(i> photos.length()- 50){
                    list.add(url);
                    list.add(url);
                    list.add(url);
                    list.add(url);
                    list.add(url);
                    list.add(url);
                    list.add(url);
                }

                if(i> photos.length()- 80){
                    list.add(url);
                    list.add(url);
                    list.add(url);

                }

            }
            Collections.shuffle(list);
        } catch (JSONException e) {
            list = new ArrayList<>(mPhotos.values());
            Collections.reverse(list);

        }
        mWeighList = list;

    }
}
