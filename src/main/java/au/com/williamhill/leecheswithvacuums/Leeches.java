package au.com.williamhill.leecheswithvacuums;

import static au.com.williamhill.leecheswithvacuums.LeechesCommon.*;
import static com.obsidiandynamics.indigo.util.PropertyUtils.*;

import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import au.com.williamhill.flywheel.socketx.*;
import au.com.williamhill.flywheel.socketx.undertow.*;
import io.undertow.websockets.core.*;

public final class Leeches extends Thread {
  private final Properties props = new Properties(System.getProperties());
  private final String url = getOrSet(props, "lwv.url", String::valueOf, "ws://localhost:8080");
  private final int connections = getOrSet(props, "lwv.connections", Integer::parseInt, 1);
  private final int minDelayMillis = getOrSet(props, "lwv.delay.min", Integer::parseInt, 0);
  private final int maxDelayMillis = getOrSet(props, "lwv.delay.max", Integer::parseInt, 0);
  private final int reportIntervalMillis = getOrSet(props, "lwv.reportInterval", Integer::parseInt, 5_000);
  private final int idleTimeoutMillis = getOrSet(props, "lwv.idleTimeout", Integer::parseInt, 600_000);
  private final boolean useThreadSleep = getOrSet(props, "lwv.useThreadSleep", Boolean::parseBoolean, false);
  
  private final long startTime = System.currentTimeMillis();
  private long lastReportTime = startTime;
  
  private final AtomicInteger liveConnections = new AtomicInteger();
  private final AtomicLong received = new AtomicLong();
  private long lastReceived;
  
  private Leeches() throws Exception {
    log("Leeches with Vacuums: starting...");
    printProps(props);
    
    if (useThreadSleep) {
      System.setProperty("socketx.undertow.ioThreads", String.valueOf(Math.max(2, connections)));
    }
    
    final XClient<UndertowEndpoint> client = UndertowClient.factory().create(new XClientConfig() {{
      idleTimeoutMillis = Leeches.this.idleTimeoutMillis;
    }});
    
    for (int i = 0; i < connections; i++) {
      connect(client);
    }
    
    new Thread(this::reportLogger, "ReportLogger").start();
  }
  
  private void connect(XClient<UndertowEndpoint> client) throws URISyntaxException, Exception {
    client.connect(new URI(url), 
                   new XEndpointListener<UndertowEndpoint>() {
      @Override
      public void onConnect(UndertowEndpoint endpoint) {
        liveConnections.incrementAndGet();
        log("Connected to " + endpoint);
      }

      @Override
      public void onText(UndertowEndpoint endpoint, String message) {
        handleMessage(endpoint);
      }

      @Override
      public void onBinary(UndertowEndpoint endpoint, ByteBuffer message) {
        handleMessage(endpoint);
      }

      @Override
      public void onPing(UndertowEndpoint endpoint, ByteBuffer data) {}

      @Override
      public void onPong(UndertowEndpoint endpoint, ByteBuffer data) {}

      @Override
      public void onDisconnect(UndertowEndpoint endpoint, int statusCode, String reason) {
        log("Disconnected from " + endpoint + " statusCode=" + statusCode + " reason=" + reason);
      }

      @Override
      public void onClose(UndertowEndpoint endpoint) {
        final int remainingConnections = liveConnections.decrementAndGet();
        log("Closed " + endpoint);
        if (remainingConnections == 0) {
          log("No more connections remaining; exiting");
          System.exit(0);
        }
      }

      @Override
      public void onError(UndertowEndpoint endpoint, Throwable cause) {
        log("Error on endpoint " + endpoint + ": " + cause);
      }
    });
  }
  
  private void handleMessage(UndertowEndpoint endpoint) {
    final int delayMillis = randomDelay(minDelayMillis, maxDelayMillis);
    if (delayMillis != 0) {
      if (useThreadSleep) {
        try {
          Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
          return;
        }
      } else {
        final WebSocketChannel channel = endpoint.getChannel();
        channel.suspendReceives();
        new Thread(() -> {
          try {
            Thread.sleep(delayMillis);
          } catch (InterruptedException e) {
            return;
          } finally {
            channel.getIoThread().execute(channel::resumeReceives);
          }
        }, "ReceiveResumer").start();
      }
    }
    received.incrementAndGet();
  }
  
  private void reportLogger() {
    for (;;) {
      try {
        Thread.sleep(reportIntervalMillis);
      } catch (InterruptedException e) {
        return;
      }
      
      final long now = System.currentTimeMillis();
      final long totalReceived = received.get();
      final long totalElapsed = now - startTime;
      final long reportReceived = totalReceived - lastReceived;
      final long reportElapsed = now - lastReportTime;
      
      final double averageRate = 1000d * totalReceived / totalElapsed;
      final double currentRate = 1000d * reportReceived / reportElapsed;
      
      log("%,d messages; rate: %,.0f msg/s average, %,.0f msg/s current; %,d live connection(s); %,d active threads",
          totalReceived, averageRate, currentRate, liveConnections.get(), Thread.activeCount());
      
      lastReportTime = now;
      lastReceived = totalReceived;
    }
  }
  
  public static void main(String[] args) throws Exception {
    new Leeches();
  }
}
