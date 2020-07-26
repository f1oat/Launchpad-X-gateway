package com.vortexel.swingswag;

import javax.swing.*;

public class JSpacer extends JComponent {

    private int width;
    private int height;

    public JSpacer(int w, int h) {
        width = w;
        height = h;

        setSize(width, height);
    }
}
