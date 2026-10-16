import java.io.BufferedReader;
import java.io.InputStreamReader;

/** Optional integration check; it never initializes the application or saves configuration. */
public class JProfilerIntegration {
    public static void main(String[] args) throws Exception {
        String key = new BufferedReader(new InputStreamReader(System.in)).readLine();
        Class<?> validator = Class.forName("com.jprofiler.b.p");
        Object instance = validator.getConstructor().newInstance();
        int status = (Integer) validator.getMethod("d", String.class).invoke(instance, key);
        System.out.println("shipped_validator_status=" + status);
        if (status < 0) throw new AssertionError("Generated key rejected: " + status);
    }
}
