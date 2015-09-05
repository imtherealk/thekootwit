package com.realk.thekootwit;

import com.realk.thekootwit.service.ListService;
import com.realk.thekootwit.service.UserService;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom twitter client
 */
public class CustomTwitterApiClient extends TwitterApiClient {
    private static Map<Session, CustomTwitterApiClient> sharedClients = new HashMap<>();

    public CustomTwitterApiClient(Session session) {
        super(session);
    }

    public UserService getUserService() {
        return this.getService(UserService.class);
    }

    public ListService getCustomListService() {
        return this.getService(ListService.class);
    }
    public static CustomTwitterApiClient getSharedClient(Session session) {
        if (!sharedClients.containsKey(session)) {
            sharedClients.put(session, new CustomTwitterApiClient(session));
        }
        return sharedClients.get(session);
    }

    public static CustomTwitterApiClient getActiveClient() {
        TwitterSession session = Twitter.getSessionManager().getActiveSession();
        return getSharedClient(session);
    }
}
