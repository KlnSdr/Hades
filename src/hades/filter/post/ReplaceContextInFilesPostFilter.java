package hades.filter.post;

import dobby.files.StaticFile;
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.response.Response;
import dobby.util.Config;
import dobby.util.Json;
import hades.filter.FilterOrder;
import hades.template.TemplateEngine;

public class ReplaceContextInFilesPostFilter implements Filter {
    @Override
    public String getName() {
        return "ReplaceContextInFilesPostFilter";
    }

    @Override
    public FilterType getType() {
        return FilterType.POST;
    }

    @Override
    public int getOrder() {
        return FilterOrder.REPLACE_CONTEXT_IN_FILES_POST_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext httpContext) {
        final Response response = httpContext.getResponse();

        final String contentTypeHeader = response.getHeader("Content-Type");

        if (contentTypeHeader == null || !contentTypeHeader.matches("text/.*")) {
            return true;
        }

        final StaticFile staticFile = new StaticFile();
        staticFile.setContent(response.getBody());

        final Json json = new Json();
        json.setString("CONTEXT", Config.getInstance().getString("hades.context", ""));

        final StaticFile renderedFile = TemplateEngine.render(staticFile, json);
        response.setBody(renderedFile.getContent());

        return true;
    }
}
