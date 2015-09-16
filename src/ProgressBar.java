import java.util.Arrays;

/**
 * Created by Xinan on 16/9/15.
 */
public class ProgressBar {

  final private long size;
  private long current = 0;

  public ProgressBar(String contentLength) {
    long parsed;
    try {
      parsed = Long.parseLong(contentLength);
    } catch (NumberFormatException e) {
      parsed = -1;
    }
    size = parsed;
    System.out.print(getBar());
  }

  public ProgressBar(long contentLength) {
    size = contentLength;
    System.out.print(getBar());
  }

  public void update(int amount) {
    current += amount;
    System.out.print(getBar());
  }

  private String getBar() {
    int numCols;
    try {
      numCols = Integer.parseInt(System.getenv("COLUMNS"));
    } catch (NumberFormatException e) {
      numCols = 80;
    }

    long total = size == -1 ? current : size;

    char[] equals = new char[numCols - 6];
    char[] spaces = new char[numCols - 6];
    Arrays.fill(equals, '=');
    Arrays.fill(spaces, ' ');
    int percentage = (int) (current * 100F / total);
    int numEquals = (int) (percentage / 100F * (numCols - 6));
    int numSpaces = numCols - 6 - numEquals;
    String downloaded = new String(equals, 0, numEquals);
    String remaining = new String(spaces, 0, numSpaces);
    String done = percentage == 100 ? "Done" : "";
    return String.format("|%s%s%3d%%| %s\r", downloaded, remaining, percentage, done);
  }
}
