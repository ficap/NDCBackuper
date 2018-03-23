package ml.storky.NDCBackuper;

import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by storky on 22.4.16.
 */
public class Config {
    public static final int RUNNING_CONFIG = 1;
    public static final int STARTUP_CONFIG = 2;

    private boolean isLoaded = false;
    private Date timestamp;
    private NetworkDevice parent;
    private int type;
    private String content;
    private int configID;
    private String comment; // TODO: implement

    public Config(NetworkDevice parent) {
        this.parent = parent;
    }

    public Config(int id, int type, String content, Date date, NetworkDevice device) {
        this.configID = id;
        this.type = type;
        this.content = content;
        this.timestamp = date;
        this.parent = device;
    }

    public Config(NetworkDevice parent, String content, int type) {
        this(parent);
        this.content = content;
        this.type = type;
        this.timestamp = new Date(System.currentTimeMillis());
    }

    public boolean loadFromDB() {
        return true;
    }

    public boolean dumpToFile(File output) {
        return true;
    }

    public NetworkDevice getParent() {
        return this.parent;
    }

    public int getType() {
        return this.type;
    }

    public String getRawContent() {
        return this.content;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public int getConfigID() {
        return this.configID;
    }

    public ConfRow getConfRow() {
        return new ConfRow(Integer.toString(this.configID), DateFormat.getDateInstance().format(this.timestamp), this.comment, this.content);
    }
    public static class ConfRow {
        private final SimpleStringProperty id;
        private final SimpleStringProperty date;
        private final SimpleStringProperty comment;
        private final String content;

        private ConfRow(String id, String date, String comment, String content) {
            this.id = new SimpleStringProperty(id);
            this.date = new SimpleStringProperty(date);
            this.comment = new SimpleStringProperty(comment);
            this.content = content;
        }

        public String getId() {
            return this.id.get();
        }

        public String getDate() {
            return this.date.get();
        }

        public String getComment() {
            return this.comment.get();
        }

        public String getContent() {
            return this.content;
        }
    }
}
