package fr.tcordel.mini;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import fr.tcordel.Action;
import fr.tcordel.Player;

public class HurdleRaceTest {
	@Test
	void testSimulation() {
		String gpu = "....#...#...#....#............";
		HurdleRace race = new HurdleRace();
		race.map = gpu;
		List<Action> bestMove = race.getBestMove(Player.playerIdx);
		String collect = bestMove.stream().map(Action::name).collect(Collectors.joining());
		System.err.println(collect);
		char[] charArray = "UURURUUURUUUDUR".toCharArray();
		Action[] actions = new Action[charArray.length];
		for (int i = 0; i < charArray.length; i++) {
			actions[i] = Action.from(charArray[i]);
		}
		double simulate = race.simulate(actions, charArray.length, Player.playerIdx);
		System.err.println(simulate);
	}
}
