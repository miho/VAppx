/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vappx.vappxshell;

import eu.mihosoft.vappx.util.AppxImpl;
import java.io.File;
import java.io.PrintStream;

/**
 * Executes native appx.
 * 
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public interface VAppxShell {

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
    VAppxShell print(PrintStream out, PrintStream err);

    /**
     * Prints the appx output to the standard output.
     * @return this shell
     */
    VAppxShell print();

    /**
     * Waits until the appx process terminates.
     * @return this shell
     */
    VAppxShell waitFor();

    
    /**
     * Executes appx with the specified arguments.
     *
     * @param arguments arguments
     * @param wd working directory
     * @return appx process
     */
    public static Process execute(File wd, String... arguments) {
        return AppxImpl.execute(false, wd, arguments);
    }
    
}
