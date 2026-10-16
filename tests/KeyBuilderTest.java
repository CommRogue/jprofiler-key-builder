import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class KeyBuilderTest {
    static final String FIXTURE = "E-J16-ERWEW#2053215-2026.10.27-10-2xr2qg52tpksax#2863b";
    static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    static void invalid(Runnable action) {
        try { action.run(); throw new AssertionError("Expected input rejection"); }
        catch (IllegalArgumentException expected) { }
    }
    public static void main(String[] args) throws Exception {
        require(FIXTURE.equals(KeyBuilder.key(16, "ERWEW", "2053215", LocalDate.of(2026, 10, 27), 10)),
            "Does not reproduce the supplied vendor key");
        invalid(() -> KeyBuilder.parseDate("2027-02-29"));
        require(KeyBuilder.parseDate("2028-02-29").getDayOfMonth() == 29, "Leap-year date failed");
        invalid(() -> KeyBuilder.parseDate("2026-1-01"));
        invalid(() -> KeyBuilder.key(15, "ERWEW", "2053215", LocalDate.now(), 10));
        invalid(() -> KeyBuilder.key(16, "BAD-ISSUER", "2053215", LocalDate.now(), 10));
        invalid(() -> KeyBuilder.key(16, "ERWEW", "1#2", LocalDate.now(), 10));
        invalid(() -> KeyBuilder.key(16, "ERWEW", "2053215", LocalDate.now(), 0));
        System.out.println("Known vendor fixture and input validation passed.");
        if (args.length > 0 && args[0].equals("--gui")) {
            KeyBuilder[] app = new KeyBuilder[1];
            SwingUtilities.invokeAndWait(() -> {
                app[0] = new KeyBuilder();
                app[0].frame.setVisible(true);
                require(app[0].expiration.getValue() == 12, "Default duration is not one year");
                require(app[0].selectedExpiry().equals(LocalDate.now().plusYears(1)), "Default expiry is not one year away");
                require(app[0].duration.getText().equals("1 year"), "Default duration label is incorrect");
                require(!app[0].copy.isEnabled() && !app[0].save.isEnabled(), "Empty output actions enabled");
                for (int months : new int[]{12, 1, 60, 13}) {
                    app[0].expiration.setValue(months);
                    require(app[0].output.getText().isEmpty(), "Slider change left a stale key");
                    require(!app[0].copy.isEnabled() && !app[0].save.isEnabled(), "Slider change left actions enabled");
                    LocalDate today = LocalDate.now();
                    LocalDate expiry = today.plusMonths(months);
                    require(app[0].selectedExpiry().equals(expiry), "Slider selected the wrong expiry");
                    app[0].generate.doClick();
                    require(KeyBuilder.key(16, "ERWEW", "2053215", expiry,
                        (int) ChronoUnit.DAYS.between(today, expiry)).equals(app[0].output.getText()),
                        "GUI key does not match the selected duration");
                    require(app[0].copy.isEnabled() && app[0].save.isEnabled(), "Output actions disabled");
                }
                require(app[0].duration.getText().equals("1 year, 1 month"), "Mixed duration label is incorrect");
                app[0].expiration.setValue(12);
                app[0].generate.doClick();
                app[0].copy.doClick();
                require(app[0].status.getText().equals("Key copied to clipboard."), "Copy action failed");
            });
            BufferedImage[] preview = new BufferedImage[1];
            SwingUtilities.invokeAndWait(() -> {
                preview[0] = new BufferedImage(app[0].frame.getWidth(), app[0].frame.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = preview[0].createGraphics();
                app[0].frame.printAll(graphics);
                graphics.dispose();
                app[0].frame.dispose();
            });
            ImageIO.write(preview[0], "png", Path.of("preview.png").toFile());
            System.out.println("GUI default year, slider bounds, expiry generation, stale-output clearing, clipboard, and preview rendering passed.");
        }
    }
}
