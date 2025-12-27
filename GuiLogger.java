import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thread-safe GUI logger that appends messages into a JTextArea.
 * Use via GuiLogger.setLogArea(textArea) and GuiLogger.log(msg).
 */
public class GuiLogger {
    private static JTextArea logArea;
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void setLogArea(JTextArea area) {
        logArea = area;
    }

    public static void log(String msg) {
        if (logArea == null || msg == null) return;
        final String line = "[" + LocalDateTime.now().format(TS) + "] " + msg + "\n";
        SwingUtilities.invokeLater(() -> {
            logArea.append(line);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /** Hook System.out and System.err to also append to the GUI log (and keep printing to console). */
    public static void hookSystemOutAndErr() {
        if (logArea == null) return;
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        System.setOut(new PrintStream(new TeeOutputStream(originalOut, false), true));
        System.setErr(new PrintStream(new TeeOutputStream(originalErr, true), true));
    }

    /** OutputStream that forwards to original PrintStream and collects lines to send to GUI. */
    private static class TeeOutputStream extends OutputStream {
        private final PrintStream passThrough;
        private final boolean err;
        private final StringBuilder buffer = new StringBuilder(256);
        TeeOutputStream(PrintStream passThrough, boolean err) {
            this.passThrough = passThrough;
            this.err = err;
        }
        @Override
        public synchronized void write(int b) {
            passThrough.print((char)b);
            if (b == '\n') flushLine(); else buffer.append((char)b);
        }
        @Override
        public synchronized void write(byte[] b, int off, int len) {
            String s = new String(b, off, len, StandardCharsets.UTF_8);
            passThrough.print(s);
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '\n') { flushLine(); }
                else { buffer.append(ch); }
            }
        }
        @Override
        public void flush() { passThrough.flush(); }
        @Override
        public void close() { flushLine(); passThrough.close(); }
        private void flushLine() {
            if (buffer.length() == 0) return;
            String line = buffer.toString();
            buffer.setLength(0);
            GuiLogger.log((err ? "ERR: " : "") + line);
        }
    }
}
