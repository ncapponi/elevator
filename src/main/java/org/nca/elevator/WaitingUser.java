package org.nca.elevator;

import org.nca.elevator.Elevator.Direction;

class WaitingUser implements Comparable<WaitingUser> {
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

    public boolean hasCompatibleDirection(Direction dir) {
      return direction==dir || direction==Direction.NONE;
    }

    /** Returns the maximum number of points that can be earned if user is taken to target floor directly */
    public  int estimateMaximumPointsToEarn() {
      return Score.maxPointsToEarnWhenWaiting(ticks);
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
    public int compareTo(WaitingUser other) {
        int f = this.floor - other.floor;
        if (f != 0)
            return f;
        return this.ticks - other.ticks;
    }

    @Override
    public String toString() {
        return floor + "/" + direction.toShortString() + " T=" + ticks;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((direction == null) ? 0 : direction.hashCode());
        result = prime * result + floor;
        result = prime * result + ticks;
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
        WaitingUser other = (WaitingUser) obj;
        if (direction != other.direction)
            return false;
        if (floor != other.floor)
            return false;
        if (ticks != other.ticks)
            return false;
        return true;
    }

}