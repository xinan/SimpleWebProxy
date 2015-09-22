import java.io.*;
import java.util.HashMap;

/**
 * Created by Xinan on 16/9/15.
 */
public class HttpResponse {

  private String httpVersion;
  private String responseCode;
  private String responseMessage;
  private HashMap<String, String> headers = new HashMap<String, String>();
  private BufferedInputStream in;

  public HttpResponse(InputStream serverInputStream) throws IOException, MalformedResponseException {
    try {
      in = new BufferedInputStream(serverInputStream);

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
      String[] parts = line.split("\\s");
      httpVersion = parts[0];
      responseCode = parts[1];
      responseMessage = parts[2];

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
      headers.put("Connection", "close");
    } catch (IOException e) {
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

  public String getLastModified() {
    return headers.get("Last-Modified");
  }

  public byte[] getRawHeaders() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      out.write(String.format("%s %s %s\r\n", httpVersion, responseCode, responseMessage).getBytes());
      for (HashMap.Entry<String, String> pair : headers.entrySet()) {
        out.write(String.format("%s: %s\r\n", pair.getKey(), pair.getValue()).getBytes());
      }
      out.write("\r\n".getBytes());
    } catch (IOException e) {
      System.out.printf("Converting to raw header failed.\n");
    }
    return out.toByteArray();
  }

  public HashMap<String, String> getHeaders() {
    return headers;
  }

  public void send(OutputStream clientOutputStream) throws IOException {
    BufferedOutputStream out = new BufferedOutputStream(clientOutputStream);
    out.write(getRawHeaders());
    out.flush();
    ProgressBar progressBar = new ProgressBar(headers.get("Content-Length"));
    int bytesRead;
    byte[] buffer = new byte[Constants.BUFFER_SIZE];
    while ((bytesRead = in.read(buffer)) > 0) {
      out.write(buffer, 0, bytesRead);
      out.flush();
      if (progressBar.update(bytesRead)) { // ProgressBar would return true if downloaded == Content-Length
        break;
      }
    }
  }

  public void cacheAndSend(OutputStream fileOutputStream, OutputStream clientOutputStream) throws IOException {
    BufferedOutputStream clientOut = new BufferedOutputStream(clientOutputStream);
    BufferedOutputStream fileOut = new BufferedOutputStream(fileOutputStream);

    byte[] rawHeaders = getRawHeaders();
    clientOut.write(rawHeaders);
    clientOut.flush();
    fileOut.write(rawHeaders);

    ProgressBar progressBar = new ProgressBar(headers.get("Content-Length"));
    int bytesRead;
    byte[] buffer = new byte[Constants.BUFFER_SIZE];
    while ((bytesRead = in.read(buffer)) > 0) {
      clientOut.write(buffer, 0, bytesRead);
      clientOut.flush();
      fileOut.write(buffer, 0, bytesRead);
      if (progressBar.update(bytesRead)) { // ProgressBar would return true if downloaded == Content-Length
        break;
      }
    }
    fileOut.flush();
  }
}
