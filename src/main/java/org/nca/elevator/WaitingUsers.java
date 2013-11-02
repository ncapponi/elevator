package org.nca.elevator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nca.elevator.Elevator.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WaitingUsers {

	static final Logger logger = LoggerFactory.getLogger(WaitingUsers.class);

	private List<WaitingUser> users = new ArrayList<WaitingUser>();

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
      selectedUser = new WaitingUser(floor, Direction.UNKNOWN);
    }
    return selectedUser;
	}

  public int nbUsersToward(Direction direction, int currentFloor) {
    int min = direction == Direction.UP ? currentFloor + 1 : 0;
    int max = direction == Direction.UP ? Elevator.MAX_FLOOR : currentFloor - 1;
    int count = 0;
    for (int floor = min; floor <= max; floor++) {
      count = count + nbUsersFor(floor);
    }
    return count;
  }

  public int scoreToward(Direction direction, int currentFloor) {
    int min = direction == Direction.UP ? currentFloor + 1 : 0;
    int max = direction == Direction.UP ? Elevator.MAX_FLOOR : currentFloor - 1;
    int score = 0;
    for (int floor = min; floor <= max; floor++) {
      score += (Elevator.MAX_FLOOR - Math.abs(currentFloor - floor)) * nbUsersFor(floor);
    }
    return score;
  }

	public boolean hasUserToward(Direction direction, int currentFloor) {
    return nbUsersToward(direction, currentFloor) > 0;
  }

  public boolean hasUserFor(int floor) {
		return nbUsersFor(floor) > 0;
	}

	public int nbUsersFor(int floor) {
		int number = 0;
		for (WaitingUser user : users) {
      if (user.isWaitingAt(floor))
				number++;
		}
		return number;
	}

	@Override
	public String toString() {
    return "Waiting: [" + users + "]";
	}
}