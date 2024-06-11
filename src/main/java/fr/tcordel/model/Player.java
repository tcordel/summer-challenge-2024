package fr.tcordel.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
			List<Integer> myReg = new ArrayList<>();
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
					gpus.add(gpu);
					myReg.add(reg);
				}
				System.err.println("Frame %d, position %d and stunt %d".formatted(i, reg, stuntCounter));
			}
			in.nextLine();
			if (gpus.isEmpty()) {
				continue;
			}
			int closestTrap = Integer.MAX_VALUE;
			for (int i = 0; i < gpus.size(); i++) {
				Integer reg = myReg.get(i);
				String subpath = gpus.get(i).substring(reg);
				int trap = subpath.indexOf("#");
				if (trap == 0 && subpath.length() > 1) {
					trap = subpath.indexOf("#", 1);
				}
				System.err.println("Path %s, at index %d, trap... %d".formatted(subpath, reg, trap));
				if (trap > 0 && trap < closestTrap) {
					closestTrap = trap;
				}
			}
			String action = switch (closestTrap) {
				case 1 -> "UP";
				case 2 -> "LEFT";
				case 3 -> "DOWN";
				default -> "RIGHT";
			};
			System.out.println(action);
		}
	}
}
