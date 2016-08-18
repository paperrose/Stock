package com.artfonapps.clientrestore.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.artfonapps.clientrestore.db.AlertPointItem;
import com.artfonapps.clientrestore.MainActivity;
import com.artfonapps.clientrestore.R;

import java.util.ArrayList;

/**
 * Created by paperrose on 01.04.2016.
 */
public class AlertPointAdapter extends ArrayAdapter<AlertPointItem> {

    MainActivity context;
    private ArrayList<AlertPointItem> items;

    public AlertPointAdapter(Context context, int resource, ArrayList<AlertPointItem> objects) {
        super(context, resource, objects);
        this.context = (MainActivity)context;
        this.items = objects;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void refresh(ArrayList<AlertPointItem> items)
    {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public AlertPointItem getItem(int i) {
        return items.get(i);
    }


    public int getItemIndex(AlertPointItem item) {
        return items.indexOf(item);
    }


    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = context.getLayoutInflater();
        if (view == null) {
            view = inflater.inflate(R.layout.alert_point_item, viewGroup, false);
        }
        AlertPointItem p = items.get(i);
        ((TextView) view.findViewById(R.id.address)).setText(p.getAddress());
        ((TextView) view.findViewById(R.id.name)).setText(p.getPoint());
        ((TextView) view.findViewById(R.id.plan)).setText(p.getFormatPlanDatetime());
        return view;
    }
}