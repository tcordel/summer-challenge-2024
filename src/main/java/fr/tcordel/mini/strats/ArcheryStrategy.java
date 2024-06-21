package fr.tcordel.mini.strats;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import fr.tcordel.Action;
import fr.tcordel.Game;
import fr.tcordel.Player;
import fr.tcordel.algorythms.Element;
import fr.tcordel.algorythms.Genetic;
import fr.tcordel.mini.Archery;

public class ArcheryStrategy implements Strategy {

	private static final Genetic genetic = new Genetic(75, 10).withLimitSimulation(true);
	private final Archery archery;
	private boolean useGenetic = true;

	private List<Element> myBest;
	private final List<Element> oppBest = new ArrayList<>();

	public ArcheryStrategy(
			String gpu,
			int reg0,
			int reg1,
			int reg2,
			int reg3,
			int reg4,
			int reg5,
			int reg6) {
		archery = new Archery();
		archery.wind.clear();
		for (char ch : gpu.toCharArray()) {
			archery.wind.add(Integer.parseInt(String.valueOf(ch)));
		}

		archery.cursors.get(0)[0] = reg0;
		archery.cursors.get(0)[1] = reg1;
		archery.cursors.get(1)[0] = reg2;
		archery.cursors.get(1)[1] = reg3;
		archery.cursors.get(2)[0] = reg4;
		archery.cursors.get(2)[1] = reg5;
		genetic.refresh(List.of(this), archery.wind.size());
	}

	@Override
	public void init() {
		for (int i = 0; i < Game.PLAYER_COUNT; i++) {
			if (i == Player.playerIdx) {
				continue;
			}
			genetic.resetFittest();
			Element best = genetic.findBest(i);
			oppBest.add(best);
		}

		genetic.resetFittest();
		genetic.findBest(Player.playerIdx);
		myBest = genetic.getPopulation();
	}

	@Override
	public List<ActionScore> compute() {
		// TODO use genetic here
		if (useGenetic && !myBest.isEmpty()) {
			return computeWithGenetic();
		}
		return Stream.of(Action.values())
				.map(action -> new ActionScore(action,
						getScore(action, archery.cursors.get(Player.playerIdx),
								archery.wind.get(0))))
				.toList();
	}

	private List<ActionScore> computeWithGenetic() {
		double bestScore = 0;
		int scoreLeft = 0;
		int scoreUp = 0;
		int scoreRight = 0;
		int scoreDown = 0;
		boolean compareWithOther = oppBest.stream()
				.noneMatch(i -> i.score() < 0);

		int oppBestScore = (int) oppBest.stream()
				.mapToDouble(Element::score)
				.max()
				.orElse(-1d);
		for (int i = 0; i < myBest.size(); i++) {
			Element element = myBest.get(i);
			double score = element.score();
			if (i == 0) {
				bestScore = score;
			}
			boolean isBestScore = score == bestScore;

			if (bestScore <= score ||
					(compareWithOther && score >= oppBestScore)) {
				switch (element.genome()[0]) {
					case UP -> scoreUp = Math.max(scoreUp, isBestScore ? 3 : 2);
					case DOWN -> scoreDown = Math.max(scoreDown, isBestScore ? 3 : 2);
					case RIGHT -> scoreRight = Math.max(scoreRight, isBestScore ? 3 : 2);
					case LEFT -> scoreLeft = Math.max(scoreLeft, isBestScore ? 3 : 2);
				}
			}
		}
		return List.of(
				new ActionScore(Action.RIGHT, scoreRight),
				new ActionScore(Action.UP, scoreUp),
				new ActionScore(Action.DOWN, scoreDown),
				new ActionScore(Action.LEFT, scoreLeft));
	}

	private int getScore(Action action, int[] cursor, Integer offset) {
		int[] newCursor = new int[] { cursor[0], cursor[1] };
		archery.applyWind(action, newCursor, offset);
		int currentDistance = distanceToTarget(cursor);
		int newDistance = distanceToTarget(newCursor);
		if (currentDistance < newDistance) {
			return -1;
		}
		return 1;
	}

	int distanceToTarget(int[] cursor) {
		return Math.abs(cursor[0]) + Math.abs(cursor[1]);
	}

	@Override
	public int position() {
		if (myBest.isEmpty()) {
			return 0;
		}
		double myScore = myBest.get(0).score();
		// System.err.print("Archer - my %d o1 %d o2 %d".formatted(
		// 		myScore,
		// 		oppBest.get(0).score(),
		// 		oppBest.get(1).score()));
		return (int) oppBest.stream()
				.filter(o -> o.score() > myScore)
				.count();
	}

	@Override
	public int nbOfTurnLeft() {
		return archery.wind.size();
	}

	@Override
	public double simulate(Action[] actions, int sizeOf, int playerIdx) {
		return archery.simulate(actions, sizeOf, playerIdx);
	}

	@Override
	public String getGameName() {
		return archery.getName();
	}
}
