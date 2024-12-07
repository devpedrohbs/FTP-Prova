import java.io.*;
import java.net.*;
import java.util.Random;

public class Cliente implements Runnable {
    private static final String user = "pedro";  // Usuário, mude se preciso
    private static final String pass = "12345";  // Senha, mude se quiser
    private static final String[] listaArquivos = { "file1.txt", "file2.txt", "file3.txt" };  // Arquivos

    private final Socket clienteSocket;  // O socket do cliente, para a comunicacao

    public Cliente(Socket clienteSocket) {
        this.clienteSocket = clienteSocket;  // Recebe o socket do cliente
    }

    @Override
    public void run() {
        try (
            BufferedReader entrada = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));  // Lê o comando do cliente
            PrintWriter saida = new PrintWriter(clienteSocket.getOutputStream(), true)  // Envia resposta para o cliente
        ) {
            saida.println("220 Hello! Welcome to the Simple FTP Server :)");  

            boolean usuarioAutenticado = false;  // Controla se o usuário já fez login

            while (true) {
                String comandoRecebido = entrada.readLine();  // Lê o comando do cliente
                if (comandoRecebido == null) break;  // Se o cliente fechar a conexão, a gente sai do loop
                
                String[] partesComando = comandoRecebido.split(" ", 2);  // Divide o comando e o argumento
                String comando = partesComando[0].toUpperCase();  // Sempre em maiúsculas para não ter erro
                String argumento = "";
                if (partesComando.length > 1) {
                    argumento = partesComando[1];  // Se tiver argumento, pega
                }

                // Agora a gente processa os comandos que o cliente enviar
                switch (comando) {
                    case "USER":
                        if (user.equals(argumento)) {
                            saida.println("331 Password required");  
                        } else {
                            saida.println("530 Invalid username"); 
                        }
                        break;

                    case "PASS":
                        if (pass.equals(argumento)) {
                            usuarioAutenticado = true;  // Se a senha estiver certa, autentica o usuário
                            saida.println("230 User logged in");  
                        } else {
                            saida.println("530 Login incorrect"); 
                        }
                        break;

                    case "LIST":
                        if (usuarioAutenticado) {
                            listarArquivos(saida);  // Se estiver logado, mostra a lista de arquivos
                        } else {
                            saida.println("530 You need to be authenticated to use this command.");  
                        }
                        break;

                    case "QUIT":
                        saida.println("221 Goodbye");  
                        return;  

                    default:
                        saida.println("500 Command not understood");  
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());  
            e.printStackTrace();
        } finally {
            try {
                clienteSocket.close(); //fechar a conexao
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());  
            }
        }
    }

  
    private void listarArquivos(PrintWriter saida) throws IOException {
        Random geradorPorta = new Random();
        int portaAleatoria = geradorPorta.nextInt(64512) + 1024;  // Gera uma porta aleatória para transferência de dados
        int parteAlta = portaAleatoria / 256;  
        int parteBaixa = portaAleatoria % 256;  

        try (ServerSocket socketParaDados = new ServerSocket(portaAleatoria)) {  // Cria um servidor de dados na porta aleatória
            String respostaModoPassivo = String.format(
                "227 Entering Passive Mode (%s,%d,%d)",  // Resposta do modo passivo, com o IP e a porta
                clienteSocket.getInetAddress().getHostAddress().replace('.', ','),
                parteAlta, parteBaixa
            );
            saida.println(respostaModoPassivo);

            // Aqui a gente aceita a conexão para enviar os dados
            try (Socket conexaoDados = socketParaDados.accept();
                 PrintWriter saidaDados = new PrintWriter(conexaoDados.getOutputStream(), true)) {
                saidaDados.println("### List of available files ###");  // Inicia a lista de arquivos
                for (String arquivo : listaArquivos) {
                    saidaDados.println("- " + arquivo);  // Envia o nome de cada arquivo para o cliente
                }
            }

            saida.println("226 Transfer complete");  
        }
    }
}
