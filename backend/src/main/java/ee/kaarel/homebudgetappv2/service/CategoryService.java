package ee.kaarel.homebudgetappv2.service;

import ee.kaarel.homebudgetappv2.dto.CategoryRequest;
import ee.kaarel.homebudgetappv2.dto.CategoryResponse;
import ee.kaarel.homebudgetappv2.dto.SubCategoryRequest;
import ee.kaarel.homebudgetappv2.dto.SubCategoryResponse;
import ee.kaarel.homebudgetappv2.model.Category;
import ee.kaarel.homebudgetappv2.model.CategoryGroup;
import ee.kaarel.homebudgetappv2.model.SubCategory;
import ee.kaarel.homebudgetappv2.repository.CategoryRepository;
import ee.kaarel.homebudgetappv2.repository.SubCategoryRepository;
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
    private final SubCategoryRepository subCategoryRepository;
    private final UserAccessService userAccessService;
    private final LocalizationService localizationService;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll(CategoryGroup group) {
        var familyId = userAccessService.getCurrentUser().getFamilyId();
        List<Category> categories = group == null
                ? categoryRepository.findByOwnerFamilyIdOrderByNameAsc(familyId)
                : categoryRepository.findByOwnerFamilyIdAndGroupOrderByNameAsc(familyId, group);
        return categories.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return toResponse(getAccessibleCategory(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name().trim());
        category.setGroup(request.group());
        category.setOwnerFamilyId(userAccessService.getCurrentUser().getFamilyId());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getAccessibleCategory(id);
        category.setName(request.name().trim());
        category.setGroup(request.group());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(getAccessibleCategory(id));
    }

    @Transactional
    public SubCategoryResponse createSubCategory(SubCategoryRequest request) {
        Category category = getAccessibleCategory(request.parentCategoryId());
        SubCategory subCategory = new SubCategory();
        subCategory.setName(request.name().trim());
        subCategory.setParentCategory(category);
        return toSubCategoryResponse(subCategoryRepository.save(subCategory));
    }

    @Transactional
    public SubCategoryResponse updateSubCategory(Long id, SubCategoryRequest request) {
        SubCategory subCategory = getAccessibleSubCategory(id);
        Category category = getAccessibleCategory(request.parentCategoryId());
        subCategory.setName(request.name().trim());
        subCategory.setParentCategory(category);
        return toSubCategoryResponse(subCategoryRepository.save(subCategory));
    }

    @Transactional
    public void deleteSubCategory(Long id) {
        subCategoryRepository.delete(getAccessibleSubCategory(id));
    }

    @Transactional(readOnly = true)
    public SubCategory getAccessibleSubCategory(Long id) {
        return subCategoryRepository.findByIdAndParentCategoryOwnerFamilyId(id, userAccessService.getCurrentUser().getFamilyId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.subcategory.notFound")));
    }

    private Category getAccessibleCategory(Long id) {
        return categoryRepository.findByIdAndOwnerFamilyId(id, userAccessService.getCurrentUser().getFamilyId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, localizationService.getMessage("error.category.notFound")));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getGroup(),
                category.getOwnerFamilyId(),
                category.getSubCategories().stream().map(this::toSubCategoryResponse).toList()
        );
    }

    private SubCategoryResponse toSubCategoryResponse(SubCategory subCategory) {
        return new SubCategoryResponse(
                subCategory.getId(),
                subCategory.getName(),
                subCategory.getParentCategory().getId(),
                subCategory.getParentCategory().getName()
        );
    }
}
