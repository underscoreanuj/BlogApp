package com.example.anuj.blogapp;

/**
 * Created by anuj on 29/4/18.
 */

public class BlogContent {

    private String title;
    private String content;
    private String image_url;
    private String user_name;

    public BlogContent() {}

    public BlogContent(String title, String content, String image_url, String user_name) {
        this.title = title;
        this.content = content;
        this.image_url = image_url;
        this.user_name = user_name;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
