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
            addToHistory(command, `Comandos disponíveis:\n
                - pwd: Exibe o caminho atual do diretório\n
                - mkdir [dir]: Cria um novo diretório\n
                - rmdir [dir]: Remove um diretório vazio\n
                - tree: Exibe a estrutura hierárquica de diretórios\n
                - rename [nome_atual] [novo_nome]: Renomeia um arquivo ou diretório\n
                - touch [arquivo]: Cria um arquivo vazio\n
                - echo [texto] > [arquivo]: Escreve texto em um arquivo\n
                - cat [arquivo]: Exibe o conteúdo de um arquivo\n
                - rm [arquivo]: Remove um arquivo ou diretório\n
                - ls: Lista arquivos e diretórios\n
                - cd [dir]: Muda para o diretório especificado\n
                - find [dir] -name [nome]: Busca arquivos por nome\n
                - grep [termo] [arquivo]: Procura por um termo dentro de um arquivo\n
                - chmod [permissão] [arquivo]: Modifica permissões de um arquivo (simulado)\n
                - chown [dono] [arquivo]: Modifica o dono de um arquivo (simulado)\n
                - stat [arquivo]: Exibe informações detalhadas sobre um arquivo\n
                - du [diretório]: Exibe o tamanho total de um diretório\n
                - cp [origem] [destino]: Copia arquivos ou diretórios\n
                - mv [origem] [destino]: Move arquivos ou diretórios\n
                - diff [arquivo1] [arquivo2]: Compara dois arquivos e exibe as diferenças\n
                - zip [arquivo.zip] [itens]: Simula a compactação de arquivos\n
                - unzip [arquivo.zip]: Simula a extração de um arquivo ZIP\n
                - history: Exibe o histórico de comandos digitados\n
            `);
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

        // 🔥 Scroll automático para o último comando
        commandContainer.scrollIntoView({ behavior: "smooth" });
    }

    focusInput();
});
