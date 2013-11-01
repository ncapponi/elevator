package org.nca.elevator;

import org.nca.elevator.Elevator.Command;

public interface ElevatorController {

  public abstract Command doNothing();

  public abstract Command openDoor();

  public abstract Command closeDoor();

  public abstract Command goCurrentDirection();

  public abstract Command goOppositeDirection();

}