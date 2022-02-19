package org.secuso.privacyfriendlywerwolf.enums;

/**
 * All game phases are defined here
 *
 * @author Tobias Kowalski <tobias.kowalski@stud.tu-darmstadt.de>
 */
public enum GamePhaseEnum {
    GAME_START,
    PHASE_WEREWOLF_START,
    PHASE_WEREWOLF_VOTING,
    PHASE_WEREWOLF_END,
    PHASE_WITCH_ELIXIR,
    PHASE_WITCH_POISON,
    PHASE_SEER,
    PHASE_SEER_END,
    PHASE_DAY_START,
    PHASE_DAY_VOTING,
    PHASE_DAY_END;

    //if it should ever be decided not to follow a cycle for the phases,
    //you would need a switch or give each phase the ordinal of the next phase as attribute
    public GamePhaseEnum getNextPhase(){
        int nextPhaseOrdinal = (this.ordinal() + 1) % values().length;
        for (GamePhaseEnum phase : values()) {
            if(phase.ordinal() == nextPhaseOrdinal) return phase;
        }
        return GAME_START;
    }
}
