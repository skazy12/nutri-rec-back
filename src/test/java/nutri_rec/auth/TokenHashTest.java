package nutri_rec.auth;

import nutri_rec.auth.application.TokenHash;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenHashTest {

    @Test
    void test_sha256_deterministic_and_hex() {
        System.out.println("[UT-BE-03] TokenHash.sha256 estable y hexadecimal");

        String raw = "refresh-token-demo";
        String h1 = TokenHash.sha256(raw);
        String h2 = TokenHash.sha256(raw);

        System.out.println("  Entrada: raw=" + raw);
        System.out.println("  Obtenido:");
        System.out.println("    hash1=" + h1);
        System.out.println("    hash2=" + h2);
        System.out.println("  Esperado:");
        System.out.println("    hash1 == hash2");
        System.out.println("    len(hash)=64");

        assertEquals(h1, h2);
        assertEquals(64, h1.length());
        assertTrue(h1.matches("^[0-9a-f]{64}$"));
    }

    @Test
    void test_sha256_diff_inputs_diff_hash() {
        System.out.println("[UT-BE-04] TokenHash.sha256 cambia con entrada (Opción B)");

        String h1 = TokenHash.sha256("A");
        String h2 = TokenHash.sha256("B");

        System.out.println("  Entradas: A vs B");
        System.out.println("  Obtenido: hashA=" + h1 + " | hashB=" + h2);
        System.out.println("  Esperado: hashA != hashB");

        assertNotEquals(h1, h2);
    }
}
