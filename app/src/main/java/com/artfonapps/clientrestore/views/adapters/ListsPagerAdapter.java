package com.artfonapps.clientrestore.views.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.models.Order;
import com.artfonapps.clientrestore.models.Point;

import java.util.List;

/**
 * Created by Emil on 15.08.2016.
 */
public class ListsPagerAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;
    PointsAdapter pointsAdapter;
    OrdersAdapter ordersAdapter;

    public ListsPagerAdapter setPoints(List<Point> points) {
       // this.points.clear();
       // this.points.addAll(points);
        if (pointsAdapter != null)
            pointsAdapter.refresh(points);
        return this;
    }

    public ListsPagerAdapter setOrders(List<Order> orders) {
       // this.orders.clear();
        //this.orders.addAll(orders);
        if (ordersAdapter != null)
            ordersAdapter.refresh(orders);
        return this;
    }

    List<Integer> ids;

    public ListsPagerAdapter(Context context, List<Integer> ids, List<Point> points, List<Order> orders) {
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pointsAdapter = new PointsAdapter(context, R.layout.point_item, points);
        ordersAdapter = new OrdersAdapter(context, R.layout.order_item, orders);
        this.ids = ids;
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
        View itemView = layoutInflater.inflate(R.layout.list_layout, container, false);
        ListView itemsList = (ListView)itemView.findViewById(R.id.items);
        if (ids.get(position) == R.layout.point_item) {
            itemsList.setAdapter(pointsAdapter);
        } else {
            itemsList.setAdapter(ordersAdapter);
            itemsList.setDividerHeight(3);
        }
        //((TextView)itemView.findViewById(R.id.text)).setText(pages.get(position));
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
