package fr.tcordel;

import java.util.List;
import fr.tcordel.algorythms.Genetic;
import fr.tcordel.algorythms.LocalMaximum;
import fr.tcordel.mini.strats.GameOverStrategy;
import fr.tcordel.mini.strats.Strategy;

public class StrategySupervisor {

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
			return new Genetic(predictableStrats, turn).findBestAction();
		} else {
			return new LocalMaximum(strats).findBestAction();
		}
	}

}
