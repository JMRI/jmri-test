package jmri.jmris.json;

/**
 * Constants used in the JMRI JSON protocol.
 * <p>
 * With the exception of the constants F0-F28 (see {@link #F}), all object names
 * used in the JMRI JSON protocol are constants in this class.
 *
 * @author Randall Wood (C) 2013, 2014
 */
public final class JSON {

    /**
     * JMRI JSON protocol version.
     *<p>
     * Changes to the major number represent a backwards incompatible change in
     * the protocol, while changes to the minor number represent an addition to
     * the protocol.
     *<p>
     * Protocol 1.0 first in JMRI 3.4<br>
     * Protocol 1.1 first in JMRI 3.7.1
     *<p>
     * {@value #JSON_PROTOCOL_VERSION}
     */
    public static final String JSON_PROTOCOL_VERSION = "1.1"; // NOI18N

    /* JSON structure */
    /**
     * {@value #TYPE}
     */
    public static final String TYPE = "type"; // NOI18N
    /**
     * {@value #LIST}
     */
    public static final String LIST = "list"; // NOI18N
    /**
     * {@value #DATA}
     */
    public static final String DATA = "data"; // NOI18N
    /**
     * {@value #PING}
     */
    public static final String PING = "ping"; // NOI18N
    /**
     * {@value #PONG}
     */
    public static final String PONG = "pong"; // NOI18N
    /**
     * {@value #GOODBYE}
     */
    public static final String GOODBYE = "goodbye"; // NOI18N
    /**
     * {@value #NAME}
     */
    public static final String NAME = "name"; // NOI18N

    /* JSON methods */
    /**
     * {@value #METHOD}
     */
    public static final String METHOD = "method"; // NOI18N
    /**
     * {@value #DELETE}
     */
    public static final String DELETE = "delete"; // NOI18N
    /**
     * {@value #GET}
     */
    public static final String GET = "get"; // NOI18N
    /**
     * {@value #POST}
     */
    public static final String POST = "post"; // NOI18N
    /**
     * {@value #PUT}
     */
    public static final String PUT = "put"; // NOI18N

    /* JSON common tokens */
    /**
     * {@value #COMMENT}
     */
    public static final String COMMENT = "comment"; // NOI18N
    /**
     * {@value #USERNAME}
     */
    public static final String USERNAME = "userName"; // NOI18N
    /**
     * {@value #STATE}
     */
    public static final String STATE = "state"; // NOI18N
    /**
     * {@value #VALUE}
     */
    public static final String VALUE = "value"; // NOI18N
    /**
     * {@value #ID}
     */
    public static final String ID = "id"; // NOI18N
    /**
     * {@value #STATUS}
     */
    public static final String STATUS = "status"; // NOI18N

    /* JSON error */
    /**
     * {@value #ERROR}
     */
    public static final String ERROR = "error"; // NOI18N
    /**
     * {@value #CODE}
     */
    public static final String CODE = "code"; // NOI18N
    /**
     * {@value #MESSAGE}
     */
    public static final String MESSAGE = "message"; // NOI18N

    /* JSON hello and metadata */
    /**
     * {@value #HELLO}
     */
    public static final String HELLO = "hello"; // NOI18N
    /**
     * {@value #JMRI}
     */
    public static final String JMRI = "JMRI"; // NOI18N
    /**
     * {@value #HEARTBEAT}
     */
    public static final String HEARTBEAT = "heartbeat"; // NOI18N
    /**
     * {@value #RAILROAD}
     */
    public static final String RAILROAD = "railroad"; // NOI18N
    /**
     * {@value #NODE}
     */
    public static final String NODE = "node"; // NOI18N
    /**
     * {@value #FORMER_NODES}
     * @since 1.1
     */
    public static final String FORMER_NODES = "formerNodes"; // NOI18N

