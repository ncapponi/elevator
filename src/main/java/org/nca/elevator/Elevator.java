package org.nca.elevator;

import java.util.Iterator;
import java.util.LinkedList;

import org.nca.elevator.strategy.ElevatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Elevator implements ElevatorState, ElevatorController {

  static final Logger logger = LoggerFactory.getLogger(Elevator.class);

  private int lowerFloor, higherFloor;
  private int currentFloor;
  private Direction currentDirection;
  private Door doorState;
  private WaitingUsers waitingUsers;
  private ElevatorUsers elevatorUsers;
  private StateHistory stateHistory;

  private ElevatorStrategy strategy;

  public Elevator(ElevatorStrategy strategy) {
    logger.info("Initialising elevator with strategy {}", strategy.getClass());
    this.strategy = strategy;
    resetState(0, 19);
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

  static class StateHistory {
    private LinkedList<Command> commandsHistory = new LinkedList<Command>();
    private LinkedList<String> stateHistory = new LinkedList<String>();
    int counter = 0;

    public void add(Command command, String stateAsHtml) {
      // keep only 50 last items
      commandsHistory.addFirst(command);
      stateHistory.addFirst(stateAsHtml);
      counter++;
      if (counter >= 50) {
        commandsHistory.removeLast();
        stateHistory.removeLast();
      }
    }

    public boolean isStaleSince(int numberOfCommands) {
      if (counter <= numberOfCommands) {
        // not enough commands to look at
        return false;
      }
      for (int i = 0; i < numberOfCommands; i++) {
        Command command = commandsHistory.get(i);
        if (!(command.equals(Command.NOTHING) || command.equals(Command.CLOSE))) {
          return false;
        }
      }
      return true;
    }

    public Command getLastCommand() {
      return commandsHistory.getFirst();
    }

    public String getHistoryAsHtml(int numberOfEntries) {
      StringBuilder history = new StringBuilder();
      history
          .append("<table cellpadding='5' cellmargin='2'>")
          .append(
              "<th>Command</th><th>Ticks</th><th>Floor</th><th>Direction</th><th>Door</th><th>Waiting</th><th>In Elevator</th>");
      Iterator<Command> commandsIt = commandsHistory.iterator();
      Iterator<String> stateIt = stateHistory.iterator();
      for (int i = 0; i < Math.min(numberOfEntries, commandsHistory.size()); i++) {
        history.append("<tr>").append("<td>").append(commandsIt.next()).append("</td>").append(
            stateIt.next()).append("</tr>");
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

  private void resetState(int lowerFloor, int higherFloor) {
    this.lowerFloor = lowerFloor;
    this.higherFloor = higherFloor;
    currentFloor = 0;
    doorState = Door.CLOSED;
    currentDirection = Direction.UP;
    stateHistory = new StateHistory();
    waitingUsers = new WaitingUsers();
    elevatorUsers = new ElevatorUsers();
  }

  void setStrategy(ElevatorStrategy newStrategy) {
    logger.info("--- Changing strategy to {} ---", newStrategy);
    this.strategy = newStrategy;
  }

  Class<? extends ElevatorStrategy> getStrategy() {
    return this.strategy.getClass();
  }

  public void reset(int lowerFloor, int higherFloor) {
    resetState(lowerFloor, higherFloor);
  }

  // floor: 0-5, to : UP/DOWN
  public void call(int atFloor, String to) {
    waitingUsers.add(new WaitingUser(atFloor, Direction.valueOf(to)));
  }

  public void go(int floor) {
    elevatorUsers.userRequestedFloor(floor, currentFloor);
  }

  public void userHasEntered() {
      WaitingUser user = waitingUsers.popUser(currentFloor);
      elevatorUsers.userEntered(user);
      logger.info("User has entered, added " + user);
  }

  public void userHasExited() {
    elevatorUsers.userExited(currentFloor);
  }

  public Command nextCommand() {
    ajustDirection();
    Command command = strategy.nextCommand(this, this);
    recordState(command, getStateAsHtmlString());
    increaseTick();
    logger.info("Command returned: {}", command);
    return command;
  }

  private void increaseTick() {
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

  private void recordState(Command command, String stateAsHtml) {
    stateHistory.add(command, stateAsHtml);
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
    return waitingUsers.hasUserFor(currentFloor);
  }

  /* (non-Javadoc)
   * @see org.nca.elevator.ElevatorState#hasElevatorUserForCurrentFloor()
   */
  @Override
  public boolean hasElevatorUserForCurrentFloor() {
    return elevatorUsers.hasUserFor(currentFloor);
  }

  @Override
  public boolean hasUsersInCurrentDirection() {
    return hasUsersInDirection(currentDirection);
  }

  /* (non-Javadoc)
   * @see org.nca.elevator.ElevatorState#hasUsersInOppositeDirection()
   */
  @Override
  public boolean hasUsersInOppositeDirection() {
    return hasUsersInDirection(currentDirection.flip());
  }

  private boolean hasUsersInDirection(Direction direction) {
    return elevatorUsers.hasUserToward(direction, currentFloor)
        || waitingUsers.hasUserToward(direction, currentFloor, higherFloor);
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
    return elevatorUsers.nbUsersToward(direction, currentFloor)
        + waitingUsers.nbUsersToward(direction, currentFloor, higherFloor);
  }

  private int scoreInDirection(Direction direction) {
    return elevatorUsers.scoreToward(direction, currentFloor, higherFloor)
        + waitingUsers.scoreToward(direction, currentFloor, higherFloor);
  }

  /* (non-Javadoc)
   * @see org.nca.elevator.ElevatorState#isStale()
   */
  @Override
  public boolean isStale() {
    return stateHistory.isStaleSince(3);
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

  /* (non-Javadoc)
   * @see org.nca.elevator.ElevatorController#close()
   */
  @Override
  public Command closeDoor() {
    doorState = Door.CLOSED;
    return Command.CLOSE;

  }

  /* (non-Javadoc)
   * @see org.nca.elevator.ElevatorController#goCurrentDirection()
   */
  @Override
  public Command goCurrentDirection() {
    currentFloor = currentFloor + (currentDirection == Direction.UP ? 1 : -1);
    return currentDirection.toCommand();
  }

  /* (non-Javadoc)
   * @see org.nca.elevator.ElevatorController#goOppositeDirection()
   */
  @Override
  public Command goOppositeDirection() {
    currentDirection = currentDirection.flip();
    currentFloor = currentFloor + (currentDirection == Direction.UP ? 1 : -1);
    return currentDirection.toCommand();
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
