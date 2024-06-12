package fr.tcordel.mini.strats;

import java.util.List;
import java.util.stream.Stream;

import fr.tcordel.Action;
import fr.tcordel.Player;
import fr.tcordel.mini.Archery;

public class ArcheryStrategy implements Strategy {

	private final Archery archery;

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
	}

	@Override
	public List<ActionScore> compute() {
		return Stream.of(Action.values())
				.map(action -> new ActionScore(action,
						getScore(action, archery.cursors.get(Player.playerIdx),
								archery.wind.get(0))))
				.toList();
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
}
