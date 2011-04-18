package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.List;

import jmri.Block;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.SignalMast;

/**
 * An Portal is a boundary between two Blocks.
 * 
 * <P>
 * A Portal has Lists of the OPaths that connect through it.
 *
 * @author	Pete Cressman  Copyright (C) 2009
 */
public class Portal  {

    private ArrayList <OPath> _fromPaths = new ArrayList <OPath>();
    private OBlock      _fromBlock;
    private NamedBean   _fromSignal;          // may be either SignalHead or SignalMast
    private long        _fromSignalDelay;
    private ArrayList <OPath> _toPaths = new ArrayList <OPath>();
    private OBlock      _toBlock;
    private NamedBean   _toSignal;          // may be either SignalHead or SignalMast
    private long        _toSignalDelay;
    private String      _portalName;
    private java.awt.Point _iconPosition;
    
    public Portal(OBlock fromBlock, String portalName, OBlock toBlock) {
        _fromBlock = fromBlock;
        _portalName = portalName;
        _toBlock = toBlock;
        if (_fromBlock!=null) _fromBlock.addPortal(this);
        if (_toBlock!=null) _toBlock.addPortal(this);
        //if (log.isDebugEnabled()) log.debug("Ctor: name= "+_portalName+", fromBlock= "+
        //           getFromBlockName()+", toBlock= "+getToBlockName()); 
    }

    /**
    * Determine which list the Path belongs to and add it to the list
    * @return false if Path does not have a matching block for this Portal
    */
    public boolean addPath(OPath path) {
        Block block = path.getBlock();
        if (block==null) {
            log.error("Path \""+path.getName()+"\" has no block.");
            return false;
        }
        //if (log.isDebugEnabled()) log.debug("addPath: "+toString());
        if (!this.equals(path.getFromPortal()) &&
                !this.equals(path.getToPortal()) ){
        //    log.warn("Path \""+path.getName()+"\" in block \""+block.getSystemName()+
        //        "\" does not pass through Portal \""+_portalName+"\".");
            return false;
        }
        if (_fromBlock != null && _fromBlock.equals(block)) {
            if (!_fromPaths.contains(path))  {
                return addPath(_fromPaths, path);
            }
        } else if (_toBlock != null && _toBlock.equals(block)) {
            if (!_toPaths.contains(path))  {
                return addPath(_toPaths, path);
            }
        } else {
        //    log.warn("Path \""+path.getName()+"\" in block \""+block.getSystemName()+
        //        "\" is not in either of the blocks of Portal \""+_portalName+"\".");
        }
        // path already in one of the path lists
        return true;
    }

    /**
    *  Utility for both path lists
    */
    private boolean addPath(List <OPath> list, OPath path) {
        String pName =path.getName();
        for (int i=0; i<list.size(); i++) {
            if (pName.equals(list.get(i).getName())) { log.error("Path \""+path.getName()+
                "\" is duplicate name for another path in Portal \""+_portalName+"\".");
                return false; 
            }
        }
        list.add(path);
        return true;
    }

    public void removePath(OPath path) {
        Block block = path.getBlock();
        if (block==null) {
            log.error("Path \""+path.getName()+"\" has no block.");
            return;
        }
        //if (log.isDebugEnabled()) log.debug("removePath: "+toString());
        if (!this.equals(path.getFromPortal()) &&
                !this.equals(path.getToPortal()) ){
            return;
        }
        if (_fromBlock != null && _fromBlock.equals(block)) {
            _fromPaths.remove(path);
        } else if (_toBlock != null && _toBlock.equals(block)) {
            _toPaths.remove(path);
        }
    }

    /**
    * Check for duplicate name in either block
    * @return return error message, return null if name change is OK 
    */
    public String setName(String name) {
        if (name == null || name.length()==0) { return null; }
        if (_portalName.equals(name)) { return null; }

        String msg = checkName(name, _fromBlock);
        if (msg==null) {
            msg = checkName(name, _toBlock);
        }
        if (msg==null) {
            _portalName = name;
        } else {
            msg = java.text.MessageFormat.format(
                  WarrantTableAction.rb.getString("DuplicatePortalName"), msg, name); 
        }
        return msg;
    }
    private String checkName(String name, OBlock block) {
        List<Portal> list = block.getPortals();
        for (int i=0; i<list.size(); i++) {
            if (name.equals(list.get(i).getName())) {
                return list.get(i).getName(); 
            }
        }
        return null;
    }

    public String getName() { return _portalName; }

