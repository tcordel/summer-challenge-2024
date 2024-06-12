package fr.tcordel.mini.strats;

import java.util.List;
import java.util.stream.Stream;

import fr.tcordel.Action;
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
		return Stream.of(Action.values())
				.map(action -> new ActionScore(action,
						action.equals(request) ? (diving.combo[Player.playerIdx] > 0 ? 2 : 1) : 0))
				.toList();
	}

}
