package com.genius.petr.brnomapbox;

/**
 * Created by Petr on 3. 3. 2017.
 */

public class FromTo {
    public BrnoTime getFrom() {
        return from;
    }

    public void setFrom(BrnoTime from) {
        this.from = from;
    }

    public BrnoTime getTo() {
        return to;
    }

    public void setTo(BrnoTime to) {
        this.to = to;
    }

    BrnoTime from;
    BrnoTime to;
}
