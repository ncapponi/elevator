package org.nca.elevator.strategy;

import org.nca.elevator.Elevator.Command;
import org.nca.elevator.ElevatorController;
import org.nca.elevator.ElevatorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The "classic" strategy : go same direction until no user to serve (in or out), then go opposite
 * direction.
 */
public class ClassicStrategy implements ElevatorStrategy {

  static final Logger logger = LoggerFactory.getLogger(ClassicStrategy.class);

  @Override
  public Command nextCommand(ElevatorState e, ElevatorController c) {
    Command command = null;
    if (e.hasDoorClosed()) {
      if (wasNotClosedJustBefore(e) && hasSomeUserForThisFloor(e)) {
        command = c.openDoor();
      }
      else if (e.hasUsersInCurrentDirection()) {
        command = c.goCurrentDirection();
      }
      else if (e.hasUsersInOppositeDirection()) {
        command = c.goOppositeDirection();
      }
      else {
        // ensure we minimize next moves when next users come
        command = c.goToMiddleFloor();
      }
    }
    else if (e.hasDoorOpen()) {
      if (wasNotWaitingJustBefore(e) && e.hasWaitingUserForCurrentFloor()) {
        command = c.doNothing(); // wait for user to enter
      }
      else {
        command = c.closeDoor(); // move on
      }
    }
    else {
      logger.error("Should never happen, unexpected state {}", e.getStateAsString());
      command = c.doNothing();
    }
    return command;
  }

  private boolean wasNotWaitingJustBefore(ElevatorState e) {
    return !e.lastCommand().equals(Command.NOTHING);
  }

  private boolean hasSomeUserForThisFloor(ElevatorState e) {
    return e.hasWaitingUserForCurrentFloor() || e.hasElevatorUserForCurrentFloor();
  }

  private boolean wasNotClosedJustBefore(ElevatorState e) {
    return !e.lastCommand().equals(Command.CLOSE);
  }

}
