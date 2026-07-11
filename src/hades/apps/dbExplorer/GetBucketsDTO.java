package hades.apps.dbExplorer;

import java.util.List;

public class GetBucketsDTO {
    private final List<String> buckets;

    public GetBucketsDTO() {
        this(List.of());
    }

    public GetBucketsDTO(List<String> buckets) {
        this.buckets = buckets;
    }
}
