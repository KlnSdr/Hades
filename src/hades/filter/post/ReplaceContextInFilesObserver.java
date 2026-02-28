package hades.filter.post;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import common.util.TemplateEngine;
import dobby.IConfig;
import dobby.files.StaticFile;
import dobby.files.service.IStaticFileService;
import dobby.observer.Event;
import dobby.observer.EventType;
import dobby.observer.Observer;
import dobby.util.Tupel;
import dobby.util.json.NewJson;

import java.nio.charset.StandardCharsets;

@RegisterFor(ReplaceContextInFilesObserver.class)
public class ReplaceContextInFilesObserver implements Observer<Tupel<String, StaticFile>> {
    private final IStaticFileService staticFileService;
    private final TemplateEngine templateEngine;
    private final IConfig config;

    @Inject
    public ReplaceContextInFilesObserver(IStaticFileService staticFileService, TemplateEngine templateEngine, IConfig config) {
        this.staticFileService = staticFileService;
        this.templateEngine = templateEngine;
        this.config = config;
    }

    @Override
    public void onEvent(Event<Tupel<String, StaticFile>> event) {
        if (event.getType() != EventType.CREATED) {
            return;
        }

        final String path = event.getData()._1();
        final StaticFile file = event.getData()._2();

        if (!file.getContentType().matches("text/.*")) {
            return;
        }

        final NewJson json = new NewJson();
        json.setString("CONTEXT", config.getString("hades.context", ""));

        final String renderedContent = templateEngine.render(new String(file.getContent()), json);
        file.setContent(renderedContent.getBytes(StandardCharsets.UTF_8));

        staticFileService.storeFileNoEvent(path, file);
    }
}
