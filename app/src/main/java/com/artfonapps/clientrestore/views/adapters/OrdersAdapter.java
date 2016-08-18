package com.artfonapps.clientrestore.views.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.artfonapps.clientrestore.MainActivity;
import com.artfonapps.clientrestore.db.Order;
import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.db.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paperrose on 11.07.2016.
 */
public class OrdersAdapter extends ArrayAdapter<Order> {

    public List<Order> orders;
    public AppCompatActivity mContext;
    private static final StrikethroughSpan STRIKE_THROUGH_SPAN = new StrikethroughSpan();

    public OrdersAdapter(Context context, int resource, List<Order> orders) {
        super(context, resource, orders);
        mContext = (AppCompatActivity)context;
        this.orders = new ArrayList<>();
        this.orders.addAll(orders);
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    public void refresh(List<Order> orders)
    {
        this.orders.clear();
        this.orders.addAll(orders);
        notifyDataSetChanged();
    }

    @Override
    public Order getItem(int i) {
        return orders.get(i);
    }


    public int getItemIndex(Order item) {
        return orders.indexOf(item);
    }


    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        if (view == null) {
            view = inflater.inflate(R.layout.order_item, viewGroup, false);
        }
        Order p = orders.get(i);
        LinearLayout card = (LinearLayout)view.findViewById(R.id.card);
        for (Point point : p.points) {
            TextView tv = new TextView(mContext);
            tv.setTextColor(mContext.getResources().getColor(R.color.cpb_white));


            if (point.isCurItem()) {
                tv.setTypeface(null, Typeface.BOLD);
            }
            if ((point.getArrivalDatetime() != 0 && point.getStartDatetime() != 0 && point.getFinishDatetime() != 0)) {
                tv.setText(point.getPoint(), TextView.BufferType.SPANNABLE);
                Spannable spannable = (Spannable) tv.getText();
                spannable.setSpan(STRIKE_THROUGH_SPAN, 0, point.getPoint().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                tv.setText(point.getPoint());
            }
            card.addView(tv);
        }
        card.setBackgroundResource(p.isCurrentOrder() ? R.drawable.order_item_drawable : R.color.reStore_pink_light);
        ImageButton ib = (ImageButton)view.findViewById(R.id.remove_order);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Предупреждение")
                        .setMessage("Удалить заказ?")
                        .setNegativeButton("Нет",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                        })
                        .setPositiveButton("Да",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                   //     mContext.removeLog(Integer.toString(points.get(i).idListTraffic));
                                   //     mContext.declineTask(Integer.toString(points.get(i).idListTraffic));
                                        //TODO deleteEvent
                                    }
                                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        return view;
    }


}
