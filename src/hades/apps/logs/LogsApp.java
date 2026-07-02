package hades.apps.logs;

import common.html.Button;
import common.html.Div;
import common.html.Document;
import common.html.Paragraph;
import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import common.logger.LogEvent;
import common.logger.LogService;
import dobby.annotations.Get;
import dobby.files.StaticFile;
import dobby.io.HttpContext;
import hades.annotations.AuthorizedOnly;

import java.util.List;

@RegisterFor(LogsApp.class)
public class LogsApp {
    private final LogService logService;
    private static final String BASE_PATH = "/hades/logs";

    @Inject
    public LogsApp(LogService logService) {
        this.logService = logService;
    }

    @AuthorizedOnly
    @Get(BASE_PATH)
    public void render(HttpContext context) {
        context.getResponse().sendFile(buildDocument());
    }

    private StaticFile buildDocument() {
        final StaticFile file = new StaticFile();
        file.setContentType("text/html");

        final Document document = new Document();
        document.setTitle("Logs");
        document.addStyle("/hades/logs.css");

        final Div logContainer = new Div();
        logContainer.addStyle("logContainer");
        document.addChild(logContainer);

        final List<LogEvent> logs = logService.getAll();

        for (LogEvent log : logs) {
            final String logLevelString = switch (log.getLogLevel().toUpperCase()) {
                case "ERROR" -> "Error";
                case "WARN" -> "Warning";
                case "INFO" -> "Info";
                default -> "Unknown";
            };

            final Paragraph pLogLevel = new Paragraph(logLevelString);
            pLogLevel.addStyle("logLevel" + logLevelString);
            logContainer.addChild(pLogLevel);

            final Paragraph pTimestamp = new Paragraph("[" + log.getTimestamp() + "]");
            pTimestamp.addStyle("logTimestamp");
            logContainer.addChild(pTimestamp);

            final Paragraph pMessage = new Paragraph(log.getMessage().substring(0, Math.min(log.getMessage().length(), 100)));
            pMessage.addStyle("logMessage");
            logContainer.addChild(pMessage);

            final Button btnOpenDetails = new Button("view");
            btnOpenDetails.setOnClick("console.log(\"" + log.getMessage() + "\")");
            logContainer.addChild(btnOpenDetails);
        }

        file.setContent(document.toHtml().getBytes());
        return file;
    }
}
