package com.example.Terminal.service;

import org.springframework.stereotype.Service;
import java.util.*;
import com.example.Terminal.model.Directory;
import com.example.Terminal.model.File;
import java.util.stream.Collectors;

@Service
public class TerminalService {
    private final Directory root;
    private Directory currentDirectory;
    private final List<String> commandHistory = new ArrayList<>();
    private final Map<String, String> permissions = new HashMap<>();

    public TerminalService() {
        this.root = new Directory("~", null);
        this.currentDirectory = root;
    }

    public String executeCommand(String command) {
        commandHistory.add(command);

        if (command.startsWith("echo ")) {
            return echo(command.substring(5));
        }

        String[] parts = command.split(" ", 3);
        switch (parts[0]) {
            case "pwd":
                return getCurrentPath();
            case "mkdir":
                return parts.length > 1 ? mkdir(parts[1]) : "mkdir: missing operand";
            case "rmdir":
                return parts.length > 1 ? rmdir(parts[1]) : "rmdir: missing operand";
            case "tree":
                return printTree(currentDirectory, 0);
            case "rename":
                return parts.length > 2 ? rename(parts[1], parts[2]) : "rename: missing operands";
            case "touch":
                return parts.length > 1 ? touch(parts[1]) : "touch: missing operand";
            case "cat":
                return parts.length > 1 ? cat(parts[1]) : "cat: missing operand";
            case "rm":
                return parts.length > 1 ? rm(parts[1]) : "rm: missing operand";
            case "ls":
                return ls(parts.length > 1 && parts[1].equals("-l"));
            case "cd":
                return parts.length > 1 ? cd(parts[1]) : "cd: missing operand";
            case "find":
                return parts.length > 2 && parts[1].equals("-name") ? find(parts[2]) : "find: invalid syntax";
            case "grep":
                return parts.length > 2 ? grep(parts[1], parts[2]) : "grep: missing operands";
            case "chmod":
                return parts.length > 2 ? chmod(parts[1], parts[2]) : "chmod: missing operands";
            case "chown":
                return parts.length > 2 ? chown(parts[1], parts[2]) : "chown: missing operands";
            case "stat":
                return parts.length > 1 ? stat(parts[1]) : "stat: missing operand";
            case "du":
                return parts.length > 1 ? du(parts[1]) : "du: missing operand";
            case "cp":
                return parts.length > 2 ? cp(parts[1], parts[2]) : "cp: missing operands";
            case "mv":
                return parts.length > 2 ? mv(parts[1], parts[2]) : "mv: missing operands";
            case "diff":
                return parts.length > 2 ? diff(parts[1], parts[2]) : "diff: missing operands";
            case "zip":
                return parts.length > 1 ? zip(parts) : "zip: missing operand";
            case "unzip":
                return parts.length > 1 ? unzip(parts[1]) : "unzip: missing operand";
            case "history":
                return history();
            default:
                return "zsh: command not found: " + command; // Retorna erro se o comando não for encontrado
        }
    }

    // ✅ Método pwd: Retorna o caminho atual
    private String getCurrentPath() {
        StringBuilder path = new StringBuilder();
        Directory temp = currentDirectory;
        while (temp != null) {
            path.insert(0, "/" + temp.getName());
            temp = temp.getParent();
        }
        return path.toString().replaceFirst("/~", "~");
    }

    // ✅ mkdir: Criar diretórios
    private String mkdir(String name) {
        if (currentDirectory.findSubdirectory(name).isPresent()) {
            return "mkdir: cannot create directory '" + name + "': File exists";
        }
        currentDirectory.addDirectory(new Directory(name, currentDirectory));
        return "";
    }

    // ✅ rmdir: Remover diretórios vazios
    private String rmdir(String name) {
        Optional<Directory> dir = currentDirectory.findSubdirectory(name);
        if (dir.isPresent() && dir.get().getSubdirectories().isEmpty() && dir.get().getFiles().isEmpty()) {
            currentDirectory.getSubdirectories().remove(dir.get());
            return "";
        }
        return "rmdir: failed to remove '" + name + "': Directory not empty or does not exist";
    }

