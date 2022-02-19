package org.secuso.privacyfriendlywerwolf;

import org.junit.Assert;
import org.junit.Test;
import org.secuso.privacyfriendlywerwolf.enums.GamePhaseEnum;


public class GamePhaseEnumTest {

    @Test
    public void shouldWerewolfStartFollowGameStart() {
        GamePhaseEnum gameStart = GamePhaseEnum.GAME_START;
        GamePhaseEnum nextPhase = gameStart.getNextPhase();
        Assert.assertEquals(nextPhase, GamePhaseEnum.PHASE_WEREWOLF_START);
    }

    @Test
    public void shouldGameStartFollowDayEnd() {
        GamePhaseEnum gameStart = GamePhaseEnum.PHASE_DAY_END;
        GamePhaseEnum nextPhase = gameStart.getNextPhase();
        Assert.assertEquals(nextPhase, GamePhaseEnum.GAME_START);
    }
}
