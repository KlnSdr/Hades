package hades.authorized.rest;

import hades.authorized.Permission;

import java.util.List;

public record GetPermissionsDTO(List<Permission> permissions) {
    public GetPermissionsDTO() {
        this(List.of());
    }

}
