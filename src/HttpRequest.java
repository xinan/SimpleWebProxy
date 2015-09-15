import java.io.*;
import java.util.HashMap;

/**
 * Created by Xinan on 15/9/15.
 */
public class HttpRequest {

  private String method;
  private String url;
  private String httpVersion;
  private HashMap<String, String> headers;
  private byte[] rawRequest;

  public HttpRequest(InputStream clientInputStream) throws IOException, MalformedRequestException {
    try {
      BufferedInputStream in = new BufferedInputStream(clientInputStream);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      headers = new HashMap<String, String>();

      StringBuilder line = new StringBuilder();
      int b;
      boolean isFirstLine = true;
      String[] parts;
      while ((b = in.read()) > 0) {
        line.append((char) b);
        if (line.length() >= 2 && line.substring(line.length() - 2).equals("\r\n")) {
          if (line.length() == 2) {
            out.write(line.toString().getBytes());
            break;
          } else if (isFirstLine) {
            parts = line.toString().trim().split("\\s");
            method = parts[0];
            url = parts[1];
            httpVersion = parts[2].split("/")[1];
            isFirstLine = false;
          } else {
            parts = line.toString().trim().split("\\s*:\\s*");
            if (parts[0].equals("Connection")) {
              line.setLength(0);
              continue;
            }
            headers.put(parts[0], parts[1]);
          }
          out.write(line.toString().getBytes());
          line.setLength(0);
        }
      }

      if (headers.containsKey("Content-Length")) {
        int bodySize = Integer.parseInt(headers.get("Content-Length"));
        byte[] buffer = new byte[bodySize];
        int read = in.read(buffer, 0, bodySize);
        if (read != bodySize) {
          throw new MalformedRequestException("Content-Length mismatch!");
        }
        out.write(buffer);
      }

      rawRequest = out.toByteArray();
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

  public String getHttpVersion() {
    return httpVersion;
  }

  public HashMap<String, String> getHeaders() {
    return headers;
  }

  public String getHost() {
    return headers.get("Host");
  }

  public byte[] toByteBuffer() {
    return rawRequest;
  }

  public String toString() {
    return String.format("%s %s", method, url);
  }
}
