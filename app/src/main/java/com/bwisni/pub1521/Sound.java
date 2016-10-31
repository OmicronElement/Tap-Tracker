package com.bwisni.pub1521;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.orm.SugarRecord;

/**
 * Created by Bryan on 10/28/2016.
 */
public class Sound extends SugarRecord{
    private String uriString;
    private String title;

    public Sound(){}

    public Sound(Context context, Uri uri){
        uriString = uri.toString();
        title = generateTitle(context);
    }

    public Sound(Context context, String s){
        uriString = s;
        title = generateTitle(context);
    }

    private String generateTitle(Context context) {
        Ringtone ringtone = RingtoneManager.getRingtone(context, Uri.parse(uriString));
        String title = ringtone.getTitle(context);
        ringtone.stop();
        return title;
    }

    public Uri getUri() {
        return Uri.parse(uriString);
    }

    public String getTitle() {
        return title;
    }
}
