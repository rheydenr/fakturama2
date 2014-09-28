/**
 * 
 */
package com.sebulli.fakturama.misc;

/**
 * Order states.
 *
 */
public enum OrderState {
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
}
