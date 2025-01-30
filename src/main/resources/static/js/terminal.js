document.addEventListener("DOMContentLoaded", function () {
    const terminalInput = document.querySelector(".cmd-input");
    const terminalHistory = document.querySelector(".history");
    const apiUrl = "http://localhost:8080/api/terminal/execute";

    let commandHistory = [];
    let historyIndex = -1;

    function focusInput() {
        if (document.activeElement !== terminalInput) {
            terminalInput.focus();
        }
    }

    terminalInput.addEventListener("keydown", async function (event) {
        if (event.key === "Enter") {
            event.preventDefault();
            const command = terminalInput.value.trim();

            if (command !== "") {
                commandHistory.push(command);
                historyIndex = commandHistory.length;
                processCommand(command);
            }

            terminalInput.value = "";
            focusInput();
        }

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
    });

    async function processCommand(command) {
        if (command === "clear") {
            terminalHistory.innerHTML = "";
            return;
        }

        if (command === "help") {
            addToHistory(command,   "Comandos disponíveis:\n" +
                "- help: Exibe esta mensagem\n" +
                "- clear: Limpa o terminal\n" +
                "- cd [dir]: Muda de diretório\n" +
                "- dir: Lista arquivos\n" +
                "- pwd: Mostra diretório atual\n" +
                "- echo [texto]: Exibe texto\n" +
                "- mkdir [dir]: Cria uma nova pasta\n" +
                "- del [arquivo]: Deleta um arquivo (Windows)\n" +
                "- ren [arquivo_atual] [novo_nome]: Renomeia um arquivo\n" +
                "- ping [host]: Testa a conexão com um site/IP"

        );
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
            addToHistory(command, "Erro ao conectar com o servidor.");
        }
    }

    function addToHistory(command, output) {
        const terminalContainer = document.querySelector(".terminal");

        const commandContainer = document.createElement("div");
        commandContainer.classList.add("history-entry");

        // Exibir usuário e diretório no prompt
        const userPrompt = `<span class="prompt">user@terminal:$</span> ${command}`;

        if (command) {
            const commandLine = document.createElement("div");
            commandLine.classList.add("command-line");
            commandLine.innerHTML = userPrompt;
            commandContainer.appendChild(commandLine);
        }

        if (output) {
            const outputLine = document.createElement("pre");
            outputLine.classList.add("command-output");
            outputLine.textContent = output;
            commandContainer.appendChild(outputLine);
        }

        terminalHistory.appendChild(commandContainer);

        // 🔥 Scroll automático para o final do terminal
        setTimeout(() => {
            terminalContainer.scrollTop = terminalContainer.scrollHeight;
        }, 50);
    }

    focusInput();
});
