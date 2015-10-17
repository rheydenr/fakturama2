/**
 * 
 */
package com.sebulli.fakturama.misc;

import java.util.Arrays;
import java.util.Optional;

/**
 * Order states.
 *
 */
public enum OrderState {
    NONE(0),
    PENDING(10),
    PROCESSING(50),
    SHIPPED(90),
    COMPLETED(100),
    ;
    
    int state;

    /**
     * @param state
     */
    private OrderState(int state) {
        this.state = state;
    }

    /**
     * @return the state
     */
    public final int getState() {
        return state;
    }
    
    public static OrderState findByProgressValue(Optional<Integer> progress) {
        return Arrays.stream(OrderState.values()).filter(os -> os.getState() == progress.orElse(Integer.valueOf(0))).findFirst().get();
    }
}
