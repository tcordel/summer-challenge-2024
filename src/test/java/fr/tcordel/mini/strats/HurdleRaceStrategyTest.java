package fr.tcordel.mini.strats;

import org.junit.jupiter.api.Test;

public class HurdleRaceStrategyTest {
	@Test
	void testSimulation() {
		String gpu = "....#...#...#....#............";
		String hhh = "   @ @ @ @ @ @  @ @  @  @  @   @";
		HurdleRaceStrategy strategy = new HurdleRaceStrategy(gpu, 0, 0, 0, 0, 0, 0, 0);
		int turnLeft = strategy.nbOfTurnLeft();
		System.err.println(turnLeft);
	}
}
