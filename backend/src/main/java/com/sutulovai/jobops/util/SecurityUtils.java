package com.sutulovai.jobops.util;

import com.sutulovai.jobops.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UUID)) {
            throw new UnauthorizedException("Not authenticated");
        }
        return (UUID) auth.getPrincipal();
    }
}
