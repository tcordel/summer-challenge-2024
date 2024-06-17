package fr.tcordel;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import fr.tcordel.mini.strats.Strategy;

public class Player {

	public static int playerIdx = 0;
	private static long turnStartedAt = 0L;
	public static int turn = 0;
	public static List<Player> players = new ArrayList<>();

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		playerIdx = in.nextInt();
		int nbGames = in.nextInt();

		for (int i = 0; i < 3; i++) {
			Player player = new Player();
			player.init(nbGames);
			players.add(player);
		}
		if (in.hasNextLine()) {
			in.nextLine();
		}

		// game loop
		while (true) {
			turn++;
			for (int i = 0; i < 3; i++) {
				players.get(i).refresh(in.nextLine());
			}
			List<Strategy> strategies = new ArrayList<>();
			for (int i = 0; i < nbGames; i++) {
				String gpu = in.next();
				int reg0 = in.nextInt();
				int reg1 = in.nextInt();
				int reg2 = in.nextInt();
				int reg3 = in.nextInt();
				int reg4 = in.nextInt();
				int reg5 = in.nextInt();
				int reg6 = in.nextInt();
				strategies.add(Strategy.builder(i, gpu, reg0, reg1, reg2, reg3, reg4, reg5, reg6));
			}
			in.nextLine();
			turnStartedAt = System.currentTimeMillis();

			StrategySupervisor supervisor = new StrategySupervisor(strategies);
			Action selectedAction = supervisor.process();
			// Write an action using System.out.println()
			// To debug: System.err.println("Debug messages...");

			System.out.println(selectedAction);
		}
	}

	public static boolean hasTime(int offset) {
		return (System.currentTimeMillis() - turnStartedAt) < (40 - offset);
	}

	String message;
	Action action;
	int[][] medals;
	int totalPoints;

	public Player() {
	}

	public void init(int gameCount) {
		medals = new int[gameCount][3];
	}

	public int getPoints() {
		int p = 1;
		for (int i = 0; i < medals.length; ++i) {
			p *= getPointsForGame(i);
		}
		return p;
	}

	public int getPointsForGame(int gameId) {
		return (3 * medals[gameId][0] + medals[gameId][1]);
	}

	public void reset() {
		this.message = null;
		this.action = null;
	}

	public void refresh(String scoreInfo) {
		String[] scores = scoreInfo.split(" ");
		totalPoints = Integer.parseInt(scores[0]);
		medals[0][0] = Integer.parseInt(scores[1]);
		medals[0][1] = Integer.parseInt(scores[2]);
		medals[0][2] = Integer.parseInt(scores[3]);
		medals[1][0] = Integer.parseInt(scores[4]);
		medals[1][1] = Integer.parseInt(scores[5]);
		medals[1][2] = Integer.parseInt(scores[6]);
		medals[2][0] = Integer.parseInt(scores[7]);
		medals[2][1] = Integer.parseInt(scores[8]);
		medals[2][2] = Integer.parseInt(scores[9]);
		medals[3][0] = Integer.parseInt(scores[10]);
		medals[3][1] = Integer.parseInt(scores[11]);
		medals[3][2] = Integer.parseInt(scores[12]);
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
