package com.sobriety.sobriety.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sobriety.sobriety.R;
import com.sobriety.sobriety.commons.Commons;
import com.sobriety.sobriety.main.AudioPlayActivity;
import com.sobriety.sobriety.main.VideoPlayActivity;
import com.sobriety.sobriety.models.Message;
import com.sobriety.sobriety.models.Story;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by LGH419 on 8/17/2018.
 */

public class StoryListAdapter extends BaseAdapter {

    private Context _context;
    private ArrayList<Story> _datas = new ArrayList<>();
    private ArrayList<Story> _alldatas = new ArrayList<>();

    public StoryListAdapter(Context context){

        super();
        this._context = context;
    }

    public void setDatas(ArrayList<Story> datas) {

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
            convertView = inflater.inflate(R.layout.story_list_item, parent, false);

            holder.videoThumbnail = (ImageView) convertView.findViewById(R.id.videothumbnail);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.username = (TextView) convertView.findViewById(R.id.user);
            holder.dateTime = (TextView) convertView.findViewById(R.id.datetime);
            holder.audio = (ImageView) convertView.findViewById(R.id.audio);
            holder.videomark = (ImageView) convertView.findViewById(R.id.videomark);
            holder.audiomark = (ImageView) convertView.findViewById(R.id.audiomark);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final Story entity = (Story) _datas.get(position);

        Typeface font = Typeface.createFromAsset(_context.getAssets(), "Comfortaa_Bold.ttf");
        holder.title.setTypeface(font);
        font = Typeface.createFromAsset(_context.getAssets(), "lobster.ttf");
        holder.username.setTypeface(font);
        holder.title.setText(entity.getTitle());

        String[] monthes={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

        Calendar c = Calendar.getInstance();
        //Set time in milliseconds
        c.setTimeInMillis(Long.parseLong(entity.getDateTime()));
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMin = c.get(Calendar.MINUTE);
        if(mDay<10)
            holder.dateTime.setText(monthes[mMonth] + " 0" + mDay + ", " + mYear + " " + mHour + ":" + mMin);
        else
            holder.dateTime.setText(monthes[mMonth] + " " + mDay + ", " + mYear + " " + mHour + ":" + mMin);
        holder.username.setText(entity.getUserName());
        if(Commons.isMyStory)holder.username.setVisibility(View.GONE);
        if(entity.getThumbnail().length() > 0){
            holder.audiomark.setVisibility(View.GONE);
            holder.videomark.setVisibility(View.VISIBLE);
            holder.videoThumbnail.setVisibility(View.VISIBLE);
            holder.audio.setVisibility(View.GONE);
            Picasso.with(_context)
                    .load(entity.getThumbnail())
                    .error(R.drawable.noresult)
                    .placeholder(R.drawable.noresult)
                    .into(holder.videoThumbnail, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onError() {
                            holder.progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
        }else {
            holder.videomark.setVisibility(View.GONE);
            holder.audiomark.setVisibility(View.VISIBLE);
            holder.videoThumbnail.setVisibility(View.GONE);
            holder.audio.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.INVISIBLE);
            Picasso.with(_context)
                    .load(R.drawable.voiceicon)
                    .error(R.drawable.noresult)
                    .placeholder(R.drawable.noresult)
                    .into(holder.audio);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(entity.getUrl().endsWith(".mp4")){

                    Intent intent = new Intent(_context, VideoPlayActivity.class);
                    intent.putExtra("title", entity.getTitle());
                    intent.putExtra("videoUrl", entity.getUrl());
                    _context.startActivity(intent);

//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(entity.getUrl()));
//                    intent.setDataAndType(Uri.parse(entity.getUrl()), "video/*");
//                    _context.startActivity(intent);
                }else {

                    Intent intent = new Intent(_context, AudioPlayActivity.class);
                    intent.putExtra("title", entity.getTitle());
                    intent.putExtra("audioUrl", entity.getUrl());
                    _context.startActivity(intent);

//                    Intent intent = new Intent();
//                    intent.setAction(android.content.Intent.ACTION_VIEW);
//                    intent.setDataAndType(Uri.parse(entity.getUrl()), "audio/*");
//                    _context.startActivity(intent);
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

            for (Story story : _alldatas){

                if (story instanceof Story) {

                    String value = ((Story) story).getTitle().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(story);
                    }
                    else {
                        value = ((Story) story).getUserName().toLowerCase();
                        if (value.contains(charText)) {
                            _datas.add(story);
                        }
                        else {
                            String timeStamp = ((Story) story).getDateTime();
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
                            if (value.contains(charText) || value.startsWith(charText)) {
                                _datas.add(story);
                            }
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {

        ImageView videoThumbnail, audio, videomark, audiomark;
        TextView title, dateTime, username;
        ProgressBar progressBar;
    }
}



