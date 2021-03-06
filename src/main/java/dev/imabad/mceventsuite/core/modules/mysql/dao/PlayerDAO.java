package dev.imabad.mceventsuite.core.modules.mysql.dao;

import dev.imabad.mceventsuite.core.BaseEventPlayer;
import dev.imabad.mceventsuite.core.api.objects.EventPlayer;
import dev.imabad.mceventsuite.core.modules.mysql.MySQLDatabase;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import java.sql.*;
import java.util.UUID;

public class PlayerDAO extends DAO {

    public PlayerDAO(MySQLDatabase mySQLDatabase) {
        super(mySQLDatabase);
    }


    /**
     * Get or create a player using a UUID or username
     * If a player does not exist a new one is created using the provided UUID and username.
     *
     * @param uuid      - UUID of the player to find or create
     * @param username  - Username of the player to find or create
     * @return          - An instance of EventPlayer
     * @see             EventPlayer
     */
    public EventPlayer getOrCreatePlayer(UUID uuid, String username){
        try (Session session = mySQLDatabase.getSession()) {
            EventPlayer eventPlayer = session.get(EventPlayer.class, uuid);
            if (eventPlayer == null) {
                eventPlayer = getPlayer(username);
                if (eventPlayer == null) {
                    eventPlayer = new BaseEventPlayer(uuid, username);
                    savePlayer(eventPlayer);
                }
            }
            return eventPlayer;
        }
    }

    /**
     * Get an EventPlayer by their UUID
     * This is the preferred method
     *
     * @param uuid - UUID of the player to find
     * @return     - An instance of EventPlayer or null
     * @see        EventPlayer
     */
    public EventPlayer getPlayer(UUID uuid){
        try (Session session = mySQLDatabase.getSession()) {
            return session.get(EventPlayer.class, uuid);
        }
    }

    /**
     * Get an EventPlayer by their username
     * Avoid this where possible.
     *
     * @param username  - Username of the player to find
     * @return          - the player with that username or null
     * @see             EventPlayer
     */
    public EventPlayer getPlayer(String username){
        try(Session session = mySQLDatabase.getSession()) {
            Query<EventPlayer> q = session.createQuery("select p FROM EventPlayer p WHERE p.lastUsername = :username", EventPlayer.class).setParameter("username", username);
            try {
                return q.getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        }
    }

    /**
     * Saves an EventPlayer to the database
     * You should avoid this where possible and ask the central service to do this for you
     * @param player - The EventPlayer you wish to save
     */
    @Deprecated
    public void savePlayer(EventPlayer player){
        Transaction tx = null;
        try (Session session = mySQLDatabase.getSession()) {
            tx = session.beginTransaction();
            session.saveOrUpdate(player);
            tx.commit();
        } catch (RuntimeException e) {
            assert tx != null;
            tx.rollback();
            e.printStackTrace();
        }
    }
}
