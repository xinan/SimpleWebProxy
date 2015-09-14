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

  public HttpRequest(InputStream clientInputStream) throws IOException, MalformedRequestException {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(clientInputStream))) {
      String line;
      String[] parts = br.readLine().split("\\s+");
      method = parts[0];
      url = parts[1];
      httpVersion = parts[2];

      headers = new HashMap<String, String>(16);
      while ((line = br.readLine()) != null) {
        parts = line.split("\\s*:\\s*");
        headers.put(parts[0], parts[1]);
      }
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
}
