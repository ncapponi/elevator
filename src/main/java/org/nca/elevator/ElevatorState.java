package org.nca.elevator;


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

  String getStateAsString();

  int scoreInCurrentDirection();

  int scoreInOppositeDirection();

}