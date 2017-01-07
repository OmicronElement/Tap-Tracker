package com.bwisni.pub1521;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.thebluealliance.spectrum.SpectrumDialog;

import java.util.ArrayList;


/**
 * Created by Bryan on 4/14/2016.
 * ListViewAdapter for Drinker class
 */
//TODO: https://developer.android.com/guide/topics/ui/menus.html#FloatingContextMenu
public class DrinkerAdapter extends ArrayAdapter<Drinker> {
    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView credits;
        ImageView icon;
    }

    DrinkerAdapter(Context context, ArrayList<Drinker> drinkers) {
        super(context, 0, drinkers);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        Drinker drinker = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_drinker, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.drinkerName);
            viewHolder.credits = (TextView) convertView.findViewById(R.id.drinkerCredits);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.list_user_icon);

            convertView.setTag(viewHolder);


        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        assert drinker != null;
        viewHolder.name.setText(drinker.getName());
        viewHolder.credits.setText(String.valueOf(drinker.getCredits()));

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .bold()
                .endConfig()
                .buildRound(drinker.getShortName(), drinker.getColor());

        viewHolder.icon.setImageDrawable(drawable);

        // Return the completed view to render on screen
        return convertView;
    }
}