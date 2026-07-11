package hades.authorized.rest;

import java.util.List;

public record GetCheckedRoutesDTO(List<String> routes) {
    public GetCheckedRoutesDTO() {
        this(List.of());
    }
}
