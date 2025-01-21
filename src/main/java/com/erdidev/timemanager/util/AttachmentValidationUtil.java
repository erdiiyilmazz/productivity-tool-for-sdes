package com.erdidev.timemanager.util;

import com.erdidev.timemanager.model.AttachmentType;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class AttachmentValidationUtil {

    private AttachmentValidationUtil() {
        // Private constructor to prevent instantiation
    }

    public static void validateFilePath(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }
        Path normalizedPath = Paths.get(fileName).normalize();
        if (normalizedPath.startsWith("..") || normalizedPath.isAbsolute()) {
            throw new IllegalArgumentException("Invalid file path: possible path traversal attempt");
        }
    }

    public static void validateFileUpload(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }
        validateFilePath(fileName);
    }

    public static Set<String> getAllowedExtensions(AttachmentType type) {
        if (Objects.requireNonNull(type) == AttachmentType.FILE) {
            return Set.of("pdf", "doc", "docx", "txt", "rtf", "csv", "xls", "xlsx",
                    "jpg", "jpeg", "png", "gif");
        }
        return Collections.emptySet();
    }

    public static void validateCodeSnippet(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Code snippet content cannot be empty");
        }
    }

    public static void validateLink(String url) {
        if (!url.matches("^(http|https)://.*$")) {
            throw new IllegalArgumentException("Invalid URL format");
        }
        
        try {
            URI.create(url).toURL();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL");
        }
    }

    public static void validateExtension(String extension, AttachmentType type) {
        Set<String> allowedExtensions = getAllowedExtensions(type);
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Invalid file extension: " + extension);
        }
    }
} 