    /**
    * Set block name. Verify that all toPaths are contained in the block.
    * @return false if paths are not in the block
    */
    public boolean setToBlock(OBlock block, boolean changePaths) {
        if ((block!=null && block.equals(_toBlock)) || (block==null && _toBlock==null)) {
            return true;
        }
        if (changePaths) {
            //Switch paths to new block.  User will need to verify connections
            for (int i=0; i<_toPaths.size(); i++) {
                    _toPaths.get(i).setBlock(block);
            }
        } else if (!verify(_toPaths, block)) {
            return false;
        }
        if (log.isDebugEnabled()) log.debug("setToBlock: oldBlock= \""+getToBlockName()
                  +"\" newBlock \""+(block!=null ? block.getDisplayName() : null)+"\".");
        if (_toBlock!=null) { _toBlock.removePortal(this); }
        _toBlock = block;
        if (_toBlock!=null) { _toBlock.addPortal(this); }
        return true;
    }
    public OBlock getToBlock() { return _toBlock;  }
    public String getToBlockName() { return (_toBlock!=null ? _toBlock.getDisplayName() : null); }
    public List <OPath> getToPaths() { return _toPaths; }

    /**
    * Set block name. Verify that all fromPaths are contained in the block.
    * @return false if paths are not in the block
    */
    public boolean setFromBlock(OBlock block, boolean changePaths) {
        if ((block!=null && block.equals(_fromBlock)) || (block==null && _fromBlock==null)) {
            return true;
        }
        if (changePaths) {
            //Switch paths to new block.  User will need to verify connections
            for (int i=0; i<_fromPaths.size(); i++) {
                    _fromPaths.get(i).setBlock(block);
            }
        } else if (!verify(_fromPaths, block)) {
            return false;
        }
        if (log.isDebugEnabled()) log.debug("setFromBlock: oldBlock= \""+getFromBlockName()+
                  "\" newBlock \""+(block!=null ? block.getDisplayName() : null)+"\".");
        if (_fromBlock!=null) { _fromBlock.removePortal(this); }
        _fromBlock = block;
        if (_fromBlock!=null) { _fromBlock.addPortal(this); }
        return true;
    }
    public OBlock getFromBlock() { return _fromBlock;  }
    public String getFromBlockName() { return (_fromBlock!=null ? _fromBlock.getDisplayName() : null);  }
    public List <OPath> getFromPaths() { return _fromPaths;  }

    public boolean setProtectSignal(NamedBean signal, long time, OBlock protectedBlock) {
        if (protectedBlock==null) return false;
        if (_fromBlock.equals(protectedBlock)) {
            _toSignal = signal;
            _toSignalDelay = time;
            //log.debug("setSignal: _toSignal= \""+name+", protectedBlock= "+protectedBlock);
        }
        if (_toBlock.equals(protectedBlock)) {
            _fromSignal = signal;
            _fromSignalDelay = time;
            //log.debug("setSignal: _fromSignal= \""+name+", protectedBlock= "+protectedBlock);
        }
        return true;
    }

    public boolean setApproachSignal(NamedBean signal, long time, OBlock approachBlock) {
        if (approachBlock==null) return false;
        if (_fromBlock.equals(approachBlock)) {
            _fromSignal = signal;
            _fromSignalDelay = time;
            //log.debug("setSignal: _toSignal= \""+name+", approachBlock= "+approachBlock);
        }
        if (_toBlock.equals(approachBlock)) {
            _toSignal = signal;
            _toSignalDelay = time;
            //log.debug("setSignal: _fromSignal= \""+name+", approachBlock= "+approachBlock);
        }
        return true;
    }
    public NamedBean getFromSignal() {
        return _fromSignal;
    }
    public String getFromSignalName() {
        return (_fromSignal!=null ? _fromSignal.getDisplayName() : null);
    }
    public long getFromSignalDelay() {
        return _fromSignalDelay;
    }
    public NamedBean getToSignal() {
        return _toSignal;
    }
    public String getToSignalName() {
        return (_toSignal!=null ? _toSignal.getDisplayName() : null); 
    }
    public long getToSignalDelay() {
        return _toSignalDelay;
    }
    public void deleteSignal(NamedBean signal) {
        if (signal.equals(_toSignal)) {
            _toSignal = null;
        } else if (signal.equals(_fromSignal)) {
            _fromSignal = null;
        }
    }

    static public NamedBean getSignal(String name) {
        NamedBean signal = InstanceManager.signalMastManagerInstance().getSignalMast(name);
        if (signal==null) {
            signal = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        }
        return signal;
    }

    /**
    * Get the paths to the portal within the connected Block
    * i.e. the paths in this (the param) block through the Portal
    * @param block 
    * @return null if portal does not connect to block
    */
    public List <OPath> getPathsWithinBlock(OBlock block) { 
        if (block == null) { return null; }
        if (block.equals(_fromBlock)) {
            return _fromPaths;
        } else if (block.equals(_toBlock)) {
            return _toPaths;
        }
        return null; 
    }

    /**
    * Return the block on the other side of the portal
    * from this (the param) block
    */
    public OBlock getOpposingBlock(OBlock block) {
        if (block.equals(_fromBlock)) {
            return _toBlock;
        } else if (block.equals(_toBlock)) {
            return _fromBlock;
        }
        return null; 
    }
    
