package com.erdidev.timemanager.model;

public enum AttachmentType {
    FILE("File"),
    CODE_SNIPPET("Code Snippet"),
    LINK("Link");

    private final String displayName;

    AttachmentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 