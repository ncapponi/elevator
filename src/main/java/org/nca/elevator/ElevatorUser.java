package org.nca.elevator;

import org.nca.elevator.Elevator.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ElevatorUser implements Comparable<ElevatorUser> {

    static final Logger logger = LoggerFactory.getLogger(ElevatorUser.class);

    private static final int UNSET_EXIT_FLOOR = -1;

    private final int entryFloor;
    private Direction direction;
    private int exitFloor;
    private final int waitingTicks;
    private int ticks;

    public ElevatorUser(WaitingUser user) {
        this.entryFloor = user.getFloor();
        this.direction = user.getDirection();
        this.waitingTicks = user.getTicks();
        this.exitFloor = UNSET_EXIT_FLOOR;
    }

    /**
     * @return true if and only if 1) exit floor has not been already set, and
     *         2) entry floor and direction are known to be compatible with exit
     *         floor
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
                || (!hasExitFloor() && entryFloor != exitFloor && direction.equals(Direction.NONE));
    }

    public boolean hasExitFloor() {
        return exitFloor != UNSET_EXIT_FLOOR;
    }

    public ElevatorUser setExitAt(int floor) {
        if (!hasExitFloor()) {
            exitFloor = floor;
            if (direction==Direction.NONE) {
              direction = exitFloor > entryFloor ? Direction.UP : Direction.DOWN;
            }
        } else {
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

    /**
     * Returns the total number of ticks this user has passed (waiting + in elevator).
     */
    public int getTotalTicks() {
        return waitingTicks + ticks;
    }

    /**
     * Returns the number of ticks this user has passed in the elevator.
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

    /**
     * Returns the estimated points for this user if elevator goes to
     * exit floor when it is at provided floor, with an estimated number
     * of stops between.
     */
    public int estimatePointsEarned(int currentFloor, int numberOfStops) {
        return Score.estimatePointsEarned(entryFloor, exitFloor, waitingTicks, ticks, currentFloor, numberOfStops);
    }

    /**
     * Return the final score for this user. Only valid if called when user
     * exits.
     */
    public int getFinalPointsEarned() {
        return Score.maxPointsToEarnInElevator(entryFloor, exitFloor, waitingTicks, ticks);
    }

    @Override
    public String toString() {
        return entryFloor + "/" + direction.toShortString() + "/" + (hasExitFloor() ? exitFloor : "_") + " T="
                + getTotalTicks() + "/" + waitingTicks + "+" + ticks;
    }

    @Override
    public int compareTo(ElevatorUser other) {
        int f = this.entryFloor - other.entryFloor;
        if (f != 0)
            return f;
        f = this.exitFloor - other.exitFloor;
        if (f != 0)
            return f;
        return (this.getTotalTicks()) - (other.getTotalTicks());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((direction == null) ? 0 : direction.hashCode());
        result = prime * result + entryFloor;
        result = prime * result + exitFloor;
        result = prime * result + ticks;
        result = prime * result + waitingTicks;
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
        if (ticks != other.ticks)
            return false;
        if (waitingTicks != other.waitingTicks)
            return false;
        return true;
    }



}