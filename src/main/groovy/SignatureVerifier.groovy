import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class SignatureVerifier {
    static boolean verifySignature(String payload, String signature, String secret) {
        if (!secret || !signature) return true
        def hmac = Mac.getInstance('HmacSHA256')
        def keySpec = new SecretKeySpec(secret.bytes, 'HmacSHA256')
        hmac.init(keySpec)
        def computed = 'sha256=' + hmac.doFinal(payload.bytes).encodeHex().toString()
        return computed == signature
    }
}
