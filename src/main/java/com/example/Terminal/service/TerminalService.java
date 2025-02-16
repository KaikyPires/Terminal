package com.example.Terminal.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        String cmd = parts[0];
        String arg1 = (parts.length > 1) ? parts[1] : "";
        String arg2 = (parts.length > 2) ? parts[2] : "";

        switch (cmd) {
            case "pwd":
                return getCurrentPath();
            case "mkdir":
                return !arg1.isEmpty() ? mkdir(arg1) : "mkdir: missing operand";
            case "rmdir":
                return !arg1.isEmpty() ? rmdir(arg1) : "rmdir: missing operand";
            case "tree":
                return printTree(currentDirectory, 0);
            case "rename":
                return (!arg1.isEmpty() && !arg2.isEmpty()) ? rename(arg1, arg2) : "rename: missing operands";
            case "touch":
                return !arg1.isEmpty() ? touch(arg1) : "touch: missing operand";
            case "cat":
                return !arg1.isEmpty() ? cat(arg1) : "cat: missing operand";
            case "rm":
                return !arg1.isEmpty() ? rm(arg1) : "rm: missing operand";
            case "ls":
                return ls(arg1.equals("-l"));
            case "cd":
                return !arg1.isEmpty() ? cd(arg1) : "cd: missing operand";
            case "find":
                return (parts.length > 2 && parts[1].equals("-name")) ? find(parts[2]) : "find: invalid syntax";
            case "grep":
                return (!arg1.isEmpty() && !arg2.isEmpty()) ? grep(arg1, arg2) : "grep: missing operands";
            case "chmod":
                return (!arg1.isEmpty() && !arg2.isEmpty()) ? chmod(arg1, arg2) : "chmod: missing operands";
            case "chown":
                return (!arg1.isEmpty() && !arg2.isEmpty()) ? chown(arg1, arg2) : "chown: missing operands";
            case "stat":
                return !arg1.isEmpty() ? stat(arg1) : "stat: missing operand";
            case "du":
                return !arg1.isEmpty() ? du(arg1) : "du: missing operand";
            case "cp":
                return (!arg1.isEmpty() && !arg2.isEmpty()) ? cp(arg1, arg2) : "cp: missing operands";
            case "mv":
                return (!arg1.isEmpty() && !arg2.isEmpty()) ? mv(arg1, arg2) : "mv: missing operands";
            case "diff":
                return (!arg1.isEmpty() && !arg2.isEmpty()) ? diff(arg1, arg2) : "diff: missing operands";
            case "zip":
                if (parts.length > 2) {
                    // Verifica se todos os arquivos estão entre aspas
                    for (int i = 2; i < parts.length; i++) {
                        if (!parts[i].startsWith("\"") || !parts[i].endsWith("\"")) {
                            return "Erro: Todos os arquivos devem estar entre aspas.";
                        }
                    }
                    return zip(Arrays.copyOfRange(parts, 1, parts.length));
                }
                return "zip: missing operand";

            case "unzip":
                return !arg1.isEmpty() ? unzip(arg1) : "unzip: missing operand";
            case "history":
                return history();
            case "tail":
                return (!arg1.isEmpty() && !arg2.isEmpty()) ? tail(arg1, Integer.parseInt(arg2))
                        : "tail: missing operands";
            case "wc":
                return !arg1.isEmpty() ? wc(arg1) : "wc: missing operand";
            case "head":
                return (!arg1.isEmpty() && !arg2.isEmpty()) ? head(arg1, Integer.parseInt(arg2))
                        : "head: missing operands";
            case "help":
                return getHelpMessage();

            case "exit":
                resetTerminal();
                return "exit: Terminal encerrado. Inicie uma nova sessão.";
            default:
                return "zsh: command not found: " + command;
        }
    }

    public String getCurrentPath() {
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
        command = command.replaceAll("^\"|\"$", ""); // Remove aspas extras

        if (command.contains(">>")) {
            String[] parts = command.split(">>", 2);
            if (parts.length < 2)
                return "echo: syntax error";
            String content = parts[0].trim();
            String fileName = parts[1].trim();

            Optional<File> file = currentDirectory.findFile(fileName);
            if (file.isPresent()) {
                file.get().setContent(file.get().getContent() + "\n" + content);
            } else {
                File newFile = new File(fileName);
                newFile.setContent(content);
                currentDirectory.addFile(newFile); // Agora adicionamos o arquivo ao diretório correto
            }

            System.out.println("DEBUG: Arquivo '" + fileName + "' criado com conteúdo: " + content + " no diretório '"
                    + currentDirectory.getName() + "'");
            return "";
        } else if (command.contains(">")) {
            String[] parts = command.split(">", 2);
            if (parts.length < 2)
                return "echo: syntax error";
            String content = parts[0].trim();
            String fileName = parts[1].trim();

            Optional<File> file = currentDirectory.findFile(fileName);
            if (file.isPresent()) {
                file.get().setContent(content);
            } else {
                File newFile = new File(fileName);
                newFile.setContent(content);
                currentDirectory.addFile(newFile); // Agora adicionamos o arquivo ao diretório correto
            }

            System.out.println("DEBUG: Arquivo '" + fileName + "' criado com conteúdo: " + content + " no diretório '"
                    + currentDirectory.getName() + "'");
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

        if (file.isPresent()) {
            String content = file.get().getContent();
            content = content.replaceAll("\"$", "");
            return content;
        }

        return "cat: " + fileName + ": No such file or directory";
    }

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

    private String ls(boolean isDetailed) {
        StringBuilder output = new StringBuilder();

        // 🔥 Obtém o formato de data para simular `ls -l`
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMM dd HH:mm");

        // 🔥 Obtém os itens e os ordena corretamente
        List<Directory> dirs = currentDirectory.getSubdirectories().stream()
                .sorted(Comparator.comparing(Directory::getName))
                .toList();

        List<File> files = currentDirectory.getFiles().stream()
                .sorted(Comparator.comparing(File::getName))
                .toList();

        if (isDetailed) {
            // 🔹 `ls -l`: Exibe informações detalhadas (permissões, dono, grupo, tamanho, data, nome)
            for (Directory dir : dirs) {
                String formattedDate = LocalDateTime.now().format(dateFormat);
                output.append(String.format("drwxr-xr-x  user  root  4096  %s  %s/\n", formattedDate, dir.getName()));
            }
            for (File file : files) {
                String permission = permissions.getOrDefault(file.getName(), "-rw-r--r--");
                int size = file.getContent().length();
                String formattedDate = LocalDateTime.now().format(dateFormat);
                output.append(String.format("%s  user  root  %4d  %s  %s\n", permission, size, formattedDate,
                        file.getName()));
            }
        } else {
            // 🔹 `ls`: Exibe os arquivos e diretórios em colunas, organizados
            List<String> items = new ArrayList<>();
            for (Directory dir : dirs)
                items.add(dir.getName() + "/");
            for (File file : files)
                items.add(file.getName());

            int colWidth = 15; // Largura da coluna
            int numCols = 4; // Número de colunas exibidas antes da quebra de linha
            int index = 0;

            for (String item : items) {
                output.append(String.format("%-" + colWidth + "s", item));
                index++;
                if (index % numCols == 0)
                    output.append("\n"); // Quebra de linha a cada 4 itens
            }
        }

        return output.toString().trim();
    }

    private String cd(String name) {
        if (name.equals("/")) {
            currentDirectory = root; // Volta para o diretório raiz (~)
            return "";
        }

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
        String result = searchRecursively(currentDirectory, name);
        return result.isEmpty() ? "find: no matches found for '" + name + "'" : result;
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

        if (file.isEmpty()) {
            return "grep: " + fileName + ": No such file or directory";
        }

        final String finalTerm = term.replaceAll("^\"|\"$", ""); // Remove aspas externas

        List<String> matchingLines = Arrays.stream(file.get().getContent().split("\n"))
                .filter(line -> line.contains(finalTerm))
                .collect(Collectors.toList());

        return matchingLines.isEmpty() ? "grep: no matches found for '" + finalTerm + "'"
                : String.join("\n", matchingLines);
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
        if (permission.equals("777"))
            return "-rwxrwxrwx";
        if (permission.equals("755"))
            return "-rwxr-xr-x";
        if (permission.equals("644"))
            return "-rw-r--r--";
        return "-rw-r--r--";
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
        return getCurrentPath();
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

    private String head(String fileName, int n) {
        Optional<File> file = currentDirectory.findFile(fileName);

        if (file.isEmpty()) {
            return "head: " + fileName + ": No such file or directory";
        }

        List<String> lines = Arrays.asList(file.get().getContent().split("\n"));
        int endIndex = Math.min(n, lines.size()); // Pegando as primeiras N linhas

        // Remover aspas extras nas linhas antes de retornar
        return lines.subList(0, endIndex).stream()
                .map(line -> line.replaceAll("^\"|\"$", "")) // Remove aspas extras
                .collect(Collectors.joining("\n"));
    }

    private String tail(String fileName, int n) {
        Optional<File> file = currentDirectory.findFile(fileName);

        if (file.isEmpty()) {
            return "tail: " + fileName + ": No such file or directory";
        }

        List<String> lines = Arrays.asList(file.get().getContent().split("\n"));
        int startIndex = Math.max(0, lines.size() - n); // Pegando as últimas N linhas

        // Remover aspas extras nas linhas antes de retornar
        return lines.subList(startIndex, lines.size()).stream()
                .map(line -> line.replaceAll("^\"|\"$", "")) // Remove aspas extras
                .collect(Collectors.joining("\n"));
    }

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
        Optional<File> file = currentDirectory.findFile(name);
        Optional<Directory> dir = currentDirectory.findSubdirectory(name);

        if (file.isPresent()) {
            return "File: " + name + "\nSize: " + file.get().getContent().length() + " bytes";
        }
        if (dir.isPresent()) {
            return "Directory: " + name + "\nSubdirectories: " + dir.get().getSubdirectories().size();
        }
        return "stat: cannot stat '" + name + "': No such file or directory";
    }

    // ✅ du: Exibe o tamanho do diretório
    private String du(String name) {
        Directory targetDirectory;

        if (name.equals(".")) {
            targetDirectory = currentDirectory; // Se for ".", usar o diretório atual
        } else {
            Optional<Directory> dir = currentDirectory.findSubdirectory(name);
            if (dir.isEmpty()) {
                return "du: cannot access '" + name + "': No such file or directory";
            }
            targetDirectory = dir.get();
        }

        int totalSize = calculateDirectorySize(targetDirectory);
        return "Directory size: " + totalSize + " bytes";
    }

    // 🔥 Função auxiliar para calcular o tamanho total do diretório e seus arquivos
    private int calculateDirectorySize(Directory dir) {
        int size = 0;

        System.out.println("DEBUG: Verificando arquivos dentro do diretório '" + dir.getName() + "'");

        for (File file : dir.getFiles()) {
            int fileSize = file.getContent().length();
            System.out.println("DEBUG: Arquivo '" + file.getName() + "' tamanho: " + fileSize + " bytes");
            size += fileSize;
        }

        for (Directory subdir : dir.getSubdirectories()) {
            size += calculateDirectorySize(subdir);
        }

        System.out.println("DEBUG: Tamanho total do diretório '" + dir.getName() + "': " + size + " bytes");
        return size;
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
        Optional<File> f1 = currentDirectory.findFile(file1);
        Optional<File> f2 = currentDirectory.findFile(file2);

        if (f1.isPresent() && f2.isPresent()) {
            List<String> lines1 = Arrays.asList(f1.get().getContent().split("\n"));
            List<String> lines2 = Arrays.asList(f2.get().getContent().split("\n"));

            StringBuilder result = new StringBuilder();
            int maxLines = Math.max(lines1.size(), lines2.size());

            for (int i = 0; i < maxLines; i++) {
                String line1 = (i < lines1.size()) ? lines1.get(i) : "";
                String line2 = (i < lines2.size()) ? lines2.get(i) : "";

                if (!line1.equals(line2)) {
                    result.append("< " + line1 + "\n> " + line2 + "\n");
                }
            }
            return result.toString().isEmpty() ? "No differences found." : result.toString();
        }
        return "diff: One or both files do not exist";
    }

    private String zip(String[] args) {
        System.out.println("DEBUG: Comando ZIP chamado com argumentos: " + Arrays.toString(args));

        if (args.length < 2)
            return "Erro: Nenhum arquivo especificado.";

        String zipName = args[0]; // O primeiro argumento deve ser o nome do ZIP
        if (!zipName.endsWith(".zip")) {
            zipName += ".zip";
        }

        // Criar um novo diretório para simular o ZIP
        Directory zipDirectory = new Directory(zipName, currentDirectory);
        System.out.println("DEBUG: Criando diretório '" + zipName + "' para simular ZIP");

        boolean hasValidFiles = false;
        List<String> arquivosParaCompactar = new ArrayList<>();

        // 🔥 Garantindo que os arquivos sejam corretamente separados e sem aspas extras
        for (int i = 1; i < args.length; i++) {
            String[] arquivosSeparados = args[i].split("\\s+"); // Divide corretamente caso args[i] esteja concatenado
            for (String file : arquivosSeparados) {
                file = file.replaceAll("^\"|\"$", "").trim(); // Remove aspas no início e no fim
                if (!file.isEmpty()) {
                    arquivosParaCompactar.add(file);
                }
            }
        }

        System.out.println("DEBUG: Arquivos extraídos corretamente: " + arquivosParaCompactar);

        // 🔹 Processamento dos arquivos para compactação
        for (String fileName : arquivosParaCompactar) {
            System.out.println(
                    "DEBUG: Buscando arquivo '" + fileName + "' no diretório '" + currentDirectory.getName() + "'");

            Optional<File> file = currentDirectory.findFile(fileName);
            if (file.isPresent()) {
                // Criar uma cópia do arquivo dentro do ZIP
                File copiedFile = new File(file.get().getName());
                copiedFile.setContent(file.get().getContent());
                zipDirectory.addFile(copiedFile);
                System.out.println("DEBUG: Arquivo '" + fileName + "' copiado para '" + zipName + "'");
                hasValidFiles = true;
            } else {
                System.out.println("Aviso: O arquivo '" + fileName + "' não existe.");
            }
        }

        if (!hasValidFiles) {
            return "Erro: Nenhum arquivo válido encontrado.";
        }

        // Adicionar o diretório ZIP ao sistema de arquivos virtual
        currentDirectory.addDirectory(zipDirectory);

        return "Arquivos compactados em '" + zipName + "'";
    }

    private String unzip(String zipName) {
        if (!zipName.endsWith(".zip")) {
            zipName += ".zip";
        }

        Optional<Directory> zipDirectory = currentDirectory.findSubdirectory(zipName);

        if (zipDirectory.isEmpty()) {
            return "unzip: cannot find '" + zipName + "'";
        }

        Directory zipFolder = zipDirectory.get();
        int extractedFiles = 0;

        for (File file : zipFolder.getFiles()) {
            // Extraindo arquivos de volta ao diretório atual
            File extractedFile = new File(file.getName());
            extractedFile.setContent(file.getContent());
            currentDirectory.addFile(extractedFile);
            extractedFiles++;
            System.out.println("DEBUG: Arquivo '" + file.getName() + "' extraído.");
        }

        // Removendo a pasta ZIP após a extração
        currentDirectory.getSubdirectories().remove(zipFolder);
        return "unzip: " + extractedFiles + " files extracted from " + zipName;
    }

    private void resetTerminal() {
        System.out.println("DEBUG: Resetando terminal...");

        // Voltar para o diretório raiz
        currentDirectory = root;

        // Limpar todos os arquivos e diretórios criados na sessão
        root.getSubdirectories().clear();
        root.getFiles().clear();

        // Limpar histórico de comandos
        commandHistory.clear();

        System.out.println("DEBUG: Terminal resetado.");
    }

    private String getHelpMessage() {
        return "Comandos disponíveis:\n"
                + "  - pwd: Exibe o caminho atual do diretório\n"
                + "  - mkdir [dir]: Cria um novo diretório\n"
                + "  - rmdir [dir]: Remove um diretório vazio\n"
                + "  - tree: Exibe a estrutura hierárquica de diretórios\n"
                + "  - rename [nome_atual] [novo_nome]: Renomeia um arquivo ou diretório\n"
                + "  - touch [arquivo]: Cria um arquivo vazio\n"
                + "  - echo [texto] > [arquivo]: Escreve texto em um arquivo\n"
                + "  - cat [arquivo]: Exibe o conteúdo de um arquivo\n"
                + "  - rm [arquivo]: Remove um arquivo ou diretório\n"
                + "  - ls: Lista arquivos e diretórios\n"
                + "  - cd [dir]: Muda para o diretório especificado\n"
                + "  - find [dir] -name [nome]: Busca arquivos por nome\n"
                + "  - grep [termo] [arquivo]: Procura por um termo dentro de um arquivo\n"
                + "  - chmod [permissão] [arquivo]: Modifica permissões de um arquivo (simulado)\n"
                + "  - chown [dono] [arquivo]: Modifica o dono de um arquivo (simulado)\n"
                + "  - stat [arquivo]: Exibe informações detalhadas sobre um arquivo\n"
                + "  - du [diretório]: Exibe o tamanho total de um diretório\n"
                + "  - cp [origem] [destino]: Copia arquivos ou diretórios\n"
                + "  - mv [origem] [destino]: Move arquivos ou diretórios\n"
                + "  - diff [arquivo1] [arquivo2]: Compara dois arquivos e exibe as diferenças\n"
                + "  - zip [arquivo.zip] [itens]: Realiza a compactação de arquivos\n"
                + "  - unzip [arquivo.zip]: Realiza a extração de um arquivo ZIP\n"
                + "  - history: Exibe o histórico de comandos digitados\n"
                + "  - exit: Encerra a sessão do terminal e reseta os dados\n";
    }

}