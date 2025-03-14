class File {
    constructor(name) {
        this.name = name;
        this.content = "";
    }

    getName() {
        return this.name;
    }

    getContent() {
        return this.content;
    }

    setContent(content) {
        this.content = content;
    }

    setName(name) {
        this.name = name;
    }
}

class Directory {
    constructor(name, parent = null) {
        this.name = name;
        this.parent = parent;
        this.subdirectories = [];
        this.files = [];
    }

    setName(name) {
        this.name = name;
    }

    getName() {
        return this.name;
    }

    getParent() {
        return this.parent;
    }

    getSubdirectories() {
        return this.subdirectories;
    }

    getFiles() {
        return this.files;
    }

    addDirectory(directory) {
        this.subdirectories.push(directory);
    }

    findSubdirectory(name) {
        return this.subdirectories.find(dir => dir.getName() === name) || null;
    }

    findFile(name) {
        return this.files.find(file => file.getName().trim() === name.trim()) || null;
    }

    addFile(file) {
        this.files.push(file);
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

        if (command.startsWith("echo ")) {
            return this.echo(command.substring(5));
        }

        const parts = command.split(" ");
        const cmd = parts[0];
        const arg1 = parts[1] || "";
        const arg2 = parts[2] || "";

        switch (cmd) {
            case "mkdir":
                return arg1 ? this.mkdir(arg1) : "mkdir: missing operand";
            case "rmdir":
                return arg1 ? this.rmdir(arg1) : "rmdir: missing operand";
            case "tree":
                return this.printTree(this.currentDirectory, "");
            case "rename":
                return arg1 && arg2 ? this.rename(arg1, arg2) : "rename: missing operands";
            case "touch":
                return arg1 ? this.touch(arg1) : "touch: missing operand";
            case "echo":
                return this.echo(command.substring(5));
            case "cat":
                return arg1 ? this.cat(arg1) : "cat: missing operand";
            case "rm":
                return arg1 ? this.rm(arg1) : "rm: missing operand";
            case "cd":
                return arg1 ? this.cd(arg1) : "cd: missing operand";
            case "pwd":
                return this.getCurrentPath();
            case "history":
                return this.history();
            case "help":
                return this.getHelpMessage();
            case "exit":
                this.resetTerminal();
                return "exit: Terminal encerrado. Inicie uma nova sessão.";
            default:
                return `zsh: command not found: ${command}`;
        }
    }

    mkdir(name) {
        const existingDir = this.currentDirectory.findSubdirectory(name);
        if (existingDir) return "mkdir: O diretório já existe";
        const newDir = new Directory(name, this.currentDirectory);
        this.currentDirectory.addDirectory(newDir);
        return `mkdir: Diretório '${name}' criado com sucesso`;
    }

    rmdir(name) {
        const dir = this.currentDirectory.findSubdirectory(name);
        if (dir && dir.getSubdirectories().length === 0 && dir.getFiles().length === 0) {
            this.currentDirectory.subdirectories = this.currentDirectory.subdirectories.filter(d => d !== dir);
            return `rmdir: '${name}' removido`;
        }
        return `rmdir: Falha ao remover '${name}': diretório não está vazio ou não existe`;
    }

    printTree(dir, prefix) {
        let result = "";
        dir.getSubdirectories().forEach((subdir, index) => {
            const isLast = index === dir.getSubdirectories().length - 1;
            result += `${prefix}${isLast ? "└── " : "├── "}${subdir.getName()}\n`;
            result += this.printTree(subdir, prefix + (isLast ? "    " : "│   "));
        });
        dir.getFiles().forEach((file, index) => {
            const isLast = index === dir.getFiles().length - 1;
            result += `${prefix}${isLast ? "└── " : "├── "}${file.getName()}\n`;
        });
        return result;
    }

    rename(oldName, newName) {
        let item = this.currentDirectory.findSubdirectory(oldName) || this.currentDirectory.findFile(oldName);
        if (item) {
            item.setName(newName);
            return `rename: '${oldName}' renomeado para '${newName}'`;
        }
        return `rename: Não existe arquivo ou diretório '${oldName}'`;
    }

    touch(name) {
        if (this.currentDirectory.findFile(name)) return "";
        this.currentDirectory.addFile(new File(name));
        return `touch: Arquivo '${name}' criado`;
    }

    echo(command) {
        return command.replace(/^\"|\"$/, "");
    }

    cat(fileName) {
        const file = this.currentDirectory.findFile(fileName);
        return file ? file.getContent() : `cat: ${fileName}: arquivo não encontrado`;
    }

    rm(name) {
        let file = this.currentDirectory.findFile(name);
        if (file) {
            this.currentDirectory.files = this.currentDirectory.files.filter(f => f !== file);
            return `rm: Arquivo '${name}' removido.`;
        }
        let dir = this.currentDirectory.findSubdirectory(name);
        if (dir) {
            this.currentDirectory.subdirectories = this.currentDirectory.subdirectories.filter(d => d !== dir);
            return `rm: Diretório '${name}' removido.`;
        }
        return `rm: '${name}' não encontrado.`;
    }

    cd(path) {
        if (path === "~") {
            this.currentDirectory = this.root;
            return "";
        }
        if (path === ".." && this.currentDirectory.getParent()) {
            this.currentDirectory = this.currentDirectory.getParent();
            return "";
        }
        const dir = this.currentDirectory.findSubdirectory(path);
        if (dir) {
            this.currentDirectory = dir;
            return "";
        }
        return `cd: '${path}' não encontrado`;
    }

    getCurrentPath() {
        let path = [];
        let temp = this.currentDirectory;
        while (temp) {
            path.unshift(temp.getName());
            temp = temp.getParent();
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
  - pwd: Exibe o caminho atual do diretório
  - mkdir [dir]: Cria um novo diretório
  - rmdir [dir]: Remove um diretório vazio
  - tree: Exibe a estrutura hierárquica de diretórios
  - rename [nome_atual] [novo_nome]: Renomeia um arquivo ou diretório
  - touch [arquivo]: Cria um arquivo vazio
  - echo [texto]: Exibe texto
  - cat [arquivo]: Exibe o conteúdo de um arquivo
  - rm [arquivo]: Remove um arquivo ou diretório
  - cd [dir]: Muda para o diretório especificado
  - history: Exibe o histórico de comandos
  - exit: Reseta o terminal`;
    }
}

// Exemplo de uso:
const terminal = new TerminalService();
console.log(terminal.executeCommand("mkdir teste"));
console.log(terminal.executeCommand("touch arquivo.txt"));
console.log(terminal.executeCommand("ls"));
