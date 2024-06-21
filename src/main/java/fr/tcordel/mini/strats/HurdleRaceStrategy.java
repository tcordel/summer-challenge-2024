package fr.tcordel.mini.strats;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.Collections;
import java.util.List;

import fr.tcordel.Action;
import fr.tcordel.Game;
import fr.tcordel.Player;
import fr.tcordel.mini.HurdleRace;

public class HurdleRaceStrategy implements Strategy {

	public final HurdleRace hurdleRace;

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
		if (discard()) {
			return Collections.emptyList();
		}
		String remainingMap = hurdleRace.map
				.substring(hurdleRace.positions[Player.playerIdx]);
		int hurdlePosition = remainingMap.indexOf("#", 1);
		if (hurdlePosition == 1) {
			return List.of(
					new ActionScore(Action.UP, 3),
					new ActionScore(Action.DOWN, -1),
					new ActionScore(Action.LEFT, -1),
					new ActionScore(Action.RIGHT, -1));
		}
		boolean noMoreHurdles = false;
		if (hurdlePosition == -1) {
			noMoreHurdles = true;
			hurdlePosition = remainingMap.length();
		}
		int move = hurdlePosition - 1;
		System.err.println("Hurdle - computing %s, %d %d".formatted(remainingMap, hurdlePosition, move));
		if (move == 1) {
			return List.of(
					new ActionScore(Action.UP, noMoreHurdles ? 3 : -1),
					new ActionScore(Action.DOWN, noMoreHurdles ? 3 : -1),
					new ActionScore(Action.LEFT, 3),
					new ActionScore(Action.RIGHT, noMoreHurdles ? 3 : -1));
		} else if (move == 2) {
			return List.of(
					new ActionScore(Action.UP, 3),
					new ActionScore(Action.DOWN, 3),
					new ActionScore(Action.LEFT, noMoreHurdles ? 3 : 1),
					new ActionScore(Action.RIGHT, noMoreHurdles ? 3 : -1));
		}

		boolean winning = !incomingThreat();
		if (winning) {
			System.err.println("Hurdle - lazy mode");
		}
		if (winning && noMoreHurdles) {
			return Collections.emptyList();
		}
		return List.of(
				new ActionScore(Action.UP, (winning || (move % 3 == 2)) ? 3 : 1),
				new ActionScore(Action.DOWN, (winning || (move % 3 == 2)) ? 3 : 1),
				new ActionScore(Action.LEFT, (winning || (move % 3 == 1)) ? 3 : 1),
				new ActionScore(Action.RIGHT, 3));
	}

	boolean incomingThreat() {
		int myScore = hurdleRace.getBestMove(Player.playerIdx).size();
		return IntStream.range(0, Game.PLAYER_COUNT)
				.filter(i -> i != Player.playerIdx)
				.map(i -> nbOfTurnLeft(i))
				.anyMatch(i -> i < myScore + 1);
	}

	boolean discard() {
		return hurdleRace.stunTimers[Player.playerIdx] > 0;
	}

	@Override
	public int position() {
		int myScore = hurdleRace.getBestMove(Player.playerIdx).size();
		return (int) IntStream.range(0, Game.PLAYER_COUNT)
				.filter(i -> i != Player.playerIdx)
				.map(i -> nbOfTurnLeft(i))
				.filter(i -> i > myScore)
				.count();
	}

	@Override
	public int nbOfTurnLeft() {
		return nbOfTurnLeft(Player.playerIdx);
	}

	public int nbOfTurnLeft(int playerIdx) {
		return hurdleRace.getBestMove(playerIdx).size() + hurdleRace.stunTimers[playerIdx];
	}

	@Override
	public double simulate(Action[] actions, int sizeOf, int playerIdx) {
		return hurdleRace.simulate(actions, sizeOf, playerIdx);
	}

	@Override
	public String getGameName() {
		return hurdleRace.getName();
	}
}
