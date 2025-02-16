document.addEventListener("DOMContentLoaded", async function () {
    const terminalHistory = document.querySelector(".history");
    const apiUrl = "http://localhost:8080/api/terminal/execute";

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
});
