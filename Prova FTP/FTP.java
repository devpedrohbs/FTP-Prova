import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FTP {
    private final int porta;  // A porta onde o servidor FTP vai escutar

    public FTP(int porta) {
        this.porta = porta;  
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(porta)) {  
            System.out.println("FTP na porta " + porta);  

            while (true) {
               
                Socket clientSocket = serverSocket.accept();  
                System.out.println("Novo cliente conectado: " + clientSocket.getInetAddress());  

                // Para cada novo cliente, cria uma nova thread para atender o cliente
                new Thread(new Cliente(clientSocket)).start();  
            }
        } catch (IOException e) {
            e.printStackTrace();  
        }
    }
}
