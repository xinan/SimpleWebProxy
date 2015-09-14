import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by Xinan on 15/9/15.
 */
public class HttpRequest {

  final private String method;
  final private String url;
  final private String httpVersion;
  final private HashMap<String, String> headers;
  final private String rawRequest;

  public HttpRequest(InputStream clientInputStream) throws IOException, MalformedRequestException {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(clientInputStream));
      String line;
      StringBuffer sb = new StringBuffer();

      line = br.readLine();
      String[] parts = line.split("\\s+");
      method = parts[0];
      url = parts[1];
      httpVersion = parts[2].split("/")[1];
      sb.append(line).append("\r\n");

      headers = new HashMap<String, String>(16);
      while ((line = br.readLine()) != null && !line.trim().isEmpty()) {
        parts = line.split("\\s*:\\s*");
        headers.put(parts[0], parts[1]);
        if (parts[0].equals("Connection")) {
          continue; // Do not keep-alive.
        }
        sb.append(line).append("\r\n");
      }
      sb.append("\r\n"); // Indicate end of request.
      rawRequest = sb.toString();
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
    return rawRequest.getBytes();
  }

  public String toString() {
    return rawRequest;
  }
}
