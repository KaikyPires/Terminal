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
                - help: Exibe esta mensagem
                - clear: Limpa o terminal
                - cd [dir]: Muda de diretório
                - cd ..: Volta para o diretório anterior
                - dir / ls: Lista arquivos e diretórios
                - pwd: Mostra diretório atual
                - echo [texto]: Exibe texto no terminal
                - mkdir [dir]: Cria uma nova pasta
                - del [arquivo]: Deleta um arquivo (Windows)
                - rm [arquivo]: Deleta um arquivo (Linux)
                - ren [arquivo_atual] [novo_nome]: Renomeia um arquivo
                - touch [arquivo]: Cria um arquivo vazio
                - cat [arquivo]: Exibe o conteúdo de um arquivo
                - rmdir [diretorio]: Remove um diretório vazio
                - tree: Exibe a estrutura de diretórios
                - ping [host]: Testa a conexão com um site/IP
                - find [diretorio] [nome]: Busca arquivos pelo nome
                - chmod [permissão] [arquivo]: Simula alteração de permissões
                - ls -l: Lista arquivos com detalhes (permissões, tamanho)
                - cp [origem] [destino]: Copia um arquivo ou diretório
                - mv [origem] [destino]: Move um arquivo ou diretório
                - diff [arquivo1] [arquivo2]: Compara dois arquivos e exibe diferenças
                - zip [arquivo.zip] [itens]: Simula compactação de arquivos
                - unzip [arquivo.zip]: Simula descompactação de arquivos
                - stat [arquivo]: Exibe informações detalhadas de um arquivo ou diretório
                - du [diretorio]: Exibe o tamanho total de um diretório
                - history: Exibe os últimos comandos digitados
                - exit: Sai do terminal
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
