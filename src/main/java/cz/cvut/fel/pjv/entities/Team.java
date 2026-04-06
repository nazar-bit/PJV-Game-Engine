package cz.cvut.fel.pjv.entities;

import java.util.ArrayList;


/**
 * The Team enum defines the different teams in the game along with the classes that
 * should be exempt from collision checks when interacting with other entities of the same team.
 *
 * <p>
 * Each team specifies a list of classes that are considered collision-exempt. For example,
 * players are exempt with respect to Player and TransmittedPlayer classes.
 * </p>
 */
public enum Team {
    Players(createTeamPlayer()), Enemies(createTeamEnemies()), Neutral(createTeamNeutral());


    private final ArrayList<Class<?>> collisionExempt;
    /**
     * Constructs a team with a specific list of collision exempt classes.
     *
     * @param collisionExempt list of classes to be exempt from collision detection.
     */
    Team(ArrayList<Class<?>> collisionExempt) {
        this.collisionExempt = collisionExempt;
    }

    public ArrayList<Class<?>> getCollisionExempt() {
        return collisionExempt;
    }




    private static ArrayList<Class<?>> createTeamPlayer()
    {
        ArrayList<Class<?>> list = new ArrayList<>();
        list.add(Player.class);
        list.add(TransmittedPlayer.class);
        return list;
    }


    private static ArrayList<Class<?>> createTeamEnemies()
    {
        ArrayList<Class<?>> list = new ArrayList<>();
        list.add(Enemy.class);
        return list;
    }


    private static ArrayList<Class<?>> createTeamNeutral()
    {
        ArrayList<Class<?>> list = new ArrayList<>();
        return list;
    }
}



