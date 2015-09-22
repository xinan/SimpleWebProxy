import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Xinan on 21/9/15.
 */
public class ResponseCache {

  private static ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();

  public static void process(HttpRequest request, OutputStream clientOutputStream)
      throws MalformedRequestException, MalformedResponseException, SocketException {
    try {
      if (map.containsKey(request.toString())) {
        File fileIn = new File(map.get(request.toString()));
        Files.copy(fileIn.toPath(), clientOutputStream);
        System.out.println("From cache...");
      } else {
        Socket serverSocket = new Socket(request.getHost(), 80);
        request.send(serverSocket.getOutputStream());
        HttpResponse response = new HttpResponse(serverSocket.getInputStream());

        if (request.isCacheable()) {
          String filePath = Paths.get(Constants.CACHE_PATH, request.getHost(), request.hashCode() + "_" + response.getLastModified()).toString();
          File fileOut = new File(filePath);
          Files.createDirectories(fileOut.toPath().getParent());
          FileOutputStream fileOutStream = new FileOutputStream(fileOut);
          response.cacheAndSend(fileOutStream, clientOutputStream);
          map.put(request.toString(), fileOut.getPath());
          fileOutStream.close();
        } else {
          response.send(clientOutputStream);
        }
        serverSocket.close();
      }
    } catch (FileNotFoundException e) {
      System.out.printf("File Not Found: %s\n", e.getMessage());
    } catch (SocketException | MalformedRequestException | MalformedResponseException e) {
      throw e;
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("File transfer failed");
    }
  }

  public static void clearCache() {
    try {
      Path path = Paths.get(Constants.CACHE_PATH);
      if (!Files.exists(path)) {
        return;
      }
      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          System.out.printf("Deleting file: %s\n", file);
          Files.delete(file);
          return super.visitFile(file, attrs);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          System.out.printf("Deleting directory: %s\n", dir);
          if (exc == null) {
            Files.delete(dir);
          } else {
            throw exc;
          }
          return super.postVisitDirectory(dir, exc);
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
