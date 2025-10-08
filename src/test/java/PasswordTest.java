import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    
    @Test
    public void generatePassword123Hash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate hash for password123
        String password123Hash = encoder.encode("password123");
        System.out.println("password123 hash: " + password123Hash);
        System.out.println("password123 matches: " + encoder.matches("password123", password123Hash));
    }
}


