package com.artfonapps.clientrestore.views.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.models.Order;
import com.artfonapps.clientrestore.models.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 15.08.2016.
 */
public class MainPagerAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;
    List<Integer> ids;
    public ViewPager hvp;

    ListsPagerAdapter listsPagerAdapter;

    public MainPagerAdapter(Context context, List<Integer> ids, List<Point> points, List<Order> orders) {
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.ids = ids;
        List<Integer> ids2 = new ArrayList<>();
        ids2.add(R.layout.point_item);
        ids2.add(R.layout.order_item);
        listsPagerAdapter = new ListsPagerAdapter(context, ids2, points, orders);
    }

    public MainPagerAdapter setPoints(List<Point> points) {
        listsPagerAdapter.setPoints(points);
        return this;
    }

    public MainPagerAdapter setOrders(List<Order> orders) {
        listsPagerAdapter.setOrders(orders);
        return this;
    }


    @Override
    public int getCount() {
        return ids.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(ids.get(position), container, false);
        if (ids.get(position) == R.layout.view_pager_inside_test) {
            hvp = (ViewPager)itemView.findViewById(R.id.hvp);
            hvp.setAdapter(listsPagerAdapter);
        } else {
            itemView.setVisibility(View.GONE);
        }
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}