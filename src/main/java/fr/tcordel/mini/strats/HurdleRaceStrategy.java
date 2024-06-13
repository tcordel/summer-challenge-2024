package fr.tcordel.mini.strats;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.List;

import fr.tcordel.Action;
import fr.tcordel.Game;
import fr.tcordel.Player;
import fr.tcordel.mini.HurdleRace;

public class HurdleRaceStrategy implements Strategy {

	private final HurdleRace hurdleRace;

	public HurdleRaceStrategy(
			String gpu,
			int reg0,
			int reg1,
			int reg2,
			int reg3,
			int reg4,
			int reg5,
			int reg6) {
		hurdleRace = new HurdleRace();
		hurdleRace.map = gpu;
		hurdleRace.positions[0] = reg0;
		hurdleRace.positions[1] = reg1;
		hurdleRace.positions[2] = reg2;
		hurdleRace.stunTimers[0] = reg3;
		hurdleRace.stunTimers[1] = reg4;
		hurdleRace.stunTimers[2] = reg5;
	}

	public static int actionScore(Action action) {
		return switch (action) {
			case LEFT -> 1;
			case DOWN -> 2;
			case UP -> 2;
			case RIGHT -> 3;
		};
	}

	private static int getScore(Action action, String gpu) {
		int serchOffset = switch (action) {
			case UP -> 2;
			case LEFT, DOWN, RIGHT -> 1;
		};
		int move = switch (action) {
			case UP, DOWN -> 2;
			case LEFT -> 1;
			case RIGHT -> 3;
		};

		int nextTrap = gpu.indexOf("#", serchOffset);
		if (nextTrap == -1 || nextTrap > move) {
			return actionScore(action);
		}
		return -1;
	}

	@Override
	public List<ActionScore> compute() {
		return Stream.of(Action.values())
				.map(action -> new ActionScore(action,
						getScore(action, hurdleRace.map
								.substring(hurdleRace.positions[Player.playerIdx]))))
				.toList();
	}

	@Override
	public int position() {
		int myScore = hurdleRace.positions[Player.playerIdx];
		return (int) IntStream.range(0, Game.PLAYER_COUNT)
				.filter(i -> i != Player.playerIdx)
				.map(i -> hurdleRace.positions[i])
				.filter(i -> i > myScore)
				.count();
	}
}
