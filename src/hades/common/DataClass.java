package hades.common;

import dobby.util.Json;

import java.io.Serializable;

public interface DataClass extends Serializable {
    String getKey();
    Json toJson();
}
