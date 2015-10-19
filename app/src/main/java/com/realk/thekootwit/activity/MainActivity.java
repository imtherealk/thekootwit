package com.realk.thekootwit.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.realk.thekootwit.R;
import com.realk.thekootwit.activity.fragment.ListManagerFragment;
import com.realk.thekootwit.activity.fragment.SearchFragment;
import com.realk.thekootwit.activity.fragment.TimelineFragment;

public class MainActivity extends AppCompatActivity {
    private Drawer drawer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withSavedInstance(savedInstanceState)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("타임라인"),
                        new PrimaryDrawerItem().withName("사용자 검색"),
                        new PrimaryDrawerItem().withName("팔로우 관리")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        switch (position) {
                            case 0: {
                                setTitle("타임라인");
                                Fragment f = new TimelineFragment();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
                            } break;
                            case 1: {
                                setTitle("사용자 검색");
                                Fragment f = new SearchFragment();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
                            } break;
                            case 2: {
                                setTitle("팔로우 관리");
                                Fragment f = new ListManagerFragment();
                                getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
                            } break;
                        }
                        drawer.closeDrawer();
                        return true;
                    }
                })
                .build();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        drawer.setSelectionAtPosition(0, true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = drawer.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}
