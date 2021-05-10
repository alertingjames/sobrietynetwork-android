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

import com.sobriety.sobriety.R;
import com.sobriety.sobriety.main.NetworkUsersActivity;
import com.sobriety.sobriety.models.Network;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by LGH419 on 8/19/2018.
 */

public class NetworkListAdapter extends BaseAdapter {
    private Context _context;
    private ArrayList<Network> _datas = new ArrayList<>();
    private ArrayList<Network> _alldatas = new ArrayList<>();

    public NetworkListAdapter(Context context){

        super();
        this._context = context;
    }

    public void setDatas(ArrayList<Network> datas) {

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
            convertView = inflater.inflate(R.layout.network_list_item, parent, false);

            holder.picture = (ImageView) convertView.findViewById(R.id.picture);
            holder.picture1 = (ImageView) convertView.findViewById(R.id.picture1);
            holder.picture2 = (ImageView) convertView.findViewById(R.id.picture2);
            holder.picture3 = (ImageView) convertView.findViewById(R.id.picture3);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);
            holder.progressBar1 = (ProgressBar)convertView.findViewById(R.id.progressBar1);
            holder.progressBar2 = (ProgressBar)convertView.findViewById(R.id.progressBar2);
            holder.progressBar3 = (ProgressBar)convertView.findViewById(R.id.progressBar3);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final Network entity = (Network) _datas.get(position);

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
                Intent intent = new Intent(_context, NetworkUsersActivity.class);
                intent.putExtra("network_id", entity.getIdx());
                intent.putExtra("name", String.valueOf(entity.getName()));
                _context.startActivity(intent);
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

            for (Network network : _alldatas){

                if (network instanceof Network) {

                    String value = ((Network) network).getName().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(network);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {
        ImageView picture, picture1, picture2, picture3;
        TextView name;
        ProgressBar progressBar, progressBar1,progressBar2,progressBar3;
    }
}