    // ✅ touch: Criar arquivos vazios
    private String touch(String name) {
        if (currentDirectory.findFile(name).isPresent()) {
            return ""; // Arquivo já existe
        }
        currentDirectory.addFile(new File(name));
        return "";
    }

    // ✅ echo: Adicionar ou sobrescrever texto em arquivos
    private String echo(String command) {
        command = command.replaceAll("^\"|\"$", ""); // Remove aspas no início e no final
    
        if (command.contains(">>")) {
            String[] parts = command.split(">>", 2);
            if (parts.length < 2) return "echo: syntax error";
            String content = parts[0].trim();
            String fileName = parts[1].trim();
    
            Optional<File> file = currentDirectory.findFile(fileName);
            if (file.isPresent()) {
                file.get().setContent(file.get().getContent() + "\n" + content);
            } else {
                File newFile = new File(fileName);
                newFile.setContent(content);
                currentDirectory.addFile(newFile);
            }
            return "";
        } else if (command.contains(">")) {
            String[] parts = command.split(">", 2);
            if (parts.length < 2) return "echo: syntax error";
            String content = parts[0].trim();
            String fileName = parts[1].trim();
    
            Optional<File> file = currentDirectory.findFile(fileName);
            if (file.isPresent()) {
                file.get().setContent(content);
            } else {
                File newFile = new File(fileName);
                newFile.setContent(content);
                currentDirectory.addFile(newFile);
            }
            return "";
        } else {
            return command;
        }
    }
    

    // ✅ cat: Mostrar conteúdo de arquivos
    private String cat(String fileName) {
        System.out.println("DEBUG: Current Directory -> " + currentDirectory.getName());
        System.out.println("DEBUG: Files in Current Directory -> " + currentDirectory.getFiles());
        Optional<File> file = currentDirectory.findFile(fileName);
        return file.map(File::getContent)
                .orElse("cat: " + fileName + ": No such file or directory");
    }

    // ✅ rm: Remover arquivos ou diretórios
    private String rm(String name) {
        Optional<File> file = currentDirectory.findFile(name);
        if (file.isPresent()) {
            currentDirectory.getFiles().remove(file.get());
            return "";
        }
        Optional<Directory> dir = currentDirectory.findSubdirectory(name);
        if (dir.isPresent()) {
            currentDirectory.getSubdirectories().remove(dir.get());
            return "";
        }
        return "rm: cannot remove '" + name + "': No such file or directory";
    }

    // ✅ ls: Listar arquivos e diretórios
    private String ls(boolean isDetailed) {
        StringBuilder output = new StringBuilder();
        for (Directory dir : currentDirectory.getSubdirectories()) {
            output.append(isDetailed ? "drwxr-xr-x " : "").append(dir.getName()).append("/\n");
        }
        for (File file : currentDirectory.getFiles()) {
            String permission = permissions.getOrDefault(file.getName(), "-rw-r--r--");
            output.append(isDetailed ? permission + " " : "").append(file.getName()).append("\n");
        }
        return output.toString().trim();
    }
    

    // ✅ cd: Navegar entre diretórios
    private String cd(String name) {
        if (name.equals("..")) {
            if (currentDirectory.getParent() != null) {
                currentDirectory = currentDirectory.getParent();
            }
            return ""; // Navega para o diretório pai
        }
        Optional<Directory> newDir = currentDirectory.findSubdirectory(name);
        if (newDir.isPresent()) {
            currentDirectory = newDir.get();
            return "";
        }
        return "cd: no such file or directory: " + name;
    }

    // ✅ find: Procurar arquivos e diretórios
    private String find(String name) {
        name = name.replaceAll("^\"|\"$", ""); // Remove aspas externas
        return searchRecursively(currentDirectory, name);
    }
    
