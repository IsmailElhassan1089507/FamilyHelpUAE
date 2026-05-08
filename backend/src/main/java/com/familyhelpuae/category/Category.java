package com.familyhelpuae.category;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Node("Category")
public class Category {
    @Id
    private String categoryId;
    private String name;
}
