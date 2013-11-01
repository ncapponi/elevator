package org.nca.elevator.strategy;

import org.nca.elevator.Elevator;
import org.nca.elevator.ElevatorController;
import org.nca.elevator.ElevatorState;

/**
 * Strategy for an elevator, responsible for providing commands for the elevator.
 */
public interface ElevatorStrategy {

  /**
   * Returns the next command to perform.
   * 
   * @param state
   *          provides information on the elevator state
   * @param controller
   *          provides all commands that can be performed by the elevator
   * @return the next command
   */
  public Elevator.Command nextCommand(ElevatorState state, ElevatorController controller);

}