    private String searchRecursively(Directory dir, String name) {
        StringBuilder result = new StringBuilder();
        for (Directory subdir : dir.getSubdirectories()) {
            if (subdir.getName().equals(name)) {
                result.append(dir.getName()).append("/").append(subdir.getName()).append("\n");
            }
            result.append(searchRecursively(subdir, name));
        }
        for (File file : dir.getFiles()) {
            if (file.getName().equals(name)) {
                result.append(dir.getName()).append("/").append(file.getName()).append("\n");
            }
        }
        return result.toString().isEmpty() ? "find: no matches found for '" + name + "'" : result.toString();
    }
    
    

    // ✅ grep: Procurar texto em arquivos
    private String grep(String term, String fileName) {
        Optional<File> file = currentDirectory.findFile(fileName);
        if (file.isPresent()) {
            String content = file.get().getContent();
            if (content.contains(term)) {
                return content.lines().filter(line -> line.contains(term)).collect(Collectors.joining("\n"));
            } else {
                return "grep: no matches found for '" + term + "'";
            }
        }
        return "grep: " + fileName + ": No such file or directory";
    }

    // ✅ chmod: Alterar permissões simuladas
    private String chmod(String permission, String name) {
        Optional<File> file = currentDirectory.findFile(name);
        if (file.isPresent()) {
            permissions.put(name, convertPermission(permission));
            return "";
        }
        return "chmod: cannot access '" + name + "': No such file or directory";
    }
    
    private String convertPermission(String permission) {
        if (permission.equals("777")) return "-rwxrwxrwx";
        if (permission.equals("755")) return "-rwxr-xr-x";
        if (permission.equals("644")) return "-rw-r--r--";
        return "-rw-r--r--"; // Padrão
    }
    

    // ✅ chown: Alterar proprietário (simulado)
    private String chown(String owner, String name) {
        Optional<File> file = currentDirectory.findFile(name);
        Optional<Directory> dir = currentDirectory.findSubdirectory(name);

        if (file.isPresent() || dir.isPresent()) {
            return ""; // Simula sucesso
        }
        return "chown: cannot access '" + name + "': No such file or directory";
    }

    // ✅ history: Exibir histórico de comandos
    private String history() {
        return String.join("\n", commandHistory);
    }

    public String getPrompt() {
        return getCurrentPath() + " $ ";
    }

    // ✅ tree: Exibe estrutura de diretórios
    private String printTree(Directory dir, int level) {
        StringBuilder result = new StringBuilder("  ".repeat(level) + dir.getName() + "/\n");
        for (Directory subdir : dir.getSubdirectories()) {
            result.append(printTree(subdir, level + 1));
        }
        for (File file : dir.getFiles()) {
            result.append("  ".repeat(level + 1)).append(file.getName()).append("\n");
        }
        return result.toString();
    }
    

    // ✅ rename: Renomeia um arquivo ou diretório
    private String rename(String oldName, String newName) {
        Optional<Directory> dir = currentDirectory.findSubdirectory(oldName);
        if (dir.isPresent()) {
            dir.get().setName(newName);
            return "";
        }
        Optional<File> file = currentDirectory.findFile(oldName);
        if (file.isPresent()) {
            file.get().setName(newName);
            return "";
        }
        return "rename: no such file or directory: " + oldName;
    }

    // ✅ head: Exibir primeiras `n` linhas de um arquivo
    private String head(String fileName, int n) {
        Optional<File> file = currentDirectory.findFile(fileName);
        return file.map(f -> Arrays.stream(f.getContent().split("\n")).limit(n).collect(Collectors.joining("\n")))
                .orElse("head: " + fileName + ": No such file or directory");
    }

    // ✅ tail: Exibir últimas `n` linhas de um arquivo
    private String tail(String fileName, int n) {
        Optional<File> file = currentDirectory.findFile(fileName);
        return file.map(f -> {
            List<String> lines = Arrays.asList(f.getContent().split("\n"));
            return lines.stream().skip(Math.max(0, lines.size() - n)).collect(Collectors.joining("\n"));
        }).orElse("tail: " + fileName + ": No such file or directory");
    }

