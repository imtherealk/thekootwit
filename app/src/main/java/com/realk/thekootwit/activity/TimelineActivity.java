package com.realk.thekootwit.activity;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.realk.thekootwit.R;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;

import java.util.ArrayList;
import java.util.List;

public class TimelineActivity extends Activity {
    ListView timelineListView;
    List<Tweet> tweets = new ArrayList<Tweet>();

    final BaseAdapter timelineListAdapter = new BaseAdapter() {
        class ViewHolder {
            ImageView profileImage;
            TextView userName;
            TextView userId;
            TextView content;
            ImageButton retweetButton;
        }

        @Override
        public int getCount() {
            return tweets.size();
        }

        @Override
        public Object getItem(int position) {
            return tweets.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_item_search, parent, false);
                viewHolder = new ViewHolder();
                convertView.setTag(viewHolder);

                viewHolder.profileImage = (ImageView) convertView.findViewById(R.id.user_profile_image);
                viewHolder.userName = (TextView) convertView.findViewById(R.id.username);
                viewHolder.userId = (TextView) convertView.findViewById(R.id.user_id);
                viewHolder.content = (TextView) convertView.findViewById(R.id.content);
                viewHolder.retweetButton = (ImageButton) convertView.findViewById(R.id.btn_retweet);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Tweet tweet = (Tweet) this.getItem(position);

            return convertView;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        timelineListView = (ListView) findViewById(R.id.timeline);
    }

}
