package com.artfonapps.clientrestore.views.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.db.Order;
import com.artfonapps.clientrestore.db.Point;
import com.artfonapps.clientrestore.network.events.local.LocalDeleteEvent;
import com.artfonapps.clientrestore.network.utils.BusProvider;
import com.artfonapps.clientrestore.views.StartActivity;
import com.squareup.otto.Produce;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by paperrose on 11.07.2016.
 */
public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    public List<Order> orders;
    public Context mContext;
    private static final StrikethroughSpan STRIKE_THROUGH_SPAN = new StrikethroughSpan();
    LayoutInflater inflater;
    //TODO change with recyclerView

    public OrdersAdapter(Context context, List<Order> orders) {
        mContext = context;
        this.orders = new ArrayList<>();
        this.orders.addAll(orders);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void refresh(List<Order> orders)
    {
        this.orders = orders;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.order_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final OrdersAdapter.ViewHolder holder, int position) {
        Order p = orders.get(position);
        holder.card.removeAllViews();

        //Фу так делать
        TextView orderId = new TextView(mContext);
        orderId.setText("Заказ №" + p.getIdListTraffic());
        orderId.setTextColor(mContext.getResources().getColor(R.color.cpb_white));
        orderId.setTypeface(null, Typeface.BOLD);
        holder.card.addView(orderId);

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
            holder.card.addView(tv);
        }
        holder.removeOrder.setTag(position);
        holder.backLayout.setBackgroundResource(p.isCurrentOrder() ? R.drawable.order_item_drawable : R.color.reStore_pink_light);
        holder.removeOrder.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Предупреждение")
                    .setMessage("Удалить заказ?")
                    .setNegativeButton("Нет",
                            (dialog, id) -> {
                                dialog.cancel();
                            })
                    .setPositiveButton("Да",
                            (dialog, id) -> {
                                if (mContext instanceof StartActivity) {
                                    BusProvider.getInstance()
                                            .post(produceDeleteEvent(orders.get((Integer)v.getTag())));
                                }
                            });

            AlertDialog alert = builder.create();
            alert.show();
        });
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.remove_order)
        ImageButton removeOrder;
        @BindView(R.id.card)
        LinearLayout card;
        @BindView(R.id.backLayout)
        LinearLayout backLayout;
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    @Produce
    public LocalDeleteEvent produceDeleteEvent(Order order)  {
        return new LocalDeleteEvent().setCurOrder(order.getIdListTraffic()).setFromPush(false);
    }


}
