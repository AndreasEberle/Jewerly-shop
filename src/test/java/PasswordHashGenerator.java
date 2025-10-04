import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate hash for admin123
        String adminHash = encoder.encode("admin123");
        System.out.println("admin123 hash: " + adminHash);
        
        // Generate hash for password123
        String password123Hash = encoder.encode("password123");
        System.out.println("password123 hash: " + password123Hash);
        
        // Verify the hashes work
        System.out.println("admin123 matches: " + encoder.matches("admin123", adminHash));
        System.out.println("password123 matches: " + encoder.matches("password123", password123Hash));
    }
}
