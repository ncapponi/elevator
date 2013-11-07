package org.nca.elevator;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.setIpAddress;
import static spark.Spark.setPort;

import java.net.InetAddress;
import java.util.concurrent.locks.ReentrantLock;

import org.nca.elevator.Elevator.Command;
import org.nca.elevator.strategy.ElevatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Elevator server based on Spark.
 */
public class Server {

  static final Logger logger = LoggerFactory.getLogger(Server.class);

  private final String ipAddress;

  private final int port;

  public static void main(String[] args) {
    System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT:%1$tL] %4$s: %5$s %n");
    try {
      String ipAddress = "localhost";
      int port = 8080;
      String strategy = "ClassicStrategy";

      if (args.length == 3) {
        ipAddress = args[0];
        if (ipAddress.equals("find")) {
          ipAddress = InetAddress.getLocalHost().getHostAddress();
        }
        port = Integer.valueOf(args[1]);
        strategy = args[2];
      }
      String strategyClass = "org.nca.elevator.strategy." + strategy;

      logger.info("Launch Elevator Server on address {}, port {}, using strategy {}", ipAddress, port, strategyClass);
      new Server(ipAddress, port).startElevator(strategyClass);

    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Unable to start elevator", e);
    }
  }

  Server(String ipAddress, int port) {
    this.ipAddress = ipAddress;
    this.port = port;

  }

  void startElevator(String strategyClass) throws Exception {
    setIpAddress(ipAddress);
    setPort(port);
    ElevatorStrategy strategy = (ElevatorStrategy) Class.forName(strategyClass).newInstance();
    final Elevator elevator = new Elevator(strategy);
    defineFilters(elevator);
    defineRoutes(elevator);
    defineFeedbackRoutes(elevator);
  }

  private void defineFeedbackRoutes(final Elevator elevator) {
    get(new Route("/status") {
      @Override
      public Object handle(Request request, Response response) {
        String result = "";
        try {
          String entries = request.queryParams("entries");
          int numberOfEntries = entries == null ? 1 : Integer.valueOf(entries);
          response.type("text/html");
          result = "<p><b>Elevator</b> using strategy: " + elevator.getStrategy().getName()
              + ".</p>" + "<p><b>State</b> :" + elevator.getHistoryAsHtml(numberOfEntries) + "</p>";
        } catch (Exception e) {
          result = e.getMessage();
        }
        return result;
      }
    });

    get(new Route("/strategy") {
      @Override
      public Object handle(Request request, Response response) {
        String klass = "org.nca.elevator.strategy." + request.queryParams("klass");
        try {
          ElevatorStrategy strategy = (ElevatorStrategy) Class.forName(klass).newInstance();
          elevator.setStrategy(strategy);
        } catch (Exception e) {
          logger.error("Unable to change the strategy : {}", e.toString());
          return "Strategy change failed";
        }
        return "Strategy successfully changed to " + klass;
      }
    });
  }

  private void defineRoutes(final Elevator elevator) {
    get(new Route("/call") {
      @Override
      public Object handle(Request request, Response response) {
        String atFloor = request.queryParams("atFloor");
        String to = request.queryParams("to");
        elevator.call(Integer.valueOf(atFloor), to);
        return "";
      }
    });

    get(new Route("/go") {
      @Override
      public Object handle(Request request, Response response) {
        String floor = request.queryParams("floorToGo");
        elevator.go(Integer.valueOf(floor));
        return "";
      }
    });

    get(new Route("/userHasEntered") {
      @Override
      public Object handle(Request request, Response response) {
        elevator.userHasEntered();
        return "";
      }
    });

    get(new Route("/userHasExited") {
      @Override
      public Object handle(Request request, Response response) {
        elevator.userHasExited();
        return "";
      }
    });

    get(new Route("/reset") {
      @Override
      public Object handle(Request request, Response response) {
        String lowerFloor = request.queryParams("lowerFloor");
        String higherFloor = request.queryParams("higherFloor");
        String cause = request.queryParams("cause");

        logger.info("Resetting elevator from {} to {} because of : {}", lowerFloor, higherFloor, cause);
        if (lowerFloor!=null && higherFloor!=null) {
            elevator.reset(Integer.valueOf(lowerFloor), Integer.valueOf(higherFloor));
        }
        else {
          elevator.reset(0, 19); // allow to use the not up-to-date elevator server
        }
        return "";
      }
    });

    get(new Route("/nextCommand") {
      @Override
      public Object handle(Request request, Response response) {
        try {
          Command command = elevator.nextCommand();
          return command.toString();
        } catch (Exception e) {
          logger.error("Unexpected error in next command: {}", e.getMessage());
          return Command.NOTHING;
        }
      }
    });
  }

  private void defineFilters(final ElevatorState elevator) {
    // performance is not important, just lock each request handling to avoid fine-tuned locking
    final ReentrantLock lock = new ReentrantLock();

    before(new Filter() { // matches all routes
      @Override
      public void handle(Request request, Response response) {
        lock.lock();
        logger.info("Request {}{}", request.pathInfo(), request.queryString() == null ? "" : "?"
            + request.queryString());
      }
    });

    after(new Filter() {// matches all routes
      @Override
      public void handle(Request request, Response response) {
        logger.info("Done {}{}, {}", request.pathInfo(), request.queryString() == null ? ""
            : "?" + request.queryString(), elevator);
        lock.unlock();
      }
    });
  }

}
