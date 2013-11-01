package org.nca.elevator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nca.elevator.Elevator.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ElevatorUsers {

  static final Logger logger = LoggerFactory.getLogger(ElevatorUsers.class);

  private List<ElevatorUser> users = new ArrayList<ElevatorUser>();

  private static enum ExitReason {
    WANT, CAN, COULD;
  }

  /**
   * Acknowledge a request for the provided floor.
   *
   * @param requestedFloor
   *          floor requested
   * @param currentFloor
   *          current floor of elevator
   * @return the user updated with this floor
   */
  public ElevatorUser userRequestedFloor(int requestedFloor, int currentFloor) {
    // known users
    for (ElevatorUser user : users) {
      if (user.canExitAt(requestedFloor)) {
        user.setExitAt(requestedFloor);
        return user;
      }
    }
    // possible users
    for (ElevatorUser user : users) {
      if (user.couldExitAt(requestedFloor)) {
        user.setExitAt(requestedFloor);
        return user;
      }
    }
    logger.warn("Can't find an elevator user to go to requested floor {}. Creating a dummy one.",
        requestedFloor);
    ElevatorUser user = new ElevatorUser(new WaitingUser(currentFloor,
        requestedFloor > currentFloor ? Direction.UP : Direction.DOWN));
    user.setExitAt(requestedFloor);
    return user;
  }

  public void floorServiced(int currentFloor) {
    // nothing to do
  }

  /**
   * Acknowledge a user has entered into the elevator.
   */
  public void userEntered(WaitingUser user) {
    users.add(new ElevatorUser(user));
  }

  /**
   * Remove any user that matches, always using best match first.
   */
  public boolean userExited(int exitFloor) {
    boolean removed = removeUserAtFloor(exitFloor, ExitReason.WANT)
        || removeUserAtFloor(exitFloor, ExitReason.CAN) || removeUserAtFloor(exitFloor, ExitReason.COULD);
    if (!removed) {
      logger.warn("Unable to select any user to remove on user exit event, at exit floor {}",
          exitFloor);
    }
    return removed;
  }

  private boolean removeUserAtFloor(int exitFloor, ExitReason reason) {
    Iterator<ElevatorUser> iterator = users.iterator();
    while (iterator.hasNext()) {
      ElevatorUser user = iterator.next();
      if (shouldRemoveUserAtFloor(user, exitFloor, reason)) {
        iterator.remove();
        logger.info("An user has exited at floor {}, exit reason {}, removed user: {}", exitFloor,
            reason, user);
        return true;
      }
    }
    return false;
  }

  private boolean shouldRemoveUserAtFloor(ElevatorUser user, int exitFloor, ExitReason reason) {
    return (user.wantsToExitAt(exitFloor) && reason.equals(ExitReason.WANT))
        || (user.canExitAt(exitFloor) && reason.equals(ExitReason.CAN))
        || (user.couldExitAt(exitFloor) && reason.equals(ExitReason.COULD));
  }

  public boolean hasUserFor(int floor) {
    return nbUsersFor(floor) > 0;
  }

  public int nbUsersFor(int floor) {
    int count = 0;
    for (ElevatorUser user : users) {
      if (user.wantsToExitAt(floor)) {
        count++;
      }
    }
    return count;
  }

  public boolean hasUserToward(Direction direction, int currentFloor) {
    return nbUsersToward(direction, currentFloor) > 0;
  }

  public int nbUsersToward(Direction direction, int currentFloor) {
    int count = 0;
    for (ElevatorUser user : users) {
      int exitFloor = user.getExitFloor();
      if ((direction == Direction.UP && exitFloor > currentFloor)
          || (direction == Direction.DOWN && exitFloor < currentFloor)) {
        count++;
      }
    }
    return count;
  }

  public int scoreToward(Direction direction, int currentFloor) {
    int score = 0;
    for (ElevatorUser user : users) {
      if (user.hasExitFloor()) {
        int exitFloor = user.getExitFloor();
        if ((direction == Direction.UP && exitFloor > currentFloor)
            || (direction == Direction.DOWN && exitFloor < currentFloor)) {
          score += (2 * Elevator.MAX_FLOOR) - Math.abs(exitFloor - currentFloor);
        }
      }
    }
    return score;
  }

  @Override
  public String toString() {
    return "ElevatorUsers: [" + users + "]";
  }
}