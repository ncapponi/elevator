package org.nca.elevator;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.nca.elevator.Elevator.Direction;

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
    users.userEntered(new WaitingUser(2, Direction.UNKNOWN));
    WaitingUser knownUser = new WaitingUser(2, Direction.UP);
    users.userEntered(knownUser);

    ElevatorUser selectedUser = users.userRequestedFloor(3, 2);

    assertThat(selectedUser).isEqualTo(new ElevatorUser(knownUser).setExitAt(3));
  }

  @Test
  public void floorRequested_OnePossibleUserMatching() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    WaitingUser unknownUser = new WaitingUser(2, Direction.UNKNOWN);
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
    assertThat(users.nbUsersFor(3)).isEqualTo(0);
  }

  @Test
  public void userExited_UserCan() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(0, Direction.UP));

    // no explicit requested floor
    assertThat(users.userExited(3)).isGreaterThan(0);
    assertThat(users.nbUsersFor(3)).isEqualTo(0);
  }

  @Test
  public void userExited_UserCould() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(2, Direction.UNKNOWN));

    // no explicit requested floor
    assertThat(users.userExited(3)).isGreaterThan(0);
    assertThat(users.nbUsersFor(3)).isEqualTo(0);
  }

  @Test
  public void userExited_TwoUsersWithOneWant() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userRequestedFloor(3, 0);
    users.userEntered(new WaitingUser(2, Direction.UNKNOWN));

    assertThat(users.userExited(3)).isGreaterThan(0);
    assertThat(users.nbUsersFor(3)).isEqualTo(0);
    assertThat(users.nbUsersToward(Direction.DOWN, 3)).isEqualTo(1); // match unknown direction
  }

  @Test
  public void hasUserForFloor() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userRequestedFloor(3, 0);

    assertThat(users.hasUserFor(0)).isFalse();
    assertThat(users.hasUserFor(1)).isFalse();
    assertThat(users.hasUserFor(2)).isFalse();
    assertThat(users.hasUserFor(3)).isTrue();
    assertThat(users.hasUserFor(4)).isFalse();
    assertThat(users.hasUserFor(5)).isFalse();
  }

  @Test
  public void hasUserTowardDirectionFromFloor() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userRequestedFloor(3, 0);

    assertThat(users.hasUserToward(Direction.UP, 0)).isTrue();
    assertThat(users.hasUserToward(Direction.UP, 1)).isTrue();
    assertThat(users.hasUserToward(Direction.UP, 2)).isTrue();
    assertThat(users.hasUserToward(Direction.UP, 3)).isFalse();
    assertThat(users.hasUserToward(Direction.UP, 4)).isFalse();

    assertThat(users.hasUserToward(Direction.DOWN, 5)).isTrue();
    assertThat(users.hasUserToward(Direction.DOWN, 4)).isTrue();
    assertThat(users.hasUserToward(Direction.DOWN, 3)).isFalse();
    assertThat(users.hasUserToward(Direction.DOWN, 2)).isFalse();
  }

  @Test
  public void nbUsersTowardDirectionFromFloor() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userRequestedFloor(2, 0);
    users.userRequestedFloor(3, 0);

    assertThat(users.nbUsersToward(Direction.UP, 0)).isEqualTo(2);
    assertThat(users.nbUsersToward(Direction.UP, 1)).isEqualTo(2);
    assertThat(users.nbUsersToward(Direction.UP, 2)).isEqualTo(1);
    assertThat(users.nbUsersToward(Direction.UP, 3)).isEqualTo(0);
    assertThat(users.nbUsersToward(Direction.UP, 4)).isEqualTo(0);

  }

  @Test
  public void nbUsersTowardDirectionFromFloorSameTargetFloor() throws Exception {
    ElevatorUsers users = new ElevatorUsers();
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userEntered(new WaitingUser(0, Direction.UP));
    users.userRequestedFloor(2, 0);
    users.userRequestedFloor(2, 0);

    assertThat(users.nbUsersToward(Direction.UP, 0)).isEqualTo(2);
    assertThat(users.nbUsersToward(Direction.UP, 1)).isEqualTo(2);
    assertThat(users.nbUsersToward(Direction.UP, 2)).isEqualTo(0);
    assertThat(users.nbUsersToward(Direction.UP, 3)).isEqualTo(0);

  }

}
