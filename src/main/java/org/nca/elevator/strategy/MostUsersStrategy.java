package org.nca.elevator.strategy;

import org.nca.elevator.Elevator.Command;
import org.nca.elevator.ElevatorController;
import org.nca.elevator.ElevatorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the "most users" strategy : go to direction where there are most waiting users, once
 * all elevator users are serviced
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
      else if ((e.lastCommand().equals(Command.CLOSE) && e.scoreInCurrentDirection() >= e
          .scoreInOppositeDirection())
          || (!e.lastCommand().equals(Command.CLOSE) && e.hasUsersInCurrentDirection())) {
        logger.info("score current dir {}, score opposite dir {}, last command {}", e
            .scoreInCurrentDirection(), e.scoreInOppositeDirection(), e.lastCommand());
        command = c.goCurrentDirection();
      }
      else if (e.hasUsersInOppositeDirection()) {
        logger.info("score current dir {}, score opposite dir {}", e.scoreInCurrentDirection(), e
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
