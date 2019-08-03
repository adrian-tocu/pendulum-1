package net.helix.hlx.service.dto;

import net.helix.hlx.XI;

/**
 * <p>
 *     When a command is not recognized by the default API, we try to process it as a XI module.
 *     XI stands for Helix eXtension Interface. See {@link XI} for more information.
 * </p>
 * <p>
 *     The response will contain the reply that the XI module gave.
 *     This could be empty, depending on the module.
 * </p>
 *
 *
 */
public class XIResponse extends AbstractResponse {
    private Object XI;

    public static XIResponse create(Object myXI) {
        XIResponse XIResponse = new XIResponse();
        XIResponse.XI = myXI;
        return XIResponse;
    }

    public Object getResponse() {
        return XI;
    }
}