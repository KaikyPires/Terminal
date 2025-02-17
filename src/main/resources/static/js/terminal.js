document.addEventListener("DOMContentLoaded", async function () { 
    const terminalHistory = document.querySelector(".history");
    const apiUrl = "http://localhost:8080/api/terminal/execute";
    let currentPrompt = "user@terminal:~ $";
    const commandList = [
        "pwd", "mkdir", "rmdir", "tree", "rename", "touch", "cat",
        "rm", "ls", "cd", "find", "grep", "chmod", "chown", "stat",
        "du", "cp", "mv", "diff", "zip", "unzip", "history", "tail",
        "wc", "head", "help", "exit", "echo"
    ];
    let commandHistory = [];  // Histórico de comandos
    let historyIndex = -1;  // Índice do histórico
    // 🔥 Criar o primeiro prompt assim que a página carregar
    createNewPrompt();

    function focusInput() {
        const activeInput = document.querySelector(".cmd-input");
        if (activeInput && document.activeElement !== activeInput) {
            activeInput.focus();
        }
    }

    document.addEventListener("keydown", async function (event) {
        const terminalInput = document.querySelector(".cmd-input");
        if (!terminalInput) return;

        // Permite navegar no histórico de comandos com ↑ e ↓
        if (event.key === "ArrowUp") {
            event.preventDefault();
            if (historyIndex > 0) {
                historyIndex--;
                terminalInput.value = commandHistory[historyIndex];
            }
        }

        if (event.key === "ArrowDown") {
            event.preventDefault();
            if (historyIndex < commandHistory.length - 1) {
                historyIndex++;
                terminalInput.value = commandHistory[historyIndex];
            } else {
                historyIndex = commandHistory.length;
                terminalInput.value = "";
            }
        }

        if (event.key === "Enter") {
            event.preventDefault();
            const command = terminalInput.value.trim();

            if (command !== "") {
                terminalInput.disabled = true; // Impede edição do input antigo
                commandHistory.push(command); // Salva no histórico
                historyIndex = commandHistory.length; // Atualiza índice
                processCommand(command);
            } else {
                focusInput();
            }
        }

        if (event.key === "Tab") {
            event.preventDefault();
            autocompleteCommand(terminalInput);
        }
    });

    async function processCommand(command) {
        if (command === "clear") {
            terminalHistory.innerHTML = "";
            createNewPrompt(); // Garante que o prompt aparece após limpar
            return;
        }
    
        try {
            const response = await fetch(apiUrl, {
                method: "POST",
                headers: { "Content-Type": "text/plain" },
                body: command,
            });
    
            const output = await response.text();
            addToHistory(command, output);
    
            // 🔥 Agora espera a atualização do caminho antes de criar o próximo prompt
            await updatePrompt();
            await createNewPrompt();
        } catch (error) {
            console.error("Erro ao processar comando:", error);
            addToHistory(command, "Erro ao conectar com o servidor: " + error.message);
        }
    }
    
    
    async function addToHistory(command, output) {
        const commandContainer = document.createElement("div");
        commandContainer.classList.add("history-entry");

        // 🔥 Usa o último prompt conhecido ANTES do comando ser executado
        const userPrompt = document.createElement("div");
        userPrompt.classList.add("command-line");
        userPrompt.innerHTML = `<span class="prompt">${currentPrompt}</span> ${command}`;

        commandContainer.appendChild(userPrompt);

        if (output.trim()) {
            const outputLine = document.createElement("pre");
            outputLine.classList.add("command-output");
            outputLine.textContent = output;
            commandContainer.appendChild(outputLine);
        }

        terminalHistory.appendChild(commandContainer);

        // 🔥 Criar nova linha de entrada sem duplicação
        createNewPrompt();

        commandContainer.scrollIntoView({ behavior: "smooth" });
    }

    async function createNewPrompt() {
        // 🔥 Remover qualquer input antigo antes de criar um novo
        const existingInput = document.querySelector(".cmd-input");
        if (existingInput) {
            existingInput.parentElement.remove();
        }

        const inputContainer = document.createElement("div");
        inputContainer.classList.add("input-line");

        // 🔥 Usa o último prompt conhecido
        inputContainer.innerHTML = `
            <span class="prompt">${currentPrompt}</span>
            <input type="text" class="cmd-input" autofocus>
        `;

        terminalHistory.appendChild(inputContainer);

        // 🔥 Focar no novo input
        setTimeout(() => {
            document.querySelector(".cmd-input").focus();
        }, 10);
    }

    async function updatePrompt() {
        try {
            const response = await fetch("http://localhost:8080/api/terminal/current-path");
            if (!response.ok) throw new Error("Erro ao obter caminho atual");
            const currentPath = await response.text();
            currentPrompt = `user@terminal:${currentPath} $`; // 🔥 Atualiza o caminho corretamente
        } catch (error) {
            console.error(error);
        }
    }
    
    function autocompleteCommand(inputElement) {
        const typedText = inputElement.value.trim().toLowerCase();
        if (!typedText) return;

        const matchingCommands = commandList.filter(cmd => cmd.startsWith(typedText));

        if (matchingCommands.length === 1) {
            inputElement.value = matchingCommands[0]; 
            updateSuggestion(inputElement);
        }
    }

    function updateSuggestion(inputElement) {
        const typedText = inputElement.value.trim().toLowerCase();
        const suggestionElement = inputElement.parentElement.querySelector(".autocomplete-suggestion");

        if (!typedText) {
            suggestionElement.textContent = "";
            return;
        }

        const matchingCommands = commandList.filter(cmd => cmd.startsWith(typedText));

        if (matchingCommands.length > 0) {
            const suggestionText = matchingCommands[0].substring(typedText.length);
            suggestionElement.textContent = suggestionText;
        } else {
            suggestionElement.textContent = "";
        }
    }
});
