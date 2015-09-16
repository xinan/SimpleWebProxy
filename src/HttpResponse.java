import java.io.*;
import java.util.HashMap;

/**
 * Created by Xinan on 16/9/15.
 */
public class HttpResponse {

  private String responseCode;
  private String responseMessage;
  private HashMap<String, String> headers = new HashMap<String, String>();
  private byte[] rawHeaders;
  private BufferedInputStream in;

  public HttpResponse(InputStream serverInputStream) throws IOException, MalformedResponseException {
    try {
      in = new BufferedInputStream(serverInputStream);
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      String header = "";
      int len;

      // Reading response headers
      while (true) {
        header += (char) in.read();
        len = header.length();
        if (header.charAt(len - 1) == '\n' && header.charAt(len - 2) == '\r' &&
            header.charAt(len - 3) == '\n' && header.charAt(len - 4) == '\r') {
          break;
        }
      }

      // Process response headers
      int toIndex = header.indexOf("\r\n", 0);
      String line = "HTTP/1.0" + header.substring(8, toIndex);
      out.write(line.getBytes());
      out.write("\r\n".getBytes());
      String[] parts = line.split("\\s");
      responseCode = parts[1];
      responseMessage = parts[2];

      int fromIndex = toIndex + 2;
      while ((toIndex = header.indexOf("\r\n", fromIndex)) != -1) {
        line = header.substring(fromIndex, toIndex);
        fromIndex = toIndex + 2;
        if (line.isEmpty()) {
          out.write("\r\n".getBytes());
          break;
        }
        parts = line.split("\\s*:\\s*");
        out.write(line.getBytes());
        out.write("\r\n".getBytes());
        headers.put(parts[0], parts[1]);
      }
      rawHeaders = out.toByteArray();
    } catch (IOException e) {
      System.out.printf("Exception in HttpResponse: %s\n", e.getMessage());
      throw e;
    } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
      throw new MalformedResponseException("Invalid response from server!");
    }
  }

  public String getResponseCode() {
    return responseCode;
  }

  public String getResponseMessage() {
    return responseMessage;
  }

  public HashMap<String, String> getHeaders() {
    return headers;
  }

  public void send(OutputStream clientOutputStream) throws IOException {
    BufferedOutputStream out = new BufferedOutputStream(clientOutputStream);
    out.write(rawHeaders);
    out.flush();
    ProgressBar progressBar = new ProgressBar(headers.get("Content-Length"));
    int bytesRead;
    byte[] buffer = new byte[Constants.BUFFER_SIZE];
    while ((bytesRead = in.read(buffer)) > 0) {
      out.write(buffer, 0, bytesRead);
      out.flush();
      progressBar.update(bytesRead);
    }
  }
}
