package com.erdidev.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class SecurityUtils {
    
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            // For development, return admin user ID (1)
            return 1L;
            // For production, you would get the actual user ID from UserPrincipal
            // UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            // return principal.getId();
        }
        throw new IllegalStateException("No authenticated user found");
    }
} 