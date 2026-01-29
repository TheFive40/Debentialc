package org.example.tools.fragments;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Generador de hashes únicos de 12 caracteres
 */
public class HashGenerator {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int HASH_LENGTH = 12;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Genera un hash único verificando contra los existentes
     */
    public static String generateUniqueHash(Set<String> existingHashes) {
        String hash;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            hash = generateHash();
            attempts++;

            if (attempts >= maxAttempts) {
                // Usar timestamp como parte del hash para garantizar unicidad
                long timestamp = System.currentTimeMillis();
                hash = generateHash() + Long.toString(timestamp, 36);
                hash = hash.substring(0, HASH_LENGTH);
                break;
            }
        } while (existingHashes.contains(hash));

        return hash;
    }

    /**
     * Genera un hash aleatorio de 12 caracteres
     */
    private static String generateHash() {
        StringBuilder hash = new StringBuilder(HASH_LENGTH);

        for (int i = 0; i < HASH_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            hash.append(CHARACTERS.charAt(index));
        }

        return hash.toString();
    }

    /**
     * Valida que un hash tenga el formato correcto
     */
    public static boolean isValidHash(String hash) {
        if (hash == null || hash.length() != HASH_LENGTH) {
            return false;
        }

        for (char c : hash.toCharArray()) {
            if (CHARACTERS.indexOf(c) == -1) {
                return false;
            }
        }

        return true;
    }
}