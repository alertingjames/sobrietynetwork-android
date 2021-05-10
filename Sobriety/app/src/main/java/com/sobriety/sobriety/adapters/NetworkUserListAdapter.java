package com.sobriety.sobriety.adapters;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sobriety.sobriety.R;
import com.sobriety.sobriety.main.NetworkUsersActivity;
import com.sobriety.sobriety.models.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by LGH419 on 8/19/2018.
 */

public class NetworkUserListAdapter extends BaseAdapter {

    private NetworkUsersActivity _context;
    private ArrayList<User> _datas = new ArrayList<>();
    private ArrayList<User> _alldatas = new ArrayList<>();

    public NetworkUserListAdapter(NetworkUsersActivity context){

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
            convertView = inflater.inflate(R.layout.network_user_list_item, parent, false);

            holder.picture = (ImageView) convertView.findViewById(R.id.picture);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.inviteButton = (ImageButton) convertView.findViewById(R.id.inviteButton);
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

        holder.inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _context.sendInvitationNoti(String.valueOf(entity.getIdx()), "", "");
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

        ImageView picture;
        ImageButton inviteButton;
        TextView name, username, phoneNumber;
        ProgressBar progressBar;
    }
}




