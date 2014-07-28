package com.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.commons.exceptions.SObjectAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.api.impl.convertor.ApplicationConvertor;
import com.bonitasoft.engine.business.application.Application;
import com.bonitasoft.engine.business.application.ApplicationCreator;
import com.bonitasoft.engine.business.application.ApplicationNotFoundException;
import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.impl.ApplicationImpl;
import com.bonitasoft.engine.business.application.impl.SApplicationImpl;
import com.bonitasoft.engine.service.TenantServiceAccessor;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationAPIDelegateTest {

    @Mock
    private TenantServiceAccessor accessor;

    @Mock
    private ApplicationConvertor convertor;

    @Mock
    private ApplicationService applicationService;

    private ApplicationAPIDelegate delegate;

    private final long APPLICATION_ID = 15;

    private final long LOGGED_USER_ID = 10;

    private static final String NAME = "app";

    private static final String VERSION = "1.0";

    private static final String PATH = "/app";

    private static final String DESCRIPTION = "app desc";

    @Before
    public void setUp() throws Exception {
        delegate = new ApplicationAPIDelegate(accessor, convertor, LOGGED_USER_ID);
        given(accessor.getApplicationService()).willReturn(applicationService);
    }

    @Test
    public void createApplication_should_call_applicationService_add() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(NAME, VERSION, PATH);
        creator.setDescription(DESCRIPTION);
        final SApplicationImpl sApp = new SApplicationImpl(NAME, VERSION, PATH);
        sApp.setDescription(DESCRIPTION);
        final ApplicationImpl application = new ApplicationImpl(NAME, VERSION, PATH, DESCRIPTION);
        given(convertor.buildSApplication(creator, LOGGED_USER_ID)).willReturn(sApp);
        given(convertor.toApplication(sApp)).willReturn(application);
        given(applicationService.createApplication(sApp)).willReturn(sApp);

        //when
        final Application createdApplication = delegate.createApplication(creator);

        //then
        assertThat(createdApplication).isEqualTo(application);
    }

    @Test(expected = AlreadyExistsException.class)
    public void createApplication_should_throw_AlreadyExistsException_when_applicationService_throws_SObjectAlreadyExistsException() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(NAME, VERSION, PATH);
        final SApplicationImpl sApp = new SApplicationImpl(NAME, VERSION, PATH);
        given(convertor.buildSApplication(creator, LOGGED_USER_ID)).willReturn(sApp);
        given(applicationService.createApplication(sApp)).willThrow(new SObjectAlreadyExistsException(""));

        //when
        delegate.createApplication(creator);

        //then exception
    }

    @Test(expected = CreationException.class)
    public void createApplication_should_throw_CreationException_when_applicationService_throws_SObjectCreationException() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator(NAME, VERSION, PATH);
        final SApplicationImpl sApp = new SApplicationImpl(NAME, VERSION, PATH);
        given(convertor.buildSApplication(creator, LOGGED_USER_ID)).willReturn(sApp);
        given(applicationService.createApplication(sApp)).willThrow(new SObjectCreationException(""));

        //when
        delegate.createApplication(creator);

        //then exception
    }

    @Test
    public void delete_Application_should_call_applicationService_delete() throws Exception {
        //when
        delegate.deleteApplication(APPLICATION_ID);

        //then
        verify(applicationService, times(1)).deleteApplication(APPLICATION_ID);
    }

    @Test(expected = DeletionException.class)
    public void delete_Application_should_throw_DeletionException_when_applicationService_throws_SObjectModificationException() throws Exception {
        doThrow(new SObjectModificationException()).when(applicationService).deleteApplication(APPLICATION_ID);

        //when
        delegate.deleteApplication(APPLICATION_ID);

        //then exception
    }

    @Test
    public void getApplication_should_return_the_application_returned_by_applicationService_coverted() throws Exception {
        //given
        final SApplicationImpl sApp = new SApplicationImpl(NAME, VERSION, PATH);
        final ApplicationImpl application = new ApplicationImpl(NAME, VERSION, PATH, null);
        given(applicationService.getApplication(APPLICATION_ID)).willReturn(sApp);
        given(convertor.toApplication(sApp)).willReturn(application);

        //when
        final Application retriedApp = delegate.getApplication(APPLICATION_ID);

        //then
        assertThat(retriedApp).isEqualTo(application);

    }

    @Test(expected = RetrieveException.class)
    public void getApplication_should_throw_RetrieveException_when_applicationService_throws_SBonitaReadException() throws Exception {
        //given
        given(applicationService.getApplication(APPLICATION_ID)).willThrow(new SBonitaReadException(""));

        //when
        delegate.getApplication(APPLICATION_ID);

        //then exception
    }

    @Test(expected = ApplicationNotFoundException.class)
    public void getApplication_should_throw_ApplicationNotFoundException_when_applicationService_throws_SObjectNotFoundException() throws Exception {
        //given
        given(applicationService.getApplication(APPLICATION_ID)).willThrow(new SObjectNotFoundException());

        //when
        delegate.getApplication(APPLICATION_ID);

        //then exception
    }

}
