package com.artfonapps.clientrestore.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.models.Helper;
import com.artfonapps.clientrestore.models.Point;
import com.artfonapps.clientrestore.network.events.local.ChangeCurPointEvent;
import com.artfonapps.clientrestore.network.utils.BusProvider;
import com.artfonapps.clientrestore.views.MainActivity;
import com.artfonapps.clientrestore.views.StartActivity;
import com.squareup.otto.Produce;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paperrose on 01.04.2016.
 */
public class PointsAdapter extends ArrayAdapter<Point> {

    //TODO change with recyclerView!!!

    AppCompatActivity context;
    private List<Point> items;
    LayoutInflater inflater;
    public PointsAdapter(Context context, int resource, List<Point> objects) {
        super(context, resource, objects);
        this.context = (AppCompatActivity)context;
        this.items = new ArrayList<>();
        this.items.addAll(objects);
        inflater = this.context.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public void refresh(List<Point> items)
    {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public Point getItem(int i) {
        return items.get(i);
    }



    public int getItemIndex(Point item) {
        return items.indexOf(item);
    }

    private class PointHolder {
        TextView point;
        TextView type;
        TextView datetime;
        TextView address;
        TextView doc;
        TextView client;
        TextView contact;
        ImageButton call;
        ImageButton expand;
        LinearLayout ll;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final PointHolder holder;

        if (view == null) {
            view = inflater.inflate(R.layout.point_item, viewGroup, false);

            holder = new PointHolder();
            holder.point = (TextView) view.findViewById(R.id.point);
            holder.type = (TextView) view.findViewById(R.id.typePoint);
            holder.datetime = (TextView) view.findViewById(R.id.plan);
            holder.address = (TextView) view.findViewById(R.id.address);
            holder.doc = (TextView) view.findViewById(R.id.document);
            holder.client = (TextView) view.findViewById(R.id.client);
            holder.contact = (TextView) view.findViewById(R.id.contact);
            holder.call = (ImageButton) view.findViewById(R.id.phoneCall);
            holder.expand = (ImageButton) view.findViewById(R.id.expand);
            view.setTag(holder);
            view.setTag(R.id.point, holder.point);
            view.setTag(R.id.typePoint, holder.type);
            view.setTag(R.id.plan, holder.datetime);
            view.setTag(R.id.address, holder.address);
            view.setTag(R.id.document, holder.doc);
            view.setTag(R.id.contact, holder.contact);
            view.setTag(R.id.client, holder.client);
            view.setTag(R.id.expand, holder.expand);
            view.setTag(R.id.phoneCall, holder.call);
        } else {
            holder = (PointHolder)view.getTag();
        }
        Point p = items.get(i);
        holder.point.setTag(i);
        holder.type.setTag(i);
        holder.datetime.setTag(i);
        holder.address.setTag(i);
        holder.doc.setTag(i);
        holder.client.setTag(i);
        holder.contact.setTag(i);
        holder.call.setTag(i);
        holder.expand.setTag(i);
        view.setBackgroundResource(p.isCurItem() ? R.color.cpb_green : R.color.reStore_pink_light);
        holder.point.setText(p.getPoint());
        holder.type.setText((p.getType() == 1) ? "Загрузка" : "Выгрузка");
        holder.datetime.setText(p.getFormatPlanDatetime());
        holder.address.setText(p.getAddress());
        holder.doc.setText(p.getDoc());
        holder.client.setText(p.getClient());
        holder.contact.setText(p.getContactName());
        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + items.get((Integer)holder.contact.getTag()).getContact()));
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                context.startActivity(intent);
            }
        });
        holder.expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Point p0 = items.get((Integer)holder.contact.getTag());
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
                if (context instanceof MainActivity) {
                    //TODO remove this after refactoring
                    Intent intent2 = new Intent("refresh_push_count");
                    intent2.putExtra("type", "change");
                    intent2.putExtra("id", Integer.toString(p0.getIdListTrafficRoute()));
                    intent2.putExtra("traffic_id", Integer.toString(p0.getIdListTraffic()));
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
                } else if (context instanceof StartActivity) {
                    BusProvider.getInstance()
                            .post(produceChangeCurPointEvent(p0));
                }
                return false;
            }
        });
/*      if (p.getContact().equals(""))
            view.findViewById(R.id.contactLayout).setVisibility(View.GONE);
        else
            view.findViewById(R.id.contactLayout).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.contact)).setText(p.getContact());
*/        return view;
    }


    @Produce
    public ChangeCurPointEvent produceChangeCurPointEvent(Point point)  {
        return new ChangeCurPointEvent().setCurPoint(point);
    }
}