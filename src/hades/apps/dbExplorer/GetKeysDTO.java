package hades.apps.dbExplorer;

import java.util.List;

public class GetKeysDTO {
    private List<String> keys;

    public GetKeysDTO() {
        this(List.of());
    }

    public GetKeysDTO(List<String> keys) {
        this.keys = keys;
    }
}
