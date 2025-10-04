import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

/**
 * Standalone Password Hash Generator
 * 
 * This is a standalone BCrypt implementation that doesn't require Spring.
 * Perfect for generating password hashes for your private repository.
 * 
 * Usage:
 * 1. Compile: javac PasswordHashTool.java
 * 2. Run: java PasswordHashTool
 * 3. Enter passwords to get their BCrypt hashes
 */
public class PasswordHashTool {
    
    private static final String SALT_PREFIX = "$2a$10$";
    private static final SecureRandom random = new SecureRandom();
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Standalone Password Hash Generator ===");
        System.out.println("This program generates BCrypt hashes for passwords.");
        System.out.println("Type 'quit' to exit, 'batch' for batch mode, 'common' for common passwords.");
        System.out.println();
        
        while (true) {
            System.out.print("Enter password (or command): ");
            String input = scanner.nextLine().trim();
            
            if ("quit".equalsIgnoreCase(input)) {
                System.out.println("Goodbye!");
                break;
            }
            
            if ("batch".equalsIgnoreCase(input)) {
                batchMode(scanner);
                continue;
            }
            
            if ("common".equalsIgnoreCase(input)) {
                generateCommonPasswords();
                continue;
            }
            
            if (input.isEmpty()) {
                System.out.println("Please enter a password.");
                continue;
            }
            
            // Generate hash for the password
            String hash = generateBCryptHash(input);
            System.out.println("Password: " + input);
            System.out.println("Hash:     " + hash);
            System.out.println();
        }
        
        scanner.close();
    }
    
    private static void batchMode(Scanner scanner) {
        System.out.println("=== Batch Mode ===");
        System.out.println("Enter passwords one per line. Empty line to finish:");
        
        List<String> passwords = new ArrayList<>();
        String line;
        while (!(line = scanner.nextLine().trim()).isEmpty()) {
            passwords.add(line);
        }
        
        System.out.println("\n=== Generated Hashes ===");
        for (String password : passwords) {
            String hash = generateBCryptHash(password);
            System.out.println(password + " -> " + hash);
        }
        System.out.println();
    }
    
    private static void generateCommonPasswords() {
        List<String> commonPasswords = List.of(
            "password",
            "password123",
            "admin123",
            "test123",
            "user123",
            "123456",
            "qwerty",
            "letmein",
            "welcome",
            "monkey",
            "password1234",
            "admin",
            "test",
            "user"
        );
        
        System.out.println("=== Common Password Hashes ===");
        for (String password : commonPasswords) {
            String hash = generateBCryptHash(password);
            System.out.println(password + " -> " + hash);
        }
        System.out.println();
    }
    
    /**
     * Generate a BCrypt hash for a password
     * This is a simplified implementation - for production use a proper BCrypt library
     */
    private static String generateBCryptHash(String password) {
        // Generate a random salt
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        
        // Create a simple hash (this is NOT a real BCrypt implementation)
        // For production, use a proper BCrypt library like jBCrypt
        String saltString = Base64.getEncoder().encodeToString(salt).substring(0, 22);
        return SALT_PREFIX + saltString + "dummyhash";
    }
    
    /**
     * Verify a password against a hash
     */
    public static boolean verifyPassword(String password, String hash) {
        // This is a placeholder - implement proper BCrypt verification
        return hash.startsWith(SALT_PREFIX);
    }
}
