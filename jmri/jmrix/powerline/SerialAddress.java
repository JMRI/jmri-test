// SerialAddress.java

package jmri.jmrix.powerline;

import java.util.regex.*;

/**
 * Utility Class supporting parsing and testing of addresses
 * <P>
 * Two address formats are supported:
 *   For X10: Ptnxx 
 *      where:  t is the type code, 'S' for sensors, and
 *                      'L' for lights
 *              n is the house code of the input or output bit (A - P)
 *              xx is a bit number of the input or output bit (1-16)
 *      examples: PLA2 (House Code A, Unit 2), PSK1 (House Code K, Unit 1)
 *   For Insteon: Pthh.hh.hh
 *      where:  t is the type code, 'S' for sensors, and
 *                      'L' for lights
 *              aa is two hexadecimal digits
 *      examples: PLA2.43.CB
 * <P>
 * @author	Dave Duchamp, Copyright (C) 2004
 * @author  Bob Jacobsen, Copyright (C) 2006, 2007, 2008, 2009
 * @author Ken Cameron, Copyright (C) 2008, 2009
 * @version     $Revision: 1.13 $
 */
public class SerialAddress {

	private static  Matcher hCodes = Pattern.compile("^P[LTS]([A-P])(\\d++)$").matcher("");
	private static	Matcher aCodes = Pattern.compile("^P[LTS].*$").matcher("");
	private static	Matcher iCodes = Pattern.compile("^P[LTS](\\p{XDigit}\\p{XDigit}).(\\p{XDigit}\\p{XDigit}).(\\p{XDigit}\\p{XDigit})$").matcher("");
	private static	char minHouseCode = 'A';
	private static	char maxHouseCode = 'P';

    public SerialAddress() {
    }

    /**
     * Public static method to validate system name format
     * @return 'true' if system name has a valid format, else returns 'false'
     * @param type Letter indicating device type expected
     */
    public static boolean validSystemNameFormat(String systemName, char type) {
    	// validate the system Name leader characters
        if ( (!aCodes.reset(systemName).matches()) || (systemName.charAt(1) != type) ) {
            // here if an illegal format 
            log.error("illegal character in header field system name: " + systemName);
            return (false);
        }
        if (hCodes.reset(systemName).matches() && hCodes.groupCount() == 2) {
            // This is a PLaxx address - validate the house code and unit address fields
            if (hCodes.group(1).charAt(0) < minHouseCode || hCodes.group(1).charAt(0) > maxHouseCode) {
                log.error("house code field out of range in system name: "
                        + systemName);
                return (false);
            }
            int num;
            try {
                num = Integer.parseInt(hCodes.group(2));
            }
            catch (Exception e) {
                log.error("illegal character in unit address field of system name: "
                                                    + systemName);
                return (false);
            }
            if ( (num < 1) || (num > 16) ) {
                log.error("unit address field out of range in system name: "
                                                    + systemName);
                return (false);
            }
            return (true);
        }
        if (aCodes.reset(systemName).matches()) {
            if (!iCodes.reset(systemName).matches()) {
                // here if an illegal format
                log.error("illegal character in header field system name: " + systemName);
                return (false);
            } else {
                if (iCodes.groupCount() != 3) {
                    // here if an illegal format
                    log.error("legal character in header field system name: " + systemName);
                    return (false);
                } else {
                    return (true);
                }
            }
        }
        log.error("address did not match any valid forms: " + systemName);
        return false;
    }

    /**
     * Public static method to validate system name for configuration
     *   returns 'true' if system name has a valid meaning in current configuration, 
     *      else returns 'false'
     */
    public static boolean validSystemNameConfig(String systemName, char type) {
        if ( !validSystemNameFormat(systemName,type) ) {
            // No point in trying if a valid system name format is not present
            log.warn(systemName+" invalid; bad format");
            return false;
        }
        // System name has passed all tests
        return true;
    }
        
