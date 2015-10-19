package com.realk.thekootwit.activity.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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

public class ListManagerFragment extends Fragment {
    ListView listView;
    private List<User> users = new ArrayList<User>();
    private final BaseAdapter listAdapter = new BaseAdapter() {
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
                viewHolder.profleImage = (ImageView) convertView.findViewById(R.id.user_profile_image);
                viewHolder.username = (TextView) convertView.findViewById(R.id.username);
                viewHolder.biography = (TextView) convertView.findViewById(R.id.biography);
                viewHolder.unfollowButton = (ImageButton) convertView.findViewById(R.id.btnfollow);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final User user = (User) this.getItem(position);
            Picasso.with(getActivity()).load(user.profileImageUrl).into(viewHolder.profleImage);
            viewHolder.username.setText(user.name);
            viewHolder.biography.setText(user.description);

            viewHolder.unfollowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unfollow(user);
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    /*
                    // twitter app
                    */
                    Toast.makeText(getActivity(), "트위터 앱 연결", Toast.LENGTH_LONG).show();
                }
            });
            return convertView;
        }

        class ViewHolder {
            ImageView profleImage;
            TextView username;
            TextView biography;
            ImageButton unfollowButton;
        }
    };

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
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchMembers() {
        fetchMembers(new LinkedList<User>(), -1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_manager, container, false);
        listView = (ListView) view.findViewById(R.id.followingList);
        listView.setAdapter(listAdapter);
        fetchMembers();

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

    void unfollow(final User user) {
        long userId = Twitter.getSessionManager().getActiveSession().getUserId();
        CustomTwitterApiClient.getActiveClient().getCustomListService().removeMember(Globals.LIST_SLUG, userId, user.getId(), new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                Toast.makeText(getActivity(), "성공", Toast.LENGTH_LONG).show();
                users.remove(user);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
