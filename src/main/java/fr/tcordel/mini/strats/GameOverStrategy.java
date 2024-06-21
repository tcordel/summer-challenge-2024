package fr.tcordel.mini.strats;

import java.util.List;
import java.util.stream.Stream;

import fr.tcordel.Action;

public class GameOverStrategy implements Strategy {

	List<ActionScore> defaultScores = Stream.of(Action.values())
			.map(a -> new ActionScore(a, 0))
			.toList();

	@Override
	public List<ActionScore> compute() {
		return defaultScores;
	}

	@Override
	public int position() {
		return 0;
	}

	@Override
	public int nbOfTurnLeft() {
		return 0;
	}

	@Override
	public double simulate(Action[] actions, int sizeOf, int playerIdx) {
		return 1d;
	}

	@Override
	public String getGameName() {
		return "Game over";
	}
}
