import java.util.Arrays;

/**
 * Created by Xinan on 16/9/15.
 */
public class ProgressBar {

  final private long size;
  private long current = 0;

  public ProgressBar(String contentLength) {
    this(Long.parseLong(contentLength));
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
    char[] equals = new char[numCols - 6];
    char[] spaces = new char[numCols - 6];
    Arrays.fill(equals, '=');
    Arrays.fill(spaces, ' ');
    int percentage = (int) (current * 100F / size);
    int numEquals = (int) (percentage / 100F * (numCols - 6));
    int numSpaces = numCols - 6 - numEquals;
    String downloaded = new String(equals, 0, numEquals);
    String remaining = new String(spaces, 0, numSpaces);
    return String.format("|%s%s%3d%%|\r", downloaded, remaining, percentage);
  }
}
