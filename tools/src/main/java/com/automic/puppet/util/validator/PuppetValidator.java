package com.automic.puppet.util.validator;

import java.io.File;

import com.automic.puppet.constants.ExceptionConstants;
import com.automic.puppet.exception.AutomicException;
import com.automic.puppet.util.CommonUtil;

/**
 * This class provides common validations as required by action(s).
 *
 */

public final class PuppetValidator {

    private PuppetValidator() {
    }

    public static void checkNotEmpty(String parameter, String parameterName) throws AutomicException {
        if (!CommonUtil.checkNotEmpty(parameter)) {
            throw new AutomicException(String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, parameterName,
                    parameter));
        }
    }
    
    public static void checkNotNull(Object parameter, String parameterName) throws AutomicException {
        if (!CommonUtil.checkNotNull(parameter)) {
            throw new AutomicException(String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, parameterName,
                    parameter));
        }
    }

    public static void checkFileExists(File file) throws AutomicException {
        if (!(file.exists() && file.isFile())) {
            throw new AutomicException(String.format(ExceptionConstants.INVALID_FILE, file));
        }
    }

    public static void checkDirectoryExists(File filePath, String parameterName) throws AutomicException {
        if (!(filePath.exists() && !filePath.isFile())) {
            throw new AutomicException(String.format(ExceptionConstants.INVALID_INPUT_PARAMETER, parameterName,
                    filePath));
        }
    }

}
