package org.nca.elevator;

import org.nca.elevator.Elevator.Command;

public interface ElevatorController {

  Command doNothing();

  Command openDoor();

  Command closeDoor();

  Command goCurrentDirection();

  Command goOppositeDirection();

  Command goToMiddleFloor();

}