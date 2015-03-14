package apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import org.openide.util.RequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood
 */
public class SystemConsoleSupport implements Runnable {

    private static final RequestProcessor RP = new RequestProcessor(SystemConsoleSupport.class);
    boolean shouldStop = false;
    FileInputStream filestream = null;
    BufferedReader ins;
    private final File fileName;
    String ioName;
    int lines;
    Ring ring;
    private final RequestProcessor.Task task = RP.create(this);

    private final static Logger log = LoggerFactory.getLogger(SystemConsoleSupport.class);

    public SystemConsoleSupport(final File fileName) {
        this.fileName = fileName;
    }

    private void init() {
        final int LINES = 2000;
        final int OLD_LINES = 2000;
        this.ring = new Ring(OLD_LINES);
        String line;

        // Read the log file without displaying everything
        try {
            while ((line = this.ins.readLine()) != null) {
                this.ring.add(line);
            }
        } catch (IOException e) {
            log.error("Unable to read {}", this.fileName, e);
        }

        // Now show the last OLD_LINES
        this.lines = this.ring.output();
        this.ring.setMaxCount(LINES);
    }

    @Override
    public void run() {
        final int MAX_LINES = 10000;
        String line;

        shouldStop = !SystemConsole.getInstance().isVisible();

        if (!shouldStop) {
            try {
                if (lines >= MAX_LINES) {
                    lines = ring.output();
                }

                while ((line = ins.readLine()) != null) {
                    if ((line = ring.add(line)) != null) {
                        SystemConsole.getInstance().getOutputStream().println(line);
                        lines++;
                    }
                }

            } catch (IOException e) {
                log.error("Unable to read log {}", this.fileName, e);
            }
            task.schedule(10000);
        } else {
            stopUpdatingSystemConsole();
        }
    }

    public void showSystemConsole() throws IOException {
        shouldStop = false;
        filestream = new FileInputStream(fileName);
        SystemConsole.getConsole().setVisible(true);
        ins = new BufferedReader(new InputStreamReader(filestream));
        RP.post(() -> {
            init();
            task.schedule(0);
        });
    }

    public void stopUpdatingSystemConsole() {
        try {
            ins.close();
            filestream.close();
        } catch (IOException e) {
            log.error("Unable to close log {}", this.fileName, e);
        }
    }

    private class Ring {

        private int maxCount;
        private int count;
        private LinkedList<String> anchor;

        public Ring(int max) {
            maxCount = max;
            count = 0;
            anchor = new LinkedList<>();
        }

        public String add(String line) {
            if (line == null || line.isEmpty()) { // NOI18N
                return null;
            } // end of if (line == null || line.equals(""))

            while (count >= maxCount) {
                anchor.removeFirst();
                count--;
            } // end of while (count >= maxCount)

            anchor.addLast(line);
            count++;

            return line;
        }

        public void setMaxCount(int newMax) {
            maxCount = newMax;
        }

        public int output() {
            int i = 0;
            for (String s : anchor) {
                SystemConsole.getInstance().getOutputStream().println(s);
                i++;
            }

            return i;
        }

        public void reset() {
            anchor = new LinkedList<>();
        }
    }

}
