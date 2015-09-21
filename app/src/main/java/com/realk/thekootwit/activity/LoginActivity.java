package com.realk.thekootwit.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.realk.thekootwit.CustomTwitterApiClient;
import com.realk.thekootwit.Globals;
import com.realk.thekootwit.R;
import com.realk.thekootwit.model.CursoredUsers;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;
import retrofit.RetrofitError;
import retrofit.client.Response;

import android.content.Intent;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;


public class LoginActivity extends Activity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "HMpxJak0xkJAO7SZoD8JH4PnS";
    private static final String TWITTER_SECRET = "8On2ncVaZLw92LA99lRtO2ODVvHNfOojeNUBaP6IwTUu7icN3g";
    private TwitterLoginButton loginButton;

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void createList() {
        CustomTwitterApiClient.getActiveClient().getCustomListService().create(Globals.LIST_SLUG, new retrofit.Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
                startMainActivity();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("재시도하시겠습니까?")
                        .setMessage("리스트 생성에 실패했습니다.")
                        .setPositiveButton("재시도", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                afterLogin();
                            }
                        })
                        .setNegativeButton("종료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .show();
            }
        });
    }

    private void afterLogin() {
        long userId = Twitter.getSessionManager().getActiveSession().getUserId();
        CustomTwitterApiClient.getActiveClient().getCustomListService().members(Globals.LIST_SLUG, userId, new retrofit.Callback<CursoredUsers>() {
            @Override
            public void success(CursoredUsers cursoredUsers, Response response) {
                startMainActivity();
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.getResponse().getStatus() == 404) {
                    createList();
                } else {
                    Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_login);

        TwitterSession session = Twitter.getSessionManager().getActiveSession();

        if (session != null) {
            afterLogin();
        }

        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                afterLogin();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
}
