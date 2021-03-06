package com.realk.thekootwit.activity.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.realk.thekootwit.CustomTwitterApiClient;
import com.realk.thekootwit.Globals;
import com.realk.thekootwit.R;
import com.realk.thekootwit.TwitterUtil;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TimelineFragment extends Fragment {
    ListView timelineListView;
    List<Tweet> tweets = new ArrayList<Tweet>();
    final BaseAdapter timelineListAdapter = new BaseAdapter() {
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.tweet_listview_item, parent, false);
                viewHolder = new ViewHolder();
                convertView.setTag(viewHolder);

                viewHolder.profileImage = (ImageView) convertView.findViewById(R.id.user_profile_image);
                viewHolder.userName = (TextView) convertView.findViewById(R.id.username);
                viewHolder.userId = (TextView) convertView.findViewById(R.id.user_id);
                viewHolder.content = (TextView) convertView.findViewById(R.id.content);
                viewHolder.retweetButton = (ImageButton) convertView.findViewById(R.id.btn_retweet);
                viewHolder.retweetButton.setImageDrawable(
                        new IconDrawable(getActivity(), FontAwesomeIcons.fa_retweet).sizeDp(20));
                viewHolder.time = (TextView) convertView.findViewById(R.id.time);
                viewHolder.photo = (ImageView) convertView.findViewById(R.id.photo);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Tweet tweet = (Tweet) this.getItem(position);
            Picasso.with(getActivity()).load(tweet.user.profileImageUrl).into(viewHolder.profileImage);
            viewHolder.userName.setText(tweet.user.name);
            viewHolder.userId.setText("@" + tweet.user.screenName);
            viewHolder.content.setText(tweet.text);
            try {
                Date date = TwitterUtil.getTwitterDate(tweet.createdAt);
                SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일 a hh시 mm분", Locale.KOREA);
                String createdAt = format.format(date);
                viewHolder.time.setText(createdAt);
            } catch (ParseException ignored) {
            }
            viewHolder.photo.setVisibility(View.GONE);
            if ((tweet.entities != null && tweet.entities.media != null)) {
                for (MediaEntity mediaEntity : tweet.entities.media) {
                    if (mediaEntity.type.equals("photo")) {
                        viewHolder.photo.setVisibility(View.VISIBLE);
                        Picasso.with(getActivity()).load(mediaEntity.mediaUrlHttps).into(viewHolder.photo);
                        break;
                    }
                }
            }

            viewHolder.retweetButton.setEnabled(!tweet.retweeted);
            viewHolder.retweetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomTwitterApiClient.getActiveClient().getStatusesService().retweet(tweet.getId(), false, new com.twitter.sdk.android.core.Callback<Tweet>() {
                        @Override
                        public void success(Result<Tweet> result) {
                            Toast.makeText(getActivity(), "리트윗 하였습니다.", Toast.LENGTH_SHORT).show();
                            Tweet origin = result.data.retweetedStatus;
                            tweets = Lists.newLinkedList(Iterables.concat(
                                    tweets.subList(0, position),
                                    Arrays.asList(origin),
                                    tweets.subList(position + 1, tweets.size())));
                            notifyDataSetChanged();
                        }

                        @Override
                        public void failure(TwitterException e) {
                            Toast.makeText(getActivity(), "리트윗 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            return convertView;
        }

        class ViewHolder {
            ImageView profileImage;
            TextView userName;
            TextView userId;
            TextView content;
            ImageButton retweetButton;
            ImageView photo;
            TextView time;
        }
    };
    SwipeRefreshLayout swipeLayout;
    boolean loading = false;
    boolean noMore = false;
    boolean refreshing = false;

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(final boolean loading) {
        this.loading = loading;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public void setRefreshing(final boolean refreshing) {
        this.refreshing = refreshing;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(refreshing);
            }
        });
    }

    void updateTweets(List<Tweet> tweets) {
        this.tweets = tweets;
        timelineListAdapter.notifyDataSetChanged();
    }

    void appendTweets(List<Tweet> tweets) {
        updateTweets(Lists.newLinkedList(Iterables.concat(this.tweets, tweets)));
    }

    void fetchTweets() {
        if (isLoading()) {
            return;
        }
        setLoading(true);
        setRefreshing(true);

        CustomTwitterApiClient.getActiveClient().getCustomListService().statuses(Globals.LIST_SLUG, CustomTwitterApiClient.getActiveUserId(), new Callback<List<Tweet>>() {
            @Override
            public void success(List<Tweet> tweets, Response response) {
                updateTweets(tweets);
                setLoading(false);
                setRefreshing(false);
                noMore = false;
            }

            @Override
            public void failure(RetrofitError error) {
                setLoading(false);
                setRefreshing(false);
                Toast.makeText(getActivity(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    void fetchMoreTweets() {
        if (isLoading() || noMore) {
            return;
        }
        setLoading(true);

        CustomTwitterApiClient.getActiveClient().getCustomListService()
                .statuses(Globals.LIST_SLUG, CustomTwitterApiClient.getActiveUserId(),
                        this.tweets.get(this.tweets.size() - 1).getId(),
                        new Callback<List<Tweet>>() {
                            @Override
                            public void success(List<Tweet> tweets, Response response) {
                                tweets = tweets.subList(1, tweets.size());
                                appendTweets(tweets);
                                setLoading(false);
                                noMore = tweets.isEmpty();
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                setLoading(false);
                                Toast.makeText(getActivity(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);

        timelineListView = (ListView) view.findViewById(R.id.timeline);
        timelineListView.setAdapter(timelineListAdapter);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTweets();
            }
        });

        // Add loading footer
        View loadMoreView = inflater.inflate(R.layout.view_loadmore, null);
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
        return view;
    }

}
