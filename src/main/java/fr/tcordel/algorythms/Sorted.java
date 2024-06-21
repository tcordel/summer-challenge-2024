package fr.tcordel.algorythms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.tcordel.Action;
import fr.tcordel.Player;
import fr.tcordel.mini.strats.ActionScore;
import fr.tcordel.mini.strats.Strategy;

public class Sorted implements Algorythm, Comparator<Strategy> {

	private final List<Strategy> strats;
	private static List<String> lastSort = new ArrayList<>();

	public Sorted(List<Strategy> strats) {
		this.strats = strats;
	}

	@Override
	public Action findBestAction() {
		strats.sort(this);
		lastSort = strats.stream()
				.map(s -> s.getGameName())
				.toList();
		List<ActionScore> actions = Collections.emptyList();
		for (int i = 0; i < strats.size(); i++) {
			Strategy strat = strats.get(i);
			System.err.println("Processing %s".formatted(strat.getGameName()));
			List<ActionScore> subActions = strat.compute().stream().filter(a -> a.score() > 0).toList();
			if (subActions.size() == 0) {
				continue;
			}
			if (actions.size() == 0) {
				actions = subActions;
				continue;
			}
			if (actions.stream().anyMatch(a -> subActions.stream().anyMatch(b -> b.action().equals(a.action())))) {
				actions = actions.stream().filter(a -> subActions.stream().anyMatch(b -> b.action().equals(a.action())))
						.toList();
			}
		}
		if (actions.size() == 0) {
			return Action.RIGHT;
		}
		return actions.get(0).action();
	}

	@Override
	public int compare(Strategy first, Strategy second) {
		if (first.getGameName().equals("Roller Speed Skating")) {
			return 1;
		}
		if (second.getGameName().equals("Roller Speed Skating")) {
			return -1;
		}
		int compare = Integer.compare(getMedalsFor(first.getIndex()), getMedalsFor(second.getIndex()));
		String debug = "Comparing %s & %s, medals %d".formatted(first.getGameName(), second.getGameName(), compare);
		if (compare == 0) {
			compare = Integer.compare(first.position(), second.position());
			debug = "%s, position %d".formatted(debug, compare);
		}

		if (compare == 0) {
			compare = Integer.compare(first.nbOfTurnLeft(), second.nbOfTurnLeft());
			debug = "%s, nbOfTurnLeft %d".formatted(debug, compare);
		}
		
		if (compare == 0) {
			compare = Integer.compare(lastSort.indexOf(first.getGameName()), lastSort.indexOf(second.getGameName()));
			debug = "%s, lastIndex %d".formatted(debug, compare);
		}
		System.err.println(debug);
		return compare;
	}

	int getMedalsFor(int gameId) {
		if (gameId == -1) {
			return Integer.MAX_VALUE;
		}
		return Player.players.get(Player.playerIdx).getPointsForGame(gameId);
	}

}