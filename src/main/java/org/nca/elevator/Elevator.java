package org.nca.elevator;

import java.util.Iterator;
import java.util.LinkedList;

import org.nca.elevator.strategy.ElevatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Elevator implements ElevatorState, ElevatorController {

  static final Logger logger = LoggerFactory.getLogger(Elevator.class);

  /** Total of ticks since beginning or last reset */
  private long clockTicks;

  /** Total of user exits since beginning or last reset */
  private long totalExits;

  private int lowerFloor, higherFloor;
  private int currentFloor;
  private Direction currentDirection;
  private Door doorState;
  private WaitingUsers waitingUsers;
  private ElevatorUsers elevatorUsers;
  private StateHistory stateHistory;

  private ElevatorStrategy strategy;
  private Optimization optimization;

  public Elevator(ElevatorStrategy strategy) {
    this(strategy, Optimization.NONE);
  }

  public Elevator(ElevatorStrategy strategy, Optimization optimization) {
    logger.info("Initialising elevator with strategy {} and optimization {}", strategy.getClass(), optimization);
    this.strategy = strategy;
    this.optimization = optimization;
    resetState(0, 19);
  }

  private void resetState(int lowerFloor, int higherFloor) {
    this.lowerFloor = lowerFloor;
    this.higherFloor = higherFloor;
    clockTicks = 0;
    totalExits = 0;
    currentFloor = 0;
    doorState = Door.CLOSED;
    currentDirection = Direction.UP;
    stateHistory = new StateHistory();
    waitingUsers = new WaitingUsers();
    elevatorUsers = new ElevatorUsers();
  }

  public static enum Optimization {
    NONE, // no optimization
    POINTS // optimization based on points
  }

  public static enum Command {
    NOTHING, OPEN, CLOSE, UP, DOWN;
  }

  static enum Direction {
    UP("U"), DOWN("D"), NONE("_");

    private final String label;

    private Direction(String label) {
        this.label = label;
    }

    public String toShortString() {
        return label;
    }

    public Direction flip() {
      if (this == NONE) {
        throw new RuntimeException("Unable to flip this direction: " + this);
      }
      return this == DOWN ? UP : DOWN;
    }

    public Command toCommand() {
      if (this == NONE) {
        throw new RuntimeException("Unable to transform this direction into command: " + this);
      }
      return Command.valueOf(this.toString());
    }
  }

  static enum Door {
    OPEN, CLOSED;
  }

  static class State {
    Command command;
    int floor;
    Direction dir;
    Door door;
    Stats stats;
    String htmlState;

    State(Command command, int floor, Direction dir, Door door, Stats stats, String htmlState) {
      this.command = command;
      this.floor = floor;
      this.dir = dir;
      this.door = door;
      this.stats = stats;
      this.htmlState = htmlState;
    }
  }

  static class Stats {
    long ticksClock;
    long totalExits;
    int nbWait;
    int nbIn;
    int waitingTicks;
    int inboardTicks;
    int nbFloorsAsEntry;
    int nbFloorsAsExit;

    public Stats(long ticksClock, long totalExits, int nbWait, int nbIn, int waitingTicks,
        int inboardTicks, int nbFloorsAsEntry, int nbFloorsAsExit) {
      this.ticksClock = ticksClock;
      this.totalExits = totalExits;
      this.nbWait = nbWait;
      this.nbIn = nbIn;
      this.waitingTicks = waitingTicks;
      this.inboardTicks = inboardTicks;
      this.nbFloorsAsEntry = nbFloorsAsEntry;
      this.nbFloorsAsExit = nbFloorsAsExit;
    }
  }

  static class StateHistory {
    private LinkedList<State> states = new LinkedList<State>();
    int counter = 0;

    public void add(State state) {
      // keep only N last items
      states.addFirst(state);
      counter++;
      if (counter >= 100) {
        states.removeLast();
      }
    }

    public Command getLastCommand() {
      return states.isEmpty() ? null : states.getFirst().command;
    }

    public String getDetailedHistoryAsHtml(int numberOfEntries) {
      StringBuilder history = new StringBuilder();
      history
          .append("<table cellpadding='5' cellmargin='2'>")
          .append(
              "<th>Command</th><th>Ticks</th><th>Floor</th><th>Direction</th><th>Door</th><th>Waiting</th><th>In Elevator</th>");
      Iterator<State> stateIt = states.iterator();
      for (int i = 0; i < Math.min(numberOfEntries, states.size()); i++) {
        State state = stateIt.next();
        history.append("<tr>").append("<td>").append(state.command).append("</td>").append(
            state.htmlState).append("</tr>");
      }
      history.append("</table>");
      return history.toString();
    }

    public String getHistoryAsHtml(int numberOfEntries) {
      StringBuilder history = new StringBuilder();
      history
        .append("<table cellpadding='5' cellmargin='2'>")
        .append("<th>Clock</th><th>Exits</th><th>Command</th><th>Floor</th>")
        .append("<th>Direction</th><th>Door</th><th>Users</th><th>Ticks</th><th>Floors</th>");
      Iterator<State> stateIt = states.iterator();
      for (int i = 0; i < Math.min(numberOfEntries, states.size()); i++) {
        State state = stateIt.next();
        history.append("<tr>")
          .append("<td>").append(state.stats.ticksClock).append("</td>")
          .append("<td>").append(state.stats.totalExits).append("</td>")
          .append("<td>").append(state.command).append("</td>")
          .append("<td>").append(state.floor).append("</td>")
          .append("<td>").append(state.dir).append("</td>")
          .append("<td>").append(state.door).append("</td>")
          .append("<td>").append(state.stats.nbWait).append(" | ").append(state.stats.nbIn).append("</td>")
          .append("<td>").append(state.stats.waitingTicks).append(" | ").append(state.stats.inboardTicks).append("</td>")
          .append("<td>").append(state.stats.nbFloorsAsEntry).append(" | ").append(state.stats.nbFloorsAsExit).append("</td>")
          .append("</tr>");
      }
      history.append("</table>");
      return history.toString();
    }

  }

  public int getHigherFloor() {
    return higherFloor;
  }

  public String getHistoryAsHtml(int numberOfEntries) {
    return stateHistory.getHistoryAsHtml(numberOfEntries);
  }

  void setStrategy(ElevatorStrategy newStrategy) {
    logger.info("--- Changing strategy to {} ---", newStrategy);
    this.strategy = newStrategy;
  }

  void setOptimization(Optimization optim) {
    logger.info("--- Changing optimization to {} ---", optim);
    this.optimization = optim;
  }

  Class<? extends ElevatorStrategy> getStrategy() {
    return this.strategy.getClass();
  }

  public Elevator reset(int lowerFloor, int higherFloor) {
    resetState(lowerFloor, higherFloor);
    return this;
  }

  // floor: 0-5, to : UP/DOWN
  public Elevator call(int atFloor, String to) {
    waitingUsers.add(new WaitingUser(atFloor, Direction.valueOf(to)));
    return this;
  }

  public Elevator go(int floor) {
    elevatorUsers.userRequestedFloor(floor, currentFloor);
    return this;
  }

  public Elevator userHasEntered() {
      WaitingUser user = waitingUsers.popUser(currentFloor);
      elevatorUsers.userEntered(user);
      logger.info("User has entered, added " + user);
    return this;
  }

  public Elevator userHasExited() {
    elevatorUsers.userExited(currentFloor);
    totalExits++;
    return this;
  }

  public Command nextCommand() {
    ajustDirection();
    Command command = strategy.nextCommand(this, this);
    recordState(command);
    increaseTick();
    logger.info("Command returned: {}", command);
    return command;
  }

  private void increaseTick() {
    clockTicks++;
    waitingUsers.tick();
    elevatorUsers.tick();
  }

  private void ajustDirection() {
    if (currentFloor == lowerFloor) {
      currentDirection = Direction.UP;
    }
    else if (currentFloor == higherFloor) {
      currentDirection = Direction.DOWN;
    }
  }

  private void recordState(Command command) {
    Stats stats = new Stats(clockTicks, totalExits, waitingUsers.nbUsers(), elevatorUsers.nbUsers(),
        waitingUsers.getTotalTicks(), elevatorUsers.getTotalTicks(), 0, 0);
    State state = new State(command, currentFloor, currentDirection, doorState, stats, getStateAsHtmlString());
    stateHistory.add(state);
  }

  /* (non-Javadoc)
   * @see org.nca.elevator.ElevatorState#hasDoorClosed()
   */
  @Override
  public boolean hasDoorClosed() {
    return doorState == Door.CLOSED;
  }

  /* (non-Javadoc)
   * @see org.nca.elevator.ElevatorState#hasDoorOpen()
   */
  @Override
  public boolean hasDoorOpen() {
    return doorState == Door.OPEN;
  }

  /* (non-Javadoc)
   * @see org.nca.elevator.ElevatorState#hasWaitingUserForCurrentFloor()
   */
  @Override
  public boolean hasWaitingUserForCurrentFloor() {
    return waitingUsers.hasUserForFloor(currentFloor);
  }

  @Override
  public boolean hasWaitingUserForCurrentFloorInCurrentDirection() {
    return waitingUsers.hasUserForFloorInDirection(currentFloor, currentDirection, optimization);
  }

  @Override
  public boolean hasElevatorUserForCurrentFloor() {
    return elevatorUsers.hasUserForFloor(currentFloor, optimization);
  }

  @Override
  public boolean hasUsersInCurrentDirection() {
    return hasUsersInDirection(currentDirection);
  }

  @Override
  public boolean hasUsersInOppositeDirection() {
    return hasUsersInDirection(currentDirection.flip());
  }

  private boolean hasUsersInDirection(Direction direction) {
    return nbUsersInDirection(direction) > 0;
  }

  @Override
  public int nbUsersWaiting() {
    return waitingUsers.nbUsers();
  }

  @Override
  public int nbUsersInElevator() {
    return elevatorUsers.nbUsers();
  }

  @Override
  public int nbUsersInCurrentDirection() {
    return nbUsersInDirection(currentDirection);
  }

  @Override
  public int nbUsersInOppositeDirection() {
    return nbUsersInDirection(currentDirection.flip());
  }

  @Override
  public int scoreInCurrentDirection() {
    return scoreInDirection(currentDirection);
  }

  @Override
  public int scoreInOppositeDirection() {
    return scoreInDirection(currentDirection.flip());
  }

  public int getTotalTicks() {
      return waitingUsers.getTotalTicks() + elevatorUsers.getTotalTicks();
  }

  private int nbUsersInDirection(Direction direction) {
    return elevatorUsers.nbUsersTowardDirection(direction, currentFloor, optimization)
        + waitingUsers.nbUsersToward(direction, currentFloor, higherFloor, optimization);
  }

  private int scoreInDirection(Direction direction) {
    return elevatorUsers.scoreTowardDirection(direction, currentFloor, higherFloor)
        + waitingUsers.scoreToward(direction, currentFloor, higherFloor);
  }

  @Override
  public Command lastCommand() {
    return stateHistory.getLastCommand();
  }

  /* (non-Javadoc)
   * @see org.nca.elevator.ElevatorController#doNothing()
   */
  @Override
  public Command doNothing() {
    return Command.NOTHING;
  }

  /* (non-Javadoc)
   * @see org.nca.elevator.ElevatorController#open()
   */
  @Override
  public Command openDoor() {
    elevatorUsers.floorServiced(currentFloor);
    doorState = Door.OPEN;
    return Command.OPEN;
  }

  @Override
  public Command closeDoor() {
    doorState = Door.CLOSED;
    return Command.CLOSE;

  }

  @Override
  public Command goCurrentDirection() {
    return go(currentDirection);
  }

  @Override
  public Command goOppositeDirection() {
    currentDirection = currentDirection.flip();
    return go(currentDirection);
  }

  public Command go(Direction direction) {
    switch (direction) {
    case UP: return goUp();
    case DOWN: return goDown();
    default:
      return Command.NOTHING;
    }
  }

  @Override
  public Command goToMiddleFloor() {
    int middleFloor = lowerFloor + (higherFloor - lowerFloor) / 2;
    if (currentFloor > middleFloor) {
      return goDown();
    }
    else if (currentFloor < middleFloor) {
      return goUp();
    }
    else {
      return Command.NOTHING;
    }
  }

  public Command goDown() {
    currentFloor--;
    return Command.DOWN;
  }

  public Command goUp() {
    currentFloor++;
    return Command.UP;
  }

  @Override
  public String getStateAsString() {
    return toString();
  }

  private String getStateAsHtmlString() {
    return "<td>" + getTotalTicks() + "/" + waitingUsers.getAverageTicksPerUser() + "/" + elevatorUsers.getAverageTicksPerUser() +
            "</td><td>" + currentFloor + "</td><td>" + currentDirection + "</td><td>" + doorState +
            "</td><td>" + waitingUsers.toHTMLString() + "</td><td>" + elevatorUsers.toHTMLString() + "</td>";
  }

  @Override
  public String toString() {
    return "Ticks: " + getTotalTicks() + "/" + waitingUsers.getAverageTicksPerUser() + "/" + elevatorUsers.getAverageTicksPerUser() +
            ", Floor=" + currentFloor + ", Dir=" + currentDirection + ", Door=" + doorState +
            ", WAIT " + waitingUsers + ", ELEV " + elevatorUsers;
  }

}
