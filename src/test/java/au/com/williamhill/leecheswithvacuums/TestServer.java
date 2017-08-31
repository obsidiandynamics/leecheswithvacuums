package au.com.williamhill.leecheswithvacuums;

import java.nio.*;
import java.util.*;

import au.com.williamhill.flywheel.socketx.*;
import au.com.williamhill.flywheel.socketx.undertow.*;

public final class TestServer {
  public static void main(String[] args) throws Exception {
    final XServerConfig config = new XServerConfig() {{
    }};
    final XServer<UndertowEndpoint> server = UndertowServer.factory().create(config, new XEndpointListener<UndertowEndpoint>() {
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
      public void onPing(UndertowEndpoint endpoint, ByteBuffer data) {}

      @Override
      public void onPong(UndertowEndpoint endpoint, ByteBuffer data) {}

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

    final int maxBacklog = 100;
    final int sleepMillis = 100;
    final int reportIntervalMillis = 5000;
    final long startTime = System.currentTimeMillis();
    long lastReportTime = startTime;
    long lastSent = 0;
    long totalSent = 0;
    for (;;) {
      int sent = 0;
      for (UndertowEndpoint endpoint : server.getEndpointManager().getEndpoints()) {
        if (endpoint.getBacklog() > maxBacklog) continue;
        
        endpoint.send("hello", null);
        sent++;
      }
      
      if (sent == 0) {
        Thread.sleep(sleepMillis);
      } else {
        totalSent += sent;
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
  
  private static void log(String format, Object ... args) {
    System.out.format("[" + new Date() + "] " + format + "\n", args);
  }
}
