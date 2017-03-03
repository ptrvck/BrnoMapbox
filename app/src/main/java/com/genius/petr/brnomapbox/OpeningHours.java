package com.genius.petr.brnomapbox;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr on 3. 3. 2017.
 */

public class OpeningHours {
    public List<FromTo> getOpenToday(int day) {
        return open.get(day);
    }

    public void setOpen(List<List<FromTo>> open) {
        if (open.size() == 7) {
            this.open = open;
        } else {
            throw new IllegalArgumentException("List must have 7 elements");
        }
    }

    List<List<FromTo>> open = new ArrayList<List<FromTo>>(7);
}
