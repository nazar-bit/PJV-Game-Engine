package cz.cvut.fel.pjv.entities;


/// What the entity is doing right now
public enum EntityStates {
    PURSUE,
    PURSUE_TO_LAST_KNOWN_POS,
    IDLE,
    FOLLOW_PLAYER,
    FOLLOW_PATH,
    GO_TO_SPAWN,
    WONDER,
    PREPARE_FOR_ATTACK,
}
