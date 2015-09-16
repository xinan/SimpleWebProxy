import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

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
        HttpRequest clientRequest = new HttpRequest(clientSocket.getInputStream());
        System.out.printf("[%s] %s\n", clientSocket.getInetAddress().getCanonicalHostName(), clientRequest);

        Socket serverSocket = new Socket(clientRequest.getHost(), 80);
        clientRequest.send(serverSocket.getOutputStream());

        HttpResponse serverResponse = new HttpResponse(serverSocket.getInputStream());
        serverResponse.send(clientSocket.getOutputStream());

        System.out.printf("Done. Active Thread: %d\r", Thread.activeCount());

        serverSocket.close();
      } catch (SocketException e) {
        System.out.printf("Client closed connection.\n");
      } catch (UnknownHostException e) {
        clientSocket.getOutputStream().write("HTTP/1.0 502 Bad Gateway\r\n\r\n".getBytes());
      } catch (IOException e) {
        e.printStackTrace();
      } catch (MalformedRequestException | MalformedResponseException e) {
        clientSocket.getOutputStream().write(e.getMessage().getBytes());
      } finally {
        clientSocket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
