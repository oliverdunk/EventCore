package dev.imabad.mceventmanager.core.api.player;

import java.util.UUID;

public interface IPlayer {

    UUID getUUID();

    String getUsername();

    ILocation getLocation();

    void sendMessage(String message);

    void kick(String reason);

    void teleport(ILocation location);

    void changeServer(String server);

    void toggleFlight();

    void setFlightEnabled(boolean enabled);

    boolean isFlightEnabled();

    void setVisible(boolean visible);

    boolean isVisible();
}
