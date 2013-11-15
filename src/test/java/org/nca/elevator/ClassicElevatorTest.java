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

  @Test
  public void cabinIsFull() throws Exception {
    numberOfFloors(1, 3);
    
    // 2 users : not full
    enter().enter();
    assertThat(e.isCabinFull()).isFalse();

    // One more user : cabin is full !
    enter();
    assertThat(e.isCabinFull()).isTrue();
  }
  
  @Test
  public void userIsWaitingButCabinIsFull() throws Exception {
    numberOfFloors(3, 1);
    
    // 1 user entered on the current floor : then the cabin is full
    callUp(0);
    open().enter().go(2).close();
    assertThat(e.isCabinFull()).isTrue();
    // Someone is calling on the 1st floor to go down
    callDown(1);
    // We should not stop to open for the waiting user as the cabin is full
    up(2);
    exitUser();
    down(1);
    open().enter().go(0).close();
    down(1);
    exitUser();
  }

  ClassicElevatorTest exitUser() {
    return exitUser(1);
  }

  ClassicElevatorTest exitUser(int count) {
    open();
    for (int i=0; i < count ; i++) {
      exit();
    }
    return close();
  }
  
  ClassicElevatorTest enterUsers(int[] floorsToGo) {
    open();
    for (int i = 0; i < floorsToGo.length; i++) {
      enter();
      go(floorsToGo[i]);
    }
    return close();
  }
  
  // From here, Helper methods to write the scenarios
  ClassicElevatorTest numberOfFloors(int numberOfFloors) {
    return numberOfFloors(numberOfFloors, 30);
  }

  ClassicElevatorTest numberOfFloors(int numberOfFloors, int cabinSize) {
    e.reset(0, numberOfFloors - 1, cabinSize);
    return this;
  }

  ClassicElevatorTest positionToFloorWithDoorOpened(int floor) {
    callUp(0).open().enter().go(floor).close();
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
      expect(command, count, i);
    }
    return this;
  }

  ClassicElevatorTest goAndOpen(Command command, int count) {
    go(count);
    return open();
  }

  ClassicElevatorTest close() {
    return expect(CLOSE);
  }

  ClassicElevatorTest open() {
    return expect(OPEN);
  }

  ClassicElevatorTest expect(Command command) {
    return expect(command, "command not as expected");
  }

  ClassicElevatorTest expect(Command command, int count, int ith) {
    return expect(command, ith + "th command on " + count + " not as expected");
  }

  private ClassicElevatorTest expect(Command command, String description) {
    assertThat(e.nextCommand()).as(description).isEqualTo(command);
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
