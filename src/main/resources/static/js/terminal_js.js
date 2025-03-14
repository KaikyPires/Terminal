
class File {
    constructor(name) {
        this.name = name;
        this.content = "";
    }
    getName() { return this.name; }
    getContent() { return this.content; }
    setContent(content) { this.content = content; }
    setName(name) { this.name = name; }
}

class Directory {
    constructor(name, parent = null) {
        this.name = name;
        this.parent = parent;
        this.subdirectories = [];
        this.files = [];
    }
    setName(name) { this.name = name; }
    getName() { return this.name; }
    getParent() { return this.parent; }
    getSubdirectories() { return this.subdirectories; }
    getFiles() { return this.files; }
    addDirectory(dir) { this.subdirectories.push(dir); }
    addFile(file) { this.files.push(file); }
    findSubdirectory(name) {
        return this.subdirectories.find(d => d.getName() === name) || null;
    }
    findFile(name) {
        return this.files.find(f => f.getName().trim() === name.trim()) || null;
    }
}

class TerminalService {
    constructor() {
        this.root = new Directory("~");
        this.currentDirectory = this.root;
        this.commandHistory = [];
        this.permissions = {};
    }

    executeCommand(command) {
        this.commandHistory.push(command);
        if (command.startsWith("echo ")) return this.echo(command.substring(5));
        const parts = command.split(" ");
        const cmd = parts[0], arg1 = parts[1] || "", arg2 = parts[2] || "";

        switch (cmd) {
            case "mkdir": return arg1 ? this.mkdir(arg1) : "mkdir: missing operand";
            case "rmdir": return arg1 ? this.rmdir(arg1) : "rmdir: missing operand";
            case "tree": return this.printTree(this.currentDirectory, "");
            case "rename": return (arg1 && arg2) ? this.rename(arg1, arg2) : "rename: missing operands";
            case "touch": return arg1 ? this.touch(arg1) : "touch: missing operand";
            case "cat": return arg1 ? this.cat(arg1) : "cat: missing operand";
            case "rm": return arg1 ? this.rm(arg1) : "rm: missing operand";
            case "cd": return arg1 ? this.cd(arg1) : "cd: missing operand";
            case "pwd": return this.getCurrentPath();
            case "history": return this.history();
            case "help": return this.getHelpMessage();
            case "exit": this.resetTerminal(); return "exit: Terminal encerrado.";
            default: return `zsh: command not found: ${command}`;
        }
    }

    mkdir(name) {
        if (this.currentDirectory.findSubdirectory(name)) return "mkdir: O diretório já existe";
        const newDir = new Directory(name, this.currentDirectory);
        this.currentDirectory.addDirectory(newDir);
        return `mkdir: Diretório '${name}' criado com sucesso`;
    }

    rmdir(name) {
        const dir = this.currentDirectory.findSubdirectory(name);
        if (dir && dir.getFiles().length === 0 && dir.getSubdirectories().length === 0) {
            this.currentDirectory.subdirectories = this.currentDirectory.subdirectories.filter(d => d !== dir);
            return `rmdir: '${name}' removido`;
        }
        return `rmdir: Falha ao remover '${name}': não vazio ou inexistente`;
    }

    printTree(dir, prefix) {
        let result = "";
        dir.getSubdirectories().forEach((sub, i) => {
            const last = i === dir.getSubdirectories().length - 1;
            result += `${prefix}${last ? "└── " : "├── "}${sub.getName()}\n`;
            result += this.printTree(sub, prefix + (last ? "    " : "│   "));
        });
        dir.getFiles().forEach((file, i) => {
            const last = i === dir.getFiles().length - 1;
            result += `${prefix}${last ? "└── " : "├── "}${file.getName()}\n`;
        });
        return result;
    }

    rename(oldName, newName) {
        let item = this.currentDirectory.findSubdirectory(oldName) || this.currentDirectory.findFile(oldName);
        if (item) {
            item.setName(newName);
            return `rename: '${oldName}' renomeado para '${newName}'`;
        }
        return `rename: Não encontrado '${oldName}'`;
    }

    touch(name) {
        if (this.currentDirectory.findFile(name)) return "";
        this.currentDirectory.addFile(new File(name));
        return `touch: Arquivo '${name}' criado`;
    }

