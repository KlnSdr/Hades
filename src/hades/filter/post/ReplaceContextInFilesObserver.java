package hades.filter.post;

import common.inject.annotations.Inject;
import common.inject.annotations.RegisterFor;
import dobby.Config;
import dobby.files.StaticFile;
import dobby.files.service.IStaticFileService;
import dobby.observer.Event;
import dobby.observer.EventType;
import dobby.observer.Observer;
import dobby.util.Tupel;
import dobby.util.json.NewJson;
import hades.template.TemplateEngine;

@RegisterFor(ReplaceContextInFilesObserver.class)
public class ReplaceContextInFilesObserver implements Observer<Tupel<String, StaticFile>> {
    private final IStaticFileService staticFileService;
    private final TemplateEngine templateEngine;

    @Inject
    public ReplaceContextInFilesObserver(IStaticFileService staticFileService, TemplateEngine templateEngine) {
        this.staticFileService = staticFileService;
        this.templateEngine = templateEngine;
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
        json.setString("CONTEXT", Config.getInstance().getString("hades.context", ""));

        final StaticFile renderedFile = templateEngine.render(file, json);

        staticFileService.storeFileNoEvent(path, renderedFile);
    }
}
