package com.artfonapps.clientrestore.views.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.db.Order;
import com.artfonapps.clientrestore.db.Point;

import java.util.List;

/**
 * Created by Emil on 15.08.2016.
 */
public class ListsPagerAdapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;
    PointsAdapter pointsAdapter;
    OrdersAdapter ordersAdapter;
    private LinearLayoutManager mLayoutManager;

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
        pointsAdapter = new PointsAdapter(context, points);
        ordersAdapter = new OrdersAdapter(context, orders);
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
        RecyclerView itemsList = (RecyclerView)itemView.findViewById(R.id.items);
        mLayoutManager = new LinearLayoutManager(context);
       // mLayoutManager.setStackFromEnd(true);
        // mLayoutManager.setReverseLayout(true);


        if (ids.get(position) == R.layout.point_item) {
            itemsList.setAdapter(pointsAdapter);
        } else if (ids.get(position) == R.layout.order_item) {
            itemsList.setAdapter(ordersAdapter);
         //   itemsList.setDividerHeight(3);
        }
        //itemsList.setHasFixedSize(true);
        itemsList.setLayoutManager(mLayoutManager);
        //((TextView)itemView.findViewById(R.id.text)).setText(pages.get(position));
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
