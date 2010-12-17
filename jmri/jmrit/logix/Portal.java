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
    private OBlock _fromBlock;
    private NamedBean _fromSignal;          // may be either SignalHead or SignalMast
    private long    _fromSignalDelay;
    private ArrayList <OPath> _toPaths = new ArrayList <OPath>();
    private OBlock _toBlock;
    private NamedBean _toSignal;          // may be either SignalHead or SignalMast
    private long    _toSignalDelay;
    private String _portalName;
    /*
    public Portal(String name) {
        _portalName = name;
    } */
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
        if (!_portalName.equals(path.getFromPortalName()) &&
                !_portalName.equals(path.getToPortalName()) ){
            log.error("Path \""+path.getName()+"\" in block \""+block.getSystemName()+
                "\" does not pass through Portal \""+_portalName+"\".");
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
            log.error("Path \""+path.getName()+"\" in block \""+block.getSystemName()+
                "\" is not in either of the blocks of Portal \""+_portalName+"\".");
        }
        return false;
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

    public void setName(String name) {
        if (name == null || name.length()==0) { return; }
        if (_portalName.equals(name)) { return; }

        String oldName = _portalName;
        _portalName = name;

        changePathPortalName(_fromPaths, _portalName, oldName);
        changePathPortalName(_toPaths, _portalName, oldName);
        changeBlockPortalName(_fromBlock, _portalName, oldName);
        changeBlockPortalName(_toBlock, _portalName, oldName);
    }

    /**
    *  Utility for both path lists
    */
    private void changePathPortalName(List <OPath> pathList, 
                                         String newName, String oldName) {
        for (int i=0; i<pathList.size(); i++) {
            OPath path = pathList.get(i);
            if (oldName.equals(path.getFromPortalName())) {
                path.setFromPortalName(newName);
            }
            if (oldName.equals(path.getToPortalName())) {
                path.setToPortalName(newName);
            }
            changeBlockPortalName((OBlock)path.getBlock(), newName, oldName);
        }
    }

    /**
    * should not be necessary, but just in case portal
    * has more than one object representing it
    */
    private void changeBlockPortalName(OBlock block, 
                                       String newName, String oldName) {
        if (block!=null) {
            Portal portal = block.getPortalByName(oldName);
            if (portal!=null) { portal.setName(newName); }            
        }
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

    public void dispose() {
        if (_fromBlock!=null) _fromBlock.removePortal(this);
        if (_toBlock!=null) _toBlock.removePortal(this);
    }
    
    public String toString() {
        return ("Portal \""+_portalName+"\" from block \""+getFromBlockName()+"\" to block \""+getToBlockName()+"\""); 
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Portal.class.getName());
}
