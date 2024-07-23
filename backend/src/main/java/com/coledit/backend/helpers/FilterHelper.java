package com.coledit.backend.helpers;

import org.springframework.lang.NonNull;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class FilterHelper {

    public static String getJwString(@NonNull HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        // Extract JWT from cookies if they are present
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }

}
