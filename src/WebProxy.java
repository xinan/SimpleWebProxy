import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * Created by Xinan on 11/9/15.
 */
public class WebProxy {
  public static void main(String[] args) {
    try {
      int port = Integer.parseInt(args[0]);
      ServerSocket socket = new ServerSocket(port);
      Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
          try {
            System.out.printf("Server is shutting down...\n");
            socket.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
      System.out.printf("Server listening on %d...\n\n", port);
      while (!socket.isClosed()) {
        (new RequestHandlerThread(socket.accept())).start();
      }
    } catch (SocketException e) {
      System.out.printf("%s\n", e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
    } catch (IndexOutOfBoundsException | NumberFormatException  e) {
      System.out.printf("Invalid argument!\n");
    }
  }
}