    /* JSON list types */
    /**
     * {@value #CARS}
     */
    public static final String CARS = "cars"; // NOI18N
    /**
     * {@value #CONSISTS}
     */
    public static final String CONSISTS = "consists"; // NOI18N
    /**
     * {@value #ENGINES}
     */
    public static final String ENGINES = "engines"; // NOI18N
    /**
     * {@value #LIGHTS}
     */
    public static final String LIGHTS = "lights"; // NOI18N
    /**
     * {@value #LOCATIONS}
     */
    public static final String LOCATIONS = "locations"; // NOI18N
    /**
     * {@value #MEMORIES}
     */
    public static final String MEMORIES = "memories"; // NOI18N
    /**
     * {@value #METADATA}
     */
    public static final String METADATA = "metadata"; // NOI18N
    /**
     * {@value #PANELS}
     */
    public static final String PANELS = "panels"; // NOI18N
    /**
     * {@value #REPORTERS}
     */
    public static final String REPORTERS = "reporters"; // NOI18N
    /**
     * {@value #ROSTER}
     */
    public static final String ROSTER = "roster"; // NOI18N
    /**
     * {@value #ROUTES}
     */
    public static final String ROUTES = "routes"; // NOI18N
    /**
     * {@value #SENSORS}
     */
    public static final String SENSORS = "sensors"; // NOI18N
    /**
     * {@value #SIGNAL_HEADS}
     */
    public static final String SIGNAL_HEADS = "signalHeads"; // NOI18N
    /**
     * {@value #SIGNAL_MASTS}
     */
    public static final String SIGNAL_MASTS = "signalMasts"; // NOI18N
    /**
     * {@value #TRAINS}
     */
    public static final String TRAINS = "trains"; // NOI18N
    /**
     * {@value #TURNOUTS}
     */
    public static final String TURNOUTS = "turnouts"; // NOI18N
    /**
     * {@value #NETWORK_SERVICES}
     */
    public static final String NETWORK_SERVICES = "networkServices"; // NOI18N

    /* JSON data types */
    /**
     * {@value #CAR}
     */
    public static final String CAR = "car"; // NOI18N
    /**
     * {@value #CONSIST}
     */
    public static final String CONSIST = "consist"; // NOI18N
    /**
     * {@value #ENGINE}
     */
    public static final String ENGINE = "engine"; // NOI18N
    /**
     * {@value #LIGHT}
     */
    public static final String LIGHT = "light"; // NOI18N
    /**
     * {@value #LOCATION}
     */
    public static final String LOCATION = "location"; // NOI18N
    /**
     * {@value #LOCATION_ID}
     */
    public static final String LOCATION_ID = "locationId"; // NOI18N
    /**
     * {@value #MEMORY}
     */
    public static final String MEMORY = "memory"; // NOI18N
    /**
     * {@value #OPERATIONS}
     */
    public static final String OPERATIONS = "operations"; // NOI18N
    /**
     * {@value #PANEL}
     */
    public static final String PANEL = "panel"; // NOI18N
    /**
     * {@value #POWER}
     */
    public static final String POWER = "power"; // NOI18N
    /**
     * {@value #PROGRAMMER}
     */
    public static final String PROGRAMMER = "programmer"; // NOI18N
    /**
     * {@value #ROUTE}
     */
    public static final String ROUTE = "route"; // NOI18N
    /**
     * {@value #SENSOR}
     */
    public static final String SENSOR = "sensor"; // NOI18N
    /**
     * {@value #SIGNAL_HEAD}
     */
    public static final String SIGNAL_HEAD = "signalHead"; // NOI18N
    /**
     * {@value #SIGNAL_MAST}
     */
    public static final String SIGNAL_MAST = "signalMast"; // NOI18N
    /**
     * {@value #REPORTER}
     */
    public static final String REPORTER = "reporter"; // NOI18N
    /**
     * {@value #ROSTER_ENTRY}
     */
    public static final String ROSTER_ENTRY = "rosterEntry"; // NOI18N
    /**
     * {@value #THROTTLE}
     */
    public static final String THROTTLE = "throttle"; // NOI18N
    /**
     * {@value #TRAIN}
     */
    public static final String TRAIN = "train"; // NOI18N
    /**
     * {@value #TURNOUT}
     */
    public static final String TURNOUT = "turnout"; // NOI18N

    /* JSON operations tokens */
    /**
     * {@value #LENGTH}
     */
    public static final String LENGTH = "length"; // NOI18N
    /**
     * {@value #WEIGHT}
     */
    public static final String WEIGHT = "weight"; // NOI18N
    /**
     * {@value #LEAD_ENGINE}
     */
    public static final String LEAD_ENGINE = "leadEngine"; // NOI18N
    /**
     * {@value #CABOOSE}
     */
    public static final String CABOOSE = "caboose"; // NOI18N
    /**
     * {@value #TERMINATE}
     */
    public static final String TERMINATE = "terminate"; // NOI18N

    /* JSON panel tokens */
    /**
     * {@value #CONTROL_PANEL}
     */
    public static final String CONTROL_PANEL = "Control Panel"; // NOI18N
    /**
     * {@value #LAYOUT_PANEL}
     */
    public static final String LAYOUT_PANEL = "Layout"; // NOI18N
    /**
     * {@value #PANEL_PANEL}
     */
    public static final String PANEL_PANEL = "Panel"; // NOI18N
    /**
     * {@value #URL}
     */
    public static final String URL = "URL"; // NOI18N
    /**
     * {@value #FORMAT}
     */
    public static final String FORMAT = "format"; // NOI18N
    /**
     * {@value #JSON}
     */
    public static final String JSON = "json"; // NOI18N
    /**
     * {@value #XML}
     */
    public static final String XML = "xml"; // NOI18N

