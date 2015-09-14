import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Xinan on 14/9/15.
 */
public class RequestHandlerThread extends Thread {

  final private Socket clientSocket;
  final private int BUFFER_SIZE;

  public RequestHandlerThread(Socket clientSocket) {
    this(clientSocket, 65536);
  }

  public RequestHandlerThread(Socket clientSocket, int bufferSize) {
    this.clientSocket = clientSocket;
    BUFFER_SIZE = bufferSize;
  }

  public void run() {
    try {
      System.out.printf("Connected to client: %s\n", clientSocket.getInetAddress());
      byte[] buffer = new byte[BUFFER_SIZE];
      BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream());
      PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
      int bytesRead;
      do {
        bytesRead = in.read(buffer);
        out.print(new String(buffer, 0, bytesRead));
      } while (in.available() > 0);
      out.flush();
      in.close();
      out.close();
      clientSocket.close();
    } catch (IOException e) {
      System.out.printf("Error: %s\n", e.getMessage());
    }
  }
}
