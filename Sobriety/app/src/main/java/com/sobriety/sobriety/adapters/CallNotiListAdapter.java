package com.sobriety.sobriety.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sobriety.sobriety.R;
import com.sobriety.sobriety.main.NotificationActivity;
import com.sobriety.sobriety.models.Message;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by LGH419 on 8/26/2018.
 */

public class CallNotiListAdapter extends BaseAdapter {

    private NotificationActivity _context;
    private ArrayList<Message> _datas = new ArrayList<>();
    private ArrayList<Message> _alldatas = new ArrayList<>();

    public CallNotiListAdapter(NotificationActivity context){

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
            convertView = inflater.inflate(R.layout.call_notilist_item, parent, false);

            holder.picture = (CircleImageView) convertView.findViewById(R.id.picture);
            holder.acceptedicon = (ImageView) convertView.findViewById(R.id.acceptedicon);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.phonenumber = (TextView) convertView.findViewById(R.id.phone_number);
            holder.footer = (TextView) convertView.findViewById(R.id.footer);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.datetime = (TextView) convertView.findViewById(R.id.datetime);
            holder.groupname = (TextView) convertView.findViewById(R.id.groupName);
            holder.okayButton = (Button) convertView.findViewById(R.id.okayButton);
            holder.cancelButton = (Button) convertView.findViewById(R.id.cancelButton);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final Message entity = (Message) _datas.get(position);

        Typeface font = Typeface.createFromAsset(_context.getAssets(), "Comfortaa_Bold.ttf");
        holder.title.setTypeface(font);
        holder.footer.setTypeface(font);

        if(entity.getCall_code().length() == 0) {
            if(entity.getMessage().equals("I have accepted your invitation.")) {
                holder.title.setText("Your invitation has been accepted by");
                holder.cancelButton.setVisibility(View.GONE);
                holder.footer.setVisibility(View.GONE);
                holder.acceptedicon.setVisibility(View.VISIBLE);
                holder.phonenumber.setVisibility(View.VISIBLE);
                holder.phonenumber.setText("+" + entity.getSender_phone());
            }
            else if(entity.getMessage().equals("I invite you to our Network.")){
                holder.title.setText("You have been invited by");
                holder.okayButton.setText("Accept");
                holder.footer.setVisibility(View.VISIBLE);
                holder.phonenumber.setVisibility(View.VISIBLE);
                holder.phonenumber.setText("+" + entity.getSender_phone());
            }
        }else {

            if(entity.getMessage().equals("I send you a nearest call.")) {
                holder.title.setText("You have an incoming call from a nearby alcoholic/addict");
                holder.footer.setVisibility(View.GONE);
                holder.phonenumber.setVisibility(View.GONE);
            }
            else if(entity.getMessage().equals("I send you a random call.")){
                holder.title.setText("You have an incoming call from a random alcoholic/addict");
                holder.footer.setVisibility(View.GONE);
                holder.phonenumber.setVisibility(View.GONE);
            }
            else {
                holder.title.setText("You have an incoming call by");
                holder.footer.setVisibility(View.GONE);
                holder.phonenumber.setVisibility(View.GONE);
            }
        }

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
        if(entity.getGroup_name().length() == 0) {
            holder.groupname.setText("");
        }else {
            holder.groupname.setText(entity.getGroup_name());
        }
        holder.okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _context.branch(entity);
            }
        });

        holder.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                entity.getFirebase().removeValue();
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
                            value = ((Message) message).getDate_time().toLowerCase();
                            String[] monthes={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

                            Calendar c = Calendar.getInstance();
                            //Set time in milliseconds
                            c.setTimeInMillis(Long.parseLong(value));
                            int mYear = c.get(Calendar.YEAR);
                            int mMonth = c.get(Calendar.MONTH);
                            int mDay = c.get(Calendar.DAY_OF_MONTH);
                            int mHour = c.get(Calendar.HOUR_OF_DAY);
                            int mMin = c.get(Calendar.MINUTE);
                            if(mDay<10)
                                value = monthes[mMonth] + " 0" + mDay + ", " + mYear + " " + mHour + ":" + mMin;
                            else
                                value = monthes[mMonth] + " " + mDay + ", " + mYear + " " + mHour + ":" + mMin;
                            if (value.contains(charText) || value.startsWith(charText)) {
                                _datas.add(message);
                            }
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {
        CircleImageView picture;
        ImageView acceptedicon;
        Button okayButton, cancelButton;
        TextView name, datetime, groupname, phonenumber, title, footer;
        ProgressBar progressBar;
    }
}























