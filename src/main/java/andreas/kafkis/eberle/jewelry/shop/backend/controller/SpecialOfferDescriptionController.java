package andreas.kafkis.eberle.jewelry.shop.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SpecialOfferDescription;
import andreas.kafkis.eberle.jewelry.shop.backend.service.SpecialOfferDescriptionService;

@RestController
@RequestMapping("/api/admin/special-offer-descriptions")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class SpecialOfferDescriptionController {

    @Autowired
    private SpecialOfferDescriptionService specialOfferDescriptionService;

    @GetMapping
    public ResponseEntity<List<SpecialOfferDescription>> getAllSpecialOfferDescriptions() {
        try {
            List<SpecialOfferDescription> descriptions = specialOfferDescriptionService.getAllSpecialOfferDescriptions();
            return ResponseEntity.ok(descriptions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpecialOfferDescription> getSpecialOfferDescriptionById(@PathVariable UUID id) {
        try {
            return specialOfferDescriptionService.getSpecialOfferDescriptionById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<SpecialOfferDescription>> searchSpecialOfferDescriptions(@RequestParam(required = false) String query) {
        try {
            List<SpecialOfferDescription> descriptions = specialOfferDescriptionService.searchSpecialOfferDescriptions(query);
            return ResponseEntity.ok(descriptions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<SpecialOfferDescription> createSpecialOfferDescription(@RequestBody CreateSpecialOfferDescriptionRequest request) {
        try {
            SpecialOfferDescription description = specialOfferDescriptionService.createSpecialOfferDescription(
                    request.getName(), 
                    request.getDescription()
            );
            return ResponseEntity.ok(description);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpecialOfferDescription> updateSpecialOfferDescription(
            @PathVariable UUID id, 
            @RequestBody UpdateSpecialOfferDescriptionRequest request) {
        try {
            SpecialOfferDescription description = specialOfferDescriptionService.updateSpecialOfferDescription(
                    id, 
                    request.getName(), 
                    request.getDescription()
            );
            return ResponseEntity.ok(description);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpecialOfferDescription(@PathVariable UUID id) {
        try {
            specialOfferDescriptionService.deleteSpecialOfferDescription(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Request DTOs
    public static class CreateSpecialOfferDescriptionRequest {
        private String name;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class UpdateSpecialOfferDescriptionRequest {
        private String name;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}



