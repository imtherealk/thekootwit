package com.realk.thekootwit.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.realk.thekootwit.model.CursoredUsers;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.models.User;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ListManagerActivity extends Activity {
    ListView listView;
    private List<User> users = new ArrayList<User>();

    private void updateMembers(List<User> members) {
        this.users = members;
        listAdapter.notifyDataSetChanged();
    }

    private void fetchMembers(final List<User> collected, long cursor) {
        long userId = Twitter.getSessionManager().getActiveSession().getUserId();
        CustomTwitterApiClient.getActiveClient().getCustomListService().members(Globals.LIST_SLUG, userId, 5000, cursor, new Callback<CursoredUsers>() {
            @Override
            public void success(CursoredUsers cursoredUsers, Response response) {
                List<User> members = Lists.newLinkedList(Iterables.concat(collected, cursoredUsers.users));

                if (cursoredUsers.nextCursor == 0) {
                    updateMembers(members);
                } else {
                    fetchMembers(members, cursoredUsers.nextCursor);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(ListManagerActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchMembers() {
        fetchMembers(new LinkedList<User>(), -1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_manager);
        listView = (ListView)findViewById(R.id.followingList);
        listView.setAdapter(listAdapter);
        fetchMembers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_manager, menu);
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

    private final BaseAdapter listAdapter = new BaseAdapter() {
        class ViewHolder {
            ImageView profleImage;
            TextView username;
            TextView biography;
            ImageButton unfollowButton;
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
                viewHolder.unfollowButton = (ImageButton) convertView.findViewById(R.id.btnfollow);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final User user = (User) this.getItem(position);
            Picasso.with(ListManagerActivity.this).load(user.profileImageUrl).into(viewHolder.profleImage);
            viewHolder.username.setText(user.name);
            viewHolder.biography.setText(user.description);

            viewHolder.unfollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    // follow
                    */
                    Toast.makeText(ListManagerActivity.this, "사용자를 언팔로우함", Toast.LENGTH_LONG).show();
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    /*
                    // twitter app
                    */
                    Toast.makeText(ListManagerActivity.this, "트위터 앱 연결", Toast.LENGTH_LONG).show();
                }
            });
            return convertView;
        }
    };
}
