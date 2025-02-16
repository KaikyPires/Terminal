document.addEventListener("DOMContentLoaded", async function () {
    const terminalHistory = document.querySelector(".history");
    const apiUrl = "http://localhost:8080/api/terminal/execute";
    const commandList = [
        "pwd", "mkdir", "rmdir", "tree", "rename", "touch", "cat",
        "rm", "ls", "cd", "find", "grep", "chmod", "chown", "stat",
        "du", "cp", "mv", "diff", "zip", "unzip", "history", "tail",
        "wc", "head", "help", "exit","echo"
    ];
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

        if (event.key === "Enter") {
            event.preventDefault();
            const command = terminalInput.value.trim();

            if (command !== "") {
                terminalInput.disabled = true; // Impede edição do input antigo
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
        } catch (error) {
            console.error("Erro ao processar comando:", error);
            addToHistory(command, "Erro ao conectar com o servidor: " + error.message);
        }
    }

    function addToHistory(command, output) {
        const commandContainer = document.createElement("div");
        commandContainer.classList.add("history-entry");

        // Criar o prompt com o comando digitado
        const userPrompt = document.createElement("div");
        userPrompt.classList.add("command-line");
        userPrompt.innerHTML = `<span class="prompt">${getCurrentPrompt()}</span> ${command}`;

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

    function createNewPrompt() {
        // 🔥 Remover qualquer input antigo antes de criar um novo
        const existingInput = document.querySelector(".cmd-input");
        if (existingInput) {
            existingInput.parentElement.remove();
        }

        const inputContainer = document.createElement("div");
        inputContainer.classList.add("input-line");

        inputContainer.innerHTML = `
            <span class="prompt">${getCurrentPrompt()}</span>
            <input type="text" class="cmd-input" autofocus>
        `;

        terminalHistory.appendChild(inputContainer);

        // 🔥 Focar no novo input
        setTimeout(() => {
            document.querySelector(".cmd-input").focus();
        }, 10);
    }

    function getCurrentPrompt() {
        return "user@terminal:~$"; // Ajuste conforme necessário
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
