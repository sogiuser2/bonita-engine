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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URI;
import java.util.Collections;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassLoaderServiceImplTest {

    @Mock
    private ParentRedirectClassLoader parentRedirectClassLoader;

    @Mock
    private TechnicalLoggerService technicalLoggerService;

    @Mock
    private EventService eventService;

    @Mock
    private ClassLoaderHandlerManager handlerManager;

    @Spy
    @InjectMocks
    private ClassLoaderServiceImpl classLoaderService;

    @Mock
    private VirtualClassLoader virtualClassLoader;

    private URI uri = new File("dummy").toURI();

    @Test
    public void containsClassLoaderChangeHandler_should_delegate_to_handler_manager() throws Exception {
        //given
        String identifier = "anyUnregisteredHandler";
        given(handlerManager.containsClassLoaderChangeHandler(identifier)).willReturn(true);

        //when
        boolean containsClassLoaderChangeHandler = classLoaderService.containsClassLoaderChangeHandler(identifier);

        //then
        assertThat(containsClassLoaderChangeHandler).isTrue();
    }

    @Test
    public void registerClassLoaderChangeHandler_should_delegate_to_handler_manager() throws Exception {
        //given
        ThrowErrorClassLoaderChangeHandler handler = new ThrowErrorClassLoaderChangeHandler(new SClassLoaderException("error"));

        //when
        classLoaderService.registerClassLoaderChangeHandler(handler);

        //then
        verify(handlerManager).registerClassLoaderChangeHandler(handler);
    }

    @Test
    public void refreshClassLoader_should_execute_handlers() throws Exception {
        //when
        classLoaderService.refreshClassLoader(virtualClassLoader, Collections.<String, byte[]>emptyMap(), "any", 1l, uri, parentRedirectClassLoader);

        //then
        verify(handlerManager).executeHandlers();
    }

    @Test
    public void stop_should_execute_handlers() throws Exception {
        //when
        classLoaderService.stop();

        //then
        verify(handlerManager).executeHandlers();
    }

    @Test
    public void removeAllLocalClassLoader_should_execute_handlers() throws Exception {
        //when
        classLoaderService.removeAllLocalClassLoaders("any");

        //then
        verify(handlerManager).executeHandlers();
    }

    @Test
    public void removeLocalClassLoader_should_execute_handlers() throws Exception {
        //when
        classLoaderService.removeLocalClassLoader("any", 5);

        //then
        verify(handlerManager).executeHandlers();
    }
}