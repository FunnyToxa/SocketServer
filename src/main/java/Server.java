import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

/**
 * Класс сервера
 */
public class Server {
    private List<ReadFileNode> fileNodes = new LinkedList<>();
    private ServerSocket serverSocket;

    //геттер для получения списка клиентов
    public List<ReadFileNode> getFileNodes(){
        return fileNodes;
    }

    private Boolean isServerWork = true;

    /**
     * Запуск сервера
     */
    public void startServer() {
        try {
            serverSocket = new ServerSocket(10000);
        } catch (IOException e) {
            System.out.println("Ошибка запуска сервера");
            return;
        }

        System.out.println("Сервер запущен");
        while (isServerWork) {
            try {
//                System.out.println("Waiting client...");
                Socket client = serverSocket.accept();
                //после получения клиента создаем нить для получения файла от клиента
                ReadFileNode rfn = new ReadFileNode(this, client);
                fileNodes.add(rfn);
                Thread thread = new Thread(rfn, client.getInetAddress().toString());
                thread.start();
            } catch (IOException e) {
                System.out.println("Ошибка при работе с клиентом");
            }
        }
    }
}
