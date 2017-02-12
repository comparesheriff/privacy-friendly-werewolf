package org.secuso.privacyfriendlywerwolf.client;

import android.content.Intent;
import android.util.Log;

import org.secuso.privacyfriendlywerwolf.R;
import org.secuso.privacyfriendlywerwolf.activity.GameActivity;
import org.secuso.privacyfriendlywerwolf.activity.MainActivity;
import org.secuso.privacyfriendlywerwolf.activity.StartClientActivity;
import org.secuso.privacyfriendlywerwolf.context.GameContext;
import org.secuso.privacyfriendlywerwolf.controller.Controller;
import org.secuso.privacyfriendlywerwolf.model.NetworkPackage;
import org.secuso.privacyfriendlywerwolf.model.Player;
import org.secuso.privacyfriendlywerwolf.server.ServerGameController;
import org.secuso.privacyfriendlywerwolf.util.Constants;
import org.secuso.privacyfriendlywerwolf.util.ContextUtil;
import org.secuso.privacyfriendlywerwolf.util.GameUtil;

import java.util.List;

//import org.secuso.privacyfriendlywerwolf.activity.GameHostActivity;

/**
 * updates the model on the client, aswell as the view on the client and initiates communication to the server
 *
 * @author Tobias Kowalski <tobias.kowalski@stud.tu-darmstadt.de>
 * @author Florian Staubach <florian.staubach@stud.tu-darmstadt.de>
 */
public class ClientGameController extends Controller {

    private static final String TAG = "ClientGameController";
    private static final ClientGameController GAME_CONTROLLER = new ClientGameController();

    ServerGameController serverGameController;

    Player me;
    long myId;

    StartClientActivity startClientActivity;
    GameActivity gameActivity;
    //GameHostActivity gameHostActivity;
    WebsocketClientHandler websocketClientHandler;
    GameContext gameContext;


    private ClientGameController() {
        Log.d(TAG, "GameController singleton created");
        websocketClientHandler = new WebsocketClientHandler();
        websocketClientHandler.setGameController(this);
        gameContext = GameContext.getInstance();
        //serverGameController = ServerGameController.getInstance();
    }

    public static ClientGameController getInstance() {
        return GAME_CONTROLLER;

    }

    public void startGame(GameContext gc) {
        //TODO: extract the roles of the players and give it to the activity
        //TODO: extract every other information which were send by the server

        gameContext.copy(gc);
        startClientActivity.startGame();
        //wait some time before the gameactivity has been created
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        gameActivity.outputMessage(R.string.progressBar_initial);
        gameActivity.longOutputMessage(R.string.gameStart_start);
        gameActivity.longOutputMessage(R.string.gameStart_hintRoles);


    }


    public void initiateWerewolfPhase() {

        gameActivity.outputMessage(R.string.message_werewolfes_awaken);
        //TODO: put into string.xml with translation.. everything
        gameActivity.longOutputMessage("Die Werwölfe erwachen und suchen sich ein Opfer!");
        gameActivity.longOutputMessage("Macht euch bereit für die Abstimmung!");


    }




