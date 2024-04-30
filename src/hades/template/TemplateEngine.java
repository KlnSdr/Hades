package hades.template;

import dobby.files.StaticFile;
import dobby.util.json.NewJson;

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class TemplateEngine {
    private static final Pattern VARIABLE_PATTER = Pattern.compile("\\{\\{[a-zA-Z]+}}");

    public static StaticFile render(StaticFile template, NewJson data) {
        final StaticFile renderedTemplate = new StaticFile();
        renderedTemplate.setContentType(template.getContentType());

        String templateContent = new String(template.getContent());

        final String[] matches =
                VARIABLE_PATTER.matcher(templateContent).results().map(MatchResult::group).toArray(String[]::new);

        for (String match : matches) {
            final String variable = match.substring(2, match.length() - 2);
            final String value = data.getString(variable);

            if (value == null) {
                continue;
            }
            templateContent = templateContent.replace(match, value);
        }

        renderedTemplate.setContent(templateContent.getBytes());

        return renderedTemplate;
    }
}
