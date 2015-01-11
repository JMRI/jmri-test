package jmri.jmrix.jmriclient.json;

import jmri.jmris.json.JsonServerPreferences;
import jmri.jmrix.AbstractNetworkPortController;
import jmri.jmrix.jmriclient.ActiveFlag;

/**
 *
 * @author Randall Wood 2014
 */
public class JsonNetworkPortController extends AbstractNetworkPortController {

    private final JsonClientSystemConnectionMemo memo;

    public JsonNetworkPortController() {
        super();
        this.memo = new JsonClientSystemConnectionMemo();
        this.setPort(JsonServerPreferences.DEFAULT_PORT);
    }

    @Override
    public void configure() {
        // connect to the traffic controller
        JsonClientTrafficController control = new JsonClientTrafficController();
        control.connectPort(this);
        this.memo.setTrafficController(control);

        // mark OK for menus
        ActiveFlag.setActive();
    }

    @Override
    public JsonClientSystemConnectionMemo getSystemConnectionMemo() {
        return this.memo;
    }

    @Override
    public void dispose() {
        this.memo.dispose();
    }

}
