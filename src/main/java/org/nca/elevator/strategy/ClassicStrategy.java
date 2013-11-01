package org.nca.elevator.strategy;

import org.nca.elevator.Elevator.Command;
import org.nca.elevator.ElevatorController;
import org.nca.elevator.ElevatorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the "classic" strategy : go same direction until no request, then go opposite
 * direction.
 */
public class ClassicStrategy implements ElevatorStrategy {

  static final Logger logger = LoggerFactory.getLogger(ClassicStrategy.class);

  public Command nextCommand(ElevatorState e, ElevatorController c) {
    Command command = null;
    if (e.isStale()) {
      if (e.hasDoorOpen()) {
        command = c.closeDoor();
      }
      else if (e.hasDoorClosed()) {
        command = c.goCurrentDirection();
      }
      logger.warn("force action to avoid staying in stale state: {}", command);
    }
    else if (e.hasDoorClosed()) {
      if (e.hasWaitingUserForCurrentFloor() || e.hasElevatorUserForCurrentFloor()) {
        command = c.openDoor();
      }
      else if (e.hasUsersInCurrentDirection()) {
        command = c.goCurrentDirection();
      }
      else if (e.hasUsersInOppositeDirection()) {
        command = c.goOppositeDirection();
      }
      else {
        command = c.doNothing();
      }
    }
    else if (e.hasDoorOpen()) {
      if (e.hasWaitingUserForCurrentFloor()) {
        command = c.doNothing(); // wait for user to enter
      }
      else {
        command = c.closeDoor();
      }
    }
    else {
      logger.error("Should never happen, unexpected state {}", e.getStateAsString());
      command = c.doNothing();
    }
    return command;
  }

}
