//SimpleTurnoutServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Turnout;
import jmri.jmris.AbstractTurnoutServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.*;
import org.apache.log4j.Logger;

/**
 * Simple Server interface between the JMRI turnout manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision: 21327 $
 */
public class JsonTurnoutServer extends AbstractTurnoutServer {

    private JmriConnection connection;
    private ObjectMapper mapper;
    static Logger log = Logger.getLogger(JsonTurnoutServer.class.getName());

    public JsonTurnoutServer(JmriConnection connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String turnoutName, int status) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, TURNOUT);
        ObjectNode data = root.putObject(DATA);
        data.put(NAME, turnoutName);
        data.put(STATE, status);
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void sendErrorStatus(String turnoutName) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(ERROR);
        data.put(NAME, turnoutName);
        data.put(CODE, -1);
        data.put(MESSAGE, Bundle.getMessage("ErrorObject", TURNOUT, turnoutName));
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        this.parseRequest(this.mapper.readTree(statusString).path(DATA));
    }

    public void parseRequest(JsonNode data) throws JmriException, IOException {
        int state = data.path(STATE).asInt(Turnout.UNKNOWN);
        String name = data.path(NAME).asText();
        switch (state) {
            case Turnout.THROWN:
                this.throwTurnout(name);
                break;
            case Turnout.CLOSED:
                this.closeTurnout(name);
                break;
            default:
                this.sendStatus(name, InstanceManager.turnoutManagerInstance().provideTurnout(name).getKnownState());
                break;
        }
        this.addTurnoutToList(name);
    }
}
