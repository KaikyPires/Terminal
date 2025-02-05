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
        if (output.isEmpty()) {
            return terminalService.getPrompt(); // Apenas retorna o prompt se não houver saída
        }
        return output + "\n" + terminalService.getPrompt();
    }
    
}