package hades.session;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import common.logger.Logger;
import dobby.session.ISession;
import dobby.session.ISessionStore;
import dobby.session.Session;
import dobby.session.SessionWrapper;
import dobby.session.service.ISessionService;
import thot.connector.Connector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RegisterFor(ISessionStore.class)
public class HadesSessionStore implements ISessionStore {
    private static final Logger LOGGER = new Logger(HadesSessionStore.class);
    private static final String BUCKET_NAME = "dobbySession";
    private final ISessionService sessionService;

    @Inject
    public HadesSessionStore(ISessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public Optional<ISession> find(String sessionId) {
        final Session session = Connector.read(BUCKET_NAME, sessionId, Session.class);
        if (session == null) {
            return Optional.empty();
        }
        final ISession sessionWrapper = new SessionWrapper(sessionService);
        sessionWrapper.setSession(session);
        return Optional.of(sessionWrapper);
    }

    @Override
    public void update(ISession session) {
        Connector.writeCreateVolatile(BUCKET_NAME, session.getId(), session.getSession());
    }

    @Override
    public void remove(String sessionId) {
        Connector.delete(BUCKET_NAME, sessionId);
    }

    @Override
    public Map<String, Long> getSessionAges() {
        final Session[] sessions = Connector.readPattern(BUCKET_NAME, ".*", Session.class);

        final Map<String, Long> lastAccessed = new HashMap<>();

        for (Session session: sessions) {
            lastAccessed.put(session.getId(), session.getLastAccessed());
        }

        return lastAccessed;
    }
}
