package com.realk.thekootwit.activity;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.realk.thekootwit.CustomTwitterApiClient;
import com.realk.thekootwit.Globals;
import com.realk.thekootwit.R;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TimelineActivity extends Activity {
    ListView timelineListView;
    List<Tweet> tweets = new ArrayList<Tweet>();

    boolean loading = false;
    boolean noMore = false;

    void updateTweets(List<Tweet> tweets) {
        this.tweets = tweets;
        timelineListAdapter.notifyDataSetChanged();
    }
    void appendTweets(List<Tweet> tweets) {
        updateTweets(Lists.newLinkedList(Iterables.concat(this.tweets, tweets)));
    }

    void fetchTweets() {
        if (loading) {
            return;
        }
        loading = true;

        CustomTwitterApiClient.getActiveClient().getCustomListService().statuses(Globals.LIST_SLUG, CustomTwitterApiClient.getActiveUserId(), new Callback<List<Tweet>>() {
            @Override
            public void success(List<Tweet> tweets, Response response) {
                updateTweets(tweets);
                loading = false;
                noMore = false;
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(TimelineActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    void fetchMoreTweets() {
        if (loading || noMore) {
            return;
        }
        loading = true;

        CustomTwitterApiClient.getActiveClient().getCustomListService()
                .statuses(Globals.LIST_SLUG, CustomTwitterApiClient.getActiveUserId(),
                        this.tweets.get(this.tweets.size() - 1).getId(),
                        new Callback<List<Tweet>>() {
            @Override
            public void success(List<Tweet> tweets, Response response) {
                tweets = tweets.subList(1, tweets.size());
                appendTweets(tweets);
                loading = false;
                noMore = tweets.isEmpty();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(TimelineActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

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
                convertView = inflater.inflate(R.layout.tweet_listview_item, parent, false);
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
            Picasso.with(TimelineActivity.this).load(tweet.user.profileImageUrl).into(viewHolder.profileImage);
            viewHolder.userName.setText(tweet.user.screenName);
            viewHolder.userId.setText(tweet.user.name);
            viewHolder.content.setText(tweet.text);

            viewHolder.retweetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Retweet
                }
            });

            return convertView;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        timelineListView = (ListView) findViewById(R.id.timeline);
        timelineListView.setAdapter(timelineListAdapter);

        // Add loading footer
        View loadMoreView = LayoutInflater.from(this).inflate(R.layout.view_loadmore, null);
        timelineListView.addFooterView(loadMoreView);

        timelineListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount != 1 && firstVisibleItem + visibleItemCount == totalItemCount) {
                    fetchMoreTweets();
                }
            }
        });

        fetchTweets();
    }

}
