package org.nca.elevator;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.nca.elevator.Elevator.Direction;

public class ElevatorUserTest {

  @Test
  public void canExitAtFromGroundFloor() throws Exception {
    ElevatorUser user = new ElevatorUser(new WaitingUser(0, Direction.UP));

    assertThat(user.canExitAt(0)).isFalse();
    assertThat(user.canExitAt(1)).isTrue();
    assertThat(user.canExitAt(2)).isTrue();
  }

  @Test
  public void canExitAtFromSecondFloor() throws Exception {
    ElevatorUser user = new ElevatorUser(new WaitingUser(2, Direction.UP));

    assertThat(user.canExitAt(0)).isFalse();
    assertThat(user.canExitAt(1)).isFalse();
    assertThat(user.canExitAt(2)).isFalse();
    assertThat(user.canExitAt(3)).isTrue();
    assertThat(user.canExitAt(4)).isTrue();
  }

  @Test
  public void canExitAtFromSecondFloorDown() throws Exception {
    ElevatorUser user = new ElevatorUser(new WaitingUser(2, Direction.DOWN));

    assertThat(user.canExitAt(0)).isTrue();
    assertThat(user.canExitAt(1)).isTrue();
    assertThat(user.canExitAt(2)).isFalse();
    assertThat(user.canExitAt(3)).isFalse();
    assertThat(user.canExitAt(4)).isFalse();
  }

  @Test
  public void couldExitAtFromSecondFloor() throws Exception {
    ElevatorUser user = new ElevatorUser(new WaitingUser(2, Direction.UNKNOWN));

    assertThat(user.couldExitAt(0)).isTrue();
    assertThat(user.couldExitAt(1)).isTrue();
    assertThat(user.couldExitAt(2)).isFalse();
    assertThat(user.couldExitAt(3)).isTrue();
    assertThat(user.couldExitAt(4)).isTrue();
  }
}
