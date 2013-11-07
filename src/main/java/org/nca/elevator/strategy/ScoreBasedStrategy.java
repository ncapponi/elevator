package org.nca.elevator.strategy;

import org.nca.elevator.Elevator.Command;
import org.nca.elevator.ElevatorController;
import org.nca.elevator.ElevatorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Score based strategy : choose the direction to go based on the best score that can be expected
 * given the current state.
 *
 * TODO : need some more work to be implemented.
 *
 * 1) add notion of tick to users
 *
 * 2) compute score for each user depending on choice
 *
 * 3) compute best decision given sum of score of users
 *
 * 4) use statistics of previous states to add some hint on next users to come
 */
public class ScoreBasedStrategy implements ElevatorStrategy {

  static final Logger logger = LoggerFactory.getLogger(ScoreBasedStrategy.class);

  @Override
  public Command nextCommand(ElevatorState e, ElevatorController c) {
    Command command = null;
    if (e.hasDoorClosed()) {
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
