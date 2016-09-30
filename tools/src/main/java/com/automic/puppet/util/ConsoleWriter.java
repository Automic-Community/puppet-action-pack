package com.automic.puppet.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.automic.puppet.exception.AutomicException;

/**
 * This class writes content to standard console
 * 
 * @author shrutinambiar
 *
 */
public final class ConsoleWriter {
    private static final ByteWriter WRITER = new ByteWriter(System.out);

    /**
     * Method to write string to console
     *
     * @param content
     */
    public static void write(String content) {
        String temp = content != null ? content : "null";
        try {
            WRITER.write(temp);
        } catch (AutomicException e) {
            System.out.println(content);
        }
    }

    /**
     * Method to write a newline to console
     * 
     */
    public static void newLine() {
        write(System.lineSeparator());
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
        try {
            WRITER.flush();
        } catch (AutomicException e) {
            System.out.println(e.getMessage());
        }
    }
}
