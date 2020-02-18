import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Класс отправки сообщений в потоке
 */
public class SendMsgNode implements Runnable {

    private Socket client;
    private String fileName;

    private BufferedWriter bufferedWriter;

    public SendMsgNode(Socket client, String fileName) throws IOException {
        this.client = client;
        this.fileName = fileName;

        bufferedWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
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
        //отправляем сообщение
        try {
            bufferedWriter.write("Получен новый файл: " + fileName);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
//            System.out.println("Сообщение не отправлено");
        }
    }
}
