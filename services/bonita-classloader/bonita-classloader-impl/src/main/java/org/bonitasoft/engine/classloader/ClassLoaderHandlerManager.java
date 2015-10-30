/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/

package org.bonitasoft.engine.classloader;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Elias Ricken de Medeiros
 */
public class ClassLoaderHandlerManager {

    private final Map<String, ClassLoaderChangeHandler> classLoaderChangeHandlers = new HashMap<>(5);
    private final TechnicalLoggerService loggerService;

    public ClassLoaderHandlerManager(TechnicalLoggerService loggerService) {
        this.loggerService = loggerService;
    }

    public void registerClassLoaderChangeHandler(ClassLoaderChangeHandler changeHandler) {
        if (changeHandler.getIdentifier() == null || changeHandler.getIdentifier().isEmpty()) {
            throw new IllegalArgumentException("The handler identifier cannot be null or empty.");
        }
        classLoaderChangeHandlers.put(changeHandler.getIdentifier(), changeHandler);
    }

    public boolean containsClassLoaderChangeHandler(String changeHandlerIdentifier) {
        return classLoaderChangeHandlers.containsKey(changeHandlerIdentifier);
    }

    public void executeHandlers() {
        for (ClassLoaderChangeHandler handler : classLoaderChangeHandlers.values()) {
            try {
                handler.onDestroy();
            } catch (SBonitaException e) {
                logException(handler, e);
            }
        }
    }

    private void logException(ClassLoaderChangeHandler changeHandler, SBonitaException e) {
        if (loggerService.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
            String message = new StringBuilder()
                    .append("Unable to execute the ClassLoaderChangeHandler '")
                    .append(changeHandler.getIdentifier())
                    .append("'. You may experiment ClassLoader issues. Please, consider to restart the server.").toString();
            loggerService.log(getClass(), TechnicalLogSeverity.WARNING, message, e);
        }
    }

}
