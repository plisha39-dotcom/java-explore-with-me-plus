package ewm.service;

import ewm.dto.CategoryDto;
import ewm.dto.NewCategoryDto;
import ewm.exception.ConflictException;
import ewm.exception.NotFoundException;
import ewm.mapper.CategoryMapper;
import ewm.model.Category;
import ewm.repository.CategoryRepository;
import ewm.repository.EventRepository;
import ewm.repository.OffsetBasedPageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public CategoryService(CategoryRepository categoryRepository, EventRepository eventRepository) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> findAll(int from, int size) {
        return categoryRepository.findAll(new OffsetBasedPageRequest(
                        from, size, Sort.by(Sort.Direction.ASC, "id")))
                .getContent().stream().map(CategoryMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto findById(Long categoryId) {
        return CategoryMapper.toDto(getCategory(categoryId));
    }

    @Transactional
    public CategoryDto create(NewCategoryDto request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ConflictException("Category with name=" + request.getName() + " already exists");
        }
        return CategoryMapper.toDto(categoryRepository.save(CategoryMapper.toEntity(request)));
    }

    @Transactional
    public CategoryDto update(Long categoryId, CategoryDto request) {
        Category category = getCategory(categoryId);
        if (categoryRepository.existsByNameAndIdNot(request.getName(), categoryId)) {
            throw new ConflictException("Category with name=" + request.getName() + " already exists");
        }
        category.setName(request.getName());
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long categoryId) {
        Category category = getCategory(categoryId);
        if (eventRepository.existsByCategory_Id(categoryId)) {
            throw new ConflictException("The category is not empty");
        }
        categoryRepository.delete(category);
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(
                        "Category with id=" + categoryId + " was not found"));
    }
}