    /**
    * Get the paths from the portal in the next connected Block
    * i.e. paths in the block on the other side of the portal
    * from this (the param) block
    * @param block 
    * @return null if portal does not connect to block
    */
    public List <OPath> getPathsFromOpposingBlock(OBlock block) { 
        if (block.equals(_fromBlock)) {
            return _toPaths;
        } else if (block.equals(_toBlock)) {
            return _fromPaths;
        }
        return null; 
    }

    /**
    * @param block is the direction of entry
    * @return signal protecting block
    */
    public NamedBean getSignalProtectingBlock(OBlock block) {
        if (block.equals(_toBlock)) {
            return _fromSignal;
        } else if (block.equals(_fromBlock)) {
            return _toSignal;
        }
        return null;
    }

    /**
    * Check signals, if any, for speed into next block. The signal that protects the
    * "to" block is the signal facing the "from" Block, i.e. the "from" signal.
    * (and vice-versa) 
    * @param block is the direction of entry, "from" block
    * @return permissible speed
    */
    public String getPermissibleSpeedForBlock(OBlock block) {
        String speed = "Normal";
        if (block.equals(_toBlock)) {
            if (_fromSignal!=null) {
                if (_fromSignal instanceof SignalHead) {
                    speed = getPermissibleSpeedFromSignal((SignalHead)_fromSignal);
                } else {
                    speed = getPermissibleSpeedFromSignal((SignalMast)_fromSignal);
                }
            }
        } else if (block.equals(_fromBlock)) {
            if (_toSignal!=null) {
                if (_toSignal instanceof SignalHead) {
                    speed = getPermissibleSpeedFromSignal((SignalHead)_toSignal);
                } else {
                    speed = getPermissibleSpeedFromSignal((SignalMast)_toSignal);
                }
            }
        } else {
            log.error("Block \""+block.getDisplayName()+"\" is not in Portal \""+_portalName+"\".");
        }
        // no signals, proceed at recorded speed
        return speed;
    }

    public long getEntranceSpeedChangeWaitForBlock(OBlock block) {
        if (block.equals(_toBlock)) {
            if (_fromSignal!=null) {
                if (_fromSignal instanceof SignalHead) {
                    return _fromSignalDelay;
                }
            }
        } else if (block.equals(_fromBlock)) {
            if (_toSignal!=null) {
                return _toSignalDelay;
            }
        }
        return 0;
    }

    private String getPermissibleSpeedFromSignal(SignalHead signal) {
        int appearance = signal.getAppearance();
        String speed = Warrant.getSpeedMap().getAppearanceSpeed(signal.getAppearanceName(appearance));
        if (speed==null) {
            log.info("SignalHead \""+ signal.getDisplayName()+"\" has no speed specified for appearance "+
                            signal.getAppearanceName(appearance)+"! - Restricting Movement!");
            speed = "Restricted";
        }
        if (log.isDebugEnabled()) log.debug(signal.getDisplayName()+" has speed "+speed+" from appearance "+
                                                signal.getAppearanceName(appearance)); 
        return speed;
    }

    private String getPermissibleSpeedFromSignal(SignalMast signal) {
        String aspect = signal.getAspect();
        String speed = Warrant.getSpeedMap().getAspectSpeed(aspect, signal.getSignalSystem());
        if (speed==null) {
            log.info("SignalMast \""+ signal.getDisplayName()+"\" has no speed specified for aspect "+
                                                aspect+"! - Restricting Movement!");
            speed = "Restricted";
        }
        if (log.isDebugEnabled()) log.debug(signal.getDisplayName()+" has speed= "+speed+" from aspect "+aspect);
        return speed;
    }
    
    private boolean verify(List <OPath> paths, OBlock block) {
        if (block==null) {
            if (paths.size()==0) {
                return true;
            } else {
                return false;
            }
        }
        String name = block.getSystemName();
        for (int i=0; i<paths.size(); i++) {
            String pathName = paths.get(i).getBlock().getSystemName();
            if (!pathName.equals(name)) {
                return false;
            }
        }
        return true;
    }

    public boolean isValid() {
        return (_fromBlock!=null && _toBlock!=null);
    }

    public void setIconPosition(java.awt.Point pt) {
        _iconPosition = pt;
    }
    public java.awt.Point getIconPosition() {
        return _iconPosition;
    }

    public void dispose() {
        if (_fromBlock!=null) _fromBlock.removePortal(this);
        if (_toBlock!=null) _toBlock.removePortal(this);
    }

    public String getDescription() {
        return java.text.MessageFormat.format(WarrantTableAction.rb.getString("PortalDescription"),
                        _portalName, getFromBlockName(), getToBlockName());
    }
    
    public String toString() {
        return ("Portal \""+_portalName+"\" from block \""+getFromBlockName()+"\" to block \""+getToBlockName()+"\""); 
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Portal.class.getName());
}