    public void initiateWerewolfVotingPhase() {
        gameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int time = Integer.parseInt(gameContext.getSetting(GameContext.Setting.TIME_WEREWOLF));
                // TODO: there is an ASyncNetworkSocket exception when called here
                gameActivity.makeTimer(time).start();
            }
        });
        gameActivity.outputMessage(R.string.message_werewolfes_awaken);
        gameActivity.longOutputMessage("Die Werwölfe erwachen und suchen sich ein Opfer!");
        gameActivity.outputMessage(R.string.message_werewolfes_vote);
        //voting("Werewolf");

        Player ownPlayer = GameContext.getInstance().getPlayerById(myId);
        if (!ownPlayer.isDead() && ownPlayer.getPlayerRole().equals(Player.Role.WEREWOLF)) {
            gameActivity.openVoting();
        } else {
            //TODO: if its not your turn or your dead: do nothing or do smth here
            //gameActivity.showTextPopup(R.string.voting_dialog_otherVotingTitle, R.string.voting_dialog_otherVoting);
            // we non-werewolves dont have to vote, so we are done here
            try {
                NetworkPackage<GameContext.Phase> np = new NetworkPackage<GameContext.Phase>(NetworkPackage.PACKAGE_TYPE.DONE);
                np.setPayload(GameContext.Phase.PHASE_WEREWOLF_VOTING);
                websocketClientHandler.send(np);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void endWerewolfPhase() {


        gameActivity.longOutputMessage("Die Werwölfe haben ihr Opfer gefunden und schlafen wieder ein!");
        gameActivity.outputMessage(R.string.message_werewolfes_sleep);
        // TODO: only needed if GameMaster (GM) plays as well
        // go to the next state automatically (without GM interference)

                /*try {
                    NetworkPackage<GameContext.Phase> np = new NetworkPackage<GameContext.Phase>(NetworkPackage.PACKAGE_TYPE.DONE);
                    np.setPayload(GameContext.Phase.PHASE_WEREWOLF_END);
                    websocketClientHandler.send(np);
                } catch (Exception e) {
                    e.printStackTrace();
                }*/


    }


    public void initiateWitchPhase() {
        if (GameUtil.isWitchAlive()) {


            gameActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int time = Integer.parseInt(gameContext.getSetting(GameContext.Setting.TIME_WITCH));
                    gameActivity.makeTimer(time).start();
                }
            });
            // TODO: wenn die Hexe tot ist
            gameActivity.outputMessage(R.string.message_witch_awaken);
            gameActivity.longOutputMessage("Die Hexe erwacht!");
            gameActivity.longOutputMessage("Die Hexe entscheidet ob sie Tränke einsetzen möchte");
            if(gameContext.getPlayerById(myId).getPlayerRole().equals(Player.Role.WITCH)) {
                useElixir();
            }
            gameActivity.longOutputMessage("Die Hexe hat ihre Entscheidung getroffen!");

            // TODO: only needed if GameMaster (GM) plays as well
            // go to the next state automatically (without GM interference)
            //websocketClientHandler.send("nextPhase");
            gameActivity.outputMessage(R.string.message_witch_sleep);
            gameActivity.longOutputMessage("Die Hexe schläft nun wieder ein");
                /*try {
                    NetworkPackage<GameContext.Phase> np = new NetworkPackage<GameContext.Phase>(NetworkPackage.PACKAGE_TYPE.DONE);
                    np.setPayload(GameContext.Phase.PHASE_WITCH);
                    websocketClientHandler.send(np);
                } catch (Exception e) {
                    e.printStackTrace();
                }*/

        } else {
            gameActivity.longOutputMessage("Es ist keine Hexe im Spiel vorhanden.");
        }
    }

    public void initiateSeerPhase() {
        if (GameUtil.isSeerAlive()) {
            gameActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int time = Integer.parseInt(gameContext.getSetting(GameContext.Setting.TIME_SEER));
                    gameActivity.makeTimer(120).start();
                    // TODO: wenn die Hexe tot ist
                }
            });
            gameActivity.outputMessage(R.string.message_seer_awaken);
            gameActivity.longOutputMessage("Die Seherin erwacht!");
            gameActivity.longOutputMessage("Die Seherin wählt einen Spieler aus, dessen Karte sie sich ansehen möchte");

            if(gameContext.getPlayerById(myId).getPlayerRole().equals(Player.Role.SEER)) {
                useSeerPower();
            }

            gameActivity.longOutputMessage("Die Seherin kennt jetzt ein Geheimnis mehr!");

            // TODO: only needed if GameMaster (GM) plays as well
            // go to the next state automatically (without GM interference)
            //websocketClientHandler.send("nextPhase");
            gameActivity.outputMessage(R.string.message_seer_sleep);
            gameActivity.longOutputMessage("Die Seherin schläft nun wieder ein");

                /*
                try {
                    NetworkPackage<GameContext.Phase> np = new NetworkPackage<GameContext.Phase>(NetworkPackage.PACKAGE_TYPE.DONE);
                    np.setPayload(GameContext.Phase.PHASE_SEER);
                    websocketClientHandler.send(np);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                */
        } else {
            gameActivity.longOutputMessage("Es ist kein Seher im Spiel vorhanden.");
        }
    }

    public void initiateDayPhase() {
        Player killedPlayer = GameContext.getInstance().getPlayerById(ContextUtil.lastKilledPlayerID);
        gameActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int time = Integer.parseInt(gameContext.getSetting(GameContext.Setting.TIME_VILLAGER));
                gameActivity.makeTimer(time).start();
                // TODO: wenn die Hexe tot ist
            }
        });
        gameActivity.outputMessage(R.string.message_villagers_awaken);
        gameActivity.longOutputMessage("Es wird hell und alle Dorfbewohner erwachen aus ihrem tiefen Schlaf");
        gameActivity.longOutputMessage("Leider von uns gegangen ist: " + killedPlayer.getPlayerName());


        gameActivity.showTextPopup(R.string.votingResult_werewolf_title, R.string.votingResult_werewolf_text, killedPlayer.getPlayerName());
        gameActivity.updateGamefield();

        gameActivity.outputMessage(R.string.message_villagers_vote);
        gameActivity.longOutputMessage("Die übrigen Bewohner können jetzt abstimmen.");


    }

    public void initiateDayVotingPhase() {
        Player ownPlayer = GameContext.getInstance().getPlayerById(myId);
        if (!ownPlayer.isDead()) {
            gameActivity.openVoting();
        } else {
            //TODO: if its not your turn or your dead: do nothing or do smth here
            //gameActivity.showTextPopup(R.string.voting_dialog_otherVotingTitle, R.string.voting_dialog_otherVoting);
            try {
                NetworkPackage<GameContext.Phase> np = new NetworkPackage<GameContext.Phase>(NetworkPackage.PACKAGE_TYPE.DONE);
                //TODO: why this payload here ?
                //np.setPayload(GameContext.Phase.PHASE);
                websocketClientHandler.send(np);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void endDayPhase() {
        Player killedPlayer = GameContext.getInstance().getPlayerById(ContextUtil.lastKilledPlayerID);
        gameActivity.longOutputMessage("Die Abstimmung ist beendet...");
        gameActivity.longOutputMessage("Leider von uns gegangen ist: " + killedPlayer.getPlayerName());

        gameActivity.showTextPopup(R.string.votingResult_day_title, R.string.votingResult_day_text, killedPlayer.getPlayerName());
        gameActivity.updateGamefield();

        // TODO: only needed if GameMaster (GM) plays as well
        // go to the next state automatically (without GM interference)
        //websocketClientHandler.send("nextPhase");
        gameActivity.outputMessage(R.string.message_villagers_sleep);
        gameActivity.longOutputMessage("Alle schlafen wieder ein, es wird Nacht!");

                /*try {
                    NetworkPackage<GameContext.Phase> np = new NetworkPackage<GameContext.Phase>(NetworkPackage.PACKAGE_TYPE.DONE);
                    np.setPayload(GameContext.Phase.PHASE_DAY_END);
                    websocketClientHandler.send(np);
                } catch (Exception e) {
                    e.printStackTrace();
                }*/


    }

    public void voting(String role) {
        // Werwolf voting (only werewolves vote)
        // Dorfbewohner voting (every living role votes)
    }

    public void useElixir() {
        if(gameContext.getSetting(GameContext.Setting.WITCH_ELIXIR) == null) {
            gameActivity.askWitchForElixir();
        }
        else {
            usePoison();
        }
    }

    public void usePoison() {
        if(gameContext.getSetting(GameContext.Setting.WITCH_POISON) == null) {
            gameActivity.askWitchForPoison();
        }
        else {
            endWitchPhase();
        }

    }

    public void usedElixir() {

        String id = GameContext.getInstance().getSetting(GameContext.Setting.KILLED_BY_WEREWOLF);
        gameContext.setSetting(GameContext.Setting.WITCH_POISON, id);

    }

    public void endWitchPhase() {


        try {
            NetworkPackage<GameContext.Phase> np = new NetworkPackage<>(NetworkPackage.PACKAGE_TYPE.WITCH_RESULT);
            np.setPayload(GameContext.Phase.PHASE_WITCH);
            np.setOption(GameContext.Setting.WITCH_POISON.toString(), gameContext.getSetting(GameContext.Setting.WITCH_POISON));
            np.setOption(GameContext.Setting.WITCH_ELIXIR.toString(), gameContext.getSetting(GameContext.Setting.WITCH_ELIXIR));
            websocketClientHandler.send(np);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Method gets called if the witch presses a player card button
     * If the witch has the power to use one then the setting is set in the game context
     * @param selectedPlayer the Player the potion is used on
     */
    public void selectedPlayerForWitch(Player selectedPlayer) {

        String id = String.valueOf(selectedPlayer.getPlayerId());
        gameContext.setSetting(GameContext.Setting.WITCH_POISON, id);
        endWitchPhase();
    }

    public void useSeerPower() {



        Log.d(TAG, "Seherin setzt ihre Fähigkeit ein");
        // TODO: implement Seer logic
    }

    public void sendVotingResult(Player player) {
        // host
        if (myId == Constants.SERVER_PLAYER_ID) {
            serverGameController.handleVotingResult(player.getPlayerName());
        } else {
            try {
                NetworkPackage<String> np = new NetworkPackage<String>(NetworkPackage.PACKAGE_TYPE.VOTING_RESULT);
                np.setPayload(player.getPlayerName());
                websocketClientHandler.send(np);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public void handleVotingResult(String playerName) {
        Log.d(TAG, "voting_result received. Kill this guy: " + playerName);
        final Player playerToKill = GameContext.getInstance().getPlayerByName(playerName);
        playerToKill.setDead(true);
        ContextUtil.lastKilledPlayerID = playerToKill.getPlayerId();

        // if not the host
        if (myId != 0) {
            try {
                NetworkPackage<GameContext.Phase> np = new NetworkPackage<GameContext.Phase>(NetworkPackage.PACKAGE_TYPE.DONE);
                //TODO: what does this payload do ?
                //np.setPayload(GameContext.Phase.PHASE_WEREWOLF_VOTING);
                websocketClientHandler.send(np);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public void connect(String url, String playerName) {
        websocketClientHandler.startClient(url, playerName);
    }

    /**
     * Returns the player who got killed in the current round
     * @return the player object which got killed
     */
    public Player getPlayerKilledByWerewolfesName() {
        Long id = Long.getLong(gameContext.getSetting(GameContext.Setting.KILLED_BY_WEREWOLF));
        return gameContext.getPlayerById(id);
    }

    public GameActivity getGameActivity() {
        return gameActivity;
    }

    public void setGameActivity(GameActivity gameActivity) {
        this.gameActivity = gameActivity;
    }

    public StartClientActivity getStartClientActivity() {
        return startClientActivity;
    }

    public void setStartClientActivity(StartClientActivity startClientActivity) {
        this.startClientActivity = startClientActivity;
    }

    public WebsocketClientHandler getWebsocketClientHandler() {
        return websocketClientHandler;
    }

    public void setWebsocketClientHandler(WebsocketClientHandler websocketClientHandler) {
        this.websocketClientHandler = websocketClientHandler;
    }

    public Player getMyPlayer() {
        return gameContext.getPlayerById(myId);
    }

    public void setMe(Player me) {
        this.me = me;
    }

    public long getMyPlayerId() {
        return myId;
    }

    public void setMyId(long myId) {
        this.myId = myId;
    }

    public void updateMe() {
        this.me = gameContext.getPlayerById(this.myId);
        Log.d(TAG, "Me is now: " + me.getPlayerName() + "  isDead?: " + me.isDead());
    }

    public void setServerGameController() {
        serverGameController = ServerGameController.getInstance();
    }

    public List<Player> getPlayerList() {
        return gameContext.getPlayersList();
    }

    public void showSuccesfulConnection() {
        if(myId!=0) {
            this.startClientActivity.showConnected();
        }
    }

    public void abortGame() {
        destroy();

        // go back to start screen
        Intent intent = new Intent(gameActivity, MainActivity.class);
        gameActivity.startActivity(intent);
    }

    /**
     * Destroy all game data and reset to 0.
     * After this you are able to start a new game without any old data
     */
    public void destroy() {
        gameContext = new GameContext();
        websocketClientHandler.destroy();
        if(serverGameController != null) {
            serverGameController.destroy();
        }
        me = new Player();
        System.gc();
    }
}
