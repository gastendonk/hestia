package hestia.otc.model;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import hestia.persist.Identifiable;

public class Secret implements Identifiable {
    private String name;
    /** At this moment it is always the Database password. */
    private String secret;

    @Override
    public String getId() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public static String encode(String plainText) {
        if (plainText == null) return null;
        return Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String base64Text) {
        if (base64Text == null) return null;
        byte[] decodedBytes = Base64.getDecoder().decode(base64Text);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
