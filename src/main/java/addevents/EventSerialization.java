package addevents;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface EventSerialization {
    void serialize(EventMessage event, ByteArrayOutputStream output) throws IOException;
    EventMessage deserialize(ByteArrayInputStream input) throws IOException;
}
