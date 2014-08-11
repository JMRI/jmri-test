package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Locale;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.ROSTER;
import static jmri.jmris.json.JSON.TYPE;
import jmri.jmrit.roster.Roster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listen for changes in the roster and notify subscribed clients of the change.
 *
 * @author Randall Wood Copyright (C) 2014
 */
public class JsonRosterServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper = new ObjectMapper();
    private final static Logger log = LoggerFactory.getLogger(JsonRosterServer.class);
    private final JsonRosterListener rosterListener = new JsonRosterListener();
    private boolean listening = false;

    public JsonRosterServer(JmriConnection connection) {
        this.connection = connection;
    }

    public void listen() {
        if (!this.listening) {
            Roster.instance().addPropertyChangeListener(this.rosterListener);
            this.listening = true;
        }
    }

    public void parseRosterEntryRequest(Locale locale, JsonNode data) throws IOException, JsonException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getRosterEntry(locale, data.path(NAME).asText())));
        this.listen();
    }

    public void dispose() {
        Roster.instance().removePropertyChangeListener(this.rosterListener);
        this.listening = false;
    }

    private class JsonRosterListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            ObjectNode root = mapper.createObjectNode().put(TYPE, ROSTER);
            try {
                if (evt.getPropertyName().equals(Roster.ROSTER_GROUP_ADDED)
                        || evt.getPropertyName().equals(Roster.ROSTER_GROUP_REMOVED)
                        || evt.getPropertyName().equals(Roster.ROSTER_GROUP_RENAMED)) {
                    connection.sendMessage(mapper.writeValueAsString(JsonUtil.getRosterGroups(connection.getLocale())));
                } else if (!evt.getPropertyName().equals(Roster.SAVED)) {
                    // catch all events other than SAVED
                    connection.sendMessage(mapper.writeValueAsString(JsonUtil.getRoster(connection.getLocale(), root)));
                }
            } catch (IOException ex) {
                dispose();
            }
        }
    }

}
