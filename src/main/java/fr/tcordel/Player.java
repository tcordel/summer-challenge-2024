package fr.tcordel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.tcordel.mini.HurdleRace;

public class Player {

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		int playerIdx = in.nextInt();
		int nbGames = in.nextInt();
		if (in.hasNextLine()) {
			in.nextLine();
		}

		while (true) {
			for (int i = 0; i < 3; i++) {
				String scoreInfo = in.nextLine();
			}
			List<String> gpus = new ArrayList<>();
			for (int i = 0; i < nbGames; i++) {
				String gpu = in.next();
				boolean ended = gpu.equals("GAME_OVER");
				int reg0 = in.nextInt();
				int reg1 = in.nextInt();
				int reg2 = in.nextInt();
				int reg = switch (playerIdx) {
					case 0 -> reg0;
					case 1 -> reg1;
					case 2 -> reg2;
					default -> throw new IllegalArgumentException("Unexpected value: " + playerIdx);
				};
				int reg3 = in.nextInt();
				int reg4 = in.nextInt();
				int reg5 = in.nextInt();
				int stuntCounter = switch (playerIdx) {
					case 0 -> reg3;
					case 1 -> reg4;
					case 2 -> reg5;
					default -> throw new IllegalArgumentException("Unexpected value: " + playerIdx);
				};
				int reg6 = in.nextInt();
				if (!ended && stuntCounter == 0) {
					gpus.add(gpu.substring(reg));
				}
				// System.err
				// 		.println("Frame %d, gpu %s and stunt %d".formatted(i, gpus.get(gpus.size() - 1), stuntCounter));
			}
			in.nextLine();
			if (gpus.isEmpty()) {
				continue;
			}
			Action selected = Stream.of(Action.values())
					.map(action -> new ActionScore(action, getScore(action, gpus)))
			.peek(action -> System.err.println("Action %s with score %d".formatted(action.action(), action.score())))
					.max(Comparator
				.comparingInt(ActionScore::score)
			.thenComparingInt(as -> HurdleRace.actionScore(as.action())))
					.map(ActionScore::action)
					.orElse(Action.RIGHT);
			System.out.println(selected);
		}
	}

	private static int getScore(Action action, List<String> gpus) {
		int serchOffset = switch (action) {
			case UP -> 2;
			case LEFT, DOWN, RIGHT -> 1;
		};
		int move = switch (action) {
			case UP, DOWN -> 2;
			case LEFT -> 1;
			case RIGHT -> 3;
		};
		return (int) gpus.stream()
				.filter(gpu -> {
					int nextTrap = gpu.indexOf("#", serchOffset);
					System.err.println("nexttrap for action %s on gpu %s is %d".formatted(action, gpu, nextTrap));
					return nextTrap == -1 || nextTrap > move;
				})
				.count();
	}

	record ActionScore (Action action,
		int score) {
	}

	String message;
	Action action;
	int[][] medals;

	public Player() {
	}

	public void init(int gameCount) {
		medals = new int[gameCount][3];
	}

	public int getPoints() {
		int p = 1;
		for (int i = 0; i < medals.length; ++i) {
			p *= (3 * medals[i][0] + medals[i][1]);
		}
		return p;
	}

	public void reset() {
		this.message = null;
		this.action = null;
	}

	public void setMessage(String message) {
		this.message = message;

	}

	public void setAction(Action button) {
		this.action = button;
	}

	public Action getAction() {
		return action;
	}

	public int[] getMedalsTotal() {
		int[] total = new int[3];
		for (int i = 0; i < medals.length; ++i) {
			int golds = medals[i][0];
			int silvers = medals[i][1];
			int bronzes = medals[i][2];
			total[0] += golds;
			total[1] += silvers;
			total[2] += bronzes;
		}
		return total;
	}

	public String getScoreText() {
		List<String> minigameScores = new ArrayList<>(medals.length);
		for (int i = 0; i < medals.length; ++i) {
			int golds = medals[i][0];
			int silvers = medals[i][1];
			if (golds == 0 && silvers > 1) {
				minigameScores.add(String.format("%d🥈", silvers));
			} else if (golds > 0 && silvers == 0) {
				minigameScores.add(String.format("%d🥇", golds));
			} else if (golds == 0 && silvers == 0) {
				minigameScores.add("0");
			} else {
				minigameScores.add(String.format("%d🥇+%d🥈", golds, silvers));
			}
		}
		return minigameScores.stream().collect(Collectors.joining(" * ")) + " = " + getPoints();
	}

    public boolean isActive() {
		return true;
    }

}
