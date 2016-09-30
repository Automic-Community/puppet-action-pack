package com.automic.puppet.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.automic.puppet.constants.Constants;
import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;

/**
 * Utility class to write bytes to a stream
 * 
 * @author shrutinambiar
 *
 */
public class ByteWriter {
    private BufferedOutputStream bos = null;

    public ByteWriter(OutputStream output) {
        bos = new BufferedOutputStream(output, Constants.IO_BUFFER_SIZE);
    }

    /**
     * Method to write specific part of byte array to Stream
     *
     * @param bytes
     * @param offset
     * @param length
     * @throws AutomicException
     */
    public void write(byte[] bytes, int offset, int length) throws AutomicException {
        try {
            bos.write(bytes, offset, length);
        } catch (IOException e) {
            throw new AutomicException(ExceptionConstants.UNABLE_TO_WRITE, e);
        }
    }

    /**
     * Method to write bytes to Stream
     *
     * @param bytes
     * @throws AutomicException
     */
    public void write(byte[] bytes) throws AutomicException {
        write(bytes, 0, bytes.length);
    }

    /**
     * Method to write a String to stream
     *
     * @param field
     * @throws AutomicException
     */
    public void write(String field) throws AutomicException {
        write(field.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Method to flush to stream
     *
     * @throws AutomicException
     */
    public void flush() throws AutomicException {
        if (bos != null) {
            try {
                bos.flush();
            } catch (IOException e) {
                throw new AutomicException(ExceptionConstants.UNABLE_TO_FLUSH_STREAM, e);
            }
        }
    }
}