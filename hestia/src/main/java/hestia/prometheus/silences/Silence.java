package hestia.prometheus.silences;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class Silence {
    private String id;
    private Status status;
    private List<Matcher> matchers;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private String createdBy;
    private String updatedBy;
    private String comment;

    // Getter und Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String status() {
        return status.getState();
    }

    public List<Matcher> getMatchers() {
        return matchers;
    }
    
    public String getMatchersString() {
        return matchers.stream().map(m -> m.getName() + "=" + m.getValue()).collect(Collectors.joining(", "));
    }

    public void setMatchers(List<Matcher> matchers) {
        this.matchers = matchers;
    }

    public OffsetDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(OffsetDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public OffsetDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(OffsetDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public static Gson gson() {
        return new GsonBuilder().registerTypeAdapter(OffsetDateTime.class, new TypeAdapter<OffsetDateTime>() {
            @Override
            public void write(JsonWriter out, OffsetDateTime value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else {
                    out.value(value.toString()); // Schreibt z.B. "2026-07-10T17:05:00Z"
                }
            }

            @Override
            public OffsetDateTime read(JsonReader in) throws IOException {
                if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                return OffsetDateTime.parse(in.nextString()); // Liest den ISO-String wieder ein
            }
        }).create();
    }
    
    public static class Status {
        private String state;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
    
    public static class Matcher {
        private String name; // Das Label, z.B. "alertname" oder "severity"
        private String value; // Der Wert, z.B. "InstanceDown"
        private boolean isRegex = false;
        private boolean isEqual = true;

        public Matcher() {
        }

        public Matcher(String n, String v) {
            name = n;
            value = v;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isRegex() {
            return isRegex;
        }

        public void setRegex(boolean regex) {
            isRegex = regex;
        }

        public boolean isEqual() {
            return isEqual;
        }

        public void setEqual(boolean equal) {
            isEqual = equal;
        }
    }
    
    public static class SilenceCreated {
        private String silenceID;

        public String getSilenceID() {
            return silenceID;
        }

        public void setSilenceID(String silenceID) {
            this.silenceID = silenceID;
        }
    }
}
