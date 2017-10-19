package eu.mihosoft.vappx.util;

import eu.mihosoft.vappx.vappxshell.VAppxShell;
import eu.mihosoft.vappx.vappxdist.AppxDist;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppxImpl implements VAppxShell {

    private static File executableFile;
    private static File appxRootPath;
    private final Process appxProcess;
    private static boolean initialized;

    static {
        // static init
    }
    private final File wd;

    private AppxImpl(Process proc, File wd) {
        this.appxProcess = proc;
        this.wd = wd;
    }

    /**
     * Initializes property folder and executable.
     */
    private static void initialize() {

        // already initialized: we don't do anything
        if (initialized) {
            return;
        }

        try {
            Path confDir
                    = Paths.get(System.getProperty("user.home"), ".vappx").
                            toAbsolutePath();
            Path distDir = Paths.get(confDir.toString(), "appx-dist");
            File base = confDir.toFile();

            if (!Files.exists(confDir)) {
                Files.createDirectory(confDir);
            }

            if (!Files.exists(distDir)) {
                Files.createDirectory(distDir);
            }

            ConfigurationFile confFile
                    = IOUtil.newConfigurationFile(new File(base, "config.xml"));
            confFile.load();
            String timestamp = confFile.getProperty("timestamp");
            File ugFolder = new File(distDir.toFile(), "ug");

            String timestampFromDist;

            try {
                Class<?> buildInfoCls = Class.forName("eu.mihosoft.vappx.vappxdist.BuildInfo");
                Field timestampFromDistField = buildInfoCls.getDeclaredField("TIMESTAMP");
                timestampFromDistField.setAccessible(true);
                timestampFromDist = (String) timestampFromDistField.get(buildInfoCls);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(AppxImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(
                        "Vappx distribution for \"" + VSysUtil.getPlatformInfo()
                        + "\" not available on the classpath!", ex);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(AppxImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(
                        "Vappx distribution for \"" + VSysUtil.getPlatformInfo()
                        + "\" does not contain valid build info!", ex);
            }

            // if no previous timestamp exists or if no appx folder exists
            if (timestamp == null || !ugFolder.exists()) {
                System.out.println(
                        " -> installing appx to \"" + distDir + "\"");
                AppxDist.extractTo(distDir.toFile());
                confFile.setProperty("timestamp", timestampFromDist);
                confFile.save();
            } else // we need to update the appx distribution
            if (!Objects.equals(timestamp, timestampFromDist)) {
                System.out.println(
                        " -> updating appx in \"" + distDir + "\"");
                System.out.println(" --> current version: " + timestamp);
                System.out.println(" --> new     version: " + timestampFromDist);
                AppxDist.extractTo(distDir.toFile());
                confFile.setProperty("timestamp", timestampFromDist);
                confFile.save();
            } else {
                System.out.println(
                        " -> appx up to date in \"" + distDir + "\""
                );
            }

            executableFile = getExecutablePath(distDir);

        } catch (IOException ex) {
            Logger.getLogger(AppxImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        initialized = true;
    }

    @Override
    public AppxImpl print(PrintStream out, PrintStream err) {
        new StreamGobbler(err, appxProcess.getErrorStream(), "").start();
        new StreamGobbler(out, appxProcess.getInputStream(), "").start();

        return this;
    }

    @Override
    public AppxImpl print() {
        new StreamGobbler(System.err, appxProcess.getErrorStream(), "")
                .start();
        new StreamGobbler(System.out, appxProcess.getInputStream(), "")
                .start();

        return this;
    }

    @Override
    public AppxImpl waitFor() {
        try {
            appxProcess.waitFor();
        } catch (InterruptedException ex) {
            Logger.getLogger(AppxImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Cannot wait until process is finished", ex);
        }

        return this;
    }


    @Override
    public File getWorkingDirectory() {
        return wd;
    }

    /**
     * Calls appx with the specified arguments.
     *
     * @param arguments arguments
     * @param wd working directory
     * @param waitFor indicates whether to wait for process execution
     * @return appx process
     */
    public static Process execute(boolean waitFor, File wd, String... arguments) {

        initialize();

        if (arguments == null || arguments.length == 0) {
            arguments = new String[]{"-h"};
        }

        String[] cmd = new String[arguments.length + 1];

        cmd[0] = executableFile.getAbsolutePath();

        for (int i = 1; i < cmd.length; i++) {
            cmd[i] = arguments[i - 1];
        }

        Process proc = null;

        try {
            proc = Runtime.getRuntime().exec(cmd, null, wd);
            if (waitFor) {
                proc.waitFor();
            }
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException("Error while executing appx", ex);
        }

        return proc;
    }

    @Override
    public Process getProcess() {
        return appxProcess;
    }

    /**
     * Destroys the currently running appx process.
     */
    @Override
    public void destroy() {
        if (appxProcess != null) {
            appxProcess.destroy();
        }
    }

    /**
     * Returns the path to the appx executable. If the executable has not
     * been initialized this will be done as well.
     *
     * @return the path to the appx executable
     */
    private static File getExecutablePath(Path dir) {

        if (!VSysUtil.isOsSupported()) {
            throw new UnsupportedOperationException(
                    "The current OS is not supported: "
                    + System.getProperty("os.name"));
        }

        if (executableFile == null || !executableFile.isFile()) {

            appxRootPath = new File(dir.toFile(), "appx/bin");

            String executableName = "appx";

            if (VSysUtil.isWindows()) {
                executableName += ".exe";
            }

            executableFile = new File(appxRootPath, executableName);

            if (!VSysUtil.isWindows()) {
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{
                        "chmod", "u+x",
                        executableFile.getAbsolutePath()
                    });

                    InputStream stderr = p.getErrorStream();

                    BufferedReader reader
                            = new BufferedReader(
                                    new InputStreamReader(stderr));

                    String line;

                    while ((line = reader.readLine()) != null) {
                        System.out.println("Error: " + line);
                    }

                    p.waitFor();
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(AppxImpl.class.getName()).
                            log(Level.SEVERE, null, ex);
                }
            }
        }

        return executableFile;
    }

    /**
     * Unzips specified source archive to the specified destination folder. If
     * the destination directory does not exist it will be created.
     *
     * @param archive archive to unzip
     * @param destDir destination directory
     * @throws IOException
     */
    public static void unzip(File archive, File destDir) throws IOException {
        IOUtil.unzip(archive, destDir);
    }

    /**
     * Saves the specified stream to file.
     *
     * @param in stream to save
     * @param f destination file
     * @throws IOException
     */
    public static void saveStreamToFile(InputStream in, File f) throws IOException {
        IOUtil.saveStreamToFile(in, f);
    }
}
// based on http://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki

class StreamGobbler extends Thread {

    private final InputStream is;
    private final String prefix;
    private final PrintStream pw;

    StreamGobbler(PrintStream pw, InputStream is, String prefix) {
        this.is = is;
        this.prefix = prefix;
        this.pw = pw;
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                pw.println(prefix + line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }
}
