package com.genius.petr.brnomapbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Petr on 11. 3. 2017.
 */

public class PlaceListAdapter extends ArrayAdapter<Place> {

    private ArrayList<Place> places;
    private Context context;

    public PlaceListAdapter(Context context, ArrayList<Place> places) {
        super(context, android.R.layout.simple_list_item_1, places);
        this.places = places;
        this.context = context;
    }

/**
 * Holder for the list items.
 */
private static class ViewHolder {
    TextView txtName;
    TextView txtType;
}

    /**
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        Place item = getItem(position);

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.places_item, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.name);
            viewHolder.txtType = (TextView) convertView.findViewById(R.id.type);

            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }


        viewHolder.txtName.setText(item.getName());
        viewHolder.txtType.setText(item.getType());
        // Return the completed view to render on screen
        return convertView;
    }
}