package org.nca.elevator;

import org.nca.elevator.Elevator.Direction;

class WaitingUser {
  private final int floor;
  private final Direction direction;
  private int ticks;

  public WaitingUser(int floor, Direction dir) {
		this.floor = floor;
		this.direction = dir;
    ticks = 0;
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

  /**
   * Returns the score malus to apply to this user when elevator is at provided floor.
   */
  public int getScoreMalus(int currentFloor) {
    // current ticks + ticks to go to user floor + tick to open door
    return (ticks + Math.abs(floor - currentFloor) + 1) / 2;
  }

  /**
   * Returns the number of ticks this user has waited.
   */
  public int getTicks() {
    return ticks;
  }

  /**
   * Receives a tick.
   */
  public void tick() {
    ticks++;
  }

  @Override
	public String toString() {
    return floor + "/" + direction + " T=" + ticks;
	}
}