import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate hashed passwords for different test passwords
        String[] passwords = {
            "password123",
            "admin123", 
            "customer123",
            "test123"
        };
        
        System.out.println("Generated BCrypt hashes:");
        System.out.println("========================");
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("Password: " + password);
            System.out.println("Hash: " + hash);
            System.out.println("---");
        }
    }
}
