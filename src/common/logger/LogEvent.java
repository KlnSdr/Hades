package common.logger;

import common.sql.entity.Entity;
import common.sql.entity.annotations.*;
import dobby.util.json.NewJson;

import java.sql.Timestamp;

@Table("hades_logs")
public class LogEvent implements Entity<Long> {
    @Id
    @NotNull
    @AutoIncrement
    @Column("id")
    private Long id;
    @Column("message")
    private String message;
    @Column("log_level")
    private String logLevel;
    @Default("CURRENT_TIMESTAMP")
    @Column("timestamp")
    private Timestamp timestamp;

    public LogEvent() {

    }
    public LogEvent(String message, String logLevel) {
        this.message = message;
        this.logLevel = logLevel;
    }

    public String getMessage() {
        return message;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public NewJson toJson() {
        final NewJson json = new NewJson();
        json.setInt("id", id.intValue());
        json.setString("message", message);
        json.setString("logLevel", logLevel);
        json.setString("timestamp", timestamp.toString());
        return json;
    }
}
