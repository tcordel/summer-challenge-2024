package fr.tcordel.mini.strats;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fr.tcordel.Action;
import fr.tcordel.Game;
import fr.tcordel.Player;
import fr.tcordel.mini.Diving;

public class DivingStrategy implements Strategy {

	private final Diving diving;

	public DivingStrategy(
			String gpu,
			int reg0,
			int reg1,
			int reg2,
			int reg3,
			int reg4,
			int reg5,
			int reg6) {
		diving = new Diving();
		diving.goal.clear();
		for (char ch : gpu.toCharArray()) {
			diving.goal.add(ch);
		}
		diving.points[0] = reg0;
		diving.points[1] = reg1;
		diving.points[2] = reg2;
		diving.combo[0] = reg3;
		diving.combo[1] = reg4;
		diving.combo[2] = reg5;
	}

	@Override
	public List<ActionScore> compute() {
		Action request = Action.from(diving.goal.get(0));
		if (getMyScore() > 21 && !incomingThreat()) {
			return Collections.emptyList();
		}
		return Stream.of(Action.values())
				.map(action -> new ActionScore(action,
						action.equals(request) ? (diving.combo[Player.playerIdx] > 0 ? 2 : 1) * 2 : 0))
				.toList();
	}

	boolean incomingThreat() {
		int myScore = getMyScore();
		return (int) IntStream.range(0, Game.PLAYER_COUNT)
				.filter(i -> i != Player.playerIdx)
				.filter(i -> (diving.points[i] + diving.combo[i]) >= myScore)
				.count() > 0;
	}

	@Override
	public int position() {
		int myScore = getMyScore();
		return (int) IntStream.range(0, Game.PLAYER_COUNT)
				.filter(i -> i != Player.playerIdx)
				.map(i -> diving.points[i])
				.filter(i -> i > myScore)
				.count();
	}

	private int getMyScore() {
		return diving.points[Player.playerIdx];
	}
}
