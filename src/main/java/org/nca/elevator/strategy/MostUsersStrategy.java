package org.nca.elevator.strategy;

import org.nca.elevator.Elevator.Command;
import org.nca.elevator.ElevatorController;
import org.nca.elevator.ElevatorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The "most users" strategy : go to direction where there are most users (in and out elevator) to
 * serve.
 */
public class MostUsersStrategy implements ElevatorStrategy {

  static final Logger logger = LoggerFactory.getLogger(MostUsersStrategy.class);

  @Override
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
      else if (e.hasUsersInCurrentDirection()
          && e.scoreInCurrentDirection() >= e.scoreInOppositeDirection()) {
        logger.info("Scores current {} / opposite {}", e.scoreInCurrentDirection(), e
            .scoreInOppositeDirection());
        command = c.goCurrentDirection();
      }
      else if (e.hasUsersInOppositeDirection()) {
        logger.info("Scores current {} / opposite {}", e.scoreInCurrentDirection(), e
            .scoreInOppositeDirection());
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
