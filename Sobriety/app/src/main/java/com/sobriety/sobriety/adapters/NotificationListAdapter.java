package com.sobriety.sobriety.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sobriety.sobriety.R;
import com.sobriety.sobriety.main.NotificationActivity;
import com.sobriety.sobriety.models.Message;
import com.sobriety.sobriety.models.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by LGH419 on 8/26/2018.
 */

public class NotificationListAdapter extends BaseAdapter {

    private NotificationActivity _context;
    private ArrayList<Message> _datas = new ArrayList<>();
    private ArrayList<Message> _alldatas = new ArrayList<>();

    public NotificationListAdapter(NotificationActivity context){

        super();
        this._context = context;
    }

    public void setDatas(ArrayList<Message> datas) {

        _alldatas = datas;
        _datas.clear();
        _datas.addAll(_alldatas);
    }

    @Override
    public int getCount(){
        return _datas.size();
    }

    @Override
    public Object getItem(int position){
        return _datas.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        CustomHolder holder;

        if (convertView == null) {
            holder = new CustomHolder();

            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.notilist_item, parent, false);

            holder.picture = (ImageView) convertView.findViewById(R.id.picture);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.datetime = (TextView) convertView.findViewById(R.id.datetime);
            holder.groupname = (TextView) convertView.findViewById(R.id.groupName);
            holder.message = (TextView) convertView.findViewById(R.id.message);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.readButton);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final Message entity = (Message) _datas.get(position);

        holder.name.setText(entity.getSender_name());
        Picasso.with(_context)
                .load(entity.getSender_photo())
                .error(R.drawable.noresult)
                .placeholder(R.drawable.noresult)
                .into(holder.picture, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.progressBar.setVisibility(View.INVISIBLE);
                    }
                    @Override
                    public void onError() {
                        holder.progressBar.setVisibility(View.INVISIBLE);
                    }
                });

        String[] monthes={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

        Calendar c = Calendar.getInstance();
        //Set time in milliseconds
        c.setTimeInMillis(Long.parseLong(entity.getDate_time()));
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMin = c.get(Calendar.MINUTE);
        if(mDay<10)
            holder.datetime.setText(monthes[mMonth] + " 0" + mDay + ", " + mYear + " " + mHour + ":" + mMin);
        else
            holder.datetime.setText(monthes[mMonth] + " " + mDay + ", " + mYear + " " + mHour + ":" + mMin);
        holder.groupname.setText(entity.getGroup_name());
        holder.message.setText(entity.getMessage());
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.checkBox.isChecked()){
                    entity.getFirebase().removeValue();
                    if(entity.getGroup_name().length() > 0){

                    }else {
                        _context.toChat(entity);
                    }
                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return convertView;
    }

    public void filter(String charText){

        charText = charText.toLowerCase();
        _datas.clear();

        if(charText.length() == 0){
            _datas.addAll(_alldatas);
        }else {

            for (Message message : _alldatas){

                if (message instanceof Message) {

                    String value = ((Message) message).getMessage().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(message);
                    }
                    else {
                        value = ((Message) message).getSender_name().toLowerCase();
                        if (value.contains(charText)) {
                            _datas.add(message);
                        }
                        else {
                            String timeStamp = ((Message) message).getDate_time();
                            String[] monthes={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

                            Calendar c = Calendar.getInstance();
                            //Set time in milliseconds
                            c.setTimeInMillis(Long.parseLong(timeStamp));
                            int mYear = c.get(Calendar.YEAR);
                            int mMonth = c.get(Calendar.MONTH);
                            int mDay = c.get(Calendar.DAY_OF_MONTH);
                            int mHour = c.get(Calendar.HOUR_OF_DAY);
                            int mMin = c.get(Calendar.MINUTE);
                            if(mDay<10)
                                value = monthes[mMonth] + " 0" + mDay + ", " + mYear + " " + mHour + ":" + mMin;
                            else
                                value = monthes[mMonth] + " " + mDay + ", " + mYear + " " + mHour + ":" + mMin;
                            Log.d("DATETIME===>", value);
                            if (value.toLowerCase().contains(charText) || value.toLowerCase().startsWith(charText)) {
                                _datas.add(message);
                            }else {
                                value = ((Message) message).getGroup_name().toLowerCase();
                                if (value.contains(charText)) {
                                    _datas.add(message);
                                }
                            }
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {
        ImageView picture;
        CheckBox checkBox;
        TextView name, datetime, groupname, message;
        ProgressBar progressBar;
    }
}