    echo(command) {
        return command.replace(/^\"|\"$/, "");
    }

    cat(name) {
        const file = this.currentDirectory.findFile(name);
        return file ? file.getContent() : `cat: ${name}: arquivo não encontrado`;
    }

    rm(name) {
        const file = this.currentDirectory.findFile(name);
        if (file) {
            this.currentDirectory.files = this.currentDirectory.files.filter(f => f !== file);
            return `rm: Arquivo '${name}' removido`;
        }
        const dir = this.currentDirectory.findSubdirectory(name);
        if (dir) {
            this.currentDirectory.subdirectories = this.currentDirectory.subdirectories.filter(d => d !== dir);
            return `rm: Diretório '${name}' removido`;
        }
        return `rm: '${name}' não encontrado`;
    }

    cd(path) {
        if (path === "~") return this.currentDirectory = this.root, "";
        if (path === ".." && this.currentDirectory.getParent()) {
            this.currentDirectory = this.currentDirectory.getParent();
            return "";
        }
        const dir = this.currentDirectory.findSubdirectory(path);
        if (dir) return this.currentDirectory = dir, "";
        return `cd: '${path}' não encontrado`;
    }

    getCurrentPath() {
        let path = [];
        let dir = this.currentDirectory;
        while (dir) {
            path.unshift(dir.getName());
            dir = dir.getParent();
        }
        return path.join("/").replace("/~", "~");
    }

    history() {
        return this.commandHistory.join("\n");
    }

    resetTerminal() {
        this.currentDirectory = this.root;
        this.root.subdirectories = [];
        this.root.files = [];
        this.commandHistory = [];
    }

    getHelpMessage() {
        return `Comandos disponíveis:
- mkdir [dir]
- rmdir [dir]
- touch [arquivo]
- echo [texto]
- cat [arquivo]
- rm [arquivo|dir]
- cd [dir]
- pwd
- tree
- rename [velho] [novo]
- history
- help
- exit`;
    }
}

// DOM TERMINAL - front-end
document.addEventListener("DOMContentLoaded", () => {
    const terminal = new TerminalService();
    const terminalHistory = document.querySelector(".history");
    const commandList = [
        "pwd", "mkdir", "rmdir", "tree", "rename", "touch", "cat",
        "rm", "ls", "cd", "find", "grep", "chmod", "chown", "stat",
        "du", "cp", "mv", "diff", "zip", "unzip", "history", "tail",
        "wc", "head", "help", "exit", "echo","arquivo.txt","Hello World", "pasta","teste"
    ];
    let currentPrompt = "user@terminal:~ $";
    let commandHistory = [];
    let historyIndex = -1;

    createNewPrompt();

    function focusInput() {
        const input = document.querySelector(".cmd-input");
        if (input && document.activeElement !== input) input.focus();
    }

    document.addEventListener("keydown", function (e) {
        const input = document.querySelector(".cmd-input");
        if (!input) return;

        if (e.key === "ArrowUp") {
            e.preventDefault();
            if (historyIndex > 0) {
                historyIndex--;
                input.value = commandHistory[historyIndex];
            }
        }

        if (e.key === "ArrowDown") {
            e.preventDefault();
            if (historyIndex < commandHistory.length - 1) {
                historyIndex++;
                input.value = commandHistory[historyIndex];
            } else {
                historyIndex = commandHistory.length;
                input.value = "";
            }
        }

        if (e.key === "Enter") {
            e.preventDefault();
            const command = input.value.trim();
            if (command) {
                input.disabled = true;
                commandHistory.push(command);
                historyIndex = commandHistory.length;
                processCommand(command);
            } else {
                focusInput();
            }
        }

        if (e.key === "Tab") {
            e.preventDefault();
            autocompleteCommand(input);
        }
    });

    function processCommand(command) {
        if (command === "cls") {
            terminalHistory.innerHTML = "";
            createNewPrompt();
            return;
        }

        const output = terminal.executeCommand(command);
        addToHistory(command, output);
        updatePrompt();
        createNewPrompt();
    }

    function addToHistory(command, output) {
        const container = document.createElement("div");
        container.classList.add("history-entry");

        const userPrompt = document.createElement("div");
        userPrompt.classList.add("command-line");
        userPrompt.innerHTML = `<span class="prompt">${currentPrompt}</span> ${command}`;
        container.appendChild(userPrompt);

        if (output.trim()) {
            const result = document.createElement("pre");
            result.classList.add("command-output");
            result.textContent = output;
            container.appendChild(result);
        }

        terminalHistory.appendChild(container);
        container.scrollIntoView({ behavior: "smooth" });
    }

    function createNewPrompt() {
        const existing = document.querySelector(".cmd-input");
        if (existing) existing.parentElement.remove();

        const inputLine = document.createElement("div");
        inputLine.classList.add("input-line");

        inputLine.innerHTML = `
            <span class="prompt">${currentPrompt}</span>
            <input type="text" class="cmd-input" autofocus>
        `;

        terminalHistory.appendChild(inputLine);
        setTimeout(() => document.querySelector(".cmd-input").focus(), 10);
    }

    function updatePrompt() {
        const path = terminal.getCurrentPath();
        currentPrompt = `user@terminal:${path} $`;
    }

    function autocompleteCommand(input) {
        const typed = input.value.trim().toLowerCase();
        if (!typed) return;
        const matches = commandList.filter(cmd => cmd.startsWith(typed));
        if (matches.length === 1) input.value = matches[0];
    }
});
