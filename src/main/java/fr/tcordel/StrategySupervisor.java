package fr.tcordel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.tcordel.mini.strats.ActionScore;
import fr.tcordel.mini.strats.GameOverStrategy;
import fr.tcordel.mini.strats.Strategy;

public class StrategySupervisor {

	private static final Random RANDOM = new Random();
	private static final List<Action[]> GENOMES = new ArrayList<Action[]>();
	private static final int GENOMES_SIZE = 100;
	private static final List<Element> population = new ArrayList<>();
	Action[] fittest = null;
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
		// if (turn > 1) {
			return getBestPredictiveAction(predictableStrats, turn);
		// } else {
		// 	return getBestLocalAction();
		// }
	}

	private Action getBestPredictiveAction(List<Strategy> predictableStrats, int turn) {
		GENOMES.clear();
		population.clear();
		if (fittest == null || fittest.length <= turn) {
			fittest = null;
			generateRandomGenomes(GENOMES_SIZE, turn);
		} else {
			Action[] fittestShifted = new Action[turn];
			System.arraycopy(fittest, 1, fittestShifted, 0, turn);
			GENOMES.add(fittestShifted);
			fittest = fittestShifted;
			generateRandomGenomes(GENOMES_SIZE - 1, turn);
		}
		int generation = 0;
		Element selected = null;
		while (Player.hasTime(0)) {
			generation++;
			simulate(predictableStrats, turn);
			population.sort(Comparator.comparingDouble(Element::score).reversed());
			selected = population.get(0);
			// System.err.println("Fittest %s, with score %f".formatted(
			// Stream.of(selected.getValue()).map(a -> a.name().charAt(0) +
			// "").collect(Collectors.joining()),
			// selected.getKey()));
			fittest = selected.genome();
			if (!Player.hasTime(0)) {
				break;
			}
			processNaturalSelection(turn);
			population.clear();
		}
		System.err.println("Fittest %s with score %f at generation %d".formatted(
				Stream.of(fittest).map(a -> a.name().charAt(0) + "").collect(Collectors.joining()),
				selected.score(),
				generation));
		return fittest[0];
	}

	private void processNaturalSelection(int turn) {
		GENOMES.clear();
		for (int i = 0; i < 10; i++) {
			GENOMES.add(population.get(i).genome());
		}

		for (int i = 0; i < 5; i++) {
			GENOMES.add(population.get(RANDOM.nextInt(70) + 10).genome());
		}

		generateRandomGenomes(5, turn);

		while (GENOMES.size() < GENOMES_SIZE) {
			GENOMES.add(crossover(turn));
		}
	}

	private Action[] crossover(int turn) {
		int max = 50;
		Action[] father = population.get(RANDOM.nextInt(max)).genome();
		Action[] mother = population.get(RANDOM.nextInt(max)).genome();
		Action[] crossed = new Action[turn];
		int crossedIndex = RANDOM.nextInt(turn);
		System.arraycopy(father, 0, crossed, 0, crossedIndex);
		System.arraycopy(mother, crossedIndex, crossed, crossedIndex, turn - crossedIndex);
		if (RANDOM.nextInt(100) <= 5) {
			crossed[RANDOM.nextInt(turn)] = generateRandomAction();
		}
		return crossed;
	}

	private void simulate(List<Strategy> predictableStrats, int turn) {
		for (int i = 0; i < GENOMES.size(); i++) {
			double score = 1d;
			for (Strategy strat : predictableStrats) {
				score *= strat.simulate(GENOMES.get(i), turn);
			}
			population.add(new Element(score, GENOMES.get(i)));
			if (!Player.hasTime(0)) {
				break;
			}
		}
	}

	private void generateRandomGenomes(int number, int turn) {
		for (int i = 0; i < number; i++) {
			Action[] action = new Action[turn];
			for (int j = 0; j < turn; j++) {
				action[j] = generateRandomAction();
			}
			GENOMES.add(action);
		}
	}

	private Action generateRandomAction() {
		return switch (RANDOM.nextInt(4)) {
			case 0 -> Action.UP;
			case 1 -> Action.DOWN;
			case 2 -> Action.LEFT;
			default -> Action.RIGHT;
		};

	}

	private Action getBestLocalAction() {
		fittest = null;
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

	record Element(double score, Action[] genome) {
	}
}
