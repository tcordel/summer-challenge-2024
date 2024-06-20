package fr.tcordel.mini.strats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import fr.tcordel.Action;
import fr.tcordel.Game;
import fr.tcordel.Player;
import fr.tcordel.mini.RollerSpeedSkating;

public class RollerSpeedSkatingStrategy implements Strategy {

	private final RollerSpeedSkating roller;

	private final int nbOfTurnLeft;

	public RollerSpeedSkatingStrategy(
			String gpu,
			int reg0,
			int reg1,
			int reg2,
			int reg3,
			int reg4,
			int reg5,
			int reg6) {
		roller = new RollerSpeedSkating();
		roller.directions = new ArrayList<>();
		for (char ch : gpu.toCharArray()) {
			roller.directions.add(Action.from(ch));
		}
		roller.positions[0] = reg0;
		roller.positions[1] = reg1;
		roller.positions[2] = reg2;
		roller.risk[0] = reg3;
		roller.risk[1] = reg4;
		roller.risk[2] = reg5;
		nbOfTurnLeft = reg6;
	}

	@Override
	public List<ActionScore> compute() {
		Frame frame = getFrame(Player.playerIdx);
		if (frame.risk() < 0) {
			return Collections.emptyList();
		}

		boolean hasNeighbourgh = hasNeighbourgh();
		System.err.println("Roller - %d %d %b".formatted(frame.position(), frame.risk(), hasNeighbourgh));
		if (hasNeighbourgh) {
			return List.of(
					new ActionScore(roller.directions.get(0), Math.max(frame.risk() - 2, 0)),
					new ActionScore(roller.directions.get(1), frame.risk() < 3 ? 2 : 0),
					new ActionScore(roller.directions.get(2), frame.risk() < 2 ? 1 : 1),
					new ActionScore(roller.directions.get(3), frame.risk() < 2 ? 1 : 2));
		}

		if (frame.risk() >= 3) {
			return List.of(
					new ActionScore(roller.directions.get(0), 1),
					new ActionScore(roller.directions.get(1), 1),
					new ActionScore(roller.directions.get(2), 0),
					new ActionScore(roller.directions.get(3), -1));
		}
		return List.of(
				new ActionScore(roller.directions.get(0), 0),
				new ActionScore(roller.directions.get(1), 1),
				new ActionScore(roller.directions.get(2), 1),
				new ActionScore(roller.directions.get(3), 2));
	}

	boolean hasNeighbourgh() {
		int me = roller.positions[Player.playerIdx] % 10;
		int other1 = roller.positions[(Player.playerIdx + 1) % 3] % 10;
		int other2 = roller.positions[(Player.playerIdx + 2) % 3] % 10;
		return Math.abs(me - other1) == 0 || Math.abs(me - other2) <= 0;
	}

	@Override
	public int position() {
		int myScore = roller.positions[Player.playerIdx];
		System.err.println("Roller - risk %d".formatted(roller.risk[Player.playerIdx]));
		return (int) IntStream.range(0, Game.PLAYER_COUNT)
				.filter(i -> i != Player.playerIdx)
				.map(i -> roller.positions[i])
				.filter(i -> i > myScore)
				.count();
	}

	Frame getFrame(int playerIdx) {
		return new Frame(roller.positions[playerIdx], roller.risk[playerIdx]);
	}

	record Frame(int position, int risk) {
	}

	@Override
	public double simulate(Action[] actions, int sizeOf) {
		Action first = actions[0];
		if (roller.getGPU().indexOf(first.name().charAt(0)) >= 2) {
			return 1.1d;
		}
		return 1d;
	}

	@Override
	public int nbOfTurnLeft() {
		return Integer.MAX_VALUE;
	}
	@Override
	public String getGameName() {
		return roller.getName();
	}
}
