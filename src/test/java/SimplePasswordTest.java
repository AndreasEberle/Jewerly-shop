import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SimplePasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Test current hash from database
        String currentAdminHash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        System.out.println("Current admin hash matches 'password': " + encoder.matches("password", currentAdminHash));
        System.out.println("Current admin hash matches 'admin123': " + encoder.matches("admin123", currentAdminHash));
        
        // Generate new hash for admin123
        String admin123Hash = encoder.encode("admin123");
        System.out.println("New admin123 hash: " + admin123Hash);
        System.out.println("New admin123 hash matches 'admin123': " + encoder.matches("admin123", admin123Hash));
        
        // Test current test user hash
        String currentTestHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi";
        System.out.println("Current test hash matches 'password123': " + encoder.matches("password123", currentTestHash));
        System.out.println("Current test hash matches 'password': " + encoder.matches("password", currentTestHash));
    }
}
