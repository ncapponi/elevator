package org.nca.elevator;

import org.nca.elevator.Elevator.Command;


/**
 * Provides full information on the state of the elevator.
 */
public interface ElevatorState {

  boolean hasDoorClosed();

  boolean hasDoorOpen();

  boolean hasWaitingUserForCurrentFloor();

  boolean hasElevatorUserForCurrentFloor();

  boolean hasUsersInCurrentDirection();

  boolean hasUsersInOppositeDirection();

  int nbUsersInCurrentDirection();

  int nbUsersInOppositeDirection();

  boolean isStale();

  int scoreInCurrentDirection();

  int scoreInOppositeDirection();

  Command lastCommand();

  String getStateAsString();


}