import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Xinan on 21/9/15.
 */
public class ResponseCache {

  private static ConcurrentHashMap<String, String> map = new ConcurrentHashMap<String, String>();

  public static void process(Socket clientSocket, OutputStream clientOutputStream) throws IOException{
    try {
      HttpRequest request = new HttpRequest(clientSocket.getInputStream());

      SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      Socket serverSocket = new Socket(request.getHost(), request.getPort());
      File fileIn = null;

      if (map.containsKey(request.toString())) {
        fileIn = new File(map.get(request.toString()));
        // Get the Last-Modified from filename and set Last-Modified header.
        Date lastModified = new Date(Long.parseLong(fileIn.getName().split("_")[0]));
        request.setIfModifiedSince(dateFormat.format(lastModified));
      }

      request.send(serverSocket.getOutputStream());
      HttpResponse response = new HttpResponse(serverSocket.getInputStream());

      if (fileIn != null) { // There is a cached file
        if (response.getResponseCode().equals("304")) { // Cache is still valid
          ProgressBar.clear();
          System.out.printf("[%s][cache] %s\n", clientSocket.getInetAddress().getCanonicalHostName(), request);
          Files.copy(fileIn.toPath(), clientOutputStream);
          serverSocket.close();
          return;
        } else { // Cache expired
          map.remove(request.toString());
          fileIn.delete();
        }
      }

      ProgressBar.clear();
      System.out.printf("[%s][fresh] %s\n", clientSocket.getInetAddress().getCanonicalHostName(), request);

      if (request.isCacheable() && response.getLastModified() != null) { // No cached file but can be cached
        // File name format: <lastModified>_<unsignedHashCode>.
        String fileName = String.format("%d_%d", dateFormat.parse(response.getLastModified()).getTime(), request.hashCode() & 0xFFFFFFFFL);
        // Caches for different hosts are stored in different directories.
        String filePath = Paths.get(Constants.CACHE_PATH, request.getHost(), fileName).toString();

        File fileOut = new File(filePath);
        Files.createDirectories(fileOut.toPath().getParent());
        FileOutputStream fileOutStream = new FileOutputStream(fileOut);

        response.cacheAndSend(fileOutStream, clientOutputStream);
        fileOutStream.close();

        map.put(request.toString(), fileOut.getPath());
      } else { // No cached file and cannot be cached, e.g. POST request cannot be cached
        response.send(clientOutputStream);
      }

      serverSocket.close();
    } catch (FileNotFoundException e) {
      System.out.printf("File Not Found: %s\n", e.getMessage());
    } catch (SocketException e) {
      System.out.printf("[%s][cancel] Abandon request\n", clientSocket.getInetAddress().getCanonicalHostName());
    } catch (SocketTimeoutException e) {
      System.out.printf("[%s][timeout] Socket read timeout, ignoring...\n", clientSocket.getInetAddress().getCanonicalHostName());
    } catch (UnknownHostException e) {
      clientSocket.getOutputStream().write("HTTP/1.0 502 Bad Gateway\r\n\r\n502 Bad Gateway".getBytes());
    } catch (MalformedRequestException | MalformedResponseException e) {
      clientSocket.getOutputStream().write(e.getMessage().getBytes());
    } catch (IOException e) {
      e.printStackTrace();
      System.out.printf("File transfer failed\n");
    } catch (ParseException e) {
      System.out.printf("Parse last modified date failed\n");
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
