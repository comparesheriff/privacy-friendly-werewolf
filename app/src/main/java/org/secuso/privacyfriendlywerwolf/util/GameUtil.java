package org.secuso.privacyfriendlywerwolf.util;

import android.util.Log;

import org.secuso.privacyfriendlywerwolf.context.GameContext;
import org.secuso.privacyfriendlywerwolf.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class regarding the the game logic.
 *
 * @author Tobias Kowalski <Tobias.Kowalski@stud.tu-darmstadt.de>
 */
public class GameUtil {

    private final String TAG = "GameUtil";

    public List<Player> getAllLivingPlayers() {
        List<Player> livingPlayers = new ArrayList<>();
        List<Player> players = GameContext.getInstance().getPlayersList();
        for (Player player : players) {
            if (!player.isDead()) {
                livingPlayers.add(player);
            }
        }
        return livingPlayers;
    }

    /**
     * Count the number of living innocent players
     * @return number of living non-werewolves
     */
    public int getInnocentCount() {
        List<Player> players = GameContext.getInstance().getPlayersList();
        int count = 0;
        for (Player p : players) {
            if(!p.isDead() && p.getPlayerRole()!= Player.Role.WEREWOLF) {
                count++;
            }
        }
        return count;
    }

    /**
     * Count the number of living werewolve players
     * @return number of living werewolves
     */
    public int getWerewolfCount() {
        List<Player> players = GameContext.getInstance().getPlayersList();
        int count = 0;
        for (Player p : players) {
            if(!p.isDead() && p.getPlayerRole() == Player.Role.WEREWOLF) {
                count++;
            }
        }
        return count;
    }



    public boolean isSeerAlive() {
        List<Player> players = GameContext.getInstance().getPlayersList();
        for (Player player : players) {
            if (player.getPlayerRole().equals(Player.Role.SEER)) {
                if (!player.isDead()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isWitchAlive() {
        List<Player> players = GameContext.getInstance().getPlayersList();
        for (Player player : players) {
            if (player.getPlayerRole().equals(Player.Role.WITCH)) {
                if (!player.isDead()) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Player> getAllLivingWerewolfes() {
        List<Player> werewolfes = new ArrayList<>();
        List<Player> players = GameContext.getInstance().getPlayersList();
        for (Player player : players) {
            Log.d(TAG, "player " + player + " is living and werewolf?");
            if (player.getPlayerRole().equals(Player.Role.WEREWOLF) && !player.isDead()) {
                Log.d(TAG, "yes it is");
                werewolfes.add(player);
            }
        }
        return werewolfes;
    }
}
