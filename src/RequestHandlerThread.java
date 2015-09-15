import java.io.*;
import java.net.Socket;
import java.net.SocketException;

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
      HttpRequest clientRequest = new HttpRequest(clientSocket.getInputStream());
      BufferedOutputStream clientOut = new BufferedOutputStream(clientSocket.getOutputStream(), BUFFER_SIZE);
      System.out.printf("[%s] %s\n", clientSocket.getInetAddress().getCanonicalHostName(), clientRequest);

      Socket serverSocket = new Socket(clientRequest.getHost(), 80);

      BufferedInputStream serverIn = new BufferedInputStream(serverSocket.getInputStream(), BUFFER_SIZE);
      BufferedOutputStream serverOut = new BufferedOutputStream(serverSocket.getOutputStream(), BUFFER_SIZE);
      serverOut.write(clientRequest.toByteBuffer());
      serverOut.flush();

      int bytesRead;
      byte[] buffer = new byte[BUFFER_SIZE];
      while ((bytesRead = serverIn.read(buffer)) > 0) {
        clientOut.write(buffer, 0, bytesRead);
        clientOut.flush();
      }
      System.out.printf("Done. Active Thread: %d\r", Thread.activeCount());

      serverSocket.close();
      clientSocket.close();
    } catch (SocketException e) {
      System.out.printf("Client closed connection.\n");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (MalformedRequestException e) {
      try {
        clientSocket.getOutputStream().write(e.getMessage().getBytes());
        clientSocket.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }
}
