//SimpleOperationsServer.java

package jmri.jmris.simpleserver;

import java.io.*;
import java.util.ArrayList;
import javax.management.Attribute;

/**
 * Simple interface between the JMRI operations and a network connection
 * 
 * @author Paul Bender Copyright (C) 2010
 * @author Dan Boudreau Copyright (C) 2012 (Documented the code, changed reply format, and some minor refactoring)
 * @version $Revision$
 */

public class SimpleOperationsServer extends jmri.jmris.AbstractOperationsServer {
	
	/**
	 * All operation messages start with the key word "OPERATIONS" followed by a command like
	 * "TRAINS".  The reply message also starts with the key word "OPERATIONS" followed by
	 * the original command followed by the desired results. 
	 */
	public static final String OPERATIONS = "OPERATIONS";
	
	// The supported commands for operations
	/**
	 * the tag identifying the train's identity
	 */
	public static final String TRAIN = "TRAIN";
	
	/**
	 * Returns a list of trains.  One line for each train in the list.
	 */
	public static final String TRAINS = "TRAINS";
	
	/**
	 * Returns a list of locations that the trains visit. One line
	 * for each location in the list.
	 */
	public static final String LOCATIONS = "LOCATIONS";
	
	/**
	 * Requests/returns the train's length.  The train's name is required.
	 * Proper message format: "OPERATIONS TRAIN=train_name , TRAINLENGTH"
	 * Returns train length if train exists, otherwise an error message.
	 * <P>
	 * Request: "Operations , TRAIN=train"
	 * <P>
	 * Reply: "OPERATIONS , TRAIN=train , TRAINLENGTH=train_length"
	 */
	public static final String TRAINLENGTH = "TRAINLENGTH";
	
	/**
	 * Requests/returns the train's weight.  The train's name is required.
	 */
	public static final String TRAINWEIGHT = "TRAINWEIGHT";
	
	/**
	 * Requests/returns the number of cars in the train.  The train's name is required.
	 */
	public static final String TRAINCARS = "TRAINCARS";
	
	/**
	 * Requests/returns the road and number of the lead loco for this train.  The train's name is required.
	 */
	public static final String TRAINLEADLOCO = "TRAINLEADLOCO";
	
	/**
	 * Requests/returns the road and number of the caboose for this train if there's one assigned.  The train's name is required.
	 */
	public static final String TRAINCABOOSE = "TRAINCABOOSE";
	
	/**
	 * Requests/returns the the train's status.  The train's name is required.
	 */	
	public static final String TRAINSTATUS = "TRAINSTATUS";
	
	/**
	 * Terminates the train and returns the train's status.  The train's name is required.
	 */	
	public static final String TERMINATE = "TERMINATE";
	
	/**
	 * Sets/requests/returns the train's location or gets the train's current location.
	 * <P>
	 * Sets the train's location: "Operations , TRAIN=train_name , TRAINLOCATION=location"
	 * <P>
	 * Requests the train's location: "OPERATIONS , TRAIN=train_name"
	 * <P>
	 * Returns the train's location: "OPERATIONS , TRAIN=train_name , TRAINLOCATION=location" 
	 */	
	public static final String TRAINLOCATION = "TRAINLOCATION";
	
	private static final String REQUEST_DELIMITER = " , ";
	
	/**
	 * the character that separates the field tag from its value.
	 */
	public static final String FIELDSEPARATOR = "=";
	
	private DataOutputStream output;

	public SimpleOperationsServer(DataInputStream inStream,
			DataOutputStream outStream) {
		super();
		output = outStream;
	}

	/*
	 * Protocol Specific Simple Functions
	 */

	/**
	 * send a String to the other end of the telnet connection.   The String is composed of a set of attributes.
	 * @param contents is the ArrayList of Attributes to be sent.  A linefeed ('\n") is appended
	 * to the String.
	 */
    public void sendMessage(ArrayList<Attribute> contents) throws IOException {
        output.writeBytes(constructOperationsMessage(contents) + "\n");
    }
    
	/**
	 * constructs an error message and sends it to the client.  The error message will be
	 * <ul>
	 * <li> OPERATIONS: </li>
	 * <li> the error string </li>
	 * <li> "\n" </li>
	 * </ul>
	 * @param errorStatus is the error message.  It need not include any padding - this method
	 * will add it.  It should be plain text.
	 * @throws IOException if there is a problem sending the error message
	 */
	public void sendErrorStatus(String errorStatus) throws IOException {
	    output.writeBytes(OPERATIONS + ": " + errorStatus + "\n");
	}

	/**
	 * constructs a request in a format that parseOperationsMessage can handle.  An OperationsMessage has the format:
	 * <ul>
	 * <li> OPERATIONS </li>
	 * <li> " , " (delimiter) </li>
	 * <li> request/reponse </li>
	 * <li> any number of " , " , followed by additional request/response pairs </li>
	 * </ul>
	 * The meaning of request/response is context sensitive.  If the SimpleOperationsServer client is sending
	 * the message, then it is a request.  If the SimpleOperationsServer is sending the message, then it is a response.
	 * @param contents is an array of Attributes.  An Attribute is a String (tag) and a value. For this use,
	 * the value will always be a String or null.  Thus, "=" and REQUEST_DELIMITER are illegal in a tag and REQUEST_DELIMITER is illegal
	 * in a value. 
	 * @return a String which is a serialized version of the attribute array, which can be sent to an SimpleOperationsServer
	 * or received from a SimpleOperationsServer
	 */
	public static String constructOperationsMessage(ArrayList<Attribute> contents) {
	    StringBuilder result = new StringBuilder(OPERATIONS);
	    for (Attribute content : contents) {
	        result.append(REQUEST_DELIMITER + content.getName());
	        if (content.getValue() != null) {
	            result.append(FIELDSEPARATOR + content.getValue());
	        }
	    }
	    return new String(result);
	}

