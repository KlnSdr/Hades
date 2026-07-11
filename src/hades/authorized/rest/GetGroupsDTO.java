package hades.authorized.rest;

import hades.authorized.Group;

import java.util.List;

public record GetGroupsDTO(List<Group> groups) {

    public GetGroupsDTO() {
        this(List.of());
    }
}
