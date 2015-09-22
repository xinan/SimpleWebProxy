import java.io.*;
import java.util.HashMap;

/**
 * Created by Xinan on 15/9/15.
 */
public class HttpRequest {

  private String method;
  private String url;
  private String httpVersion;
  private int port = 80;
  private HashMap<String, String> headers = new HashMap<String, String>();
  private BufferedInputStream in;

  public HttpRequest(InputStream clientInputStream) throws IOException, MalformedRequestException {
    try {
      in = new BufferedInputStream(clientInputStream);

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
      String[] parts = line.split("\\s");
      method = parts[0];
      url = parts[1];
      httpVersion = parts[2];

      int fromIndex = toIndex + 2;
      while ((toIndex = header.indexOf("\r\n", fromIndex)) != -1) {
        line = header.substring(fromIndex, toIndex);
        fromIndex = toIndex + 2;
        if (line.isEmpty()) {
          break;
        }

        parts = line.split("\\s*:\\s*", 2);
        headers.put(parts[0], parts[1]);
      }
      parts = headers.get("Host").split("\\s*:\\s*", 2);
      if (parts.length == 2) {
        port = Integer.parseInt(parts[1]);
        headers.put("Host", parts[0]);
      }
      headers.remove("Connection");
    } catch (IOException e) {
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

  public int getPort() {
    return port;
  }

  public void setIfModifiedSince(String date) {
    headers.put("If-Modified-Since", date);
  }

  public byte[] getRawHeaders() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      out.write(String.format("%s %s %s\r\n", method, url, httpVersion).getBytes());
      for (HashMap.Entry<String, String> pair : headers.entrySet()) {
        out.write(String.format("%s: %s\r\n", pair.getKey(), pair.getValue()).getBytes());
      }
      out.write("\r\n".getBytes());
    } catch (IOException e) {
      System.out.printf("Converting to raw header failed.\n");
    }
    return out.toByteArray();
  }

  public String toString() {
    return String.format("%s %s", method, url);
  }

  public int hashCode() {
    return toString().hashCode();
  }

  public boolean isCacheable() {
    return method.equals("GET") || method.equals("HEAD");
  }

  public void send(OutputStream serverOutputStream) throws IOException, MalformedRequestException {
    BufferedOutputStream out = new BufferedOutputStream(serverOutputStream);
    out.write(getRawHeaders());
    out.flush();
    if (headers.containsKey("Content-Length")) {
      int bodySize = Integer.parseInt(headers.get("Content-Length"));
      byte[] buffer = new byte[bodySize];
      int read = in.read(buffer, 0, bodySize);
      if (read != bodySize) {
        throw new MalformedRequestException("Content-Length mismatch!");
      }
      out.write(buffer);
      out.flush();
    }
  }
}
