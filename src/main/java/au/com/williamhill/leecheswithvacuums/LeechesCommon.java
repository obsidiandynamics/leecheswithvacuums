package au.com.williamhill.leecheswithvacuums;

import static com.obsidiandynamics.indigo.util.PropertyUtils.*;

import java.util.*;

final class LeechesCommon {
  private LeechesCommon() {}
  
  static void log(String format, Object ... args) {
    System.out.format("[" + new Date() + "] " + format + "\n", args);
  }
  
  static void printProps(Properties props) {
    final Map<Object, Object> sortedProps = new TreeMap<>();
    filter("lwv.", props).entrySet().stream().forEach(e -> sortedProps.put(e.getKey(), e.getValue()));
    sortedProps.entrySet().stream()
    .map(e -> String.format("%-20s: %s", e.getKey(), e.getValue())).forEach(LeechesCommon::log);
  }
  
  static int randomDelay(int minDelayMillis, int maxDelayMillis) {
    final int range = maxDelayMillis - minDelayMillis;
    return (int) (Math.random() * range + minDelayMillis);
  }
}
