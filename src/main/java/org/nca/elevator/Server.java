package org.nca.elevator;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.setIpAddress;
import static spark.Spark.setPort;

import java.net.InetAddress;
import java.util.concurrent.locks.ReentrantLock;

import org.nca.elevator.Elevator.Command;
import org.nca.elevator.Elevator.Optimization;
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

    static final String VERSION = "1.1";

    static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final String ipAddress;

    private final int port;

    boolean isServerInitialized = false;

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tT:%1$tL] %4$s: %5$s %n");
        try {
            String ipAddress = "localhost";
            int port = 8080;
            String strategy = "ClassicStrategy";
            String optimization = "NONE";

            if (args.length >= 3) {
                ipAddress = args[0];
                if (ipAddress.equals("find")) {
                    ipAddress = InetAddress.getLocalHost().getHostAddress();
                }
                port = Integer.valueOf(args[1]);
                strategy = args[2];
                if (args.length > 3) {
                    optimization = args[3];
                }
            }
            String strategyClass = "org.nca.elevator.strategy." + strategy;

            logger.info("Launch Elevator Server on address {}, port {}, using strategy {} with optimization {}",
                    ipAddress, port, strategyClass, optimization);
            new Server(ipAddress, port).startElevator(strategyClass, optimization);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable to start elevator", e);
        }
    }

    Server(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;

    }

    void startElevator(String strategyClass, String optimizationName) throws Exception {
        setIpAddress(ipAddress);
        setPort(port);
        ElevatorStrategy strategy = (ElevatorStrategy) Class.forName(strategyClass).newInstance();
        Optimization optimization = Optimization.valueOf(optimizationName);
        if (optimization == null) {
            optimization = Optimization.NONE;
        }
        final Elevator elevator = new Elevator(strategy, optimization);
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
                    result = "<p>GRElevator v. " + VERSION + "</p>" 
                            + "<p> lowerFloor :" + elevator.getLowerFloor() + " higherFloor :" + elevator.getHigherFloor() + " cabinSize :" + elevator.getCabinSize() + "</p>"
                            + "<p>Using strategy: " + elevator.getStrategy().getName()
                            + " and optimization: " + elevator.getOptimization() + ".</p>"
                            + "<p><b>State</b> :"
                            + elevator.getHistoryAsHtml(numberOfEntries) + "</p>";
                } catch (Exception e) {
                    result = e.getMessage();
                }
                return result;
            }
        });

        get(new Route("/strategy") {
            @Override
            public Object handle(Request request, Response response) {
                try {
                    String klass = "org.nca.elevator.strategy." + request.queryParams("klass");
                    ElevatorStrategy strategy = (ElevatorStrategy) Class.forName(klass).newInstance();
                    elevator.setStrategy(strategy);
                    return "Strategy successfully changed to " + klass;
                } catch (Exception e) {
                    response.status(500);
                    logger.error("Unable to change the strategy : {}", e.toString());
                    return "Strategy change failed: " + e.getMessage();
                }
            }
        });

        get(new Route("/optimization") {
            @Override
            public Object handle(Request request, Response response) {
                String name = request.queryParams("name");
                Optimization optimization = Optimization.valueOf(name);
                if (optimization == null) {
                    response.status(404);
                    return "No optimization found for " + name;
                }
                try {
                    elevator.setOptimization(optimization);
                    return "Optimization successfully changed to " + optimization;
                } catch (Exception e) {
                    response.status(500);
                    logger.error("Unable to change the optimization: {}", e.toString());
                    return "Optimization change failed: " + e.getMessage();
                }
            }
        });

        get(new Route("/forceReset") {
            @Override
            public Object handle(Request request, Response response) {
                // So the next Command will be a RESET
                isServerInitialized = false;
                return "";
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
                try {
                  elevator.userHasEntered();
                  return "";
                } catch (Exception e) {
                  response.status(500); // TODO find a more accurate status code
                  return e.getMessage();
                }
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
                String cabinSize = request.queryParams("cabinSize");

                logger.info("Resetting elevator from {} to {} with max of {} users in the cabin because of : {}", lowerFloor,
                        higherFloor, cabinSize, cause);
                if (lowerFloor != null && higherFloor != null) {
                    elevator.reset(Integer.valueOf(lowerFloor), Integer.valueOf(higherFloor), Integer.valueOf(cabinSize));
                }
                else {
                    elevator.reset(0, 19, 30); // allow to use the not up-to-date elevator server
                }
                return "";
            }
        });

        get(new Route("/nextCommand") {
            @Override
            public Object handle(Request request, Response response) {
                try {
                    if (isServerInitialized) {
                        return elevator.nextCommand().toString();
                    }
                    else {
                        // need to force reset by server by sending unknown command to it
                        isServerInitialized = true;
                        return "NEED RESET";
                    }
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
                logger.info("Request {}{}", request.pathInfo(), request.queryString() == null ? ""
                        : "?" + request.queryString());
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
