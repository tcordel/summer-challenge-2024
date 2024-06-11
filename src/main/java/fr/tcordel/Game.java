
package fr.tcordel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import fr.tcordel.mini.Archery;
import fr.tcordel.mini.Diving;
import fr.tcordel.mini.HurdleRace;
import fr.tcordel.mini.MiniGame;
import fr.tcordel.mini.RollerSpeedSkating;

public class Game {
    public static int PLAYER_COUNT = 3;
    public static final int REGISTER_COUNT = 7;

    public static boolean EARLY_RACE_END = true;

    List<MiniGame> minigames;
    List<Player> players;
    Random random;
    int[] resets;

    public void init() {

        resets = new int[4];

        minigames = new ArrayList<>(4);

        int leagueLevel = 2;

        minigames.add(new HurdleRace());

        if (leagueLevel == 2) {
            minigames.add(new HurdleRace());
            minigames.add(new HurdleRace());
            minigames.add(new HurdleRace());
        } else if (leagueLevel > 2) {
            minigames.add(new Archery());
            minigames.add(new RollerSpeedSkating());
            minigames.add(new Diving());
        }

        for (MiniGame minigame : minigames) {
            minigame.reset(random);
        }

        // players = gameManager.getPlayers();
        // for (Player p : players) {
        //     p.init(minigames.size());
        // }
    }

    public void resetGameTurnData() {
        // players.forEach(Player::reset);

    }

    public void performGameUpdate(int turn) {
        for (MiniGame minigame : minigames) {
            int gameIdx = minigames.indexOf(minigame);
            if (minigame.shouldReset) {
                minigame.resetting = true;
                minigame.reset(random);
                resets[minigames.indexOf(minigame)]++;
                minigame.shouldReset = false;
            } else {
                minigame.resetting = false;
                minigame.tick(players.stream().map(Player::getAction).collect(Collectors.toList()));

                if (minigame.isGameOver()) {
                    minigame.shouldReset = true;
                    int[] rankings = getRankings(minigame);
                    for (int i = 0; i < rankings.length; ++i) {
                        // Increment player i's medals according to their rank in the minigame
                        players.get(i).medals[gameIdx][rankings[i]]++;
                    }
                    for (int i = 0; i < rankings.length; ++i) {
                        Player p = players.get(i);
                    }
			}
            }
        }
    }

    public int[] getRankings(MiniGame minigame) {
        int[] rankings = minigame.getRankings();
        for (int i = 0; i < rankings.length; ++i) {
            if (!players.get(i).isActive()) {
                rankings[i] = 2;
            }
        }
        return rankings;
    }

    private String getMedalName(int rank) {
        if (rank == 0) {
            return "GOLD medal";
        }
        if (rank == 1) {
            return "SILVER medal";
        }
        if (rank == 2) {
            return "BRONZE medal";
        }

        return "pat on the back";
    }

    public boolean isGameOver() {
        // return gameManager.getActivePlayers().size() < 2;
		return false;
    }

    // public void onEnd() {
    //     String[] scoreTexts = new String[players.size()];
    //     for (Player p : players) {
    //         if (!p.isActive()) {
    //             p.setScore(-1);
    //             scoreTexts[p.getIndex()] = "-";
    //         } else {
    //             p.setScore(p.getPoints());
    //             scoreTexts[p.getIndex()] = p.getScoreText();
    //
    //         }
    //     }
    //     int[] scores = players.stream().mapToInt(Player::getScore).toArray();
    //     int[][] medals = players.stream().map(Player::getMedalsTotal).toArray(int[][]::new);
    //
    //     computeMetadata();
    //
    // }

    private void computeMetadata() {
        int goldMedals = 0;
        for (Player p : players) {
            goldMedals += p.getMedalsTotal()[0];
        }
    }

}
