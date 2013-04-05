//JsonMemoryServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import jmri.JmriException;
import jmri.jmris.AbstractMemoryServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON server interface between the JMRI Memory manager and a network
 * connection
 *
 * This server sends a message containing the memory value whenever a memory
 * object that has been previously requested changes. When a client requests or
 * updates a memory object, the server replies with all known memory object
 * details, but only sends the new memory value when sending a status update.
 *
 * @author mstevetodd Copyright (C) 2012 (copied from JsonSensorServer)
 * @author Randall Wood Copyright (C) 2013
 * @version $Revision: $
 */
public class JsonMemoryServer extends AbstractMemoryServer {

    private JmriConnection connection;
    private ObjectMapper mapper;
    static Logger log = LoggerFactory.getLogger(JsonMemoryServer.class.getName());

    public JsonMemoryServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String memoryName, String status) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, MEMORY);
        ObjectNode data = root.putObject(DATA);
        data.put(NAME, memoryName);
        data.put(VALUE, status);
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void sendErrorStatus(String memoryName) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.handleError(500, Bundle.getMessage("ErrorObject", TURNOUT, memoryName))));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    public void parseRequest(JsonNode data) throws JmriException, IOException, JsonException {
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            JsonUtil.putMemory(name, data);
        } else {
            JsonUtil.setMemory(name, data);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getMemory(name)));
        this.addMemoryToList(name);
    }
}
