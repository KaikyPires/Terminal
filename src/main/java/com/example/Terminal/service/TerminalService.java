package com.example.Terminal.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class TerminalService {

    private File currentDirectory = new File(System.getProperty("user.dir")); // Diretório inicial
    private final Map<String, String> permissoes = new HashMap<>(); // Simulação de permissões
    private final List<String> historicoComandos = new ArrayList<>(); // Histórico de comandos

    public String executeCommand(String command) {
        historicoComandos.add(command); // Salvar no histórico
        StringBuilder output = new StringBuilder();
        try {
            String os = System.getProperty("os.name").toLowerCase();
            boolean isWindows = os.contains("win");

            // ✅ Comando 'cd' (mudar diretório e exibir onde está)
            if (command.startsWith("cd ")) {
                String newPath = command.substring(3).trim();
                File newDirectory = new File(currentDirectory, newPath);

                if (newPath.equals("..")) {
                    if (currentDirectory.getParentFile() != null) {
                        currentDirectory = currentDirectory.getParentFile();
                        return "Diretório alterado para: " + currentDirectory.getAbsolutePath();
                    } else {
                        return "Erro: Você já está na raiz do sistema.";
                    }
                }

                if (newDirectory.exists() && newDirectory.isDirectory()) {
                    currentDirectory = newDirectory;
                    return "Diretório alterado para: " + currentDirectory.getAbsolutePath();
                } else {
                    return "Erro: Diretório não encontrado!";
                }
            }

            // ✅ Comando 'pwd' (Mostrar diretório atual)
            if (command.equals("pwd")) {
                return "Diretório atual: " + currentDirectory.getAbsolutePath();
            }

            // ✅ Comando 'ls -l' (Listar permissões de arquivos e diretórios)
            if (command.equals("ls -l")) {
                return listarArquivosDetalhado();
            }

            // ✅ Comando 'chmod' (Simulação de permissões)
            if (command.startsWith("chmod ")) {
                return simularChmod(command.substring(6).trim());
            }

            // ✅ Comando 'history' (Exibir comandos usados)
            if (command.equals("history")) {
                return String.join("\n", historicoComandos);
            }

            // ✅ Comando 'find' (Buscar arquivos por nome)
            if (command.startsWith("find ")) {
                return buscarArquivos(command.substring(5).trim());
            }

            // ✅ Comando 'diff' (Comparar arquivos)
            if (command.startsWith("diff ")) {
                return compararArquivos(command.substring(5).trim());
            }

            // ✅ Comando 'cp' (Copiar arquivos)
            if (command.startsWith("cp ")) {
                return copiarArquivo(command.substring(3).trim());
            }

            // ✅ Comando 'mv' (Mover arquivos)
            if (command.startsWith("mv ")) {
                return moverArquivo(command.substring(3).trim());
            }

            // ✅ Comando 'zip' (Simular compactação)
            if (command.startsWith("zip ")) {
                return compactarArquivos(command.substring(4).trim());
            }

            // ✅ Comando 'unzip' (Simular descompactação)
            if (command.startsWith("unzip ")) {
                return descompactarArquivo(command.substring(6).trim());
            }

            // ✅ Executar comandos reais do sistema operacional
            return executarComandoReal(command);

        } catch (Exception e) {
            return "Erro ao executar comando: " + e.getMessage();
        }
    }

    // ✅ Simulação do comando 'chmod'
    private String simularChmod(String parametros) {
        String[] parts = parametros.split("\\s+");
        if (parts.length < 2) return "Uso correto: chmod <permissao> <arquivo>";

        String permissao = parts[0];
        String nomeArquivo = parts[1];
        File file = new File(currentDirectory, nomeArquivo);

        if (!file.exists()) return "chmod: cannot access '" + nomeArquivo + "': No such file or directory\n\nExit Code: 1";

        permissoes.put(file.getAbsolutePath(), permissao);
        return "Permissões de " + nomeArquivo + " alteradas para " + permissao;
    }

    // ✅ Método para listar arquivos detalhados (permissões simuladas)
    private String listarArquivosDetalhado() {
        File[] files = currentDirectory.listFiles();
        if (files == null) return "Erro ao listar arquivos.";

        StringBuilder lista = new StringBuilder();
        lista.append("user@terminal:$ ls -l\n");

        for (File file : files) {
            String permissao = permissoes.getOrDefault(file.getAbsolutePath(), "rw-r--r--");
            lista.append(permissao).append(" ").append(file.isDirectory() ? "[DIR] " : "[FILE] ").append(file.getName()).append("\n");
        }

        return lista.toString();
    }

    // ✅ Métodos de Arquivos
    private String buscarArquivos(String nome) {
        return "Simulação: Arquivos encontrados com nome " + nome;
    }

    private String compararArquivos(String arquivos) {
        return "Simulação: Comparação entre " + arquivos;
    }

    private String copiarArquivo(String parametros) {
        return "Simulação: Arquivo copiado para " + parametros;
    }

    private String moverArquivo(String parametros) {
        return "Simulação: Arquivo movido para " + parametros;
    }

    private String compactarArquivos(String parametros) {
        return "Simulação: Arquivos compactados em " + parametros;
    }

    private String descompactarArquivo(String parametros) {
        return "Simulação: Arquivo descompactado " + parametros;
    }

    // ✅ Método para executar comandos reais do sistema operacional
    private String executarComandoReal(String command) throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");
        String[] cmdArray = isWindows ? new String[]{"cmd.exe", "/c", command} : new String[]{"/bin/bash", "-c", command};

        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
        processBuilder.directory(currentDirectory);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) output.append(line).append("\n");

        int exitCode = process.waitFor();
        output.append("\nExit Code: ").append(exitCode);
        return output.toString();
    }
}
