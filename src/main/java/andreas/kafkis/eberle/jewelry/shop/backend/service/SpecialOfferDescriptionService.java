package andreas.kafkis.eberle.jewelry.shop.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import andreas.kafkis.eberle.jewelry.shop.backend.entities.SpecialOfferDescription;
import andreas.kafkis.eberle.jewelry.shop.backend.repository.SpecialOfferDescriptionRepository;

@Service
@Transactional
public class SpecialOfferDescriptionService {

    @Autowired
    private SpecialOfferDescriptionRepository specialOfferDescriptionRepository;

    public List<SpecialOfferDescription> getAllSpecialOfferDescriptions() {
        return specialOfferDescriptionRepository.findAll();
    }

    public Optional<SpecialOfferDescription> getSpecialOfferDescriptionById(UUID id) {
        return specialOfferDescriptionRepository.findById(id);
    }

    public Optional<SpecialOfferDescription> getSpecialOfferDescriptionByName(String name) {
        return specialOfferDescriptionRepository.findByName(name);
    }

    public List<SpecialOfferDescription> searchSpecialOfferDescriptions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllSpecialOfferDescriptions();
        }
        return specialOfferDescriptionRepository.findByNameOrDescriptionContainingIgnoreCase(query.trim());
    }

    public SpecialOfferDescription createSpecialOfferDescription(String name, String description) {
        // Check if already exists
        if (specialOfferDescriptionRepository.existsByName(name)) {
            throw new IllegalArgumentException("Special offer description with name '" + name + "' already exists");
        }

        String slug = generateSlug(name);
        
        // Ensure slug is unique
        int counter = 1;
        String originalSlug = slug;
        while (specialOfferDescriptionRepository.existsBySlug(slug)) {
            slug = originalSlug + "-" + counter;
            counter++;
        }

        SpecialOfferDescription specialOfferDescription = SpecialOfferDescription.builder()
                .name(name.trim())
                .slug(slug)
                .description(description != null ? description.trim() : null)
                .build();

        return specialOfferDescriptionRepository.save(specialOfferDescription);
    }

    public SpecialOfferDescription updateSpecialOfferDescription(UUID id, String name, String description) {
        SpecialOfferDescription existing = specialOfferDescriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Special offer description not found with id: " + id));

        // Check if name is being changed and if new name already exists
        if (!existing.getName().equals(name.trim()) && specialOfferDescriptionRepository.existsByName(name.trim())) {
            throw new IllegalArgumentException("Special offer description with name '" + name + "' already exists");
        }

        existing.setName(name.trim());
        existing.setDescription(description != null ? description.trim() : null);
        
        // Update slug if name changed
        if (!existing.getName().equals(name.trim())) {
            String newSlug = generateSlug(name);
            
            // Ensure slug is unique
            int counter = 1;
            String originalSlug = newSlug;
            while (specialOfferDescriptionRepository.existsBySlug(newSlug) && !existing.getSlug().equals(newSlug)) {
                newSlug = originalSlug + "-" + counter;
                counter++;
            }
            
            existing.setSlug(newSlug);
        }

        return specialOfferDescriptionRepository.save(existing);
    }

    public void deleteSpecialOfferDescription(UUID id) {
        if (!specialOfferDescriptionRepository.existsById(id)) {
            throw new IllegalArgumentException("Special offer description not found with id: " + id);
        }
        specialOfferDescriptionRepository.deleteById(id);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
