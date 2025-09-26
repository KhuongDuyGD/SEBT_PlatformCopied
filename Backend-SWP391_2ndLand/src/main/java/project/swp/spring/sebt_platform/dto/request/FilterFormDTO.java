package project.swp.spring.sebt_platform.dto.request;

public record FilterFormDTO (
        String brand,
        String location,
        String category,
        String sortBy,
        Double minPrice,
        Double maxPrice
) {}
