import java.io.*;
import java.net.Socket;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Класс для получения файла от клиента
 * реализует интерфейс Runnable для запуска в отдельном потоке
 */
public class ReadFileNode implements Runnable {

    private Socket client;
    private BufferedReader bufferedReader;
    private Server server;

    public ReadFileNode(Server server, Socket client) throws IOException {
        this.client = client;
        bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        System.out.println("client connected: " + client.getInetAddress());
        this.server = server;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            while (!client.isClosed()){
                //ждем сообщения от клиента
                String word = bufferedReader.readLine();
                if (word == null) {
                    client.close();
                    System.out.println("Клиент разорвал соединение");
                    break;
                }

                if (word.startsWith("file:")){
                    String fileName = null;
                    try {
                        fileName = Paths.get(word.replace("file:", "")).getFileName().toString();
                        //получение файла
                        getFile(fileName);
                        //отправка ответного сообщения всем клиентам
                        sendMsg(fileName);
                    } catch (InvalidPathException e){
                        System.out.println("Некорректное имя файла");
                    } catch (IOException e){
                        System.out.println("Не удалось получить файл " + fileName);
                        System.out.println(e.getMessage());
                    }
                } else {
                    //выводим полученное сообщение
                    System.out.println(word);
                }
            }
        } catch (IOException e) {
            System.out.println("Соединение с клиентом разорвано");
        }
    }

    /**
     * Отправка сообщения всем клиентам
     * перебор через итератор, чтобы удалять мертвых
     * @param msg
     */
    private void sendMsg(String msg) {
        System.out.println("Сообщение всем клиентам о получении нового файла: " + msg);
        Iterator<ReadFileNode> iterator = server.getFileNodes().iterator();
        while (iterator.hasNext()) {
            ReadFileNode node = iterator.next();
            if (!node.client.isClosed()){
                try {
                    Thread thread = new Thread(new SendMsgNode(node.client, msg), "_" + node.client.getLocalAddress().toString());
                    thread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {

                iterator.remove();
            }
        }
    }

    /**
     * метод получения файла и сохранения его в папку с указанным именем
     * @param fileName
     * @throws IOException
     */
    private void getFile(String fileName) throws IOException {
        System.out.println("Получение файла " + fileName);
        File file = new File("c:\\test\\" + fileName);
        file.createNewFile();
        byte[] bytes = new byte[8 * 1024];
        DataInputStream in = new DataInputStream(client.getInputStream());
        OutputStream out = new FileOutputStream(file);

        //сначала получаем размер файла
        long fileLength = in.readLong();
        long inFileLength = 0;
        //после получаем сам файл
        int count;
        while ((count = in.read(bytes)) != -1) {
            inFileLength += count;
//            System.out.println(count);
            out.write(bytes, 0, count);
            if (inFileLength >= fileLength) //проверяем что файл полностью пришел
                break;
        }
        System.out.println("Файл получен!");
        out.close();
//        in.close();
    }
}
