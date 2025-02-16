document.addEventListener("DOMContentLoaded", async function () {
    const terminalHistory = document.querySelector(".history");
    const apiUrl = "http://localhost:8080/api/terminal/execute";
    let currentPrompt = "user@terminal:~ $"; // Armazena o prompt atual

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
    
});