    // ✅ wc: Conta linhas, palavras e caracteres em um arquivo
    private String wc(String fileName) {
        Optional<File> file = currentDirectory.findFile(fileName);
        return file.map(f -> {
            String content = f.getContent();
            long lines = content.lines().count();
            long words = Arrays.stream(content.split("\\s+")).count();
            long chars = content.length();
            return lines + " " + words + " " + chars + " " + fileName;
        }).orElse("wc: " + fileName + ": No such file or directory");
    }

    // ✅ stat: Exibe detalhes de um arquivo ou diretório
    private String stat(String name) {
        Optional<Directory> dir = currentDirectory.findSubdirectory(name);
        Optional<File> file = currentDirectory.findFile(name);

        if (dir.isPresent()) {
            return "Directory: " + name + " (size: " + dir.get().getSubdirectories().size() + " subdirectories)";
        }
        if (file.isPresent()) {
            return "File: " + name + " (size: " + file.get().getContent().length() + " bytes)";
        }
        return "stat: cannot stat '" + name + "': No such file or directory";
    }

    // ✅ du: Exibe o tamanho do diretório
    private String du(String name) {
        Optional<Directory> dir = currentDirectory.findSubdirectory(name);
        if (dir.isPresent()) {
            return "Directory size: " + dir.get().getSubdirectories().size() + " directories";
        }
        return "du: cannot access '" + name + "': No such file or directory";
    }

    // ✅ cp: Copia arquivos ou diretórios
    private String cp(String source, String destination) {
        Optional<Directory> dir = currentDirectory.findSubdirectory(source);
        Optional<File> file = currentDirectory.findFile(source);
    
        if (file.isPresent()) {
            File newFile = new File(destination);
            newFile.setContent(file.get().getContent());
            currentDirectory.addFile(newFile);
            return "";
        }
        if (dir.isPresent()) {
            // Verificar se o destino já contém um diretório com o mesmo nome
            if (currentDirectory.findSubdirectory(destination).isPresent()) {
                return "cp: cannot copy '" + source + "': destination already exists";
            }
    
            // Impedir a cópia do diretório dentro dele mesmo
            if (source.equals(destination)) {
                return "cp: cannot copy a directory into itself";
            }
    
            Directory originalDir = dir.get();
            Directory newDir = new Directory(destination, currentDirectory);
            for (File f : originalDir.getFiles()) {
                newDir.addFile(new File(f.getName()));
            }
            for (Directory d : originalDir.getSubdirectories()) {
                newDir.addDirectory(new Directory(d.getName(), newDir));
            }
            currentDirectory.addDirectory(newDir);
            return "";
        }
        return "cp: cannot copy '" + source + "': No such file or directory";
    }
    

        private String mv(String source, String destination) {
            Optional<Directory> dir = currentDirectory.findSubdirectory(source);
            Optional<File> file = currentDirectory.findFile(source);
        
            if (dir.isPresent()) {
                Directory targetDir = dir.get();
                targetDir.setName(destination);
                currentDirectory.getSubdirectories().remove(targetDir);
                currentDirectory.addDirectory(targetDir);
                return "";
            }
            if (file.isPresent()) {
                File targetFile = file.get();
                targetFile.setName(destination);
                return "";
            }
            return "mv: cannot move '" + source + "': No such file or directory";
        }
        
    
    // ✅ diff: Compara arquivos
    private String diff(String file1, String file2) {
        return "diff: simulated comparison between '" + file1 + "' and '" + file2 + "'";
    }

    // ✅ zip: Simula compactação de arquivos
    private String zip(String[] items) {
        return "zip: simulated compression for " + String.join(", ", Arrays.copyOfRange(items, 1, items.length));
    }

    // ✅ unzip: Simula extração de arquivos
    private String unzip(String zipFile) {
        return "unzip: simulated extraction for '" + zipFile + "'";
    }

}