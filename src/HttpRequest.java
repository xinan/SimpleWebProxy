import java.io.*;
import java.util.HashMap;

/**
 * Created by Xinan on 15/9/15.
 */
public class HttpRequest {

  private String method;
  private String url;
  private HashMap<String, String> headers = new HashMap<String, String>();
  private byte[] rawHeaders;
  private BufferedInputStream in;

  public HttpRequest(InputStream clientInputStream) throws IOException, MalformedRequestException {
    try {
      in = new BufferedInputStream(clientInputStream);
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      String header = "";
      int len;

      // Reading request headers
      while (true) {
        header += (char) in.read();
        len = header.length();
        if (header.charAt(len - 1) == '\n' && header.charAt(len - 2) == '\r' &&
            header.charAt(len - 3) == '\n' && header.charAt(len - 4) == '\r') {
          break;
        }
      }

      // Process request headers
      int toIndex = header.indexOf("\r\n", 0);
      String line = header.substring(0, toIndex - 3) + "1.0";
      out.write(line.getBytes());
      out.write("\r\n".getBytes());
      String[] parts = line.split("\\s");
      method = parts[0];
      url = parts[1];

      int fromIndex = toIndex + 2;
      while ((toIndex = header.indexOf("\r\n", fromIndex)) != -1) {
        line = header.substring(fromIndex, toIndex);
        fromIndex = toIndex + 2;
        if (line.isEmpty()) {
          out.write("\r\n".getBytes());
          break;
        }
        parts = line.split("\\s*:\\s*");
        if (!parts[0].equals("Connection")) {
          out.write(line.getBytes());
          out.write("\r\n".getBytes());
          headers.put(parts[0], parts[1]);
        }
      }

      rawHeaders = out.toByteArray();
    } catch (IOException e) {
      System.out.printf("Exception in HttpRequest: %s\n", e.getMessage());
      throw e;
    } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
      throw new MalformedRequestException("Invalid request format!");
    }
  }

  public String getMethod() {
    return method;
  }

  public String getUrl() {
    return url;
  }

  public HashMap<String, String> getHeaders() {
    return headers;
  }

  public String getHost() {
    return headers.get("Host");
  }

  public String toString() {
    return String.format("%s %s", method, url);
  }

  public void send(OutputStream serverOutputStream) throws IOException, MalformedRequestException {
    BufferedOutputStream out = new BufferedOutputStream(serverOutputStream);
    out.write(rawHeaders);
    out.flush();
    if (headers.containsKey("Content-Length")) {
      int bodySize = Integer.parseInt(headers.get("Content-Length"));
      byte[] buffer = new byte[bodySize];
      int read = in.read(buffer, 0, bodySize);
      if (read != bodySize) {
        throw new MalformedRequestException("Content-Length mismatch!");
      }
      out.write(buffer);
    }
  }
}
