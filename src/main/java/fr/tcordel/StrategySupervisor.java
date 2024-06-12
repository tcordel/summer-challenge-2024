package fr.tcordel;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.tcordel.mini.strats.ActionScore;
import fr.tcordel.mini.strats.Strategy;

public class StrategySupervisor {

	private final List<Strategy> strats;

	public StrategySupervisor(List<Strategy> strats) {
		this.strats = strats;
	}

	public Action process() {

		Map<Action, Integer> cumulatedScore = new HashMap<>();
		for (int i = 0; i < strats.size(); i++) {
			String gameName = Strategy.getGameName(i);
			Strategy strat = strats.get(i);
			List<ActionScore> actionScores = strat.compute();
			for (ActionScore actionScore : actionScores) {
				System.err.println("%s - %s scored %d".formatted(gameName, actionScore.action(), actionScore.score()));
				cumulatedScore.put(actionScore.action(),
						actionScore.score() + cumulatedScore.getOrDefault(actionScore.action(), 0));
			}
		}
		return cumulatedScore.entrySet()
				.stream()
				.max(Comparator.comparingInt(Entry::getValue))
				.map(Entry::getKey)
				.orElse(Action.RIGHT);
	}
}
