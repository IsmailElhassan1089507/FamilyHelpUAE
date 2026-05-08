package com.familyhelpuae.region;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Node("Region")
public class Region {
    @Id
    private String regionId;
    private String name;
    private String city;
}
