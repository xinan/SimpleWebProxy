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

  public boolean update(int amount) {
    current += amount;
    System.out.print(getBar());
    return current == size;
  }

  private String getBar() {
    int numCols;
    try {
      numCols = Integer.parseInt(System.getenv("COLUMNS"));
    } catch (NumberFormatException e) {
      numCols = 80;
    }

    numCols -= 20; // Leave space for other text.

    char[] equals = new char[numCols];
    char[] spaces = new char[numCols];
    Arrays.fill(equals, '=');
    Arrays.fill(spaces, '-');

    int percentage;
    if (size == -1) { // If no Content-Length available, always show 100%.
      percentage = 100;
    } else {
      percentage = (int) (current * 100F / size);
    }
    int numEquals = (int) (percentage / 100F * numCols);
    int numSpaces = numCols - numEquals;
    int remainingCount = (percentage == 100) ? Thread.activeCount() - 2 : Thread.activeCount() - 1;
    String processed = new String(equals, 0, numEquals);
    String remaining = new String(spaces, 0, numSpaces);
    return String.format("|%s%s%3d%%|Remaining: %2d|\r", processed, remaining, percentage, remainingCount);
  }
}
