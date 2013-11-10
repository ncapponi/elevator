package org.nca.elevator;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.nca.elevator.Elevator.Direction;
import org.nca.elevator.Elevator.Optimization;

public class ElevatorUsersTest {

  @Test
  public void floorRequested_NoUserIsMatching() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    ElevatorUser selectedUser = users.userRequestedFloor(3, 0);

    assertThat(selectedUser).isEqualTo(
        new ElevatorUser(new WaitingUser(0, Direction.UP)).setExitAt(3));
  }

  @Test
  public void floorRequested_TwoUsersMatchingWithOneSure() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(2, Direction.NONE));
    WaitingUser knownUser = new WaitingUser(2, Direction.UP);
    users.userEntered(knownUser);

    ElevatorUser selectedUser = users.userRequestedFloor(3, 2);

    assertThat(selectedUser).isEqualTo(new ElevatorUser(knownUser).setExitAt(3));
  }

  @Test
  public void floorRequested_OnePossibleUserMatching() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    WaitingUser unknownUser = new WaitingUser(2, Direction.NONE);
    users.userEntered(unknownUser);

    ElevatorUser selectedUser = users.userRequestedFloor(3, 2);

    assertThat(selectedUser).isEqualTo(new ElevatorUser(unknownUser).setExitAt(3));
  }

  @Test
  public void userExited_UserWant() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userRequestedFloor(3, 0);

    assertThat(users.userExited(3)).isGreaterThan(0);
    assertThat(users.nbUsersForFloor(3)).isEqualTo(0);
  }

  @Test
  public void userExited_UserCan() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(0, Direction.UP));

    // no explicit requested floor
    assertThat(users.userExited(3)).isGreaterThan(0);
    assertThat(users.nbUsersForFloor(3)).isEqualTo(0);
  }

  @Test
  public void userExited_UserCould() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(2, Direction.NONE));

    // no explicit requested floor
    assertThat(users.userExited(3)).isGreaterThan(0);
    assertThat(users.nbUsersForFloor(3)).isEqualTo(0);
  }

  @Test
  public void userExited_TwoUsersWithOneWant() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userRequestedFloor(3, 0);
    users.userEntered(new WaitingUser(2, Direction.NONE));

    assertThat(users.userExited(3)).isGreaterThan(0);
    assertThat(users.nbUsersForFloor(3)).isEqualTo(0);
    assertThat(users.nbUsersTowardDirection(Direction.DOWN, 3, Optimization.NONE)).isEqualTo(1); // match unknown direction
  }

  @Test
  public void hasUserForFloor() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userRequestedFloor(3, 0);

    assertThat(users.hasUserForFloor(0, Optimization.NONE)).isFalse();
    assertThat(users.hasUserForFloor(1, Optimization.NONE)).isFalse();
    assertThat(users.hasUserForFloor(2, Optimization.NONE)).isFalse();
    assertThat(users.hasUserForFloor(3, Optimization.NONE)).isTrue();
    assertThat(users.hasUserForFloor(4, Optimization.NONE)).isFalse();
    assertThat(users.hasUserForFloor(5, Optimization.NONE)).isFalse();
  }

  @Test
  public void nbUsersTowardDirectionFromFloor() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userRequestedFloor(2, 0);
    users.userRequestedFloor(3, 0);

    assertThat(users.nbUsersTowardDirection(Direction.UP, 0, Optimization.NONE)).isEqualTo(2);
    assertThat(users.nbUsersTowardDirection(Direction.UP, 1, Optimization.NONE)).isEqualTo(2);
    assertThat(users.nbUsersTowardDirection(Direction.UP, 2, Optimization.NONE)).isEqualTo(1);
    assertThat(users.nbUsersTowardDirection(Direction.UP, 3, Optimization.NONE)).isEqualTo(0);
    assertThat(users.nbUsersTowardDirection(Direction.UP, 4, Optimization.NONE)).isEqualTo(0);

  }

  @Test
  public void nbUsersTowardDirectionFromFloorSameTargetFloor() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userRequestedFloor(2, 0);
    users.userRequestedFloor(2, 0);

    assertThat(users.nbUsersTowardDirection(Direction.UP, 0, Optimization.NONE)).isEqualTo(2);
    assertThat(users.nbUsersTowardDirection(Direction.UP, 1, Optimization.NONE)).isEqualTo(2);
    assertThat(users.nbUsersTowardDirection(Direction.UP, 2, Optimization.NONE)).isEqualTo(0);
    assertThat(users.nbUsersTowardDirection(Direction.UP, 3, Optimization.NONE)).isEqualTo(0);

  }

}