    /* JSON programmer tokens */
    /**
     * {@value #MODE}
     */
    public static final String MODE = "mode"; // NOI18N
    /**
     * {@value #NODE_CV}
     */
    public static final String NODE_CV = "CV"; // NOI18N
    /**
     * {@value #OP}
     */
    public static final String OP = "mode"; // NOI18N
    /**
     * {@value #READ}
     */
    public static final String READ = "read"; // NOI18N
    /**
     * {@value #WRITE}
     */
    public static final String WRITE = "write"; // NOI18N

    /* JSON reporter tokens */
    /**
     * {@value #REPORT}
     */
    public static final String REPORT = "report"; // NOI18N
    /**
     * {@value #LAST_REPORT}
     */
    public static final String LAST_REPORT = "lastReport"; // NOI18N

    /* JSON roster and car/engine (operations) tokens */
    /**
     * {@value #COLOR}
     */
    public static final String COLOR = "color"; // NOI18N
    /**
     * {@value #LOAD}
     */
    public static final String LOAD = "load"; // NOI18N
    /**
     * {@value #MODEL}
     */
    public static final String MODEL = "model"; // NOI18N
    /**
     * {@value #ROAD}
     */
    public static final String ROAD = "road"; // NOI18N
    /**
     * {@value #NUMBER}
     */
    public static final String NUMBER = "number"; // NOI18N
    /**
     * {@value #DESTINATION}
     */
    public static final String DESTINATION = "destination"; // NOI18N
    /**
     * {@value #DESTINATION_TRACK}
     */
    public static final String DESTINATION_TRACK = "destinationTrack"; // NOI18N
    /**
     * {@value #LOCATION_TRACK}
     */
    public static final String LOCATION_TRACK = "locationTrack"; // NOI18N
    /**
     * {@value #IS_LONG_ADDRESS}
     */
    public static final String IS_LONG_ADDRESS = "isLongAddress"; // NOI18N
    /**
     * {@value #MFG}
     */
    public static final String MFG = "mfg"; // NOI18N
    /**
     * {@value #MAX_SPD_PCT}
     */
    public static final String MAX_SPD_PCT = "maxSpeedPct"; // NOI18N
    /**
     * {@value #IMAGE_FILE_NAME}
     */
    public static final String IMAGE_FILE_NAME = "imageFileName"; // NOI18N
    /**
     * {@value #IMAGE_ICON_NAME}
     */
    public static final String IMAGE_ICON_NAME = "imageFileName"; // NOI18N
    /**
     * {@value #FUNCTION_KEYS}
     */
    public static final String FUNCTION_KEYS = "functionKeys"; // NOI18N
    /**
     * {@value #LABEL}
     */
    public static final String LABEL = "label"; // NOI18N
    /**
     * {@value #LOCKABLE}
     */
    public static final String LOCKABLE = "lockable"; // NOI18N

    /* JSON route (operations) tokens */
    /**
     * {@value #DIRECTION}
     */
    public static final String DIRECTION = "trainDirection"; // NOI18N
    /**
     * {@value #SEQUENCE}
     */
    public static final String SEQUENCE = "sequenceId"; // NOI18N
    /**
     * {@value #EXPECTED_ARRIVAL}
     */
    public static final String EXPECTED_ARRIVAL = "expectedArrivalTime"; // NOI18N
    /**
     * {@value #EXPECTED_DEPARTURE}
     */
    public static final String EXPECTED_DEPARTURE = "expectedDepartureTime"; // NOI18N
    /**
     * {@value #DEPARTURE_TIME}
     */
    public static final String DEPARTURE_TIME = "departureTime"; // NOI18N
    /**
     * {@value #DEPARTURE_LOCATION}
     */
    public static final String DEPARTURE_LOCATION = "trainDepartsName"; // NOI18N
    /**
     * {@value #TERMINATES_LOCATION}
     */
    public static final String TERMINATES_LOCATION = "trainTerminatesName"; // NOI18N
    /**
     * {@value #DESCRIPTION}
     */
    public static final String DESCRIPTION = "description"; // NOI18N
    /**
     * {@value #ROUTE_ID}
     */
    public static final String ROUTE_ID = "routeId"; // NOI18N

