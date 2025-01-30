package com.example.Terminal.controller;


import com.example.Terminal.service.TerminalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/terminal")
@CrossOrigin("*") // Permite chamadas do front-end
public class TerminalController {

    @Autowired
    private TerminalService terminalService;

    @PostMapping("/execute")
    public String executeCommand(@RequestBody String command) {
        return terminalService.executeCommand(command);
    }
}
