package project.swp.spring.sebt_platform.model.dto.request;

public record FilterFormDTO (
        String brand,
        String location,
        String category,
        String sortBy,
        Double minPrice,
        Double maxPrice
) {}
