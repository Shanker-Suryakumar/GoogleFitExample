package com.ltts.testgooglefit.models;

public class User {
    private String email;
    private String user_name;
    private String access_token;

    public User(String email, String user_name, String access_token) {
        this.email = email;
        this.user_name = user_name;
        this.access_token = access_token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
}
