package com.bwisni.pub1521;

import android.content.Context;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Bryan on 10/27/2016.
 * Thanks guides.codepath.com
 * Designed to be used along with default ListView item template 'simple_list_item_single_choice'
 */
public class SoundAdapter extends ArrayAdapter {
    public SoundAdapter(Context context, ArrayList<Sound> sounds) {
        super(context, 0, sounds);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
       Sound sound = ((Sound) getItem(position));
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_single_choice, parent, false);
        }

        String title = sound.getTitle();

        if(title == null){
            Ringtone ringtone = RingtoneManager.getRingtone(getContext(), sound.getUri());
            title = ringtone.getTitle(getContext());
            ringtone.stop();
        }

        // Lookup view
        TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
        // Populate the data into the template view using the data object
        tv.setText(title);
        // Return the completed view to render on screen
        return convertView;
    }
}
