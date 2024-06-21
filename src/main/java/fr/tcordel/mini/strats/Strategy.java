package fr.tcordel.mini.strats;

import java.util.List;

import fr.tcordel.Action;
import fr.tcordel.Player;

public interface Strategy {

	List<ActionScore> compute();

	double simulate(Action[] actions, int sizeOf, int playerIdx);

	int position();

	int nbOfTurnLeft();

	default void init() {

	}

	public static Strategy builder(int gameId,
			String gpu,
			int reg0,
			int reg1,
			int reg2,
			int reg3,
			int reg4,
			int reg5,
			int reg6) {
		boolean willFinishBeforeGameEnd = Player.turn - 1 + (gameId != 2 ? gpu.length() : reg6) <= 100;

		if (gpu.equals("GAME_OVER") || !willFinishBeforeGameEnd) {
			return new GameOverStrategy();
		}
		return switch (gameId) {
			case 0 -> new HurdleRaceStrategy(gpu, reg0, reg1, reg2, reg3, reg4, reg5, reg6);
			case 1 -> new ArcheryStrategy(gpu, reg0, reg1, reg2, reg3, reg4, reg5, reg6);
			case 2 -> new RollerSpeedSkatingStrategy(gpu, reg0, reg1, reg2, reg3, reg4, reg5, reg6);
			case 3 -> new DivingStrategy(gpu, reg0, reg1, reg2, reg3, reg4, reg5, reg6);
			default -> new GameOverStrategy();
		};
	}

	String getGameName();

	public static String getGameName(int gameId) {
		return switch (gameId) {
			case 0 -> "Race";
			case 1 -> "Archer";
			case 2 -> "Roller";
			case 3 -> "Diving";
			default -> "Unknown";
		};
	}
}
