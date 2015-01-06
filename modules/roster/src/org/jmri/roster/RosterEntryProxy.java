package org.jmri.roster;

import java.beans.PropertyChangeEvent;
import jmri.jmrit.roster.RosterEntry;

/**
 * Proxy object for editing RosterEntries.
 *
 * This class is intended to be used by RosterEntry editors so that multiple
 * editors can be open at the same time and making changes that are reflected in
 * the editors, but not in the rest of JMRI or written to disk.
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
public final class RosterEntryProxy extends RosterEntry {

    private final RosterEntry rosterEntry;

    public RosterEntryProxy(RosterEntry re) {
        super(re, re.getId());
        re.setOpen(true);
        this._fileName = re.getFileName();
        this.rosterEntry = re;
        this.rosterEntry.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            switch (evt.getPropertyName()) {
                case RosterEntry.COMMENT:
                    this.setComment(this.rosterEntry.getComment());
                    break;
                case RosterEntry.DATE_UPDATED:
                    this.setDateUpdated(this.rosterEntry.getDateUpdated());
                    break;
                case RosterEntry.DCC_ADDRESS:
                    this.setDccAddress(this.rosterEntry.getDccAddress());
                    break;
                case RosterEntry.DECODER_COMMENT:
                    this.setDecoderComment(this.rosterEntry.getDecoderComment());
                    break;
                case RosterEntry.DECODER_FAMILY:
                    this.setDecoderFamily(this.rosterEntry.getDecoderFamily());
                    break;
                case RosterEntry.DECODER_MODEL:
                    this.setDecoderModel(this.rosterEntry.getDecoderModel());
                    break;
                case RosterEntry.FILENAME:
                    this.setFileName(this.rosterEntry.getFileName());
                    break;
                case RosterEntry.ICON_FILE_PATH:
                    this.setIconPath(this.rosterEntry.getIconPath());
                    break;
                case RosterEntry.ID:
                    this.setId(this.rosterEntry.getId());
                    break;
                case RosterEntry.IMAGE_FILE_PATH:
                    this.setImagePath(this.rosterEntry.getImagePath());
                    break;
                case RosterEntry.LONG_ADDRESS:
                    this.setLongAddress(this.rosterEntry.isLongAddress());
                    break;
                case RosterEntry.MAX_SPEED:
                    this.setMaxSpeedPCT(this.rosterEntry.getMaxSpeedPCT());
                    break;
                case RosterEntry.MFG:
                    this.setMfg(this.rosterEntry.getMfg());
                    break;
                case RosterEntry.MODEL:
                    this.setModel(this.rosterEntry.getModel());
                    break;
                case RosterEntry.OWNER:
                    this.setOwner(this.rosterEntry.getOwner());
                    break;
                case RosterEntry.PROTOCOL:
                    this.setProtocol(this.rosterEntry.getProtocol());
                    break;
                case RosterEntry.ROADNAME:
                    this.setRoadName(this.rosterEntry.getRoadName());
                    this.setRoadNumber(this.rosterEntry.getRoadNumber());
                    break;
                case RosterEntry.SHUNTING_FUNCTION:
                    this.setShuntingFunction(this.rosterEntry.getShuntingFunction());
                    break;
                case RosterEntry.SPEED_PROFILE:
                    this.setSpeedProfile(this.rosterEntry.getSpeedProfile());
                    break;
                case RosterEntry.URL:
                    this.setURL(this.rosterEntry.getURL());
                    break;
                default:
                    if (evt.getPropertyName().startsWith(RosterEntry.FUNCTION_IMAGE)) {
                        int function = Integer.parseInt(evt.getPropertyName().substring(RosterEntry.FUNCTION_IMAGE.length()));
                        this.setFunctionImage(function, this.rosterEntry.getFunctionImage(function));
                    } else if (evt.getPropertyName().startsWith(RosterEntry.FUNCTION_SELECTED_IMAGE)) {
                        int function = Integer.parseInt(evt.getPropertyName().substring(RosterEntry.FUNCTION_SELECTED_IMAGE.length()));
                        this.setFunctionSelectedImage(function, this.rosterEntry.getFunctionSelectedImage(function));
                    } else if (evt.getPropertyName().startsWith(RosterEntry.FUNCTION_LABEL)) {
                        int function = Integer.parseInt(evt.getPropertyName().substring(RosterEntry.FUNCTION_LABEL.length()));
                        this.setFunctionLabel(function, this.rosterEntry.getFunctionLabel(function));
                    } else if (evt.getPropertyName().startsWith(RosterEntry.FUNCTION_LOCKABLE)) {
                        int function = Integer.parseInt(evt.getPropertyName().substring(RosterEntry.FUNCTION_LOCKABLE.length()));
                        this.setFunctionLockable(function, this.rosterEntry.getFunctionLockable(function));
                    } else if (evt.getPropertyName().startsWith(RosterEntry.SOUND_LABEL)) {
                        int function = Integer.parseInt(evt.getPropertyName().substring(RosterEntry.SOUND_LABEL.length()));
                        this.setSoundLabel(function, this.rosterEntry.getSoundLabel(function));
                    } else if (evt.getPropertyName().startsWith(RosterEntry.ATTRIBUTE_DELETED)) {
                        String key = evt.getPropertyName().substring(RosterEntry.ATTRIBUTE_DELETED.length());
                        this.deleteAttribute(key);
                    } else if (evt.getPropertyName().startsWith(RosterEntry.ATTRIBUTE_UPDATED)) {
                        String key = evt.getPropertyName().substring(RosterEntry.ATTRIBUTE_UPDATED.length());
                        this.putAttribute(key, evt.getNewValue().toString());
                    }
            }
        });
    }

    @Override
    public void updateFile() {
        this.rosterEntry.setId(this.getId());
        this.rosterEntry.setFileName(this.getFileName());
        this.rosterEntry.setRoadName(this.getRoadName());
        this.rosterEntry.setRoadNumber(this.getRoadNumber());
        this.rosterEntry.setMfg(this.getMfg());
        this.rosterEntry.setModel(this.getModel());
        this.rosterEntry.setDccAddress(this.getDccAddress());
        this.rosterEntry.setProtocol(this.getProtocol());
        this.rosterEntry.setComment(this.getComment());
        this.rosterEntry.setDecoderModel(this.getDecoderModel());
        this.rosterEntry.setDecoderFamily(this.getDecoderFamily());
        this.rosterEntry.setDecoderComment(this.getDecoderComment());
        this.rosterEntry.setOwner(this.getOwner());
        this.rosterEntry.setImagePath(this.getImagePath());
        this.rosterEntry.setIconPath(this.getIconPath());
        this.rosterEntry.setURL(this.getURL());
        this.rosterEntry.setMaxSpeedPCT(this.getMaxSpeedPCT());
        this.rosterEntry.setShuntingFunction(this.getShuntingFunction());

        for (int i = 0; i < RosterEntry.MAXFNNUM; i++) {
            this.rosterEntry.setFunctionLabel(i, this.getFunctionLabel(i));
            this.rosterEntry.setFunctionSelectedImage(i, this.getFunctionSelectedImage(i));
            this.rosterEntry.setFunctionImage(i, this.getFunctionImage(i));
            this.rosterEntry.setFunctionLockable(i, this.getFunctionLockable(i));
        }

        for (int i = 0; i < RosterEntry.MAXSOUNDNUM; i++) {
            this.rosterEntry.setSoundLabel(i, this.getSoundLabel(i));
        }

        this.rosterEntry.updateFile();
    }

}
