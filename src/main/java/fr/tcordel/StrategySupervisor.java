package fr.tcordel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.tcordel.mini.strats.ActionScore;
import fr.tcordel.mini.strats.GameOverStrategy;
import fr.tcordel.mini.strats.Strategy;

public class StrategySupervisor {

	private static final Random RANDOM = new Random();
	private static final List<Action[]> GENOMES = new ArrayList<Action[]>();
	private static final int GENOMES_SIZE = 200;
	private static final SortedMap<Double, Action[]> population = new TreeMap<>(Collections.reverseOrder());
	private final List<Strategy> strats;

	public StrategySupervisor(List<Strategy> strats) {
		this.strats = strats;
	}

	public Action process() {

		List<Strategy> predictableStrats = strats.stream()
				.filter(s -> !(s instanceof GameOverStrategy))
				.toList();
		int turn = predictableStrats.stream()
				.mapToInt(Strategy::nbOfTurnLeft)
				.min()
				.orElse(0);
		if (turn > 3) {
			return getBestPredictiveAction(predictableStrats, turn);
		} else {
			return getBestLocalAction();
		}
	}

	private Action getBestPredictiveAction(List<Strategy> predictableStrats, int turn) {
		GENOMES.clear();
		population.clear();
		generateRandomGenomes(GENOMES_SIZE, turn);
		Action[] fittest = null;
		int generation = 0;
		while (Player.hasTime(0) && generation < 50) {
			generation ++;
			simulate(predictableStrats, turn);
			Entry<Double, Action[]> selected = population.entrySet().iterator().next();
			// System.err.println("Fittest %s, with score %f".formatted(
			// 		Stream.of(selected.getValue()).map(a -> a.name().charAt(0) + "").collect(Collectors.joining()),
			// 		selected.getKey()));
			fittest = selected.getValue();
			if (!Player.hasTime(0)) {
				break;
			}
			processNaturalSelection(turn);
			population.clear();
		}
		return fittest[0];
	}

	private void processNaturalSelection(int turn) {
		GENOMES.clear();
		Action[][] sorted = population.values().toArray(new Action[0][]);
		for (int i = 0; i < Math.min(5, sorted.length); i++) {
			GENOMES.add(sorted[i]);
		}

		if (sorted.length >= 10) {
			for (int i = 0; i < 5; i++) {
				GENOMES.add(sorted[RANDOM.nextInt(sorted.length - 5) + 5]);
			}
		}

		generateRandomGenomes(5, turn);

		for (int i = 0; i < 35; i++) {
			GENOMES.add(crossover(sorted, turn));
		}
	}

	private Action[] crossover(Action[][] sorted, int turn) {
		int max = Math.min(35, sorted.length);
		Action[] father = sorted[RANDOM.nextInt(max)];
		Action[] mother = sorted[RANDOM.nextInt(max)];
		Action[] crossed = new Action[turn];
		int crossedIndex = RANDOM.nextInt(turn);
		System.arraycopy(father, 0, crossed, 0, crossedIndex);
		System.arraycopy(mother, crossedIndex, crossed, crossedIndex, turn - crossedIndex);
		return crossed;
	}

	private void simulate(List<Strategy> predictableStrats, int turn) {
		for (int i = 0; i < GENOMES.size(); i++) {
			double score = 1d;
			for (Strategy strat : predictableStrats) {
				score *= strat.simulate(GENOMES.get(i), turn);
			}
			population.put(score, GENOMES.get(i));
			if (!Player.hasTime(0)) {
				break;
			}
		}
	}

	private void generateRandomGenomes(int number, int turn) {
		for (int i = 0; i < number; i++) {
			Action[] action = new Action[turn];
			for (int j = 0; j < turn; j++) {
				action[j] = switch (RANDOM.nextInt(4)) {
					case 0 -> Action.UP;
					case 1 -> Action.DOWN;
					case 2 -> Action.LEFT;
					default -> Action.RIGHT;
				};
			}
			GENOMES.add(action);
		}
	}

	private Action getBestLocalAction() {
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
