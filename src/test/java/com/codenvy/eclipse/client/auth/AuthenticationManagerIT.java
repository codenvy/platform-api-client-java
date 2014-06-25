/*
 * CODENVY CONFIDENTIAL
 * ________________
 * 
 * [2012] - [2014] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.eclipse.client.auth;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;

import com.codenvy.eclipse.client.AbstractIT;
import com.codenvy.eclipse.client.store.DataStore;

/**
 * {@link AuthenticationManager} tests.
 * 
 * @author Kevin Pollet
 */
public class AuthenticationManagerIT extends AbstractIT {
    @Test(expected = NullPointerException.class)
    @SuppressWarnings("unchecked")
    public void testNewAuthenticationManagerWithNullURL() {
        new AuthenticationManager(null, DUMMY_USERNAME, new Credentials(DUMMY_USERNAME, DUMMY_PASSWORD),
                                  mock(CredentialsProvider.class), mock(DataStore.class));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("unchecked")
    public void testNewAuthenticationManagerWithNullUsername() {
        new AuthenticationManager(REST_API_URL, null, new Credentials(DUMMY_USERNAME, DUMMY_PASSWORD),
                                  mock(CredentialsProvider.class), mock(DataStore.class));
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthorizeWithNullDataStoreNullCredentialsAndNullCredentialsProvider() {
        final AuthenticationManager authenticationManager = new AuthenticationManager(REST_API_URL, DUMMY_USERNAME, null, null, null);
        authenticationManager.authorize();
    }

    @Test
    public void testAuthorizeWithNullDataStoreNullCredentialsAndCredentialsProvider() {
        final CredentialsProvider credentialsProvider = mock(CredentialsProvider.class);
        when(credentialsProvider.load(DUMMY_USERNAME)).thenReturn(new Credentials(DUMMY_USERNAME, DUMMY_PASSWORD));

        final AuthenticationManager authenticationManager =
                                                            new AuthenticationManager(REST_API_URL, DUMMY_USERNAME, null,
                                                                                      credentialsProvider, null);
        authenticationManager.authorize();

        verify(credentialsProvider, times(1)).load(DUMMY_USERNAME);
    }

    @Test
    public void testAuthorizeWithNullDataStoreCredentialsAndCredentialsProvider() {
        final CredentialsProvider credentialsProvider = mock(CredentialsProvider.class);
        final AuthenticationManager authenticationManager = new AuthenticationManager(REST_API_URL, DUMMY_USERNAME,
                                                                                      new Credentials(DUMMY_USERNAME, DUMMY_PASSWORD),
                                                                                      credentialsProvider, null);

        final Token token = authenticationManager.authorize();

        Assert.assertNotNull(token);
        Assert.assertEquals(new Token(SDK_TOKEN_VALUE), token);
        verify(credentialsProvider, times(0)).load(DUMMY_USERNAME);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAuthorizeWithDataStoreCredentialsAndCredentialsProvider() {
        final CredentialsProvider credentialsProvider = mock(CredentialsProvider.class);
        final DataStore<String, Credentials> credentialsStore = mock(DataStore.class);
        final AuthenticationManager authenticationManager = new AuthenticationManager(REST_API_URL, DUMMY_USERNAME,
                                                                                      new Credentials(DUMMY_USERNAME, DUMMY_PASSWORD),
                                                                                      credentialsProvider, credentialsStore);

        final Token token = authenticationManager.authorize();

        Assert.assertNotNull(token);
        Assert.assertEquals(new Token(SDK_TOKEN_VALUE), token);
        verify(credentialsProvider, times(0)).load(DUMMY_USERNAME);
        verify(credentialsStore, times(1)).put(eq(DUMMY_USERNAME), eq(new Credentials(DUMMY_PASSWORD, new Token(SDK_TOKEN_VALUE))));
    }

    @Test
    public void testGetTokenWithNullDataStore() {
        final AuthenticationManager authenticationManager = new AuthenticationManager(REST_API_URL, DUMMY_USERNAME, null, null, null);

        Assert.assertNull(authenticationManager.getToken());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetTokenWithMissingUsername() {
        final DataStore<String, Credentials> credentialsStore = mock(DataStore.class);
        when(credentialsStore.get(DUMMY_USERNAME)).thenReturn(null);

        final AuthenticationManager authenticationManager =
                                                            new AuthenticationManager(REST_API_URL, DUMMY_USERNAME, null, null,
                                                                                      credentialsStore);
        final Token token = authenticationManager.getToken();

        Assert.assertNull(token);
        verify(credentialsStore, times(1)).get(DUMMY_USERNAME);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetTokenWithExistingUsername() {
        final DataStore<String, Credentials> credentialsStore = mock(DataStore.class);
        when(credentialsStore.get(DUMMY_USERNAME)).thenReturn(new Credentials(DUMMY_PASSWORD, new Token(SDK_TOKEN_VALUE)));

        final AuthenticationManager authenticationManager =
                                                            new AuthenticationManager(REST_API_URL, DUMMY_USERNAME, null, null,
                                                                                      credentialsStore);
        final Token token = authenticationManager.getToken();

        Assert.assertEquals(new Token(SDK_TOKEN_VALUE), token);
        verify(credentialsStore, times(1)).get(DUMMY_USERNAME);
    }
}