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
	}

	@Override
	public List<ActionScore> compute() {
		Frame frame = getFrame(Player.playerIdx);
		if (frame.risk() < 0) {
			return Collections.emptyList();
		} else if (frame.risk() >= 3) {
			return List.of(
					new ActionScore(roller.directions.get(0), 3),
					new ActionScore(roller.directions.get(1), 2),
					new ActionScore(roller.directions.get(2), 0),
					new ActionScore(roller.directions.get(3), -1));
		} else {

			return List.of(
					new ActionScore(roller.directions.get(0), 0),
					new ActionScore(roller.directions.get(1), 2),
					new ActionScore(roller.directions.get(2), 1),
					new ActionScore(roller.directions.get(3), 3));
		}
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
}
