package ee.kaarel.homebudgetappv2.controller;

import ee.kaarel.homebudgetappv2.dto.CategoryDTO;
import ee.kaarel.homebudgetappv2.model.CategoryType;
import ee.kaarel.homebudgetappv2.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/categories", "/api/categories"})
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAll(@RequestParam(required = false) CategoryType type) {
        if (type == null) {
            return ResponseEntity.ok(categoryService.getAll());
        }
        return ResponseEntity.ok(categoryService.getAllByType(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> create(@Valid @RequestBody CategoryDTO request,
                                              @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(categoryService.create(request, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
