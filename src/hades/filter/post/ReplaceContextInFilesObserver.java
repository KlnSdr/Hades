package hades.filter.post;

import dobby.files.StaticFile;
import dobby.files.service.StaticFileService;
import dobby.observer.Event;
import dobby.observer.EventType;
import dobby.observer.Observer;
import dobby.Config;
import dobby.util.Tupel;
import dobby.util.json.NewJson;
import hades.template.TemplateEngine;

public class ReplaceContextInFilesObserver implements Observer<Tupel<String, StaticFile>> {
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

        final StaticFile renderedFile = TemplateEngine.render(file, json);

        StaticFileService.getInstance().storeFileNoEvent(path, renderedFile);
    }
}
