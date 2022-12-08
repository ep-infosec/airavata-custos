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

package org.apache.custos.resource.secret.management.interceptors;


import io.grpc.Metadata;
import org.apache.custos.integration.core.exceptions.MissingParameterException;
import org.apache.custos.integration.core.interceptor.IntegrationServiceInterceptor;
import org.apache.custos.resource.secret.service.CertificateCredential;
import org.apache.custos.resource.secret.service.KVCredential;
import org.apache.custos.resource.secret.service.PasswordCredential;
import org.apache.custos.resource.secret.service.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This class validates the  request input parameters
 */
@Component
public class InputValidator implements IntegrationServiceInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputValidator.class);

    /**
     * Input parameter validater
     *
     * @param methodName
     * @param body
     * @return
     */
    private void validate(String methodName, Object body, Metadata headers) {

        validationAuthorizationHeader(headers);
    }


    private boolean validationAuthorizationHeader(Metadata headers) {
        if (headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)) == null
                || headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)) == null) {
            throw new MissingParameterException("authorization header not available", null);
        }

        return true;
    }


    @Override
    public <ReqT> ReqT intercept(String method, Metadata headers, ReqT msg) {
        validate(method, msg, headers);

        if (method.equals("addSSHCredential") || method.equals("addPasswordCredential")
                || method.equals("addCertificateCredential")) {
            validateSecretMetadata(msg, method);
        } else if (method.equals("addKVCredential") || method.equals("getKVCredential") ||
                method.equals("updateKVCredential") || method.equals("deleteKVCredential")) {
            if (msg instanceof KVCredential) {
                String key = ((KVCredential) msg).getKey();
                String value = (((KVCredential) msg)).getValue();
                String token = (((KVCredential) msg)).getToken();
                if ((token == null || token.trim().equals("")) && (key == null || key.trim().equals(""))) {
                    throw new MissingParameterException("At least key or token should be added ", null);
                }

                if ((method.equals("addKVCredential") || method.equals("updateKVCredential")) && (value == null || value.trim().equals(""))) {
                    throw new MissingParameterException("Value should not be null ", null);
                }

            } else {
                throw new RuntimeException("Unknown message type", null);
            }
        }
        return msg;
    }


    private boolean validateSecretMetadata(Object msg, String method) {
        if (msg instanceof SSHCredential) {
            SSHCredential request = (SSHCredential) msg;

            if (request.getMetadata() == null) {
                throw new MissingParameterException("SecretMetadata should not be null ", null);
            }

        } else if (msg instanceof PasswordCredential) {
            PasswordCredential request = (PasswordCredential) msg;
            if (request.getMetadata() == null) {
                throw new MissingParameterException("SecretMetadata should not be null ", null);
            }
        } else if (msg instanceof CertificateCredential) {
            CertificateCredential request = (CertificateCredential) msg;
            if (request.getMetadata() == null) {
                throw new MissingParameterException("SecretMetadata should not be null ", null);
            }
        } else {
            throw new RuntimeException("Unexpected input type for method  " + method);
        }
        return true;
    }

}
