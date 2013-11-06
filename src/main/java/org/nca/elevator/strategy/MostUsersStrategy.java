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
    if (e.hasDoorClosed()) {
      if (wasNotClosedJustBefore(e) && hasSomeUserForThisFloor(e)) {
        command = c.openDoor();
      }
      else if (e.hasUsersInCurrentDirection() && hasBetterScoreForCurrentDirection(e)) {
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

  private boolean hasBetterScoreForCurrentDirection(ElevatorState e) {
    return e.scoreInCurrentDirection() >= e.scoreInOppositeDirection();
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
