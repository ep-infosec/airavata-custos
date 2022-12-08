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

package org.apache.custos.resource.secret.manager.adaptor.outbound;

import com.google.protobuf.GeneratedMessageV3;
import org.apache.custos.resource.secret.manager.Credential;
import org.apache.custos.resource.secret.service.CertificateCredential;
import org.apache.custos.resource.secret.service.PasswordCredential;
import org.apache.custos.resource.secret.service.SSHCredential;
import org.apache.custos.resource.secret.service.*;

import java.util.UUID;

public class ResourceCredential implements Credential {

    private String ownerId;

    private String token;

    private String description;

    private ResourceOwnerType resourceOwnerType;

    private long tenantId;

    private String type;

    private String externalId;


    public ResourceCredential(GeneratedMessageV3 message) {

        this.token = generateToken();

        if (message instanceof SSHCredential) {
            SecretMetadata metadata = ((SSHCredential) message).getMetadata();
            parseMetadata(metadata);
            this.resourceOwnerType = ResourceOwnerType.TENANT;
            this.externalId = metadata.getToken();

        } else if (message instanceof CertificateCredential) {
            SecretMetadata metadata = ((CertificateCredential) message).getMetadata();
            parseMetadata(metadata);
            this.resourceOwnerType = ResourceOwnerType.TENANT;
            this.externalId = metadata.getToken();

        } else if (message instanceof PasswordCredential) {
            SecretMetadata metadata = ((PasswordCredential) message).getMetadata();
            parseMetadata(metadata);
            this.resourceOwnerType = ResourceOwnerType.TENANT;
            this.externalId = metadata.getToken();
        } else if (message instanceof org.apache.custos.resource.secret.service.KVCredential) {
            SecretMetadata metadata = ((org.apache.custos.resource.secret.service.KVCredential) message).getMetadata();
            parseMetadata(metadata);
            this.resourceOwnerType = ResourceOwnerType.TENANT_USER;
            this.externalId = ((org.apache.custos.resource.secret.service.KVCredential) message).getKey();
        } else if (message instanceof org.apache.custos.resource.secret.service.CredentialMap) {
            SecretMetadata metadata = ((org.apache.custos.resource.secret.service.CredentialMap) message).getMetadata();
            parseMetadata(metadata);
            this.resourceOwnerType = ResourceOwnerType.TENANT;
            this.externalId = metadata.getToken();
        }
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ResourceOwnerType getResourceOwnerType() {
        return resourceOwnerType;
    }

    public void setResourceOwnerType(ResourceOwnerType resourceOwnerType) {
        this.resourceOwnerType = resourceOwnerType;
    }


    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }


    private static String generateToken() {

        return UUID.randomUUID().toString();
    }


    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private boolean parseMetadata(SecretMetadata secretMetadata) {
        this.description = secretMetadata.getDescription();
        this.ownerId = secretMetadata.getOwnerId();
        this.tenantId = secretMetadata.getTenantId();
        this.type = secretMetadata.getResourceType().name();

        return true;
    }
}
