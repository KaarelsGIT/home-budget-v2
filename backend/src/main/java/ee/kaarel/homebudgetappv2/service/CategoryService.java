package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.CategoryDTO;
import ee.kaarel.homebudgetappv2.dto.CategoryTreeDto;
import ee.kaarel.homebudgetappv2.model.Category;
import ee.kaarel.homebudgetappv2.model.CategoryType;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
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
    public List<CategoryTreeDto> getTree(CategoryType type) {
        List<Category> categories = type == null
                ? categoryRepository.findByUserIdIn(userAccessService.getAccessibleUserIds())
                : categoryRepository.findByUserIdInAndType(userAccessService.getAccessibleUserIds(), type);

        List<Category> roots = categories.stream().filter(c -> c.getParent() == null).toList();
        return roots.stream().map(c -> toTree(c, categories)).toList();
    }

    @Transactional(readOnly = true)
    public CategoryDTO getById(Long id) {
        Category category = getAccessibleCategoryOrThrow(id);
        return toDto(category);
    }

    @Transactional
    public CategoryDTO create(CategoryDTO request, Long userId) {
        User targetUser = userAccessService.resolveTargetUser(userId);

        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setUser(targetUser);

        if (request.getParentId() != null) {
            Category parent = getAccessibleCategoryOrThrow(request.getParentId());
            if (!parent.getUser().getId().equals(targetUser.getId())) {
                throw new ResponseStatusException(BAD_REQUEST, "Parent category must belong to the same user");
            }
            if (parent.getType() != request.getType()) {
                throw new ResponseStatusException(BAD_REQUEST, "Parent category type must match child category type");
            }
            category.setParent(parent);
        }

        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO update(Long id, CategoryDTO request) {
        Category category = getAccessibleCategoryOrThrow(id);
        category.setName(request.getName());
        category.setType(request.getType());

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new ResponseStatusException(BAD_REQUEST, "Category cannot be parent of itself");
            }
            Category parent = getAccessibleCategoryOrThrow(request.getParentId());
            if (!parent.getUser().getId().equals(category.getUser().getId())) {
                throw new ResponseStatusException(BAD_REQUEST, "Parent category must belong to the same user");
            }
            if (parent.getType() != request.getType()) {
                throw new ResponseStatusException(BAD_REQUEST, "Parent category type must match child category type");
            }
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        return toDto(categoryRepository.save(category));
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
        dto.setParentId(category.getParent() != null ? category.getParent().getId() : null);
        dto.setUserId(category.getUser().getId());
        return dto;
    }

    private CategoryTreeDto toTree(Category category, List<Category> all) {
        List<CategoryTreeDto> children = new ArrayList<>();
        all.stream().filter(c -> c.getParent() != null && c.getParent().getId().equals(category.getId()))
                .forEach(child -> children.add(toTree(child, all)));

        return new CategoryTreeDto(category.getId(), category.getName(), category.getType(),
                category.getParent() != null ? category.getParent().getId() : null, children);
    }
}
