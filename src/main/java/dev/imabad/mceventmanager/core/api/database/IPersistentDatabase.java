package dev.imabad.mceventmanager.core.api.database;

import dev.imabad.mceventmanager.core.api.objects.EventPlayer;
import dev.imabad.mceventmanager.core.api.objects.EventRank;

import java.util.List;
import java.util.UUID;

public interface IPersistentDatabase extends IDatabase {

    EventPlayer getOrCreatePlayer(UUID uuid, String username);

    EventPlayer getPlayer(String username);

    EventPlayer getPlayer(UUID uuid);

    void savePlayer(EventPlayer player);

    List<EventPlayer> getPlayers();

    EventRank getLowestRank();

    List<EventRank> getRanks();

    void saveRank(EventRank eventRank);

    Object get(Object object);

    void save(Object object);

}
