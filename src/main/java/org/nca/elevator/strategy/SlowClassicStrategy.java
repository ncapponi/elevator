package org.nca.elevator.strategy;

import org.nca.elevator.Elevator.Command;
import org.nca.elevator.ElevatorController;
import org.nca.elevator.ElevatorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Same as "classic" strategy, but with a pause of 100ms to check that time is not very important.
 */
public class SlowClassicStrategy extends ClassicStrategy {

  static final Logger logger = LoggerFactory.getLogger(SlowClassicStrategy.class);

  @Override
  public Command nextCommand(ElevatorState e, ElevatorController c) {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ie) {
      logger.warn("interrupted while waiting", ie);
    }
    return super.nextCommand(e, c);
  }

}
