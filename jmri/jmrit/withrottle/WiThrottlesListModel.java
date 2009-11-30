
package jmri.jmrit.withrottle;

/**
 *
 *  WiThrottle
 *
 *	@author Brett Hoffman   Copyright (C) 2009
 *	@author Created by Brett Hoffman on:
 *	@author 11/11/09.
 *	@version $Revision: 1.2 $
 */

import javax.swing.table.AbstractTableModel;
import java.util.ResourceBundle;
import java.util.ArrayList;


public class WiThrottlesListModel extends AbstractTableModel{
    ArrayList<DeviceServer> deviceList;
    //DeviceServer[] deviceList;

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.withrottle.WiThrottleBundle");


    WiThrottlesListModel(ArrayList<DeviceServer> deviceList){

        this.deviceList = deviceList;

    }

    public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return deviceList.size();
        }

        public String getColumnName(int col) {
            String title;
            switch (col){
                case 0:{
                    title = rb.getString("LabelDeviceName");
                    break;
                }
                case 1:{
                    title = rb.getString("LabelAddress");
                    break;
                }
                default:{
                    title = "";
                }
            }
            return title;
        }

    public String getValueAt(int row, int col) {
        if (deviceList.size() < 1) 
        	return null;
        // some error checking
        if (row >= deviceList.size()){
        	log.debug("row is greater than device list size");
        	return null;
        }
        if (col == 0){
            return deviceList.get(row).getName();
        }else{
            return deviceList.get(row).getCurrentAddress();
        }
    }



    public void updateDeviceList(ArrayList<DeviceServer> deviceList){
        this.deviceList = deviceList;
        this.fireTableDataChanged();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WiThrottlesListModel.class.getName());
}
