package com.genius.petr.brnomapbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr on 1. 4. 2017.
 */

public class PlaceAutocompleteAdapter extends ArrayAdapter<Place> {
    Context context;
    List<Place> items, tempItems, suggestions;

    public PlaceAutocompleteAdapter(Context context, int rowResource, List<Place> items) {
        super(context, rowResource, items);
        this.context = context;
        this.items = items;
        tempItems = new ArrayList<Place>(items); // this makes the difference.
        suggestions = new ArrayList<Place>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.places_autocomplete_item, parent, false);
        }
        Place place = items.get(position);
        if (place != null) {
            TextView name = (TextView) view.findViewById(R.id.name);
            if (name != null)
                name.setText(place.getName());
        }
        return view;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object result) {
            String str = ((Place) result).getName();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (Place p : tempItems) {
                    if (p.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(p);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<Place> filterList = (ArrayList<Place>) results.values;
            if (results != null && results.count > 0) {
                clear();
                for (Place p : filterList) {
                    add(p);
                    notifyDataSetChanged();
                }
            }
        }
    };
}
