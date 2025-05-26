package hades.session;

import common.logger.Logger;
import dobby.session.ISession;
import dobby.session.ISessionStore;
import dobby.session.Session;
import thot.connector.Connector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HadesSessionStore implements ISessionStore {
    private static final Logger LOGGER = new Logger(HadesSessionStore.class);
    private static final String BUCKET_NAME = "dobbySession";

    @Override
    public Optional<ISession> find(String sessionId) {
        final ISession session = Connector.read(BUCKET_NAME, sessionId, Session.class);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.of(session);
    }

    @Override
    public void update(ISession session) {
        Connector.writeCreateVolatile(BUCKET_NAME, session.getId(), (Session) session);
    }

    @Override
    public void remove(String sessionId) {
        Connector.delete(BUCKET_NAME, sessionId);
    }

    @Override
    public Map<String, Long> getSessionAges() {
        final ISession[] sessions = Connector.readPattern(BUCKET_NAME, ".*", Session.class);

        final Map<String, Long> lastAccessed = new HashMap<>();

        for (ISession session: sessions) {
            lastAccessed.put(session.getId(), session.getLastAccessed());
        }

        return lastAccessed;
    }
}