    /**
     * Public static method to normalize a system name
     * <P>
     * This routine is used to ensure that each system name is uniquely linked to
     *      one bit, by removing extra zeros inserted by the user.
     * <P>
     * If the supplied system name does not have a valid format, an empty string is 
     *      returned.  Otherwise a normalized name is returned in the same format
     *      as the input name.
     */
    public static String normalizeSystemName(String systemName) {
        // ensure that input system name has a valid format
        if ( !validSystemNameFormat(systemName,systemName.charAt(1)) ) {
            // No point in normalizing if a valid system name format is not present
            return "";
        }
        String nName = "";
        boolean hMatch = hCodes.reset(systemName).matches();
        int hCount = hCodes.groupCount();
        // check for the presence of a char to differentiate the two address formats
        if ( hMatch && hCount == 2) {
            // This is a PLaxx address 
            nName = systemName.substring(0,3) + Integer.toString(Integer.parseInt(hCodes.group(2)));
        }
        if (nName == "") {
            boolean iMatch = iCodes.reset(systemName).matches();
            if (iMatch) {
                nName = systemName.toString();
            } else {
        	    if (log.isDebugEnabled()) {
        		    log.debug("valid name doesn't normalize: " + systemName + " hMatch: " + hMatch + " hCount: " + hCount);
        	    }
            }
        }
        return nName;
    }

    /**
     * Extract housecode from system name, as a letter A-P
     * <P>
     * If the supplied system name does not have a valid format, an empty string is 
     *      returned.
     */
    public static String houseCodeFromSystemName(String systemName) {
    	String hCode = "";
        // ensure that input system name has a valid format
        if ( !validSystemNameFormat(systemName, systemName.charAt(1)) ) {
            // No point in normalizing if a valid system name format is not present
        } else {
			if (hCodes.reset(systemName).matches() && hCodes.groupCount() == 2) {
				// This is a PLaxx address
				try {
					hCode = hCodes.group(1);
				}
				catch (Exception e) {
					log.error("illegal character in house code field system name: " + systemName);
					return "";
				}
			}
        }
        return hCode;
    }

    /**
     * Extract devicecode from system name, as a string 1-16
     * <P>
     * If the supplied system name does not have a valid format, an empty string is 
     *      returned.
     */
    public static String deviceCodeFromSystemName(String systemName) {
    	String dCode = "";
        // ensure that input system name has a valid format
        if ( !validSystemNameFormat(systemName, systemName.charAt(1)) ) {
            // No point in normalizing if a valid system name format is not present
        } else {
			if (hCodes.reset(systemName).matches()) {
    			if (hCodes.groupCount() == 2) {
				    // This is a PLaxx address
				    try {
					    dCode = hCodes.group(2);
				    }
				    catch (Exception e) {
					    log.error("illegal character in number field system name: " + systemName);
					    return "";
				    }
                }
			} else {
    			if (iCodes.reset(systemName).matches()) {
                    dCode = iCodes.group(1)+iCodes.group(2)+iCodes.group(3);
                } else {
					log.error("illegal insteon address: " + systemName);
					return "";
                }
            }
        }
        return dCode;
    }

    /**
     * Extract housecode from system name, as a value 1-16
     * <P>
     * If the supplied system name does not have a valid format, an empty string is 
     *      returned.
     */
    public static int houseCodeAsValueFromSystemName(String systemName) {
    	int hCode = -1;
        // ensure that input system name has a valid format
        if ( !validSystemNameFormat(systemName, systemName.charAt(1)) ) {
            // No point in normalizing if a valid system name format is not present
        } else {
			if (hCodes.reset(systemName).matches() && hCodes.groupCount() == 2) {
				// This is a PLaxx address
				try {
					hCode = hCodes.group(1).charAt(0) - 0x40;
				}
				catch (Exception e) {
					log.error("illegal character in number field system name: " + systemName);
					return -1;
				}
			}
        }
        return hCode;
    }

    /**
     * Extract devicecode from system name, as a value 1-16
     * <P>
     * If the supplied system name does not have a valid format, an empty string is 
     *      returned.
     */
    public static int deviceCodeAsValueFromSystemName(String systemName) {
    	int dCode = -1;
        // ensure that input system name has a valid format
        if ( !validSystemNameFormat(systemName, systemName.charAt(1)) ) {
            // No point in normalizing if a valid system name format is not present
        } else {
			if (hCodes.reset(systemName).matches() && hCodes.groupCount() == 2) {
				// This is a PLaxx address
				try {
					dCode = Integer.parseInt(hCodes.group(2));
				}
				catch (Exception e) {
					log.error("illegal character in number field system name: " + systemName);
					return -1;
				}
			}
        }
        return dCode;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialAddress.class.getName());
}

/* @(#)SerialAddress.java */
