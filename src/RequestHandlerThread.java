import java.io.*;
import java.net.Socket;

/**
 * Created by Xinan on 14/9/15.
 */
public class RequestHandlerThread extends Thread {

  final private Socket clientSocket;

  public RequestHandlerThread(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  public void run() {
    try {
      try {
        clientSocket.setSoTimeout(3000);

        ResponseCache.process(clientSocket);
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        clientSocket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
