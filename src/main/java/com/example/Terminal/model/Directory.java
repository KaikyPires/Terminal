package com.example.Terminal.model;

import java.util.*;

import java.util.*;


public class Directory {
    private String name;
    


    private Directory parent;
    private List<Directory> subdirectories;
    private List<File> files;

    public Directory(String name, Directory parent) {
        this.name = name;
        this.parent = parent;
        this.subdirectories = new ArrayList<>();
        this.files = new ArrayList<>();
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public Directory getParent() {
        return parent;
    }

    public List<Directory> getSubdirectories() {
        return subdirectories;
    }

    public List<File> getFiles() {
        return files;
    }
    

    public void addDirectory(Directory directory) {
        subdirectories.add(directory);
    }

   
    public Optional<Directory> findSubdirectory(String name) {
        return subdirectories.stream().filter(dir -> dir.getName().equals(name)).findFirst();
    }

    public Optional<File> findFile(String name) {
        return files.stream().filter(file -> file.getName().equals(name)).findFirst();
    }
    
    
    public void addFile(File file) {
        files.add(file);
    }
    
}