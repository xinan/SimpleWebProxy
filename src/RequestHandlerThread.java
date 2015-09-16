import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

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
      try {
        HttpRequest clientRequest = new HttpRequest(clientSocket.getInputStream());
        System.out.printf("[%s] %s\n", clientSocket.getInetAddress().getCanonicalHostName(), clientRequest);

        Socket serverSocket = new Socket(clientRequest.getHost(), 80);

        BufferedOutputStream serverOut = new BufferedOutputStream(serverSocket.getOutputStream(), BUFFER_SIZE);
        serverOut.write(clientRequest.toByteBuffer());
        serverOut.flush();

        HttpResponse serverResponse = new HttpResponse(serverSocket.getInputStream());
        serverResponse.send(clientSocket.getOutputStream());
        System.out.printf("Done. Active Thread: %d\r", Thread.activeCount());

        serverSocket.close();
        clientSocket.close();
      } catch (SocketException e) {
        System.out.printf("Client closed connection.\n");
      } catch (UnknownHostException e) {
        clientSocket.getOutputStream().write("HTTP/1.0 502 Bad Gateway\r\n\r\n".getBytes());
        clientSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (MalformedRequestException e) {
        clientSocket.getOutputStream().write(e.getMessage().getBytes());
        clientSocket.close();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
