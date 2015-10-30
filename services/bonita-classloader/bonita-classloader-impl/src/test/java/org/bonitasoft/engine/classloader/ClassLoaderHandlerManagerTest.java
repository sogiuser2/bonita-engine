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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassLoaderHandlerManagerTest {

    @Mock
    private TechnicalLoggerService loggerService;

    @InjectMocks
    private ClassLoaderHandlerManager manager;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        given(loggerService.isLoggable(Matchers.<Class<?>>any(), any(TechnicalLogSeverity.class))).willReturn(true);
    }

    @Test
    public void containsClassLoaderChangeHandler_should_return_false_when_handler_is_absent() throws Exception {
        //when
        boolean containsClassLoaderChangeHandler = manager.containsClassLoaderChangeHandler("anyUnregisteredHandler");

        //then
        assertThat(containsClassLoaderChangeHandler).isFalse();
    }

    @Test
    public void containsClassLoaderChangeHandler_should_return_true_after_register() throws Exception {
        //given
        ThrowErrorClassLoaderChangeHandler handler = new ThrowErrorClassLoaderChangeHandler(new SClassLoaderException("error"));
        manager.registerClassLoaderChangeHandler(handler);

        //when
        boolean containsClassLoaderChangeHandler = manager.containsClassLoaderChangeHandler(handler.getIdentifier());

        //then
        assertThat(containsClassLoaderChangeHandler).isTrue();
    }

    @Test
    public void register_should_throw_IllegalArgumentException_when_identifier_is_null() throws Exception {
        //then
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The handler identifier cannot be null or empty.");

        //when
        manager.registerClassLoaderChangeHandler(buildHandler(null));

    }

    @Test
    public void register_should_throw_IllegalArgumentException_when_identifier_is_empty() throws Exception {
        //then
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The handler identifier cannot be null or empty.");

        //when
        manager.registerClassLoaderChangeHandler(buildHandler(""));

    }

    @Test
    public void executeHandlers_should_execute_all_handlers() throws Exception {
        //given
        ClassLoaderChangeHandler handler1 = buildHandler("id1");
        ClassLoaderChangeHandler handler2 = buildHandler("id2");
        manager.registerClassLoaderChangeHandler(handler1);
        manager.registerClassLoaderChangeHandler(handler2);

        //when
        manager.executeHandlers();

        //then
        verify(handler1).onDestroy();
        verify(handler2).onDestroy();
    }

    @Test
    public void executeHandlers_should_log_and_continue_on_exception() throws Exception {
        //given
        SClassLoaderException exception = new SClassLoaderException("somme error");
        ClassLoaderChangeHandler handler1 = new ThrowErrorClassLoaderChangeHandler(exception);
        ClassLoaderChangeHandler handler2 = buildHandler("id2");
        manager.registerClassLoaderChangeHandler(handler1);
        manager.registerClassLoaderChangeHandler(handler2);

        //when
        manager.executeHandlers();

        //then
        verify(loggerService).log(Matchers.<Class<?>>any(), eq(TechnicalLogSeverity.WARNING), contains(handler1.getIdentifier()), eq(exception));
        verify(handler2).onDestroy();
    }

    private ClassLoaderChangeHandler buildHandler(String identifier) {
        ClassLoaderChangeHandler handler = mock(ClassLoaderChangeHandler.class);
        given(handler.getIdentifier()).willReturn(identifier);
        return handler;
    }

}