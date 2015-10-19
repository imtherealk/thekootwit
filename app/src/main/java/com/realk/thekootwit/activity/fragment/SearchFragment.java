package com.realk.thekootwit.activity.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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


public class SearchFragment extends Fragment {
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
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
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
            Picasso.with(getActivity()).load(user.profileImageUrl).into(viewHolder.profileImage);
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
                    Toast.makeText(getActivity(), "트위터 앱 연결", Toast.LENGTH_LONG);
                }
            });
            return convertView;
        }
    };
    private String query = null;
    private int page = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchEditText = (EditText) view.findViewById(R.id.searchbox);
        searchButton = (Button) view.findViewById(R.id.btnsearch);
        searchResultListView = (ListView) view.findViewById(R.id.searchresult);

        searchResultListView.setAdapter(searchResultAdapter);

        // Add loading footer
        View loadMoreView = inflater.inflate(R.layout.view_loadmore, null);
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
        return view;
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
                Toast.makeText(getActivity(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(getActivity(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    void follow(User user){
        long userId = Twitter.getSessionManager().getActiveSession().getUserId();
        CustomTwitterApiClient.getActiveClient().getCustomListService().addMember(Globals.LIST_SLUG, userId, user.getId(), new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                Toast.makeText(getActivity(), "성공", Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
