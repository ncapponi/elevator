package org.nca.elevator;

import static java.lang.Math.abs;

public class Score {

  /** Returns the maximum number of points that can be earned when waiting */
  public static int maxPointsToEarnWhenWaiting(int waitingTicks) {
    return 20 - (waitingTicks / 2);
  }

  /** Returns the maximum number of points that can be earned in the provided state */
  public static int maxPointsToEarnInElevator(int entryFloor, int exitFloor, int waitingTicks, int elevatorTicks) {
    return maxPointsToEarnWhenWaiting(waitingTicks) + bestTickToGo(entryFloor, exitFloor) - elevatorTicks;
  }

  /** Returns the estimated number of points that will be earned in the provided state and hypothesis */
  public static int estimatePointsEarned(int entryFloor, int exitFloor, int waitingTicks, int elevatorTicks,  int currentFloor, int numberOfStops) {
    return maxPointsToEarnInElevator(entryFloor, exitFloor, waitingTicks, elevatorTicks) - bestTickToGo(currentFloor, exitFloor) - (numberOfStops*2);
  }

  /**
   * Shameless copy of server code, to compute minimum number of ticks from one floor to another.
   * {@link https ://github.com/xebia-france/code-elevator
   * /blob/master/elevator-server/src/main/java/elevator /server/Score.java }
   */
  private static Integer bestTickToGo(Integer startFloor, Integer targetFloor) {
    // elevator is OPEN at floor
    final Integer elevatorHasToCloseDoorsWhenAtFloor = 1;
    final Integer elevatorGoesStraightFromFloorToFloorToGo = abs(targetFloor - startFloor);
    final Integer elevatorHasToOpenDoorsWhenAtFloorToGo = 1;

    return elevatorHasToCloseDoorsWhenAtFloor + elevatorGoesStraightFromFloorToFloorToGo
        + elevatorHasToOpenDoorsWhenAtFloorToGo;
  }
}
