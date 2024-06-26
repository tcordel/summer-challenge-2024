package fr.tcordel.mini;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import fr.tcordel.Action;
import fr.tcordel.Game;
import fr.tcordel.Player;

public class HurdleRace extends MiniGame {

	public static final int STUN_DURATION = 2;

	public String map;
	public int positions[] = new int[Game.PLAYER_COUNT];
	public int stunTimers[] = new int[Game.PLAYER_COUNT];

	public boolean dead[] = new boolean[Game.PLAYER_COUNT];
	public boolean jumped[] = new boolean[Game.PLAYER_COUNT];

	private int finished[] = new int[Game.PLAYER_COUNT];
	private int rank = 0;

	public HurdleRace() {
		type = "hurdles";
	}

	@Override
	public void reset(Random random) {
		// Generate new map
		int startStretch = 3 + random.nextInt(5);
		int hurdles = 3 + random.nextInt(4);
		int length = 30;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < startStretch; ++i) {
			sb.append('.');
		}
		for (int i = 0; i < hurdles; ++i) {
			if (random.nextBoolean()) {
				sb.append("#....");
			} else {
				sb.append("#...");
			}
		}
		while (sb.length() < length) {
			sb.append(".");
		}

		map = sb.toString().substring(0, length - 1) + ".";

		for (int i = 0; i < Game.PLAYER_COUNT; ++i) {
			stunTimers[i] = 0;
			positions[i] = 0;
			finished[i] = -1;
			rank = 0;
			jumped[i] = false;
		}

	}

	@Override
	public String getGPU() {
		return map;
	}

	@Override
	public int[] getRegisters() {
		return MiniGame.fillRegisters(positions, stunTimers);
	}

	@Override
	public void tick(List<Action> actions) {
		int maxX = map.length() - 1;
		int countFinishes = 0;

		for (int i = 0; i < actions.size(); ++i) {
			jumped[i] = false;

			Action a = actions.get(i);
			if (a == null) {
				dead[i] = true;
				continue;
			}
			if (stunTimers[i] > 0) {
				stunTimers[i] -= 1;
				continue;
			}

			if (finished[i] > -1) {
				continue;
			}

			int moveBy = 0;
			boolean jump = false;
			switch (a) {
				case DOWN:
					moveBy = 2;
					break;
				case LEFT:
					moveBy = 1;
					break;
				case RIGHT:
					moveBy = 3;
					break;
				case UP:
					moveBy = 2;
					jump = true;
					jumped[i] = true;
					break;
			}
			for (int x = 0; x < moveBy; ++x) {
				positions[i] = Math.min(maxX, positions[i] + 1);
				if (map.charAt(positions[i]) == '#' && !jump) {
					stunTimers[i] = STUN_DURATION;
					break;
				}
				if (positions[i] == maxX && finished[i] == -1) {
					finished[i] = rank;
					countFinishes++;
					break;
				}
				jump = false;
			}
		}
		rank += countFinishes;

	}

	@Override
	public boolean isGameOver() {
		int count = 0;
		for (int i = 0; i < finished.length; ++i) {
			if (finished[i] > -1 && Game.EARLY_RACE_END) {
				return true;
			}
			if (finished[i] > -1 || dead[i]) {
				count++;
			}
		}

		return count >= 2;
	}

	@Override
	public int[] getRankings() {
		if (Game.EARLY_RACE_END) {
			Map<Integer, Double> scoreByPlayer = new TreeMap<>();
			for (int i = 0; i < positions.length; ++i) {
				scoreByPlayer.put(i, (double) (dead[i] ? -1 : positions[i]));
			}

			return MiniGame.createRankings(scoreByPlayer);
		}

		int[] rankings = new int[Game.PLAYER_COUNT];
		for (int i = 0; i < finished.length; ++i) {
			if (finished[i] == -1) {
				rankings[i] = rank;
			} else {
				rankings[i] = finished[i];
			}
		}
		return rankings;
	}

	@Override
	public String getName() {
		return "Hurdle Race";
	}

	@Override
	public double simulate(Action[] actions, int sizeOf) {
		int maxX = map.length() - 1;
		int stun = stunTimers[Player.playerIdx];
		int position = positions[Player.playerIdx];
		int startingPosition = position;
		int finishedAt = -1;
		int stunCounter = 0;
		for (int i = 0; i < sizeOf; i++) {
			if (stun > 0) {
				stun -= 1;
				continue;
			}
			Action a = actions[i];
			int moveBy = 0;
			boolean jump = false;
			switch (a) {
				case DOWN:
					moveBy = 2;
					break;
				case LEFT:
					moveBy = 1;
					break;
				case RIGHT:
					moveBy = 3;
					break;
				case UP:
					moveBy = 2;
					jump = true;
					break;
			}
			for (int x = 0; x < moveBy; ++x) {
				position = Math.min(maxX, position + 1);
				if (map.charAt(position) == '#' && !jump) {
					stun = STUN_DURATION;
					stunCounter++;
					break;
				}
				if (position == maxX) {
					finishedAt = i;
					break;
				}
				jump = false;
			}
		}
		if (finishedAt > 0) {
			return (100 - finishedAt * 10) / (stunCounter < 1 ? 1 : Math.max(stunCounter, 5));
		}
		return (1 + position - startingPosition) / (stunCounter < 1 ? 1 : Math.max(stunCounter, 5));
	}

	public List<Action> getBestMove() {
		List<Action> actions = new ArrayList<>();
		int position = positions[Player.playerIdx];
		while (position < map.length()) {
			String subMap = map.substring(position);
			int indexOf = subMap.indexOf("#", 1);

			Action a = switch (indexOf) {
				case 1, 3 -> Action.UP;
				case 2 -> Action.LEFT;
				default -> Action.RIGHT;
			};

			int move = switch (a) {
				case UP, DOWN -> 2;
				case LEFT -> 1;
				case RIGHT -> 3;
			};
			position += move;
			actions.add(a);
		}
		return actions;
	}
}
