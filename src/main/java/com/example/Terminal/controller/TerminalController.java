package com.example.Terminal.controller;

import com.example.Terminal.service.TerminalService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/terminal")
public class TerminalController {
    private final TerminalService terminalService;

    public TerminalController(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    @PostMapping("/execute")
    public String executeCommand(@RequestBody String command) {
        String output = terminalService.executeCommand(command).trim();

        // 🔥 Retorna apenas a saída do comando, sem adicionar o prompt novamente
        return output.isEmpty() ? "" : output;
    }

    @GetMapping("/current-path")
    public String getCurrentPath() {
        return terminalService.getCurrentPath();
    }
}