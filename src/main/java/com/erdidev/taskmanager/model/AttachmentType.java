package com.erdidev.taskmanager.model;

public enum AttachmentType {
    FILE("File", new String[]{"pdf", "doc", "docx", "xls", "xlsx", "txt", "zip", "rar", "png", "jpg", "jpeg"}),
    CODE_SNIPPET("Code Snippet", null),
    LINK("Link", null);

    private final String displayName;
    private final String[] allowedExtensions;

    AttachmentType(String displayName, String[] allowedExtensions) {
        this.displayName = displayName;
        this.allowedExtensions = allowedExtensions;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String[] getAllowedExtensions() {
        return allowedExtensions;
    }

    public boolean isValidExtension(String extension) {
        if (this == CODE_SNIPPET || this == LINK || allowedExtensions == null) return true;
        String lowerExt = extension.toLowerCase();
        for (String allowed : allowedExtensions) {
            if (allowed.equals(lowerExt)) return true;
        }
        return false;
    }
} 