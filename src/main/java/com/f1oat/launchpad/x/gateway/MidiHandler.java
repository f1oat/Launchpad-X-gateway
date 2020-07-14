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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.*;
import javax.swing.SwingUtilities;

/**
 *
 * @author f1oat
 */
public class MidiHandler {

    private MidiDevice lpx_in, lpx_out, fl_in, fl_out, fl_out_native;
    private int sysex_count = 0;
    private iNotifier myGUI = null;

    public enum eMode {
        Native, Prog
    };
    private eMode mode = eMode.Prog;
    private boolean running = false;

    public void setGUI(iNotifier gui) {
        myGUI = gui;
    }

    private MidiDevice findMidiPort(String name, boolean output) {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                boolean isOutput = (device.getMaxReceivers() == -1);
                if (device.getDeviceInfo().getName().equals(name) && isOutput == output) {
                    return device;
                }
            } catch (MidiUnavailableException e) {
                // Handle or throw exception...
            }
        }
        return null;
    }

    private MidiDevice getMidiOut(String name) {
        return findMidiPort(name, true);
    }

    private MidiDevice getMidiIn(String name) {
        return findMidiPort(name, false);
    }

    public void stop() {
        if (!running) {
            return;
        }
        if (mode == eMode.Prog) setNoteMode();
        running = false;

        if (fl_in != null) {
            fl_in.close();
        }
        if (fl_out != null) {
            fl_out.close();
        }
        if (fl_out_native != null) {
            fl_out_native.close();
        }
        if (lpx_in != null) {
            lpx_in.close();
        }
        if (lpx_out != null) {
            lpx_out.close();
        }
    }

    public void start() {
        try {
            fl_in.getTransmitter().setReceiver(
                new FL_Receiver()
            );

            lpx_in.open();
            fl_in.open();
            fl_out.open();
            fl_out_native.open();
            running = true;

            setMode();
        } catch (MidiUnavailableException ex) {
            Logger.getLogger(MidiHandler.class.getName()).log(Level.SEVERE, null, ex);
            myGUI.notifyError();
        } catch (NullPointerException ex) {
            myGUI.notifyError();
        }
    }

    public void set(MidiDevice.Info _fl_in, MidiDevice.Info _fl_out, MidiDevice.Info _fl_out_native, MidiDevice.Info _lpx_in, MidiDevice.Info _lpx_out) {
        try {
            stop();

            if (_lpx_out != null) lpx_out = MidiSystem.getMidiDevice(_lpx_out);
            if (_lpx_in != null) lpx_in = MidiSystem.getMidiDevice(_lpx_in);

            if (_fl_in != null) fl_in = MidiSystem.getMidiDevice(_fl_in);
            if (_fl_out != null) fl_out = MidiSystem.getMidiDevice(_fl_out);
            if (_fl_out_native != null) fl_out_native = MidiSystem.getMidiDevice(_fl_out_native);
        } catch (MidiUnavailableException ex) {
            Logger.getLogger(MidiHandler.class.getName()).log(Level.SEVERE, null, ex);
            myGUI.notifyError();
        }
    }

    public static byte[] hex2byte(String s) {
        String[] values = s.split(" ");
        int nbBytes = values.length;
        byte[] data = new byte[nbBytes];
        for (int i = 0; i < nbBytes; i++) {
            data[i] = (byte) ((Character.digit(values[i].charAt(0), 16) << 4)
                    + Character.digit(values[i].charAt(1), 16));
        }
        return data;
    }

    private void pressPad(int cmd, int pad, int channel, int velo) {
        //System.out.printf("pressPad %d %d %s\n", pad, channel, velo);
        try {
            if (pad < 0x5B) {
                fl_out.getReceiver().send(new ShortMessage(cmd, channel, pad, velo), -1);
            } else {
                fl_out.getReceiver().send(new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, pad - 0x5B + 0x68, velo), -1);
            }
        } catch (MidiUnavailableException | InvalidMidiDataException ex) {
            Logger.getLogger(MidiHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void pressPad(int pad, int velo) {
        pressPad(ShortMessage.NOTE_ON, pad, 0, velo);
    }

    private class LPX_Receiver implements Receiver {

        @Override
        public void send(MidiMessage msg, long timeStamp) {
            byte[] data = msg.getMessage();
            System.out.printf("%02X %02X %02X\n", data[0], data[1], data[2]);

            int command = data[0] & 0xF0;
            int channel = data[0] & 0x0F;
            int pad = data[1];
            int velo = data[2];

            switch (command) {
                case ShortMessage.NOTE_ON:
                case ShortMessage.POLY_PRESSURE:
                    pressPad(command, pad, channel, velo);
                    break;
                case ShortMessage.CONTROL_CHANGE:
                    pressPad(ShortMessage.NOTE_ON, pad, channel, velo);
                    break;
            }
         }

        public void close() {
        }
    }

    private void guiUpdate() {
        if (myGUI == null) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                myGUI.udpateStatus();
            }
        });
    }

    private void trig_Activity() {
        guiUpdate();
    }

    private void sendSysexLPX(String header, ByteBuffer payload) {
        if (lpx_out == null) {
            return;
        }

        try {
            ByteBuffer outBuffer = ByteBuffer.allocate(1024);
            outBuffer.put(hex2byte(header));
            if (payload != null) {
                outBuffer.put(payload);
            }

            SysexMessage outMsg = new SysexMessage(outBuffer.array(), outBuffer.position());

            lpx_out.open();
            lpx_out.getReceiver().send(outMsg, -1);
            lpx_out.close();
        } catch (MidiUnavailableException | InvalidMidiDataException ex) {
            Logger.getLogger(MidiHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setProgramMode() {
        sendSysexLPX("F0h 00h 20h 29h 02h 0Ch 00h 7Fh F7h", null);
    }

    private void setNoteMode() {
        sendSysexLPX("F0h 00h 20h 29h 02h 0Ch 00h 01h F7h", null);
    }

    public void setMode(eMode mode) {
        this.mode = mode;
        if (running) {
            setMode();
        }
    }

    private void setMode() {
        try {
            switch (mode) {
                case Prog: {
                    setProgramMode();
                    lpx_in.getTransmitter().setReceiver(new LPX_Receiver());
                    break;
                }
                case Native: {
                    setNoteMode();
                    lpx_in.getTransmitter().setReceiver(fl_out_native.getReceiver());
                    break;
                }
            }
        } catch (MidiUnavailableException ex) {
            Logger.getLogger(MidiHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class FL_Receiver implements Receiver {

        @Override
        public void send(MidiMessage msg, long timeStamp) {
            trig_Activity();
            byte[] data = msg.getMessage();
            byte sysex = data[6];
            switch (sysex) {
                case 0x22: {
                    //setProgramMode(); //Set layout Session / RX: F0h 00h 20h 29h 02h 18h 22h 00h F7h
                    break;
                }

                case 0x0E:  //Set all LEDs
                    break;

                case 0x0B: {
                    byte[] leds = Arrays.copyOfRange(data, 7, data.length - 1); //Set LEDs RGB mode
                    int nbLeds = leds.length / 4;
                    ByteBuffer payload = ByteBuffer.allocate(1024);
                    for (int i = 0; i < nbLeds; i++) {
                        byte index = leds[4 * i];
                        byte r = (byte) (2 * leds[4 * i + 1]);
                        byte g = (byte) (2 * leds[4 * i + 2]);
                        byte b = (byte) (2 * leds[4 * i + 3]);

                        byte[] cmd = {3, index, r, g, b};
                        payload.put(cmd);
                        myGUI.setPadColor(index, r, g, b);
                    }
                    payload.put((byte) 0xF7);
                    payload.flip();
                    sendSysexLPX("F0h 00h 20h 29h 02h 0Ch 03h", payload);   // LED lighting
                    break;
                }

            }
        }

        @Override
        public void close() {
        }
    }
}
