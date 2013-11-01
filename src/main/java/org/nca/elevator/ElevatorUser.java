package org.nca.elevator;

import org.nca.elevator.Elevator.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ElevatorUser {

  static final Logger logger = LoggerFactory.getLogger(ElevatorUser.class);

  private static final int UNSET_EXIT_FLOOR = -1;

  private final int entryFloor;
  private final Direction direction;
  private int exitFloor;

  public ElevatorUser(WaitingUser user) {
    this.entryFloor = user.getFloor();
    this.direction = user.getDirection();
    this.exitFloor = UNSET_EXIT_FLOOR;
  }

  /**
   * @return true if and only if 1) exit floor has not been already set, and 2) entry floor and
   *         direction are known to be compatible with exit floor
   */
  public boolean canExitAt(int exitFloor) {
    return !hasExitFloor()
        && ((entryFloor < exitFloor && direction.equals(Direction.UP)) || (entryFloor > exitFloor && direction
            .equals(Direction.DOWN)));
  }

  /**
   * Like canExitAt() but with loose constraint, direction can be unknown
   */
  public boolean couldExitAt(int exitFloor) {
    return canExitAt(exitFloor)
        || (!hasExitFloor() && entryFloor != exitFloor && direction.equals(Direction.UNKNOWN));
  }

  public boolean hasExitFloor() {
    return exitFloor != UNSET_EXIT_FLOOR;
  }

  public ElevatorUser setExitAt(int floor) {
    if (!hasExitFloor()) {
      exitFloor = floor;
    }
    else {
      logger.error("Error : Can't set exit twice for elevator user: {}, at floor {}", this, floor);
    }
    return this;
  }

  public boolean wantsToExitAt(int floor) {
    return exitFloor == floor;
  }

  public int getExitFloor() {
    return exitFloor;
  }

  public int getEntryFloor() {
    return entryFloor;
  }

  public Direction getDirection() {
    return direction;
  }

  @Override
  public String toString() {
    return "from " + entryFloor + " " + direction + " to " + (hasExitFloor() ? exitFloor : "?");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((direction == null) ? 0 : direction.hashCode());
    result = prime * result + entryFloor;
    result = prime * result + exitFloor;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ElevatorUser other = (ElevatorUser) obj;
    if (direction != other.direction)
      return false;
    if (entryFloor != other.entryFloor)
      return false;
    if (exitFloor != other.exitFloor)
      return false;
    return true;
  }

}