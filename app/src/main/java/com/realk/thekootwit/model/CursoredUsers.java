package com.realk.thekootwit.model;

import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

/**
 * Cursor가 포함된 User의 리스트
 */
public class CursoredUsers {
    @SerializedName("previous_cursor")
    public int previousCursor;

    @SerializedName("previous_cursor_str")
    public String previousCursorString;

    @SerializedName("next_cursor")
    public int nextCursor;

    @SerializedName("next_cursor_str")
    public String nextCursorString;

    @SerializedName("users")
    public List<User> users;

    public CursoredUsers(int previousCursor, String previousCursorString, int nextCursor, String nextCursorString, List<User> users) {
        this.previousCursor = previousCursor;
        this.previousCursorString = previousCursorString;
        this.nextCursor = nextCursor;
        this.nextCursorString = nextCursorString;
        this.users = users;
    }
}
