package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.CategoryRequest;
import ee.kaarel.homebudgetappv2.dto.CategoryResponse;
import ee.kaarel.homebudgetappv2.mapper.CategoryMapper;
import ee.kaarel.homebudgetappv2.model.Category;
import ee.kaarel.homebudgetappv2.model.CategoryType;
import ee.kaarel.homebudgetappv2.model.User;
import ee.kaarel.homebudgetappv2.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final UserAccessService userAccessService;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findByUserIdIn(userAccessService.getAccessibleUserIds())
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllByType(CategoryType type) {
        return categoryRepository.findByUserIdInAndType(userAccessService.getAccessibleUserIds(), type)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category = getAccessibleCategoryOrThrow(id);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request, Long userId) {
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

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
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

        return categoryMapper.toResponse(categoryRepository.save(category));
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
}
