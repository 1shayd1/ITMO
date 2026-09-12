package org.shayd1.request_and_response;

import org.shayd1.model.Route;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record Response(boolean success, String message, List<Route> collection)  implements Serializable {

    public Response(boolean success, String message) {
        this(success, message, null);
    }

    @Serial
    private static final long serialVersionUID = 1L;
}
