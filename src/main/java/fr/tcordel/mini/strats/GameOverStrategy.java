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

}
