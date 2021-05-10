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
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.main.ChatActivity;
import com.sobriety.sobriety.main.HomeActivity;
import com.sobriety.sobriety.main.NotiHistoryActivity;
import com.sobriety.sobriety.main.VideoChatViewActivity;
import com.sobriety.sobriety.main.VoiceChatViewActivity;
import com.sobriety.sobriety.models.Message;
import com.sobriety.sobriety.models.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by LGH419 on 8/26/2018.
 */

public class NotiHistoryAdapter extends BaseAdapter {

    private NotiHistoryActivity _context;
    private ArrayList<Message> _datas = new ArrayList<>();
    private ArrayList<Message> _alldatas = new ArrayList<>();

    public NotiHistoryAdapter(NotiHistoryActivity context){

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
            convertView = inflater.inflate(R.layout.noti_history_item, parent, false);

            holder.picture = (ImageView) convertView.findViewById(R.id.picture);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.datetime = (TextView) convertView.findViewById(R.id.datetime);
            holder.groupname = (TextView) convertView.findViewById(R.id.groupName);
            holder.message = (TextView) convertView.findViewById(R.id.message);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            holder.chatButton = (ImageButton) convertView.findViewById(R.id.chatButton);
            holder.callButton = (ImageButton) convertView.findViewById(R.id.callButton);
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
                    if(!Commons.selectedNotiIds.contains(entity.getIdx()))
                        Commons.selectedNotiIds.add(entity.getIdx());
                }
                else {
                    if(Commons.selectedNotiIds.contains(entity.getIdx())){
                        Iterator itr = Commons.selectedNotiIds.iterator();
                        while (itr.hasNext())
                        {
                            int x = (Integer)itr.next();
                            if (x == entity.getIdx())
                                itr.remove();
                        }
                    }
                }

                Log.d("NOTIIDS===>", String.valueOf(Commons.selectedNotiIds.size()));
            }
        });

        holder.chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(_context, ChatActivity.class);
                User user = new User();
                user.setIdx(entity.getSender_id());
                user.setName(entity.getSender_name());
                user.setPhotoUrl(entity.getSender_photo());
                user.setPhoneNumber(entity.getSender_phone());
                Commons.user = user;
                _context.startActivity(intent);
            }
        });

        holder.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if(entity.getOption().equals("nearest") || entity.getOption().equals("random"))
                    intent = new Intent(_context, VoiceChatViewActivity.class);
                else
                    intent = new Intent(_context, VoiceChatViewActivity.class);
                intent.putExtra("call_code", entity.getCall_code());
                intent.putExtra("member_id", String.valueOf(entity.getSender_id()));
                intent.putExtra("group_name", entity.getGroup_name());
                intent.putExtra("network_id", entity.getNetwork_id());
                _context.startActivity(intent);
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
        ImageButton chatButton, callButton;
        TextView name, datetime, groupname, message;
        ProgressBar progressBar;
        CheckBox checkBox;
    }
}























