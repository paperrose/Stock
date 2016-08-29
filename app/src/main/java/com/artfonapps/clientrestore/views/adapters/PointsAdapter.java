package com.artfonapps.clientrestore.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.db.Helper;
import com.artfonapps.clientrestore.db.Point;
import com.artfonapps.clientrestore.network.events.local.ChangeCurPointEvent;
import com.artfonapps.clientrestore.network.utils.BusProvider;
import com.artfonapps.clientrestore.views.StartActivity;
import com.squareup.otto.Produce;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.ViewHolder> {

    //TODO change with recyclerView!!!

    AppCompatActivity context;
    private List<Point> items;
    LayoutInflater inflater;
    public PointsAdapter(Context context, List<Point> objects) {
        this.context = (AppCompatActivity)context;
        this.items = new ArrayList<>();
        this.items.addAll(objects);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void refresh(List<Point> items)
    {
        this.items = items;
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Point p = items.get(position);
        holder.point.setText(p.getPoint());
        holder.type.setText((p.getType() == 1) ? "Загрузка" : "Выгрузка");
        holder.datetime.setText(p.getFormatPlanDatetime());
        holder.address.setText(p.getAddress());
        holder.doc.setText(p.getDoc());
        holder.client.setText(p.getClient());
        holder.contact.setText(p.getContactName());
        holder.call.setTag(position);
        holder.expand.setTag(position);
        holder.itemPanel.setTag(position);
        holder.call.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + items.get((Integer)v.getTag()).getContact()));
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            context.startActivity(intent);
        });
        holder.expand.setOnClickListener(v -> {
            LinearLayout vwParentRow = (LinearLayout) v.getParent().getParent();
            LinearLayout ext = (LinearLayout)vwParentRow.findViewById(R.id.extendedPart);
            ImageButton ib =  (ImageButton)vwParentRow.findViewById(R.id.expand);
            if (ext.getVisibility() == View.VISIBLE) {
                ext.setVisibility(View.GONE);
                ib.setImageResource(R.drawable.question);
            } else {
                ext.setVisibility(View.VISIBLE);
                ib.setImageResource(R.drawable.arrow);
            }
        });
        holder.itemPanel.setBackgroundResource(p.isCurItem() ? R.color.cpb_green : R.color.reStore_pink_light);
        holder.itemPanel.setOnLongClickListener(v -> {
            Point p0 = items.get((Integer)v.getTag());
            Point p2 = Helper.getCurPoint();
            if (p0.isCurItem()) return false;
       /*     for (int i = 0; i < items.size(); i++) {
                Point p2 = items.get(i);
                if (p2.isCurItem()) {
                    if (p2.getType() == 1 && p0.getType() != 1 && p0.getIdListTraffic() == p2.getIdListTraffic()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Предупреждение")
                                .setMessage("Невозможно выбрать точку выгрузки во время загрузки в рамках одного заказа")
                                .setCancelable(false)
                                .setNegativeButton("ОК",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                        return false;
                    }
                }
                items.get(i).setCurItem(false);
            }*/
            if (p2 != null)
                p2.setCurItem(false);
            p0.setCurItem(true);
            notifyDataSetChanged();
            if (context instanceof StartActivity) {
                BusProvider.getInstance()
                        .post(produceChangeCurPointEvent(p0));
            }
            return false;
        });
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.point_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.point)
        TextView point;
        @BindView(R.id.typePoint)
        TextView type;
        @BindView(R.id.plan)
        TextView datetime;
        @BindView(R.id.address)
        TextView address;
        @BindView(R.id.document)
        TextView doc;
        @BindView(R.id.client)
        TextView client;
        @BindView(R.id.contact)
        TextView contact;
        @BindView(R.id.phoneCall)
        ImageButton call;
        @BindView(R.id.expand)
        ImageButton expand;
        @BindView(R.id.itemPanel)
        LinearLayout itemPanel;
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Produce
    public ChangeCurPointEvent produceChangeCurPointEvent(Point point)  {
        return new ChangeCurPointEvent().setCurPoint(point);
    }
}