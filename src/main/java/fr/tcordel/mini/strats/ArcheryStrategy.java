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

	private Element myBest;
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
			genetic.resetFittest();
			Element best = genetic.findBest(i);

			if (i == Player.playerIdx) {
				myBest = best;
			} else {
				oppBest.add(best);
			}
		}
	}

	@Override
	public List<ActionScore> compute() {
		// TODO use genetic here
		if (useGenetic) {
			return computeWithGenetic();
		}
		return Stream.of(Action.values())
				.map(action -> new ActionScore(action,
						getScore(action, archery.cursors.get(Player.playerIdx),
								archery.wind.get(0))))
				.toList();
	}

	private List<ActionScore> computeWithGenetic() {
		return List.of(new ActionScore(myBest.genome()[0], 1));
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
		double myScore = myBest.score();
		System.err.print("Archer - my %d o1 %d o2 %d".formatted(
				myScore,
				oppBest.get(0).score(),
				oppBest.get(1).score()));
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
