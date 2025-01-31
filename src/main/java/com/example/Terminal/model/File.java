package com.example.Terminal.model;

public class File {
    private String name;
    private String content;

    public File(String name) {
        this.name = name;
        this.content = "";
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public void setName(String name) {
        this.name = name;
    }
}
