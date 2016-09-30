package com.automic.puppet.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This class writes content to standard console
 * 
 * @author shrutinambiar
 *
 */
public final class ConsoleWriter {
    private static final PrintWriter WRITER = System.console().writer();

    /**
     * Method to write string to console
     *
     * @param content
     */
    public static void write(String content) {
        String temp = content != null ? content : "null";
        WRITER.write(temp);
    }

    /**
     * Method to write a newline to console
     * 
     */
    public static void newLine() {
        WRITER.write(System.lineSeparator());
    }

    /**
     * Method to write an Object to console and followed by newline.
     *
     * @param content
     */
    public static void writeln(Object content) {
        String temp = content != null ? content.toString() : "null";
        write(temp);
        newLine();
    }

    /**
     * Method to to log the trace.
     * 
     * @param content
     */
    public static void writeln(Throwable content) {
        StringWriter sw = new StringWriter(4 * 1024);
        PrintWriter pw = new PrintWriter(sw);
        content.printStackTrace(pw);
        pw.flush();
        write(sw.toString());
        newLine();
    }

    /**
     * Method to flush to console
     * 
     */
    public static void flush() {
        WRITER.flush();
    }
}
