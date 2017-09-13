package au.com.williamhill.leecheswithvacuums;

import static au.com.williamhill.leecheswithvacuums.LeechesCommon.*;
import static com.obsidiandynamics.indigo.util.PropertyUtils.*;

import java.nio.*;
import java.util.*;

import au.com.williamhill.flywheel.socketx.*;
import au.com.williamhill.flywheel.socketx.undertow.*;

public final class Pump {
  private static final Properties props = new Properties(System.getProperties());
  private final int port = getOrSet(props, "lwv.port", Integer::parseInt, 8080);
  private final String path = getOrSet(props, "lwv.path", String::valueOf, "/");
  private final int minDelayMillis = getOrSet(props, "lwv.delay.min", Integer::parseInt, 0);
  private final int maxDelayMillis = getOrSet(props, "lwv.delay.max", Integer::parseInt, 0);
  private final int reportIntervalMillis = getOrSet(props, "lwv.reportInterval", Integer::parseInt, 5_000);
  private final int maxBacklog = getOrSet(props, "lwv.maxBacklog", Integer::parseInt, 100);
  private final int idleTimeoutMillis = getOrSet(props, "lwv.idleTimeout", Integer::parseInt, 600_000);
  private final int pingIntervalMillis = getOrSet(props, "lwv.pingInterval", Integer::parseInt, 60_000);
  private final String message = getOrSet(props, "lwv.message", String::valueOf, "hello");
  
  private Pump() throws Exception {
    log("Pump: starting...");
    printProps(props);
    
    final XServerConfig config = new XServerConfig() {{
      idleTimeoutMillis = Pump.this.idleTimeoutMillis;
      pingIntervalMillis = Pump.this.pingIntervalMillis;
      port = Pump.this.port;
      path = Pump.this.path;
    }};
    
    final XServer<? extends XEndpoint> server = UndertowServer.factory().create(config, new XEndpointListener<UndertowEndpoint>() {
      @Override
      public void onConnect(UndertowEndpoint endpoint) {
        log("Connected to " + endpoint);
      }

      @Override
      public void onText(UndertowEndpoint endpoint, String message) {
      }

      @Override
      public void onBinary(UndertowEndpoint endpoint, ByteBuffer message) {
      }

      @Override
      public void onPing(UndertowEndpoint endpoint, ByteBuffer data) {
        log("Ping " + endpoint);
      }

      @Override
      public void onPong(UndertowEndpoint endpoint, ByteBuffer data) {
        log("Pong " + endpoint);
      }

      @Override
      public void onDisconnect(UndertowEndpoint endpoint, int statusCode, String reason) {
        log("Disconnected from " + endpoint + " statusCode=" + statusCode + " reason=" + reason);
      }

      @Override
      public void onClose(UndertowEndpoint endpoint) {
        log("Closed " + endpoint);
      }

      @Override
      public void onError(UndertowEndpoint endpoint, Throwable cause) {
        log("Error on endpoint " + endpoint + ": " + cause);
      }
    });

    sendAndReport(server);
  }
  
  private void sendAndReport(XServer<?> server) throws InterruptedException {
    final long startTime = System.currentTimeMillis();
    long lastReportTime = startTime;
    long lastSent = 0;
    long totalSent = 0;
    for (;;) {
      int sent = 0;
      for (XEndpoint endpoint : server.getEndpointManager().getEndpoints()) {
        if (endpoint.getBacklog() > maxBacklog) continue;
        
        endpoint.send(message, null);
        sent++;
      }
      
      if (sent == 0) {
        Thread.sleep(100);
      } else {
        totalSent += sent;
        final int delayMillis = randomDelay(minDelayMillis, maxDelayMillis);
        if (delayMillis != 0) Thread.sleep(delayMillis);
      }
      
      final long now = System.currentTimeMillis();
      if (now - lastReportTime >= reportIntervalMillis) {
        final long totalElapsed = now - startTime;
        final long reportSent = totalSent - lastSent;
        final long reportElapsed = now - lastReportTime;
        
        final double averageRate = 1000d * totalSent / totalElapsed;
        final double currentRate = 1000d * reportSent / reportElapsed;
        
        log("%,d messages; rate: %,.0f msg/s average, %,.0f msg/s current; %,d live connection(s); %,d active threads",
            totalSent, averageRate, currentRate, server.getEndpointManager().getEndpoints().size(), Thread.activeCount());
        
        lastReportTime = now;
        lastSent = totalSent;
      }
    }
  }
  
  public static void main(String[] args) throws Exception {
    new Pump();
  }
}
