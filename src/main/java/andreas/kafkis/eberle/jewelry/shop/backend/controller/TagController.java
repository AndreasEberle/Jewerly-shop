package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.Tag;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.TagRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag Management", description = "Operations for managing tags")
@Slf4j
public class TagController {

    private final TagRepository tagRepository;

    @GetMapping
    @Operation(summary = "Get all tags")
    public ResponseEntity<List<Tag>> getAllTags() {
        try {
            List<Tag> tags = tagRepository.findAll();
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            log.error("Error getting tags: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search tags by name")
    public ResponseEntity<List<Tag>> searchTags(@RequestParam String query) {
        try {
            List<Tag> tags = tagRepository.findByNameContainingIgnoreCase(query);
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            log.error("Error searching tags: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create a new tag")
    public ResponseEntity<Tag> createTag(@RequestBody CreateTagRequest request) {
        try {
            Tag tag = Tag.builder()
                    .name(request.getName())
                    .slug(request.getName().toLowerCase().replaceAll("\\s+", "-"))
                    .build();
            
            Tag savedTag = tagRepository.save(tag);
            return ResponseEntity.ok(savedTag);
        } catch (Exception e) {
            log.error("Error creating tag: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    public static class CreateTagRequest {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
