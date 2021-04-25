package co.cxip.chrec.api.methods;

import java.util.List;

import co.cxip.chrec.api.ClubhouseAPIRequest;
import co.cxip.chrec.api.model.Event;

public class GetEvents extends ClubhouseAPIRequest<GetEvents.Response> {
    public GetEvents(){
        super("GET", "get_events", Response.class);
    }

    public static class Response{
        public List<Event> events;
    }
}
