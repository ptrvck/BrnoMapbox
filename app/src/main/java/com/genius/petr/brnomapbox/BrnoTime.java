package com.genius.petr.brnomapbox;

/**
 * Created by Petr on 3. 3. 2017.
 */

public class BrnoTime {
    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        if (hours >= 0 && hours <= 23) {
            this.hours = hours;
        } else {
            throw new IllegalArgumentException("Invalid time");
        }
    }

    int hours;

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        if (minutes >= 0 && minutes <= 59) {
            this.minutes = minutes;
        } else {
            throw new IllegalArgumentException("Invalid time");
        }
    }

    int minutes;
}
