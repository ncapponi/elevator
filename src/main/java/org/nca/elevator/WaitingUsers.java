package org.nca.elevator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.nca.elevator.Elevator.Direction;
import org.nca.elevator.Elevator.Optimization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WaitingUsers {

    static final Logger logger = LoggerFactory.getLogger(WaitingUsers.class);

    private List<WaitingUser> users = new ArrayList<WaitingUser>();

    /**
     * Receives a tick.
     */
    public void tick() {
        for (WaitingUser user : users) {
            user.tick();
        }
    }

    public void add(WaitingUser user) {
        users.add(user);
    }

    public WaitingUser popUser(int floor) {
        WaitingUser selectedUser = null;
        Iterator<WaitingUser> it = users.iterator();
        while (it.hasNext()) {
            WaitingUser user = it.next();
            if (user.isWaitingAt(floor)) {
                selectedUser = user;
                it.remove();
            }
        }
        if (selectedUser == null) {
            logger.warn(
                    "Unable to find first waiting user for floor {}, providing one without direction", floor);
            selectedUser = new WaitingUser(floor, Direction.NONE);
        }
        return selectedUser;
    }

    public int nbUsersToward(Direction direction, int currentFloor, int higherFloor, Optimization optimization) {
        int min = direction == Direction.UP ? currentFloor + 1 : 0;
        int max = direction == Direction.UP ? higherFloor : currentFloor - 1;
        int count = 0;
        for (int floor = min; floor <= max; floor++) {
            count += nbUsersForFloor(floor, optimization);
        }
        return count;
    }

    public int scoreToward(Direction direction, int currentFloor, int higherFloor) {
        int min = direction == Direction.UP ? currentFloor + 1 : 0;
        int max = direction == Direction.UP ? higherFloor : currentFloor - 1;
        int score = 0;
        for (int floor = min; floor <= max; floor++) {
            score += (higherFloor - Math.abs(currentFloor - floor)) * nbUsersForFloor(floor, Optimization.NONE);
        }
        return score;
    }

    public boolean hasUserToward(Direction direction, int currentFloor, int higherFloor) {
        return nbUsersToward(direction, currentFloor, higherFloor, Optimization.NONE) > 0;
    }

    public boolean hasUserForFloorInDirection(int floor, Direction direction, Optimization optimization) {
        return nbUsersForFloorInDirection(floor, direction, optimization) > 0;
    }

    public boolean hasUserForFloor(int floor) {
      return nbUsersForFloor(floor, Optimization.NONE) > 0;
    }

    public int nbUsersForFloorInDirection(int floor, Direction dir, Optimization optimization) {
      int number = 0;
      for (WaitingUser user : users) {
          boolean hasCompatibleDirection = dir==Direction.NONE || user.hasCompatibleDirection(dir);
          if (user.isWaitingAt(floor) && hasCompatibleDirection) {
              switch (optimization) {
              case POINTS:
                  if (user.estimateMaximumPointsToEarn() > 0) {
                      number++;
                  } else {
                      logger.info("Ignoring waiting user because no points to earn");
                  }
              case NONE:
              default:
                  number++;
              }
          }
      }
      return number;
   }

    public int nbUsersForFloor(int floor, Optimization optimization) {
        return nbUsersForFloorInDirection(floor, Direction.NONE, optimization);
    }

    public int nbUsers() {
      return users.size();
    }

    public int getTotalTicks() {
        int total = 0;
        for (WaitingUser user : users) {
            total += user.getTicks();
        }
        return total;
    }

    public int getAverageTicksPerUser() {
        return getTotalTicks() / (users.isEmpty() ? 1 : users.size());
    }

    public String toString(String separator, String[] boundaries) {
        StringBuilder builder = new StringBuilder();
        Collections.sort(users);
        for (WaitingUser user : users) {
            if (builder.length() > 0)
                builder.append(separator);
            builder.append(user);
        }
        builder.insert(0, "Nb=" + users.size() + " " + boundaries[0]);
        builder.append(boundaries[1]);
        return builder.toString();
    }

    public String toHTMLString() {
        return toString("<br/>", new String[]{ "<br/>", ""});
    }

    @Override
    public String toString() {
        return toString(" | ", new String[]{ "[", "]"});
    }
}