/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.custos.sharing.management.interceptors;

import io.grpc.Metadata;
import org.apache.custos.credential.store.client.CredentialStoreServiceClient;
import org.apache.custos.identity.client.IdentityClient;
import org.apache.custos.integration.core.exceptions.UnAuthorizedException;
import org.apache.custos.integration.services.commons.interceptors.MultiTenantAuthInterceptor;
import org.apache.custos.integration.services.commons.model.AuthClaim;
import org.apache.custos.sharing.service.*;
import org.apache.custos.tenant.profile.client.async.TenantProfileClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This validates custos credentials
 */
@Component
public class AuthInterceptorImpl extends MultiTenantAuthInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthInterceptorImpl.class);

    private CredentialStoreServiceClient credentialStoreServiceClient;

    public AuthInterceptorImpl(CredentialStoreServiceClient credentialStoreServiceClient,
                               TenantProfileClient tenantProfileClient, IdentityClient identityClient) {
        super(credentialStoreServiceClient, tenantProfileClient, identityClient);
        this.credentialStoreServiceClient = credentialStoreServiceClient;
    }


    @Override
    public <ReqT> ReqT intercept(String method, Metadata headers, ReqT msg) {
        Optional<AuthClaim> authClaim;
        if (msg instanceof EntityTypeRequest) {
            EntityTypeRequest req = ((EntityTypeRequest) msg);
            authClaim = validateAuth(headers, req.getClientId());
            return authClaim.map(cl -> {
                EntityTypeRequest request = ((EntityTypeRequest) msg);
                request = request.toBuilder()
                        .setTenantId(cl.getTenantId())
                        .setClientSec(cl.getIamAuthSecret())
                        .build();
                return (ReqT) request;

            }).orElseThrow(() -> {
                throw new UnAuthorizedException("Request is not authorized", null);
            });

        } else if (msg instanceof SearchRequest) {
            SearchRequest req = ((SearchRequest) msg);
            authClaim = validateAuth(headers, req.getClientId());
            return authClaim.map(cl -> {
                SearchRequest request = ((SearchRequest) msg);
                request = request.toBuilder()
                        .setTenantId(cl.getTenantId())
                        .setClientSec(cl.getIamAuthSecret())
                        .build();
                return (ReqT) request;

            }).orElseThrow(() -> {
                throw new UnAuthorizedException("Request is not authorized", null);
            });
        } else if (msg instanceof PermissionTypeRequest) {
            PermissionTypeRequest req = ((PermissionTypeRequest) msg);
            authClaim = validateAuth(headers, req.getClientId());
           return authClaim.map(cl -> {
                PermissionTypeRequest request = ((PermissionTypeRequest) msg);
                request = request.toBuilder()
                        .setTenantId(cl.getTenantId())
                        .setClientSec(cl.getIamAuthSecret())
                        .build();
                return (ReqT) request;

            }).orElseThrow(() -> {
                throw new UnAuthorizedException("Request is not authorized", null);
            });
        } else if (msg instanceof EntityRequest) {
            EntityRequest req = ((EntityRequest) msg);
            authClaim = validateAuth(headers, req.getClientId());
            return authClaim.map(cl -> {
                EntityRequest request = ((EntityRequest) msg);
                request = request.toBuilder()
                        .setTenantId(cl.getTenantId())
                        .setClientSec(cl.getIamAuthSecret())
                        .build();
                return (ReqT) request;

            }).orElseThrow(() -> {
                throw new UnAuthorizedException("Request is not authorized", null);
            });
        } else if (msg instanceof SharingRequest) {
            SharingRequest req = ((SharingRequest) msg);
            authClaim = validateAuth(headers, req.getClientId());
           return authClaim.map(cl -> {
                SharingRequest request = ((SharingRequest) msg);
                request = request.toBuilder()
                        .setTenantId(cl.getTenantId())
                        .setClientSec(cl.getIamAuthSecret())
                        .build();
                return (ReqT) request;

            }).orElseThrow(() -> {
                throw new UnAuthorizedException("Request is not authorized", null);
            });
        }

        return (ReqT) msg;

    }


    private Optional<AuthClaim> validateAuth(Metadata headers, String clientId) {
        try {

            return authorize(headers, clientId);
        } catch (Exception ex) {
            LOGGER.error(" Authorizing error " + ex.getMessage());
            throw new UnAuthorizedException("Request is not authorized", ex);
        }
    }


}
