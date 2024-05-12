package com.example.rally;

public class GroupMembership {
    private String userId;
    private String groupId;

    public GroupMembership() { }

    public GroupMembership(String userId, String groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public String getGroupId() {
        return groupId;
    }
}
