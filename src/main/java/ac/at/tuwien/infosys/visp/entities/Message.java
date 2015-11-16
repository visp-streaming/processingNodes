package ac.at.tuwien.infosys.visp.entities;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {

    private String id;
    private String payload;
    private String header;

    public Message(String id, String header, String payload) {
        this.id = id;
        this.payload = payload;
        this.header = header;
    }

    public Message() { }

    public Message(String payload) {
        this.payload = payload;
        this.header = "initial";
        this.id = UUID.randomUUID().toString();
    }

    public Message(String header, String payload) {
        this.payload = payload;
        this.header = header;
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public String getPayload() {
        return payload;
    }

    public String getHeader() {
        return header;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", header='" + header + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}

