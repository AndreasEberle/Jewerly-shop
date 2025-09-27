package andreas.kafkis.eberle.jewelry.shop.backend.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private boolean active;
}