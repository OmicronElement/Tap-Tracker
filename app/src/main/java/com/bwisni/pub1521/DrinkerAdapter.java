package com.bwisni.pub1521;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Bryan on 4/14/2016.
 */
public class DrinkerAdapter extends ArrayAdapter<Drinker> {
    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView credits;
    }

    public DrinkerAdapter(Context context, ArrayList<Drinker> drinkers) {
        super(context, 0, drinkers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Drinker drinker = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_drinker, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.drinkerName);
            viewHolder.credits = (TextView) convertView.findViewById(R.id.drinkerCredits);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        viewHolder.name.setText(drinker.name);
        viewHolder.credits.setText(Integer.toString(drinker.credits));
        // Return the completed view to render on screen
        return convertView;
    }
}