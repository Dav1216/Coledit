package com.coledit.backend.builders;

import jakarta.servlet.http.Cookie;

/**
 * A builder class for constructing HTTP cookies with customizable properties.
 * Provides a fluent API for setting various attributes of the cookie.
 */
public class CookieBuilder {

    // Cookie attributes
    private String name;
    private String value;
    private boolean httpOnly;
    private boolean secure;
    private String path = "/";
    private int maxAge;
    private String sameSite;

    /**
     * Sets the name of the cookie.
     *
     * @param name the name of the cookie
     * @return the current instance of CookieBuilder
     */
    public CookieBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the value of the cookie.
     *
     * @param value the value of the cookie
     * @return the current instance of CookieBuilder
     */
    public CookieBuilder setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Sets the HttpOnly flag of the cookie.
     *
     * @param httpOnly true if the cookie should be HttpOnly, false otherwise
     * @return the current instance of CookieBuilder
     */
    public CookieBuilder setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    /**
     * Sets the Secure flag of the cookie.
     *
     * @param secure true if the cookie should be secure, false otherwise
     * @return the current instance of CookieBuilder
     */
    public CookieBuilder setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    /**
     * Sets the path of the cookie.
     *
     * @param path the path for the cookie
     * @return the current instance of CookieBuilder
     */
    public CookieBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Sets the maximum age of the cookie in seconds.
     *
     * @param maxAge the maximum age of the cookie
     * @return the current instance of CookieBuilder
     */
    public CookieBuilder setMaxAge(int maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    /**
     * Sets the SameSite attribute of the cookie.
     *
     * @param sameSite the SameSite attribute value
     * @return the current instance of CookieBuilder
     */
    public CookieBuilder setSameSite(String sameSite) {
        this.sameSite = sameSite;
        return this;
    }

    /**
     * Builds and returns the configured Cookie object.
     *
     * @return the constructed Cookie object
     * @throws IllegalArgumentException if the cookie name is null or empty
     */
    public Cookie build() {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Cookie name cannot be null or empty");
        }
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        if (sameSite != null && !sameSite.isEmpty()) {
            cookie.setAttribute("SameSite", sameSite);
        }
        return cookie;
    }
}
