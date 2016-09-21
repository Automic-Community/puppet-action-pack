package com.automic.puppet.util;

import com.automic.puppet.exception.AutomicException;

/**
 * This class writes content to standard console
 * 
 * @author shrutinambiar
 *
 */
public final class ConsoleWriter {
	private static final ByteWriter WRITER = new ByteWriter(System.out);

	private ConsoleWriter() {
	}

	/**
	 * Method to write object to console
	 *
	 * @param content
	 * @throws AutomicException
	 */
	public static void write(Object content) throws AutomicException {
		String temp = content != null ? content.toString() : "null";
		try {
			WRITER.write(temp);
		} catch (AutomicException ae) {
			ConsoleWriter.writeln(ae.getMessage());
		}
	}

	/**
	 * Method to write a newline to console
	 * 
	 * @throws AutomicException
	 */
	public static void newLine() throws AutomicException {
		write(System.lineSeparator());
	}

	/**
	 * Method to write an Object to console and followed by newline.
	 *
	 * @param content
	 * @throws AutomicException
	 */
	public static void writeln(Object content) throws AutomicException {
		write(content);
		newLine();
	}

	/**
	 * Method to flush to console
	 * 
	 * @throws AutomicException
	 */
	public static void flush() throws AutomicException {
		try {
			WRITER.flush();
		} catch (AutomicException ae) {
			ConsoleWriter.writeln(ae.getMessage());
		}
	}
}
