package com.realk.thekootwit.activity;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.realk.thekootwit.CustomTwitterApiClient;
import com.realk.thekootwit.R;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class SearchActivity extends Activity {
    CustomTwitterApiClient client;
    private String query = null;
    private int page = 1;
    EditText searchEditText;
    Button searchButton;
    ListView searchResultListView;
    List<User> users = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        client = CustomTwitterApiClient.getActiveClient();
        searchEditText = (EditText) findViewById(R.id.searchbox);
        searchButton = (Button) findViewById(R.id.btnsearch);
        searchResultListView = (ListView) findViewById(R.id.searchresult);


        searchResultListView.setAdapter(searchResultAdapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query = searchEditText.getText().toString();
                searchUsersFromTwitter();
            }
        });

    }

    searchResultListAdapter searchResultAdapter = new searchResultListAdapter(this, R.layout.listview_item_search, users);

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
    }

    void searchUsersFromTwitter() {
        client.getUserService().search(query, page, new Callback<List<User>>() {
            @Override
            public void success(List<User> users, Response response) {
                updateUserList(users);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(SearchActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG);
            }
        });
    }

    class searchResultListAdapter extends BaseAdapter {
        LayoutInflater inflater;
        Context context;
        int layout;
        List<User> users;
        public searchResultListAdapter(Context context, int layout, List<User> users) {
            this.context = context;
            this.layout = layout;
            this.users = users;
            inflater = LayoutInflater.from(context);
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
                viewHolder.profleImage = (ImageView) convertView.findViewById(R.id.user_profile_image);
                viewHolder.username = (TextView) convertView.findViewById(R.id.username);
                viewHolder.biography = (TextView) convertView.findViewById(R.id.biography);
                viewHolder.followButton = (Button) convertView.findViewById(R.id.btnfollow);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final User user = (User) this.getItem(position);
            viewHolder.profleImage.setImageURI(Uri.parse(user.profileImageUrl));
            viewHolder.username.setText(user.name);
            viewHolder.biography.setText(user.description);

            viewHolder.followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Button followButton = (Button) v;
                    /*
                    // follow
                    */
                    Toast.makeText(SearchActivity.this, "사용자를 팔로우함", Toast.LENGTH_LONG);
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
    }
    private static class ViewHolder {
        ImageView profleImage;
        TextView username;
        TextView biography;
        Button followButton;
    }
}
