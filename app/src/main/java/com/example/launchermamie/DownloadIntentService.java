package com.example.launchermamie;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadIntentService extends IntentService {
    public static  File PHOTO_FOLDER;
    String SERVER_URL = "http://ovh2.phie.ovh/mamie";

    public DownloadIntentService() {
        super("DownloadIntentService");
    }

    public static void startActionDownload(Context context) {
        Intent intent = new Intent(context, DownloadIntentService.class);

        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PHOTO_FOLDER = new File(getExternalFilesDir(null),"photos");
        String PASSWORD = getString(R.string.password);
        String JSON = SERVER_URL+"/photos.php?psw="+PASSWORD;

        if (intent != null) {
            final String action = intent.getAction();



            String remote = readRemote(JSON, null);
            if(remote != null){
                try {
                    boolean hasChanged = false;
                    JSONArray photos = new JSONArray(remote);
                    List<String> photosLocal = PhotosManager.getInstance(this).getPhotosUrlsList();
                    for(int i = 0; i < photos.length(); i++){
                        JSONObject photo = photos.getJSONObject(i);
                        String url = photo.getString("url");
                        if(!photosLocal.contains(url)){
                            String name = new File(url).getName();
                            if(readRemote(SERVER_URL+"/get_photo.php?max=1024&img="+url+"&psw="+PASSWORD, new File(PHOTO_FOLDER, name).getAbsolutePath()) != null) {
                                PhotosManager.getInstance(this).addPhoto(url, name);
                                hasChanged = true;
                            }
                        }
                        else Log.d("downloaddebug", url +" already there");
                    }
                    PreferenceManager.getDefaultSharedPreferences(this).edit().putString("photos_list_json", remote).apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String readRemote(String url, String path){
    Log.d("downloaddebug","get "+url);
        Log.d("downloaddebug","to "+path);
        try {
            if(path != null) {
                new File(path).getParentFile().mkdirs();
                BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());

                FileOutputStream fileOutputStream = new FileOutputStream(path);
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                return "";
            }
            else{
                BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                String line = null;
                String total = "";
                while((line = in.readLine())!=null){
                    total+=line+"\n";
                }
                return total;
            }
        } catch (IOException e) {
            // handle exception
            e.printStackTrace();
            return null;
        }

    }

}