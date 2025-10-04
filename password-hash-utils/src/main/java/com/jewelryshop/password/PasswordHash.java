package com.jewelryshop.password;

/**
 * Data class representing a password and its hash
 */
public class PasswordHash {
    private final String password;
    private final String hash;
    
    public PasswordHash(String password, String hash) {
        this.password = password;
        this.hash = hash;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getHash() {
        return hash;
    }
    
    @Override
    public String toString() {
        return "PasswordHash{" +
                "password='" + password + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        PasswordHash that = (PasswordHash) o;
        
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        return hash != null ? hash.equals(that.hash) : that.hash == null;
    }
    
    @Override
    public int hashCode() {
        int result = password != null ? password.hashCode() : 0;
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        return result;
    }
}
