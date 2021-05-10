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
import com.sobriety.sobriety.main.MyGroupUsersActivity;
import com.sobriety.sobriety.main.VideoChatViewActivity;
import com.sobriety.sobriety.main.VoiceChatViewActivity;
import com.sobriety.sobriety.models.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by LGH419 on 8/19/2018.
 */

public class MyGroupUserListAdapter extends BaseAdapter {

    private Context _context;
    private ArrayList<User> _datas = new ArrayList<>();
    private ArrayList<User> _alldatas = new ArrayList<>();

    public MyGroupUserListAdapter(Context context){

        super();
        this._context = context;
    }

    public void setDatas(ArrayList<User> datas) {

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
            convertView = inflater.inflate(R.layout.my_group_user_list_item, parent, false);

            holder.picture = (CircleImageView) convertView.findViewById(R.id.picture);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            holder.callButton = (ImageButton) convertView.findViewById(R.id.callButton);
            holder.chatButton = (ImageButton) convertView.findViewById(R.id.chatButton);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final User entity = (User) _datas.get(position);

        holder.name.setText(entity.getName());
        Picasso.with(_context)
                .load(entity.getPhotoUrl())
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

        if(!Commons.isMyGroup) holder.checkBox.setVisibility(View.GONE);

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.checkBox.isChecked()){
                    if(!Commons.selectedUserIds.contains(entity.getIdx()))
                        Commons.selectedUserIds.add(entity.getIdx());
                }
                else {
                    if(Commons.selectedUserIds.contains(entity.getIdx())){
                        Iterator itr = Commons.selectedUserIds.iterator();
                        while (itr.hasNext())
                        {
                            int x = (Integer)itr.next();
                            if (x == entity.getIdx())
                                itr.remove();
                        }
                    }
                }

                Log.d("USERIDS===>", String.valueOf(Commons.selectedUserIds.size()));
            }
        });

        holder.callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(_context, VoiceChatViewActivity.class);
                intent.putExtra("call_code", entity.getUsername() + String.valueOf(Commons.thisUser.getUsername()));
                intent.putExtra("member_id", String.valueOf(entity.getIdx()));
                intent.putExtra("group_name", MyGroupUsersActivity.groupName);
                intent.putExtra("network_id", String.valueOf(Commons.myNetworkId));
                _context.startActivity(intent);
            }
        });

        holder.chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(_context, ChatActivity.class);
                Commons.user = entity;
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

            for (User user : _alldatas){

                if (user instanceof User) {

                    String value = ((User) user).getName().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(user);
                    }
                    else {
                        value = ((User) user).getUsername().toLowerCase();
                        if (value.contains(charText)) {
                            _datas.add(user);
                        }
                        else {
                            value = ((User) user).getPhoneNumber().toLowerCase();
                            if (value.contains(charText) || value.startsWith(charText)) {
                                _datas.add(user);
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
        ImageButton callButton, chatButton;
        TextView name;
        CheckBox checkBox;
        ProgressBar progressBar;
    }
}


