package ee.kaarel.homebudgetappv2.controller;

import ee.kaarel.homebudgetappv2.dto.CategoryRequest;
import ee.kaarel.homebudgetappv2.dto.CategoryResponse;
import ee.kaarel.homebudgetappv2.dto.SubCategoryRequest;
import ee.kaarel.homebudgetappv2.dto.SubCategoryResponse;
import ee.kaarel.homebudgetappv2.model.CategoryGroup;
import ee.kaarel.homebudgetappv2.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(@RequestParam(required = false) CategoryGroup group) {
        return ResponseEntity.ok(categoryService.getAll(group));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/subcategories")
    public ResponseEntity<SubCategoryResponse> createSubCategory(@Valid @RequestBody SubCategoryRequest request) {
        return ResponseEntity.ok(categoryService.createSubCategory(request));
    }

    @PutMapping("/subcategories/{id}")
    public ResponseEntity<SubCategoryResponse> updateSubCategory(@PathVariable Long id, @Valid @RequestBody SubCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateSubCategory(id, request));
    }

    @DeleteMapping("/subcategories/{id}")
    public ResponseEntity<Void> deleteSubCategory(@PathVariable Long id) {
        categoryService.deleteSubCategory(id);
        return ResponseEntity.noContent().build();
    }
}
