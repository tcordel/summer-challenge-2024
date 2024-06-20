package fr.tcordel.algorythms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.tcordel.Action;
import fr.tcordel.Player;
import fr.tcordel.mini.strats.HurdleRaceStrategy;
import fr.tcordel.mini.strats.Strategy;

public class Genetic implements Algorythm {

	private static final Random RANDOM = new Random();
	private static final List<Action[]> GENOMES = new ArrayList<Action[]>();
	private static final int GENOMES_SIZE = 100;
	private static final List<Element> population = new ArrayList<>();

	private final List<Strategy> predictableStrats;
	private final int turn;

	Action[] fittest = null;

	public static record Element(double score, Action[] genome) {
	}

	public Genetic(List<Strategy> predictableStrats, int turn) {
		this.predictableStrats = predictableStrats;
		this.turn = turn;
	}

	public Genetic(List<Strategy> predictableStrats, int turn, Action[] fittest) {
		this(predictableStrats, turn);
		this.fittest = fittest;
	}

	@Override
	public Action findBestAction() {
		GENOMES.clear();
		population.clear();
		if (fittest == null || fittest.length <= turn) {
			fittest = null;
			generateRandomGenomes(GENOMES_SIZE - 1, turn);
		} else {
			Action[] fittestShifted = new Action[turn];
			System.arraycopy(fittest, 1, fittestShifted, 0, turn);
			GENOMES.add(fittestShifted);
			fittest = fittestShifted;
			generateRandomGenomes(GENOMES_SIZE - 2, turn);
		}
		generateBestGenomeForHurdle(predictableStrats, turn);
		int generation = 0;
		Element selected = null;
		while (Player.hasTime(0)) {
			generation++;
			simulate(predictableStrats, turn);
			population.sort(Comparator.comparingDouble(Element::score).reversed());
			selected = population.get(0);
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

	private void generateBestGenomeForHurdle(List<Strategy> predictableStrats, int turn) {
		Optional<Strategy> hurdleRaceStrategy = predictableStrats
				.stream()
				.filter(strategy -> strategy instanceof HurdleRaceStrategy)
				.findFirst();
		if (hurdleRaceStrategy.isEmpty()) {
			return;
		}
		List<Action> actions = ((HurdleRaceStrategy) hurdleRaceStrategy.get()).hurdleRace.getBestMove();
		Action[] action = new Action[turn];
		for (int i = 0; i < turn; i++) {
			Action a;
			if (actions.size() > turn) {
				a = actions.get(i);
			} else {
				a = generateRandomAction();
			}
			action[i] = a;
		}
		GENOMES.add(action);
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

}
