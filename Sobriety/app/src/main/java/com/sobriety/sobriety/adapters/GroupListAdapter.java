package com.sobriety.sobriety.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sobriety.sobriety.R;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.main.GroupUsersActivity;
import com.sobriety.sobriety.main.MyGroupUsersActivity;
import com.sobriety.sobriety.main.JoinGroupActivity;
import com.sobriety.sobriety.models.Group;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by LGH419 on 8/18/2018.
 */

public class GroupListAdapter extends BaseAdapter {
    private Context _context;
    private ArrayList<Group> _datas = new ArrayList<>();
    private ArrayList<Group> _alldatas = new ArrayList<>();

    public GroupListAdapter(Context context){

        super();
        this._context = context;
    }

    public void setDatas(ArrayList<Group> datas) {

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
            convertView = inflater.inflate(R.layout.group_list_item, parent, false);

            holder.picture1 = (ImageView) convertView.findViewById(R.id.picture1);
            holder.picture2 = (ImageView) convertView.findViewById(R.id.picture2);
            holder.picture3 = (ImageView) convertView.findViewById(R.id.picture3);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.code = (TextView) convertView.findViewById(R.id.code);
            holder.progressBar1 = (ProgressBar)convertView.findViewById(R.id.progressBar1);
            holder.progressBar2 = (ProgressBar)convertView.findViewById(R.id.progressBar2);
            holder.progressBar3 = (ProgressBar)convertView.findViewById(R.id.progressBar3);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final Group entity = (Group) _datas.get(position);

        holder.name.setText(entity.getName());
        holder.code.setText(entity.getCode());
        holder.code.setVisibility(View.VISIBLE);
        if(Commons.isMyGroup)
            holder.code.setVisibility(View.VISIBLE);
        else holder.code.setVisibility(View.GONE);
        try{
            Picasso.with(_context)
                    .load(entity.getUsers().get(0).getPhotoUrl())
                    .error(R.drawable.noresult)
                    .placeholder(R.drawable.noresult)
                    .into(holder.picture1, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar1.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onError() {
                            holder.progressBar1.setVisibility(View.INVISIBLE);
                        }
                    });
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            Picasso.with(_context)
                    .load(R.drawable.noresult)
                    .error(R.drawable.noresult)
                    .placeholder(R.drawable.noresult)
                    .into(holder.picture1);
            holder.progressBar1.setVisibility(View.INVISIBLE);
        }
        try{
            Picasso.with(_context)
                    .load(entity.getUsers().get(1).getPhotoUrl())
                    .error(R.drawable.noresult)
                    .placeholder(R.drawable.noresult)
                    .into(holder.picture2, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar2.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onError() {
                            holder.progressBar2.setVisibility(View.INVISIBLE);
                        }
                    });
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            Picasso.with(_context)
                    .load(R.drawable.noresult)
                    .error(R.drawable.noresult)
                    .placeholder(R.drawable.noresult)
                    .into(holder.picture2);
            holder.progressBar2.setVisibility(View.INVISIBLE);
        }

        try {
            Picasso.with(_context)
                    .load(entity.getUsers().get(2).getPhotoUrl())
                    .error(R.drawable.noresult)
                    .placeholder(R.drawable.noresult)
                    .into(holder.picture3, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar3.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onError() {
                            holder.progressBar3.setVisibility(View.INVISIBLE);
                        }
                    });
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            Picasso.with(_context)
                    .load(R.drawable.noresult)
                    .error(R.drawable.noresult)
                    .placeholder(R.drawable.noresult)
                    .into(holder.picture3);
            holder.progressBar3.setVisibility(View.INVISIBLE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(entity.getUsers().isEmpty())return;
                if(Commons.isMyGroup){
                    Intent intent = new Intent(_context, MyGroupUsersActivity.class);
                    intent.putExtra("group_id", String.valueOf(entity.getIdx()));
                    intent.putExtra("group_name", String.valueOf(entity.getName()));
                    _context.startActivity(intent);
                }else {
                    Intent intent = new Intent(_context, GroupUsersActivity.class);
                    intent.putExtra("group_id", String.valueOf(entity.getIdx()));
                    intent.putExtra("group_name", entity.getName());
                    _context.startActivity(intent);
                }
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

            for (Group group : _alldatas){

                if (group instanceof Group) {

                    String value = ((Group) group).getName().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(group);
                    }
                    else {
                        value = ((Group) group).getDateTime().toLowerCase();
                        if (value.contains(charText)) {
                            _datas.add(group);
                        }
                        else {
                            value = ((Group) group).getCode().toLowerCase();
                            if (value.contains(charText) || value.startsWith(charText)) {
                                _datas.add(group);
                            }
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {
        ImageView picture1, picture2, picture3;
        TextView name, code;
        ProgressBar progressBar1,progressBar2,progressBar3;
    }
}




