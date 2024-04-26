package com.gempukku.lotro;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.sql.SQLException;

public abstract class AbstractServer {
    private static final Logger _logger = LogManager.getLogger(AbstractServer.class);
    private static final ServerCleaner _cleaningTask = new ServerCleaner();

    private boolean _started;

    public void startServer() {
        if (!_started) {
            _cleaningTask.addServer(this);
            _started = true;
            _logger.debug("Started: "+getClass().getSimpleName());
            doAfterStartup();
        }
    }

    protected void doAfterStartup() {

    }

    public void stopServer() {
        if (_started) {
            _cleaningTask.removeServer(this);
            _started = false;
            _logger.debug("Stopped: "+getClass().getSimpleName());
        }
    }

    protected abstract void cleanup() throws SQLException, IOException;
}
