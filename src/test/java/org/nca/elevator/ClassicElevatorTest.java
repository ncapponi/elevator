package org.nca.elevator;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.nca.elevator.Elevator.Command.CLOSE;
import static org.nca.elevator.Elevator.Command.DOWN;
import static org.nca.elevator.Elevator.Command.OPEN;
import static org.nca.elevator.Elevator.Command.UP;

import org.junit.Test;
import org.nca.elevator.Elevator.Command;
import org.nca.elevator.strategy.ClassicStrategy;

public class ClassicElevatorTest {

  static final String U = "UP";
  static final String D = "DOWN";
  Elevator e = new Elevator(new ClassicStrategy());

  @Test
  public void oneUserGoUp() throws Exception {
    numberOfFloors(5);

    callUp(0);
    open().enter().go(3).close();
    up(3);
    open().exit().close();
    end();
  }

  @Test
  public void oneUserGoDown() throws Exception {
    numberOfFloors(5).positionToFloorWithDoorOpened(4);

    callDown(4);
    enter().go(1).close();
    down(3);
    open().exit().close();
    end();
  }

  @Test
  public void twoUserUserGoUp() throws Exception {
    numberOfFloors(5);

    callUp(0).callUp(2);
    open().enter().go(3).close();
    up(2);
    open().enter().go(4).close();
    up(1);
    open().exit().close();
    up(1);
    open().exit().close();
    end();
  }

  @Test
  public void skipUserWaitingForOppositeDirection() throws Exception {
    numberOfFloors(5);

    callUp(0).callDown(2);
    open().enter().go(3).close();
    up(3);
    open().exit().close();
    down(1);
    open().enter().go(1).close();
    down(1);
    open().exit().close();
    end();
  }

  @Test
  public void serveUserWaitingForOppositeDirectionIfLastOneInDirection() throws Exception {
    numberOfFloors(5);

    callDown(2);
    up(2);
    open().enter().go(1).close();
    down(1);
    open().exit().close();
    end();
  }

  // From here, Helper methods to write the scenarios
  ClassicElevatorTest numberOfFloors(int numberOfFloors) {
    e.reset(0, numberOfFloors - 1);
    return this;
  }

  ClassicElevatorTest positionToFloorWithDoorOpened(int floor) {
    callUp(0).open().enter().close().go(floor);
    up(floor);
    open().exit();
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

  ClassicElevatorTest up(int count) {
    return go(UP,  count);
  }

  ClassicElevatorTest down(int count) {
    return go(DOWN,  count);
  }

  ClassicElevatorTest upAndOpen(int count) {
    return goAndOpen(UP,  count);
  }

  ClassicElevatorTest downAndOpen(int count) {
    return goAndOpen(DOWN,  count);
  }

  ClassicElevatorTest go(Command command, int count) {
    for (int i = 0; i < count; i++) {
      expect(command);
    }
    return this;
  }

  ClassicElevatorTest goAndOpen(Command command, int count) {
    for (int i = 0; i < count; i++) {
      expect(command);
    }
    return open();
  }

  ClassicElevatorTest close() {
    return expect(CLOSE);
  }

  ClassicElevatorTest open() {
    return expect(OPEN);
  }

  ClassicElevatorTest expect(Command command) {
    assertThat(e.nextCommand()).as("command not as expected").isEqualTo(command);
    return this;
  }

  ClassicElevatorTest end() {
    end(0,0);
    return this;
  }

  ClassicElevatorTest end(int waiting, int inElevator) {
    assertThat(e.nbUsersWaiting()).as("wrong number of users waiting").isEqualTo(waiting);
    assertThat(e.nbUsersInElevator()).as("wrong number of users in the elevator").isEqualTo(inElevator);
    return this;
  }
}
