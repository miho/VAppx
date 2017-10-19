/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vappx.vappxtool;

import eu.mihosoft.vappx.util.AppxImpl;
import java.io.File;
import java.io.PrintStream;

/**
 * Executes native appx.
 * 
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public interface VAppx {

    /**
     * Destroys the currently running appx process.
     */
    void destroy();

    /**
     * Returns the process of the current appx execution.
     * @return the process of the current appx execution
     */
    Process getProcess();

    /**
     * Returns the working directory
     * @return the working directory
     */
    File getWorkingDirectory();

    /**
     * Prints the appx output to the specified print streams.
     * @param out standard output stream
     * @param err error output stream
     * @return this shell
     */
    VAppx print(PrintStream out, PrintStream err);

    /**
     * Prints the appx output to the standard output.
     * @return this shell
     */
    VAppx print();

    /**
     * Waits until the appx process terminates.
     * @return this shell
     */
    VAppx waitFor();

    
    /**
     * Executes appx with the specified arguments.
     *
     * @param arguments arguments
     * @param wd working directory
     * @return appx shell
     */
    public static VAppx execute(File wd, String... arguments) {
        return AppxImpl.execute(false, wd, arguments);
    }

    /**
     * Creates an appx package.
     *
     * @param packageFolder the content of this folder and all sub-folders are included in the package
     * @param outputFile the output file name, e.g., {@code MyApp.appx}
     * @return appx shell
     */
    public static VAppx createPackage(File packageFolder, File outputFile) {
        return execute(packageFolder.getAbsoluteFile().getParentFile(),
                "-o", outputFile.getAbsolutePath(),
                "-9",
                packageFolder.getAbsolutePath());
    }

    /**
     * Creates a signed appx package.
     *
     * @param packageFolder the content of this folder and all sub-folders are included in the package
     * @param outputFile the output file name, e.g., {@code MyApp.appx}
     * @param certificate .pfx file (private key file)
     * @return appx shell
     */
    public static VAppx createPackage(File packageFolder, File outputFile, File certificate) {
        return execute(packageFolder.getAbsoluteFile().getParentFile(),
                "-o", outputFile.getAbsolutePath(),
                packageFolder.getAbsolutePath(),
                "-9",
                "-c", certificate.getAbsolutePath()
        );
    }
}
