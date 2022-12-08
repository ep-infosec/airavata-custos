/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.custos.user.management.interceptors;

import io.grpc.Metadata;
import org.apache.custos.credential.store.client.CredentialStoreServiceClient;
import org.apache.custos.identity.client.IdentityClient;
import org.apache.custos.integration.core.exceptions.UnAuthorizedException;
import org.apache.custos.integration.services.commons.interceptors.AuthInterceptor;
import org.apache.custos.integration.services.commons.model.AuthClaim;
import org.apache.custos.tenant.profile.client.async.TenantProfileClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SuperTenantRestrictedOperationsInterceptorImpl extends AuthInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuperTenantRestrictedOperationsInterceptorImpl.class);

    private CredentialStoreServiceClient credentialStoreServiceClient;


    public SuperTenantRestrictedOperationsInterceptorImpl(CredentialStoreServiceClient credentialStoreServiceClient,
                                                          TenantProfileClient tenantProfileClient, IdentityClient identityClient) {
        super(credentialStoreServiceClient, tenantProfileClient, identityClient);
        this.credentialStoreServiceClient = credentialStoreServiceClient;
    }

    @Override
    public <ReqT> ReqT intercept(String method, Metadata headers, ReqT msg) {

        if (method.equals("synchronizeUserDBs")) {
            Optional<AuthClaim> claim = null;
            try {
                claim = authorizeUsingUserToken(headers);
            } catch (Exception ex) {
                LOGGER.error(" Authorizing error " + ex.getMessage());
                throw new UnAuthorizedException("Request is not authorized", ex);
            }
            if (claim == null || claim.isEmpty() || !claim.get().isSuperTenant() || !claim.get().isAdmin()) {
                throw new UnAuthorizedException("Request is not authorized", null);
            }

        }

        return msg;
    }


}
