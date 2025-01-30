package com.example.Terminal.service;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
@Service
public class TerminalService {

   
    private File currentDirectory = new File(System.getProperty("user.dir")); // Diretório inicial do terminal

    public String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            String os = System.getProperty("os.name").toLowerCase();
            boolean isWindows = os.contains("win");

            // Comando 'help' (lista comandos disponíveis)
            if (command.equals("help")) {
                return """
                        Comandos disponíveis:
                        - help          -> Exibe esta mensagem
                        - clear         -> Limpa o terminal
                        - cd <dir>      -> Muda de diretório
                        - cd ..         -> Volta um diretório
                        - pwd           -> Mostra o diretório atual
                        - ls            -> Lista arquivos e diretórios
                        - mkdir <dir>   -> Cria um novo diretório
                        - rm <arquivo>  -> Remove um arquivo (Linux/macOS)
                        - del <arquivo> -> Remove um arquivo (Windows)
                        - ren <atual> <novo> -> Renomeia um arquivo
                        - echo <texto>  -> Exibe texto no terminal
                        - ping <host>   -> Testa conexão com um site/IP
                        """;
            }

            // Comando 'cd' (mudar diretório)
            if (command.startsWith("cd ")) {
                String newPath = command.substring(3).trim();
                
                // Corrigir 'cd..' (sem espaço)
                if (newPath.equals("..") || newPath.equals("..\\")) {
                    currentDirectory = currentDirectory.getParentFile();
                    return "Diretório alterado para: " + (currentDirectory != null ? currentDirectory.getAbsolutePath() : "C:\\");
                }

                File newDirectory = new File(currentDirectory, newPath);

                if (newDirectory.exists() && newDirectory.isDirectory()) {
                    currentDirectory = newDirectory;
                    return "Diretório alterado para: " + currentDirectory.getAbsolutePath();
                } else {
                    return "Erro: Diretório não encontrado!";
                }
            }

            // Comando 'pwd' (mostrar diretório atual)
            if (command.equals("pwd")) {
                return currentDirectory.getAbsolutePath();
            }

            // Comando 'echo' (exibir texto)
            if (command.startsWith("echo ")) {
                return command.substring(5);
            }

            // Comando 'ls' no Windows → Convertido para 'dir'
            if (isWindows && command.equals("ls")) {
                command = "dir";
            }

            // Comando 'ls' para listar arquivos corretamente no Windows/Linux
            if (command.equals("ls") || command.equals("dir")) {
                File[] files = currentDirectory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        output.append(file.isDirectory() ? "[DIR] " : "[FILE] ");
                        output.append(file.getName()).append("\n");
                    }
                } else {
                    output.append("Erro ao listar arquivos.");
                }
                return output.toString();
            }

            // Comando 'mkdir' (criar diretório)
            if (command.startsWith("mkdir ")) {
                String dirName = command.substring(6).trim();
                File newDir = new File(currentDirectory, dirName);

                if (newDir.exists()) {
                    return "Erro: O diretório já existe!";
                }

                boolean created = newDir.mkdir();
                return created ? "Diretório criado: " + newDir.getAbsolutePath() : "Erro ao criar diretório.";
            }

            // Comando 'rm' e 'del' (remover arquivo)
            if (command.startsWith("rm ") || command.startsWith("del ")) {
                String fileName = command.substring(command.indexOf(" ") + 1).trim();
                File fileToDelete = new File(currentDirectory, fileName);

                if (!fileToDelete.exists()) {
                    return "Erro: O arquivo '" + fileName + "' não existe.";
                }

                boolean deleted = fileToDelete.delete();
                return deleted ? "Arquivo deletado: " + fileToDelete.getAbsolutePath() : "Erro ao deletar o arquivo.";
            }

            // Comando 'ping' (testar conexão)
            if (command.startsWith("ping ")) {
                command = isWindows ? "ping " + command.substring(5) : "ping -c 4 " + command.substring(5);
            }

            // Comando 'ren' (renomear arquivo)
            if (command.startsWith("ren ") || command.startsWith("rename ")) {
                String[] parts = command.split("\\s+");
                if (parts.length < 3) {
                    return "Uso correto: ren <arquivo_atual> <novo_nome>";
                }

                File oldFile = new File(currentDirectory, parts[1]);
                File newFile = new File(currentDirectory, parts[2]);

                if (!oldFile.exists()) {
                    return "Erro: O arquivo '" + parts[1] + "' não existe.";
                }

                boolean renamed = oldFile.renameTo(newFile);
                return renamed ? "Arquivo renomeado para: " + newFile.getName() : "Erro ao renomear o arquivo.";
            }

            // Configuração do processo para Windows/Linux
            String[] cmdArray;
            if (isWindows) {
                cmdArray = new String[]{"cmd.exe", "/c", command};
            } else {
                cmdArray = new String[]{"/bin/bash", "-c", command};
            }

            ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
            processBuilder.directory(currentDirectory); // Mantém o diretório da sessão
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            output.append("\nExit Code: ").append(exitCode);

        } catch (Exception e) {
            output.append("Erro ao executar comando: ").append(e.getMessage());
        }

        return output.toString();
    }
}