    /* JSON signalling tokens */
    /**
     * {@value #APPEARANCE}
     */
    public static final String APPEARANCE = "appearance"; // NOI18N
    /**
     * {@value #APPEARANCE_NAME}
     */
    public static final String APPEARANCE_NAME = "appearanceName"; // NOI18N
    /**
     * {@value #ASPECT}
     */
    public static final String ASPECT = "aspect"; // NOI18N
    /**
     * {@value #ASPECT_DARK}
     */
    public static final String ASPECT_DARK = "Dark"; // NOI18N
    /**
     * {@value #ASPECT_HELD}
     */
    public static final String ASPECT_HELD = "Held"; // NOI18N
    /**
     * {@value #ASPECT_UNKNOWN}
     */
    public static final String ASPECT_UNKNOWN = "Unknown"; // NOI18N
    /**
     * {@value #TOKEN_HELD}
     */
    public static final String TOKEN_HELD = "held"; // NOI18N
    /**
     * {@value #LIT}
     */
    public static final String LIT = "lit"; // NOI18N

    /* JSON throttle tokens */
    /**
     * {@value #ADDRESS}
     */
    public static final String ADDRESS = "address"; // NOI18N
    /**
     * {@value #FORWARD}
     */
    public static final String FORWARD = "forward"; // NOI18N
    /**
     * {@value #RELEASE}
     */
    public static final String RELEASE = "release"; // NOI18N
    /**
     * {@value #ESTOP}
     */
    public static final String ESTOP = "eStop"; // NOI18N
    /**
     * {@value #IDLE}
     */
    public static final String IDLE = "idle"; // NOI18N
    /**
     * {@value #SPEED}
     */
    public static final String SPEED = "speed"; // NOI18N
    /**
     * {@value #SSM}
     */
    public static final String SSM = "SSM"; // NOI18N
    /**
     * Prefix for the throttle function keys (F0-F28).
     *<p>
     * {@value #F}
     */
    public static final String F = "F"; // NOI18N

    /* JSON Sensor and Turnout Tokens */
    /**
     * {@value #INVERTED}
     */
    public static final String INVERTED = "inverted"; // NOI18N

    /* JSON value types */
    /**
     * {@value #NULL}
     */
    public static final String NULL = "null"; // NOI18N
    /**
     * {@value #INTEGER}
     */
    public static final String INTEGER = "int"; // NOI18N

    /* JSON network services tokens */
    /**
     * {@value #PORT}
     */
    public static final String PORT = "port"; // NOI18N

    /* JSON consist tokens */
    /**
     * {@value #POSITION}
     */
    public static final String POSITION = "position"; // NOI18N
    /**
     * {@value #SIZE_LIMIT}
     */
    public static final String SIZE_LIMIT = "sizeLimit"; // NOI18N

    /*
     * JSON State (an unsigned integer)
     */

    /* Common state */
    /**
     * {@value #UNKNOWN}
     */
    public static final int UNKNOWN = 0x00;

    /* Light and PowerManager state */
    /**
     * {@value #ON}
     */
    public static final int ON = 0x02;
    /**
     * {@value #OFF}
     */
    public static final int OFF = 0x04;

    /* NamedBean state */
    /**
     * {@value #INCONSISTENT}
     */
    public static final int INCONSISTENT = 0x08;

    /* Route state */
    /**
     * {@value #TOGGLE}
     */
    public static final int TOGGLE = 0x08;

    /* Sensor state */
    /**
     * {@value #ACTIVE}
     */
    public static final int ACTIVE = 0x02;
    /**
     * {@value #INACTIVE}
     */
    public static final int INACTIVE = 0x04;

    /* SignalHead state */
    /**
     * {@value #STATE_DARK}
     */
    public static final int STATE_DARK = 0x00;
    /**
     * {@value #RED}
     */
    public static final int RED = 0x01;
    /**
     * {@value #FLASHRED}
     */
    public static final int FLASHRED = 0x02;
    /**
     * {@value #YELLOW}
     */
    public static final int YELLOW = 0x04;
    /**
     * {@value #FLASHYELLOW}
     */
    public static final int FLASHYELLOW = 0x08;
    /**
     * {@value #GREEN}
     */
    public static final int GREEN = 0x10;
    /**
     * {@value #FLASHGREEN}
     */
    public static final int FLASHGREEN = 0x20;
    /**
     * {@value #LUNAR}
     */
    public static final int LUNAR = 0x40;
    /**
     * {@value #FLASHLUNAR}
     */
    public static final int FLASHLUNAR = 0x80;
    /**
     * {@value #STATE_HELD}
     */
    public static final int STATE_HELD = 0x100;

    /* Turnout state */
    /**
     * {@value #CLOSED}
     */
    public static final int CLOSED = 0x02;
    /**
     * {@value #THROWN}
     */
    public static final int THROWN = 0x04;

    /* prevent the constructor from being documented */
    private JSON() {
    }
}
