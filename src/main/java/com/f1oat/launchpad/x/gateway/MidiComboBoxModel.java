/*
 * Copyright 2020 Frederic Rible <f1oat@f1oat.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.f1oat.launchpad.x.gateway;

import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author f1oat
 */
public class MidiComboBoxModel extends AbstractListModel implements ComboBoxModel {

    boolean isOutput;
    List<MidiDevice.Info> ports = null;
    MidiDevice.Info selection = null;
    
    public MidiComboBoxModel(boolean isOutput) {
        ports = listMidiPorts(isOutput);
        this.isOutput = isOutput;
    }
    
    public List<MidiDevice.Info> listMidiPorts(boolean output) {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        List<MidiDevice.Info> list = new ArrayList<>();
        
        for (MidiDevice.Info info : infos) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                boolean isOutput = (device.getMaxReceivers() == -1);
                if (output == isOutput) list.add(info);
            }catch (MidiUnavailableException e) {
                // Handle or throw exception...
            }
        }

        return list;
    }

    public void refreshMidiPorts() {
        int oldSize = ports.size();
        this.ports = listMidiPorts(isOutput);
        int newSize = ports.size();
        int minSize = Math.min(oldSize, newSize);
        fireContentsChanged(this, 0, minSize);
        if (newSize > oldSize) {
            fireIntervalAdded(this, oldSize, newSize);
        } else if (oldSize > newSize) {
            fireIntervalRemoved(this, newSize, oldSize);
        }
    }
        
    @Override
    public Object getElementAt(int index) {
        return ports.get(index);
    }

    @Override
    public int getSize() {
        return ports.size();
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem instanceof String) {
            for (MidiDevice.Info device : ports) {
                if (device.toString().equals((String)anItem)) {
                    selection = device;
                    break;
                }
            }            
        }
        else {
            selection = (MidiDevice.Info)anItem;
        }

    } // item from the pull-down list

    // Methods implemented from the interface ComboBoxModel
    @Override
    public Object getSelectedItem() {
        return selection; // to add the selection to the combo box
    }
}
