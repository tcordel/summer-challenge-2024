package fr.tcordel.algorythms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.tcordel.Action;
import fr.tcordel.Player;
import fr.tcordel.mini.strats.ActionScore;
import fr.tcordel.mini.strats.Strategy;

public class LocalMaximum implements Algorythm {

	private final List<Strategy> strats;

	public LocalMaximum(List<Strategy> strats) {
		this.strats = strats;
	}

	@Override
	public Action findBestAction() {
		Map<Action, Integer> cumulatedScore = new HashMap<>();
		List<Integer> miniGameScores = new ArrayList<>();
		int minimumPoints = Integer.MAX_VALUE;
		for (int i = 0; i < strats.size(); i++) {
			int gamePoints = Player.players.get(Player.playerIdx).getPointsForGame(i);
			miniGameScores.add(gamePoints);
			System.err.println("%s current score %d".formatted(Strategy.getGameName(i),
					gamePoints));
			if (gamePoints < minimumPoints) {
				minimumPoints = gamePoints;
			}
		}
		for (int i = 0; i < strats.size(); i++) {
			String gameName = Strategy.getGameName(i);
			Strategy strat = strats.get(i);
			boolean bonus = miniGameScores.get(i) == minimumPoints;
			// boolean last = strat.position() == 2;
			boolean last = false;
			List<ActionScore> actionScores = strat.compute();
			for (ActionScore actionScore : actionScores) {
				int score = actionScore.score() * (bonus ? 3 : 1) * (last ? 2 : 1);
				System.err.println("%s - %s scored %d, bonus %b".formatted(gameName,
						actionScore.action(),
						score, bonus));
				cumulatedScore.put(actionScore.action(),
						score + cumulatedScore.getOrDefault(actionScore.action(), 0));
			}
		}
		return cumulatedScore.entrySet()
				.stream()
				.max(Comparator.comparingInt(Entry::getValue))
				.map(Entry::getKey)
				.orElse(Action.RIGHT);
	}

	
}
