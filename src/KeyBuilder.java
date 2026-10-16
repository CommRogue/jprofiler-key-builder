import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class KeyBuilder {
    static final Color BACKGROUND = new Color(20, 27, 37);
    static final Color FIELD = new Color(32, 42, 55);
    static final Color TEXT = new Color(231, 238, 245);
    static final Color MUTED = new Color(153, 173, 192);
    static final Color ACCENT = new Color(82, 214, 179);
    static final DateTimeFormatter KEY_DATE = DateTimeFormatter.ofPattern("uuuu.MM.dd");
    final JFrame frame = new JFrame("JFucker");
    final JSlider expiration = new JSlider(1, 60, 12);
    final JLabel duration = label("", 22, TEXT);
    final JLabel expiryDate = label("", 13, MUTED);
    final JTextArea output = new JTextArea(3, 42);
    final JLabel status = new JLabel("Ready to generate a key.");
    final JButton generate = new JButton("Generate key");
    final JButton copy = new JButton("Copy key");
    final JButton save = new JButton("Save to file");

    static String encode(long value) {
        return Long.toString(value, 32).replace('0', 'w').replace('1', 'y')
            .replace('l', 'z').replace('o', 'x');
    }

    static String crcToken(String prefix, String salt) {
        CRC32 crc = new CRC32();
        crc.update((prefix + salt).getBytes(StandardCharsets.US_ASCII));
        return encode(crc.getValue());
    }

    static String key(int version, String issuer, String id, LocalDate expiry, int days) {
        if (version != 16 && version != 17)
            throw new IllegalArgumentException("Version must be 16 or 17.");
        if (!issuer.matches("[A-Z0-9]{1,20}"))
            throw new IllegalArgumentException("Issuer must contain 1–20 uppercase ASCII letters or digits.");
        if (!id.matches("[0-9]{1,20}"))
            throw new IllegalArgumentException("Record ID must contain 1–20 digits.");
        if (days < 1 || days > 36500)
            throw new IllegalArgumentException("Days field must be between 1 and 36500.");
        if (expiry.getYear() < 1970 || expiry.getYear() > 9999)
            throw new IllegalArgumentException("Expiration year must be between 1970 and 9999.");
        String prefix = "E-J" + version + "-" + issuer + "#" + id + "-"
            + KEY_DATE.format(expiry) + "-" + days;
        String body = crcToken(prefix, "ZwClose") + crcToken(prefix, "LdrLoadDll");
        String unsigned = prefix + "-" + body;
        int sum = unsigned.chars().sum();
        String checksum = ("" + sum % 73 + sum % 29 + sum % 64)
            .replace('0', 'a').replace('1', 'b');
        return unsigned + "#" + checksum;
    }

    static LocalDate parseDate(String value) {
        try {
            if (!value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))
                throw new DateTimeParseException("format", value, 0);
            return LocalDate.parse(value);
        } catch (DateTimeParseException error) {
            throw new IllegalArgumentException("Enter a real expiration date in YYYY-MM-DD format.");
        }
    }

    static int number(String value, String name) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException error) {
            throw new IllegalArgumentException(name + " must be a whole number.");
        }
    }

    KeyBuilder() {
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(640, 510));
        JPanel content = new JPanel(new BorderLayout(0, 22));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(30, 32, 26, 32));
        frame.setContentPane(content);

        JPanel heading = vertical();
        JLabel eyebrow = label("EVALUATION LICENSE", 12, ACCENT);
        JLabel title = label("JFucker", 29, TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        heading.add(eyebrow);
        heading.add(Box.createVerticalStrut(8));
        heading.add(title);
        heading.add(Box.createVerticalStrut(10));
        heading.add(label("Choose how long your key lasts.", 14, MUTED));
        content.add(heading, BorderLayout.NORTH);

        JPanel center = vertical();
        JLabel expirationLabel = label("EXPIRES IN", 12, MUTED);
        expirationLabel.setLabelFor(expiration);
        center.add(expirationLabel);
        center.add(Box.createVerticalStrut(8));
        center.add(duration);
        center.add(Box.createVerticalStrut(6));
        center.add(expiryDate);
        center.add(Box.createVerticalStrut(12));
        expiration.setAlignmentX(Component.LEFT_ALIGNMENT);
        expiration.setBackground(BACKGROUND);
        expiration.setForeground(ACCENT);
        expiration.setMajorTickSpacing(12);
        expiration.setMinorTickSpacing(1);
        expiration.setSnapToTicks(true);
        expiration.setPaintLabels(true);
        Hashtable<Integer, JLabel> marks = new Hashtable<>();
        marks.put(1, label("1 month", 12, MUTED));
        marks.put(12, label("1 year", 12, MUTED));
        marks.put(60, label("5 years", 12, MUTED));
        expiration.setLabelTable(marks);
        expiration.getAccessibleContext().setAccessibleName("Time until expiration, in months");
        expiration.addChangeListener(event -> {
            updateExpiration();
            invalidate();
        });
        updateExpiration();
        center.add(expiration);
        center.add(Box.createVerticalStrut(18));
        generate.setAlignmentX(Component.LEFT_ALIGNMENT);
        generate.setFont(generate.getFont().deriveFont(Font.BOLD, 15f));
        generate.setBackground(ACCENT);
        generate.setForeground(BACKGROUND);
        generate.setBorder(new EmptyBorder(11, 20, 11, 20));
        generate.addActionListener(event -> generate());
        center.add(generate);
        center.add(Box.createVerticalStrut(22));
        center.add(label("GENERATED KEY", 12, MUTED));
        center.add(Box.createVerticalStrut(8));
        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        output.setBackground(FIELD);
        output.setForeground(TEXT);
        output.setCaretColor(TEXT);
        output.setBorder(new EmptyBorder(13, 14, 13, 14));
        JScrollPane scroll = new JScrollPane(output);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(57, 74, 92)));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(scroll);
        center.add(Box.createVerticalStrut(12));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setBackground(BACKGROUND);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        copy.setEnabled(false);
        save.setEnabled(false);
        copy.addActionListener(event -> {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(output.getText()), null);
                message("Key copied to clipboard.", ACCENT);
            } catch (IllegalStateException error) {
                message("Clipboard is busy. Select and copy the key directly.", new Color(255, 191, 116));
            }
        });
        save.addActionListener(event -> save());
        actions.add(copy);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(save);
        center.add(actions);
        content.add(center, BorderLayout.CENTER);
        status.setForeground(MUTED);
        status.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        content.add(status, BorderLayout.SOUTH);
        frame.getRootPane().setDefaultButton(generate);

        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    void invalidate() {
        output.setText("");
        copy.setEnabled(false);
        save.setEnabled(false);
        message("Expiration changed. Generate a new key.", MUTED);
    }

    LocalDate selectedExpiry() {
        return LocalDate.now().plusMonths(expiration.getValue());
    }

    void updateExpiration() {
        int months = expiration.getValue();
        int years = months / 12;
        int remainder = months % 12;
        String text = years == 0 ? "" : years + (years == 1 ? " year" : " years");
        if (remainder > 0)
            text += (text.isEmpty() ? "" : ", ") + remainder + (remainder == 1 ? " month" : " months");
        duration.setText(text);
        expiryDate.setText("Expires on " + selectedExpiry().format(DateTimeFormatter.ofPattern("d MMM uuuu")));
    }

    void generate() {
        LocalDate today = LocalDate.now();
        LocalDate expiry = today.plusMonths(expiration.getValue());
        updateExpiration();
        String result = key(16, "ERWEW", "2053215", expiry, (int) ChronoUnit.DAYS.between(today, expiry));
        output.setText(result);
        output.setCaretPosition(0);
        copy.setEnabled(true);
        save.setEnabled(true);
        message("Key generated. Ready to copy or save.", ACCENT);
    }

    void save() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("JFucker-key.txt"));
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        java.io.File target = chooser.getSelectedFile();
        if (target.exists() && JOptionPane.showConfirmDialog(frame,
            "Replace " + target.getName() + "?", "Replace file", JOptionPane.YES_NO_OPTION)
            != JOptionPane.YES_OPTION) return;
        try {
            Files.writeString(target.toPath(), output.getText() + System.lineSeparator(), StandardCharsets.UTF_8);
            message("Saved to " + target.getName(), ACCENT);
        } catch (java.io.IOException error) {
            message("Could not save: " + error.getMessage(), new Color(255, 154, 151));
        }
    }

    void message(String text, Color color) { status.setText(text); status.setForeground(color); }

    static JPanel vertical() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static void usage() {
        System.out.println("JFucker 1.0\n"
            + "No arguments: open desktop app.\n"
            + "--generate [--expires YYYY-MM-DD] [--version 16|17]\n"
            + "           [--issuer ERWEW] [--id 2053215] [--days 10]\n"
            + "--help: show this help.\n"
            + "Default expiration: today + 11 days; generated key is written to stdout.");
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println("A desktop display is required. Use --generate for command-line mode.");
                System.exit(2);
            }
            SwingUtilities.invokeLater(() -> new KeyBuilder().frame.setVisible(true));
            return;
        }
        try {
            Map<String, String> values = new HashMap<>();
            boolean cli = false;
            for (int i = 0; i < args.length; i++) {
                String option = args[i];
                if (option.equals("--help") || option.equals("-h")) { usage(); return; }
                if (option.equals("--generate")) { cli = true; continue; }
                if (!java.util.Set.of("--expires", "--version", "--issuer", "--id", "--days").contains(option))
                    throw new IllegalArgumentException("Unknown option: " + option);
                if (++i == args.length || args[i].startsWith("--"))
                    throw new IllegalArgumentException("Missing value for " + option);
                if (values.put(option, args[i]) != null)
                    throw new IllegalArgumentException("Duplicate option: " + option);
                cli = true;
            }
            if (!cli) throw new IllegalArgumentException("Use --generate or --help.");
            System.out.println(key(number(values.getOrDefault("--version", "16"), "Version"),
                values.getOrDefault("--issuer", "ERWEW"), values.getOrDefault("--id", "2053215"),
                parseDate(values.getOrDefault("--expires", LocalDate.now().plusDays(11).toString())),
                number(values.getOrDefault("--days", "10"), "Days field")));
        } catch (IllegalArgumentException error) {
            System.err.println(error.getMessage());
            System.exit(2);
        }
    }
}
