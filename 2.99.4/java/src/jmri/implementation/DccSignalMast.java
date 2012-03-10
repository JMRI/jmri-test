// This file is part of JMRI.
//
// JMRI is free software; you can redistribute it and/or modify it under
// the terms of version 2 of the GNU General Public License as published
// by the Free Software Foundation. See the "COPYING" file for a copy
// of this license.
//
// JMRI is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// for more details.

package jmri.implementation;

import java.util.HashMap;
import jmri.*;

/**
 * This class implements a SignalHead the maps the various appearances values to
 * aspect values in the <B>Extended Accessory Decoder Control Packet Format</B> and
 * outputs that packet to the DCC System via the generic CommandStation interface
 * <P>
 * The mapping is as follows:
 * <P>
 *    0 = DARK        <BR>
 *    1 = RED         <BR>
 *    2 = YELLOW      <BR>
 *    3 = GREEN       <BR>
 *    4 = FLASHRED    <BR>
 *    5 = FLASHYELLOW <BR>
 *    6 = FLASHGREEN  <BR>
 * <P>
 * The FLASH appearances are expected to be implemented in the decoder.
 *
 * @author Alex Shepherd Copyright (c) 2008
 * @version $Revision: 19173 $
 */
public class DccSignalMast extends AbstractSignalMast {

  public DccSignalMast( String sys, String user ) {
    super(sys, user);
    configureFromName(sys);
  }

  public DccSignalMast( String sys ) {
    super(sys);
    configureFromName(sys);
  }
  
  public DccSignalMast( String sys, String user, String mastSubType ) {
    super(sys, user);
    mastType = mastSubType;
    configureFromName(sys);
  }

  /*public DccSignalMast( String sys, String mastSubType ) {
    super(sys);
    mastType = mastSubType;
    configureFromName(sys);
  }*/
  
  protected String mastType = "F$dsm";
  
  protected void configureFromName(String systemName) {
        // split out the basic information
        String[] parts = systemName.split(":");
        if (parts.length < 3) { 
            log.error("SignalMast system name needs at least three parts: "+systemName);
            throw new IllegalArgumentException("System name needs at least three parts: "+systemName);
        }
        if (!parts[0].endsWith(mastType)) {
            log.warn("First part of signal mast is incorrect "+systemName + " : " + mastType);
        } else {
            String commandStationPrefix = parts[0].substring(0, parts[0].indexOf("$")-1);
            java.util.List<Object> connList = jmri.InstanceManager.getList(jmri.CommandStation.class);
            if(connList!=null){
                for(int x = 0; x < connList.size(); x++){
                    jmri.CommandStation station = (jmri.CommandStation) connList.get(x);
                    if(station.getSystemPrefix().equals(commandStationPrefix)){
                        c = station;
                        break;
                    }
                }
            }
            if(c==null){
                c = InstanceManager.commandStationInstance();
                log.error("No match against the command station for " + parts[0] + ", so will use the default");
            }
        }
        String system = parts[1];
        String mast = parts[2];

        mast = mast.substring(0, mast.indexOf("("));
        String tmp = parts[2].substring(parts[2].indexOf("(")+1, parts[2].indexOf(")"));
        try {
            dccSignalDecoderAddress = Integer.parseInt(tmp);
        } catch (NumberFormatException e){
            log.warn("DCC accessory address SystemName "+ systemName + " is not in the correct format");
        }
        configureSignalSystemDefinition(system);
        configureAspectTable(system, mast);
    }

    protected HashMap<String, Integer> appearanceToOutput = new HashMap<String, Integer>();
    
    public void setOutputForAppearance(String appearance, int number){
        if(appearanceToOutput.containsKey(appearance)){
            log.debug("Appearance " + appearance + " is already defined as " + appearanceToOutput.get(appearance));
            appearanceToOutput.remove(appearance);
        }
        appearanceToOutput.put(appearance, number);
    }
    
    public int getOutputForAppearance(String appearance){
        if(!appearanceToOutput.containsKey(appearance)){
            log.error("Trying to get appearance " + appearance + " but it has not been configured");
            return -1;
        }
        return appearanceToOutput.get(appearance);
    }
    
        /*switch( mAppearance ){
          case SignalHead.DARK:        aspect = 0 ; break;
          case SignalHead.RED:         aspect = 1 ; break;
          case SignalHead.YELLOW:      aspect = 2 ; break;
          case SignalHead.GREEN:       aspect = 3 ; break;
          case SignalHead.FLASHRED:    aspect = 4 ; break;
          case SignalHead.FLASHYELLOW: aspect = 5 ; break;
          case SignalHead.FLASHGREEN:  aspect = 6 ; break;
          }
/*          0.  "Stop"
1.  "Take Siding"
2.  "Stop-Orders"
3.  "Stop-Proceed"
4.  "Restricting"
5.  "Permissive"
6.  "Slow-Approach"
7.  "Slow"
8.  "Slow-Medium"
9.  "Slow-Limited"
10. "Slow-Clear"
11. "Medium-Approach"
12. "Medium-Slow"
13. "Medium"
14. "Medium-Ltd"
15. "Medium-Clr"
16. "Limited-Approach"
17. "Limited-Slow"
18. "Limited-Med"
19. "Limited"
20. "Limited-Clear"
21. "Approach"
22. "Advance-Appr"
23. "Appr-Slow"
24. "Adv-Appr-Slow"
25. "Appr-Medium"
26. "Adv-Appr-Med"
27. "Appr-Limited"
28. "Adv-Appr-Ltd"
29. "Clear"
30. "Cab-Speed"
31. "Dark"
        */
    protected int packetRepeatCount = 3;
    
    public void setAspect(String aspect){
        
        if(appearanceToOutput.containsKey(aspect)){
            c.sendPacket( NmraPacket.altAccSignalDecoderPkt( dccSignalDecoderAddress, appearanceToOutput.get(aspect) ), packetRepeatCount);
        } else {
            log.warn("Trying to set aspect that has not been configured");
        }
        super.setAspect(aspect);
    
    }
    
    public int getDccSignalMastAddress(){
        return dccSignalDecoderAddress;
    }
    
    public CommandStation getCommandStation(){
        return c;
    }
  
  protected CommandStation c;

  protected int dccSignalDecoderAddress;
  
    public static String isDCCAddressUsed(int addr){
        for(String val : InstanceManager.signalMastManagerInstance().getSystemNameList()){
            SignalMast mast = InstanceManager.signalMastManagerInstance().getSignalMast(val);
            if(mast instanceof jmri.implementation.DccSignalMast){
                if(((DccSignalMast)mast).getDccSignalMastAddress() == addr){
                    return ((DccSignalMast)mast).getDisplayName();
                }
            }
        }
        return null;
    }
}
