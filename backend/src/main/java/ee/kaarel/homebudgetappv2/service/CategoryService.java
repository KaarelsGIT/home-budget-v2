package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.CategoryDTO;
import ee.kaarel.homebudgetappv2.model.Category;
import ee.kaarel.homebudgetappv2.model.CategoryType;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserAccessService userAccessService;

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAll() {
        return categoryRepository.findByUserIdIn(userAccessService.getAccessibleUserIds())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllByType(CategoryType type) {
        return categoryRepository.findByUserIdInAndType(userAccessService.getAccessibleUserIds(), type)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryDTO getById(Long id) {
        return toDto(getAccessibleCategoryOrThrow(id));
    }

    @Transactional
    public CategoryDTO create(CategoryDTO request, Long userId) {
        User targetUser = userAccessService.resolveTargetUser(userId);

        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setUser(targetUser);

        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO request) {
        Category existing = getAccessibleCategoryOrThrow(id);
        existing.setName(request.getName());
        existing.setType(request.getType());

        return toDto(categoryRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        Category category = getAccessibleCategoryOrThrow(id);
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public Category getAccessibleCategoryOrThrow(Long categoryId) {
        return categoryRepository.findByIdAndUserIdIn(categoryId, userAccessService.getAccessibleUserIds())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));
    }

    private CategoryDTO toDto(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setType(category.getType());
        dto.setUserId(category.getUser().getId());
        return dto;
    }
}
