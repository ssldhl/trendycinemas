package com.trendycinemas;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.trendycinemas.model.Movie;

import java.util.List;

public class PosterAdapter<T> extends ArrayAdapter<T>{
    private Context mContext;
    private LayoutInflater mInflater;
    private int mResource;
    private int mFieldId = 0;

    public PosterAdapter(Context context, int resource, int imageViewResourceId, List<T> objects) {
        super(context, resource, imageViewResourceId, objects);
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResource = resource;
        mFieldId = imageViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent,
                                        int resource) {
        View view;
        ImageView image;

        if (convertView == null) {
            view = mInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is an ImageView
                image = (ImageView) view;
            } else {
                //  Otherwise, find the ImageView field within the layout
                image = (ImageView) view.findViewById(mFieldId);
            }
        } catch (ClassCastException e) {
            Log.e("PosterAdapter", "You must supply a resource ID for a ImageView");
            throw new IllegalStateException(
                    "PosterAdapter requires the resource ID to be a ImageView", e);
        }

        T item = getItem(position);

        if (item instanceof Movie) {
            Movie movieItem = (Movie) item;
            Uri posterUri = movieItem.buildPosterUri();
            Picasso.with(mContext).load(posterUri).into(image);
        }

        return view;
    }
}
