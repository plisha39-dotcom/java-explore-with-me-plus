package ewm.mapper;

import ewm.dto.CategoryDto;
import ewm.dto.NewCategoryDto;
import ewm.model.Category;

public class CategoryMapper {
    public static Category toEntity(NewCategoryDto request) {
        Category category = new Category();
        category.setName(request.getName());
        return category;
    }

    public static CategoryDto toDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }
}
