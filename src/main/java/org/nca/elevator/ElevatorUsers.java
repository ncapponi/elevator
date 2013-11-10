package org.nca.elevator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.nca.elevator.Elevator.Direction;
import org.nca.elevator.Elevator.Optimization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ElevatorUsers {

    static final int NULL_SCORE = -1;

    static final Logger logger = LoggerFactory.getLogger(ElevatorUsers.class);

    private List<ElevatorUser> users = new ArrayList<ElevatorUser>();

    private static enum ExitReason {
        WANT, CAN, COULD;
    }

    /**
     * Acknowledge a request for the provided floor.
     *
     * @param requestedFloor
     *            floor requested
     * @param currentFloor
     *            current floor of elevator
     * @return the user updated with this floor
     */
    public ElevatorUser userRequestedFloor(int requestedFloor, int currentFloor) {
        // known users
        for (ElevatorUser user : users) {
            if (user.getEntryFloor() == currentFloor && user.getTicks() == 0
                    && user.canExitAt(requestedFloor)) {
                user.setExitAt(requestedFloor);
                return user;
            }
        }
        // possible users
        for (ElevatorUser user : users) {
            if (user.getEntryFloor() == currentFloor && user.getTicks() == 0
                    && user.couldExitAt(requestedFloor)) {
                user.setExitAt(requestedFloor);
                return user;
            }
        }
        logger.warn(
                "Can't find an elevator user to go to requested floor {}. Creating a dummy one.",
                requestedFloor);
        ElevatorUser user = new ElevatorUser(new WaitingUser(currentFloor,
                requestedFloor > currentFloor ? Direction.UP : Direction.DOWN));
        user.setExitAt(requestedFloor);
        return user;
    }

    public void floorServiced(int currentFloor) {
        // nothing to do
    }

    /**
     * Acknowledge a user has entered into the elevator.
     */
    public void userEntered(WaitingUser user) {
        users.add(new ElevatorUser(user));
    }

    /**
     * Remove any user that matches, always using best match first.
     *
     * @return the final score of the removed user, or {@code NULL_SCORE} if no user could be find
     *         to be removed
     */
    public int userExited(int exitFloor) {
        int score = removeUserAtFloor(exitFloor, ExitReason.WANT);
        score = (score == NULL_SCORE) ? removeUserAtFloor(exitFloor, ExitReason.CAN) : score;
        score = (score == NULL_SCORE) ? removeUserAtFloor(exitFloor, ExitReason.COULD) : score;

        if (score == NULL_SCORE) {
            logger.error(
                    "Unable to select any user to remove on user exit event, at exit floor {}",
                    exitFloor);
        }
        return score;
    }

    /**
     * Receives a tick.
     */
    public void tick() {
        for (ElevatorUser user : users) {
            user.tick();
        }
    }

    public boolean hasUserForFloor(int floor, Optimization optimization) {
        switch (optimization) {
        case POINTS:
            int points = nbPointsEarnedForFloor(floor);
            if (points <= 0) {
                logger.info("Ignoring elevator user at floor because no positive points {}", points);
            }
            return points > 0;
        case NONE:
        default:
            return nbUsersForFloor(floor) > 0;
        }
    }

    public int nbUsersTowardDirection(Direction direction, int currentFloor,
            Optimization optimization) {
        int count = 0;
        for (ElevatorUser user : users) {
            int exitFloor = user.getExitFloor();
            if ((direction == Direction.UP && exitFloor > currentFloor)
                    || (direction == Direction.DOWN && exitFloor < currentFloor)) {
                switch (optimization) {
                case POINTS:
                    // TODO : it's possible to be more agressive by playing on these two zeros
                    int points = user.estimatePointsEarned(currentFloor, 0);
                    if (points > 0) {
                        count++;
                    } else {
                        logger.info("Ignoring elevator user toward direction because no positive points {}", points);
                    }
                case NONE:
                default:
                    count++;
                }
            }
        }
        return count;
    }

    public int scoreTowardDirection(Direction direction, int currentFloor, int higherFloor) {
        int score = 0;
        for (ElevatorUser user : users) {
            if (user.hasExitFloor()) {
                int exitFloor = user.getExitFloor();
                if ((direction == Direction.UP && exitFloor > currentFloor)
                        || (direction == Direction.DOWN && exitFloor < currentFloor)) {
                    score += (2 * higherFloor) - Math.abs(exitFloor - currentFloor);
                }
            }
        }
        return score;
    }

    public int nbUsers() {
        return users.size();
    }

    /**
     * Remove an elevator user matching the provided exit floor and exit reason, and return its
     * final score.
     *
     * @return the final score of the removed user, or {@code NULL_SCORE} if no user could be find
     *         to be removed
     */
    private int removeUserAtFloor(int exitFloor, ExitReason reason) {
        Iterator<ElevatorUser> iterator = users.iterator();
        while (iterator.hasNext()) {
            ElevatorUser user = iterator.next();
            if (shouldRemoveUserAtFloor(user, exitFloor, reason)) {
                iterator.remove();
                int finalScore = user.getFinalPointsEarned();
                logger.info(
                        "User has exited at floor {}, score {}, exit reason {}, removed user: {}",
                        exitFloor, finalScore, reason, user);
                return finalScore;
            }
        }
        return NULL_SCORE;
    }

    private boolean shouldRemoveUserAtFloor(ElevatorUser user, int exitFloor, ExitReason reason) {
        return (user.wantsToExitAt(exitFloor) && reason.equals(ExitReason.WANT))
                || (user.canExitAt(exitFloor) && reason.equals(ExitReason.CAN))
                || (user.couldExitAt(exitFloor) && reason.equals(ExitReason.COULD));
    }

    private int nbPointsEarnedForFloor(int floor) {
        int userCount = 0;
        int points = 0;
        for (ElevatorUser user : users) {
            if (user.wantsToExitAt(floor)) {
                userCount++;
                points += Math.max(0, user.getFinalPointsEarned());
            }
        }
        logger.info("Points earned if elevator users exit at this floor {}, for {} users", points,
                userCount);
        return points;
    }

    int nbUsersForFloor(int floor) {
        int count = 0;
        for (ElevatorUser user : users) {
            if (user.wantsToExitAt(floor)) {
                count++;
            }
        }
        return count;
    }

    public int getTotalTicks() {
        int total = 0;
        for (ElevatorUser user : users) {
            total += user.getTotalTicks();
        }
        return total;
    }

    public int getAverageTicksPerUser() {
        return getTotalTicks() / (users.isEmpty() ? 1 : users.size());
    }

    public String toString(String separator, String boundaries[]) {
        StringBuilder builder = new StringBuilder();
        Collections.sort(users);
        for (ElevatorUser user : users) {
            if (builder.length() > 0)
                builder.append(separator);
            builder.append(user);
        }
        builder.insert(0, "Nb=" + users.size() + " " + boundaries[0]);
        builder.append(boundaries[1]);
        return builder.toString();
    }

    public String toHTMLString() {
        return toString("<br/>", new String[] { "<br/>", "" });
    }

    @Override
    public String toString() {
        return toString(" | ", new String[] { "[", "]" });
    }
}