package org.nca.elevator;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.nca.elevator.Elevator.Command.CLOSE;
import static org.nca.elevator.Elevator.Command.DOWN;
import static org.nca.elevator.Elevator.Command.OPEN;
import static org.nca.elevator.Elevator.Command.UP;

import org.junit.Ignore;
import org.junit.Test;
import org.nca.elevator.Elevator.Command;
import org.nca.elevator.strategy.ClassicStrategy;

public class ClassicElevatorTest {

  static final String U = "UP";
  static final String D = "DOWN";
  Elevator e = new Elevator(new ClassicStrategy());

  @Test
  public void goUp() throws Exception {
    numberOfFloors(5);

    callUp(0).callUp(0);
    expect(OPEN);
    enter().enter().go(2).go(3);
    expect(CLOSE).expect(UP).expect(UP).expect(OPEN).exit().expect(CLOSE).expect(UP).expect(OPEN);
  }

  @Test
  @Ignore
  // to fix
  public void goDown() throws Exception {
    numberOfFloors(5).positionToWithDoorOpened(4);

    callUp(4).callUp(4).enter().enter().go(2).go(1);
    expect(CLOSE).expect(DOWN).expect(DOWN).expect(OPEN).exit().expect(CLOSE).expect(DOWN).expect(
        OPEN);
  }

  // Helper methods to write the scenarios
  ClassicElevatorTest numberOfFloors(int numberOfFloors) {
    e.reset(0, numberOfFloors - 1);
    return this;
  }

  ClassicElevatorTest positionToWithDoorOpened(int floor) {
    callUp(0).expect(OPEN).enter().expect(CLOSE).go(floor);
    for (int i = 0; i < floor; i++) {
      expect(UP);
    }
    expect(OPEN).exit();
    return this;
  }

  ClassicElevatorTest enter() {
    e.userHasEntered();
    return this;
  }

  ClassicElevatorTest exit() {
    e.userHasExited();
    return this;
  }

  ClassicElevatorTest go(int floor) {
    e.go(floor);
    return this;
  }

  ClassicElevatorTest callUp(int floor) {
    e.call(floor, U);
    return this;
  }

  ClassicElevatorTest callDown(int floor) {
    e.call(floor, D);
    return this;
  }

  ClassicElevatorTest expect(Command command) {
    assertThat(e.nextCommand()).isEqualTo(command);
    return this;
  }
}
