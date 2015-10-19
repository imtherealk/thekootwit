package com.realk.thekootwit.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.realk.thekootwit.CustomTwitterApiClient;
import com.realk.thekootwit.Globals;
import com.realk.thekootwit.R;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.models.User;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SearchActivity extends Activity {
    EditText searchEditText;
    Button searchButton;
    ListView searchResultListView;
    List<User> users = new ArrayList<>();
    boolean loading = false;
    boolean noMore = false;

    private final BaseAdapter searchResultAdapter = new BaseAdapter() {
        class ViewHolder {
            ImageView profileImage;
            TextView username;
            TextView biography;
            ImageButton followButton;
        }
        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            /*  */
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_item_search, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.profileImage = (ImageView) convertView.findViewById(R.id.user_profile_image);
                viewHolder.username = (TextView) convertView.findViewById(R.id.username);
                viewHolder.biography = (TextView) convertView.findViewById(R.id.biography);
                viewHolder.followButton = (ImageButton) convertView.findViewById(R.id.btnfollow);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final User user = (User) this.getItem(position);
            Picasso.with(SearchActivity.this).load(user.profileImageUrl).into(viewHolder.profileImage);
            viewHolder.username.setText(user.name);
            viewHolder.biography.setText(user.description);

            viewHolder.followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    follow(user);
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    /*
                    // twitter app
                    */
                    Toast.makeText(SearchActivity.this, "트위터 앱 연결", Toast.LENGTH_LONG);
                }
            });
            return convertView;
        }
    };
    private String query = null;
    private int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchEditText = (EditText) findViewById(R.id.searchbox);
        searchButton = (Button) findViewById(R.id.btnsearch);
        searchResultListView = (ListView) findViewById(R.id.searchresult);

        searchResultListView.setAdapter(searchResultAdapter);

        // Add loading footer
        View loadMoreView = LayoutInflater.from(this).inflate(R.layout.view_loadmore, null);
        searchResultListView.addFooterView(loadMoreView);

        searchResultListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount != 1 && firstVisibleItem + visibleItemCount == totalItemCount) {
                    searchMore();
                }
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query = searchEditText.getText().toString();
                searchUsersFromTwitter();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void updateUserList(List<User> users) {
        this.users = users;
        searchResultAdapter.notifyDataSetChanged();
    }

    void appendUserList(List<User> newUsers) {
        Iterable<User> filtered = Iterables.filter(newUsers, new Predicate<User>() {
            @Override
            public boolean apply(final User newUser) {
                return Iterables.all(users, new Predicate<User>() {
                    @Override
                    public boolean apply(User user) {
                        return user.getId() != newUser.getId();
                    }
                });
            }
        });
        this.users = Lists.newLinkedList(Iterables.concat(this.users, filtered));
        searchResultAdapter.notifyDataSetChanged();
    }

    void searchUsersFromTwitter() {
        if(loading){
            return;
        }
        loading = true;
        CustomTwitterApiClient.getActiveClient().getUserService().search(query, page, new Callback<List<User>>() {
            @Override
            public void success(List<User> users, Response response) {
                updateUserList(users);
                loading = false;
                noMore = false;
            }

            @Override
            public void failure(RetrofitError error) {
                loading = false;
                Toast.makeText(SearchActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    void searchMore() {
        if(loading || noMore){
            return;
        }
        loading = true;

        CustomTwitterApiClient.getActiveClient().getUserService().search(query, page + 1, new Callback<List<User>>() {
            @Override
            public void success(List<User> newUsers, Response response) {
                int size = users.size();
                appendUserList(newUsers);
                if (size == users.size()) {
                    noMore = true;
                } else {
                    page++;
                }
                loading = false;
            }

            @Override
            public void failure(RetrofitError error) {
                loading = false;
                Toast.makeText(SearchActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    void follow(User user){
        long userId = Twitter.getSessionManager().getActiveSession().getUserId();
        CustomTwitterApiClient.getActiveClient().getCustomListService().addMember(Globals.LIST_SLUG, userId, user.getId(), new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                Toast.makeText(SearchActivity.this, "성공", Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(SearchActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