	/**
	 * parse a String presumably constructed by constructOperationsMessage.  It breaks the String
	 * down into tag or tag=value pairs, using a REQUEST_DELIMITER as the separator.  Each pair is further
	 * broken down into the tag and value and stuffed into an Attribute.  The Attribute is then
	 * added to the resulting ArrayList.
	 * <p>
	 * The leading OPERATIONS String is NOT included.  If the the first String is not OPERATIONS, an empty
	 * ArrayList is returned.
	 * @param message is the String received
	 * @return an ArrayList of Attributes of the constituent pieces of the message
	 */
	public static ArrayList<Attribute> parseOperationsMessage(String message) {
	    ArrayList<Attribute> contents = new ArrayList<Attribute>();
	    int start;
	    int end;
	    int equals;
	    String request;
	    if ((message != null) && message.startsWith(OPERATIONS)){
	        for (start = message.indexOf(REQUEST_DELIMITER);
	                start > 0;
	                start = end) {  // step through all the requests/responses in the message
	            start += REQUEST_DELIMITER.length();
	            end = message.indexOf(REQUEST_DELIMITER, start);
	            if (end > 0) {
	                request = message.substring(start, end);
	            }
	            else {
	                request = message.substring(start, message.length());
	            }
	            
	            //convert a request/response to an Attribute and add it to the result
	            equals = request.indexOf(FIELDSEPARATOR);
	            if ((equals > 0) && (equals < (request.length() - 1))) {
	                contents.add(new Attribute(request.substring(0, equals), request.substring(equals + 1, request.length())));
	            }
	            else {
	                contents.add(new Attribute(request, null));
	            }
	        }
	    }
	    return contents;
	}
	
	/**
	 * Parse operation commands. They all start with "OPERATIONS" followed by a
	 * command like "LOCATIONS". A command like "TRAINLENGTH" requires a train
	 * name. The delimiter is the tab character.
	 */
	public void parseStatus(String statusString) throws jmri.JmriException {
	    ArrayList<Attribute> contents = parseOperationsMessage(statusString);
	    ArrayList<Attribute> response = new ArrayList<Attribute>();
	    String trainName = null;
	    String tag;
	    String value;
	    
	    try {
	        for (Attribute field : contents) {
	            tag = field.getName();
	            if (TRAIN.equals(tag)) {
	                trainName = (String) field.getValue();
	                response.add(field);
	            }
	            else if (LOCATIONS.equals(tag)) {
	                sendLocationList();
	            }
	            else if (TRAINS.equals(tag)) {
	                sendTrainList();
	            }
	            else if (trainName != null) {
	                if (TRAINLENGTH.equals(tag)) {
	                    value = constructTrainLength(trainName);
	                    if (value != null) {
	                        response.add(new Attribute(TRAINLENGTH, value));
	                    }
	                }
	                else if (TRAINWEIGHT.equals(tag)) {
	                    value = constructTrainWeight(trainName);
	                    if (value != null) {
	                        response.add(new Attribute(TRAINWEIGHT, value));
	                    }
	                }
	                else if (TRAINCARS.equals(tag)) {
	                    value = constructTrainNumberOfCars(trainName);
	                    if (value != null) {
	                        response.add(new Attribute(TRAINCARS, value));
	                    }
	                }
	                else if (TRAINLEADLOCO.equals(tag)) {
	                    value = constructTrainLeadLoco(trainName);
	                    if (value != null) {
	                        response.add(new Attribute(TRAINLEADLOCO, value));
	                    }
	                }
	                else if (TRAINCABOOSE.equals(tag)) {
	                    value = constructTrainCaboose(trainName);
	                    if (value != null) {
	                        response.add(new Attribute(TRAINCABOOSE, value));
	                    }
	                }
	                else if (TRAINSTATUS.equals(tag)) {
	                    value = constructTrainStatus(trainName);
	                    if (value != null) {
	                        response.add(new Attribute(TRAINSTATUS, value));
	                    }
	                }
	                else if (TERMINATE.equals(tag)) {
	                    value = terminateTrain(trainName);
	                    if (value != null) {
	                        response.add(new Attribute(TERMINATE, value));
	                    }
	                }
	                else if (TRAINLOCATION.equals(tag)) {
	                    if (field.getValue() == null) {
	                        value = constructTrainLocation(trainName);
	                    }
	                    else {
	                        value = setTrainLocation(trainName, (String) field.getValue());
	                    }
	                    if (value != null) {
	                        response.add(new Attribute(TRAINLOCATION, value));
	                    }
	                } 
	                else {
	                    throw new jmri.JmriException();
	                }
	            }
                else {
                    throw new jmri.JmriException();
                }
	        }
	        if (response.size() > 1) {  // something more than just a train ID
	            sendMessage(response);
	        }
	    }
	    catch (IOException ioe) {

	    }
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(SimpleOperationsServer.class.getName());

}
