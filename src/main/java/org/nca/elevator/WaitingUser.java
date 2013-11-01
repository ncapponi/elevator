package org.nca.elevator;

import org.nca.elevator.Elevator.Direction;

class WaitingUser {
  private final int floor;
  private final Direction direction;

  public WaitingUser(int floor, Direction dir) {
		this.floor = floor;
		this.direction = dir;
	}

  public int getFloor() {
    return floor;
  }

  public boolean isWaitingAt(int floor) {
    return this.floor == floor;
  }

  public Direction getDirection() {
    return direction;
  }

  @Override
	public String toString() {
		return floor + "/" + direction;
	}
}