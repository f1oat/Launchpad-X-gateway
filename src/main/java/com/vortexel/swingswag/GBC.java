package com.vortexel.swingswag;

import java.awt.*;

/**
 * GBC is a helper for the GridBagConstraints class. The normal GridBagConstraints class has no convenience methods
 * meaning that changing more than one property is usually spread across multiple lines. Furthermore it becomes
 * unclear what the purpose of the different assignments is unless you already know what's happening.
 *
 * The {@code GBC} class makes it easy to write one-liners for setting properties and adding controls. It's
 * also easier to type {@code GBC} instead of {@code GridBagConstraints} for referencing constants.
 *
 * @author bindernews
 */
public class GBC extends GridBagConstraints {

    public Container panel;

    public GBC() {
        super();
        panel = null;
    }

    public GBC(Container panel) {
        super();
        this.panel = panel;
    }

    public GBC at(int x, int y) {
        gridx = x;
        gridy = y;
        return this;
    }

    public GBC move(int dx, int dy) {
        gridx += dx;
        gridy += dy;
        return this;
    }

    public GBC span(int width, int height) {
        gridwidth = width;
        gridheight = height;
        return this;
    }

    public GBC weight(float x, float y) {
        weightx = x;
        weighty = y;
        return this;
    }

    public GBC pad(int x, int y) {
        ipadx = x;
        ipady = y;
        return this;
    }

    public GBC anchor(int value) {
        anchor = value;
        return this;
    }

    public GBC fill(int value) {
        fill = value;
        return this;
    }

    public GBC insets(int left, int top, int right, int bottom) {
        insets.set(top, left, bottom, right);
        return this;
    }

    public void put(Component component) {
        panel.add(component, this);
    }

    @Override
    public GBC clone() {
        return (GBC)super.clone();
    }
}

