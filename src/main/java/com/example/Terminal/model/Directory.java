package com.example.Terminal.model;

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
        System.out.println("DEBUG: Buscando arquivo '" + name + "' no diretório '" + this.getName() + "'");
        
        for (File file : files) {
            System.out.println("DEBUG: Comparando com '" + file.getName() + "'");
            if (file.getName().trim().equals(name.trim())) {
                System.out.println("DEBUG: Arquivo '" + name + "' encontrado!");
                return Optional.of(file);
            }
        }
    
        System.out.println("DEBUG: Arquivo '" + name + "' NÃO encontrado.");
        return Optional.empty();
    }
    
    
    
    public void addFile(File file) {
        files.add(file);
    }
    
}