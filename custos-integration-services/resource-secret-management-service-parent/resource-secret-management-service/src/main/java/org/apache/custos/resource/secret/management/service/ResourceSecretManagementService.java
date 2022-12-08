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

package org.apache.custos.resource.secret.management.service;

import com.google.protobuf.ByteString;
import com.google.protobuf.Struct;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.custos.cluster.management.client.ClusterManagementClient;
import org.apache.custos.cluster.management.service.GetServerCertificateRequest;
import org.apache.custos.cluster.management.service.GetServerCertificateResponse;
import org.apache.custos.identity.client.IdentityClient;
import org.apache.custos.identity.service.GetJWKSRequest;
import org.apache.custos.integration.core.utils.ShamirSecretHandler;
import org.apache.custos.resource.secret.client.ResourceSecretClient;
import org.apache.custos.resource.secret.management.service.ResourceSecretManagementServiceGrpc.ResourceSecretManagementServiceImplBase;
import org.apache.custos.resource.secret.service.*;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@GRpcService
public class ResourceSecretManagementService extends ResourceSecretManagementServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceSecretManagementService.class);

    @Autowired
    private ClusterManagementClient clusterManagementClient;

    @Autowired
    private IdentityClient identityClient;

    @Autowired
    private ResourceSecretClient resourceSecretClient;

    @Override
    public void getSecret(GetSecretRequest request,
                          StreamObserver<SecretMetadata> responseObserver) {
        LOGGER.debug("Request received to get secret ");
        try {

            if (request.getMetadata().getOwnerType() == ResourceOwnerType.CUSTOS &&
                    request.getMetadata().getResourceType() == ResourceType.SERVER_CERTIFICATE) {

                GetServerCertificateRequest getServerCertificateRequest = GetServerCertificateRequest.newBuilder().build();
                GetServerCertificateResponse response = clusterManagementClient.getCustosServerCertificate(getServerCertificateRequest);

                SecretMetadata metadata = SecretMetadata.newBuilder().setValue(response.getCertificate()).build();
                responseObserver.onNext(metadata);
                responseObserver.onCompleted();
            } else {

            }

        } catch (Exception ex) {
            String msg = "Error occurred while pulling secretes " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void getJWKS(GetJWKSRequest request, StreamObserver<Struct> responseObserver) {
        LOGGER.debug("Request received to get JWKS " + request.getTenantId());
        try {

            Struct struct = identityClient.getJWKS(request);

            responseObserver.onNext(struct);
            responseObserver.onCompleted();


        } catch (Exception ex) {
            String msg = "Error occurred while pulling JWKS " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void getResourceCredentialSummary(GetResourceCredentialByTokenRequest request, StreamObserver<SecretMetadata> responseObserver) {
        LOGGER.debug("Request received to get ResourceCredentialSummary of " + request.getToken());
        try {

            SecretMetadata metadata = resourceSecretClient.getResourceCredentialSummary(request);
            responseObserver.onNext(metadata);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Error occurred while fetching resource credential summary : " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void getAllResourceCredentialSummaries(GetResourceCredentialSummariesRequest request, StreamObserver<ResourceCredentialSummaries> responseObserver) {
        LOGGER.debug("Request received to get AllResourceCredentialSummaries in tenant " + request.getTenantId());
        try {

            ResourceCredentialSummaries response = resourceSecretClient.getAllResourceCredentialSummaries(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Error occurred while fetching all resource credential summaries : " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void addSSHCredential(SSHCredential request, StreamObserver<AddResourceCredentialResponse> responseObserver) {
        LOGGER.debug("Request received to add SSHCredential ");
        try {

            if (request.getUseShamirsSecretSharingWithEncryption()) {
                List<ByteString> byteStringList = request.getPrivateKeySharesList();
                if (byteStringList != null && byteStringList.size() > 0) {

                    String secret = ShamirSecretHandler.
                            generateSecret(byteStringList, request.getNumOfShares(), request.getThreshold());
                    request = request.toBuilder().setPrivateKey(secret).build();
                }
            }

            AddResourceCredentialResponse response = resourceSecretClient.addSSHCredential(request);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Error occurred whiling saving SSH credentials :  " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void addPasswordCredential(PasswordCredential request, StreamObserver<AddResourceCredentialResponse> responseObserver) {
        LOGGER.debug("Request received to add PasswordCredential ");
        try {
            if (request.getUseShamirsSecretSharingWithEncryption()) {
                List<ByteString> byteStringList = request.getSecretSharesList();
                if (byteStringList != null && byteStringList.size() > 0) {

                    String secret = ShamirSecretHandler.
                            generateSecret(byteStringList, request.getNumOfShares(), request.getThreshold());
                    request = request.toBuilder().setPassword(secret).build();
                }
            }

            AddResourceCredentialResponse response = resourceSecretClient.addPasswordCredential(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Error occurred while  saving password credential : " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void addCertificateCredential(CertificateCredential request, StreamObserver<AddResourceCredentialResponse> responseObserver) {
        LOGGER.debug("Request received to add CertificateCredential ");
        try {
            if (request.getUseShamirsSecretSharingWithEncryption()) {
                List<ByteString> byteStringList = request.getPrivateKeySharesList();
                if (byteStringList != null && byteStringList.size() > 0) {

                    String secret = ShamirSecretHandler.
                            generateSecret(byteStringList, request.getNumOfShares(), request.getThreshold());
                    request = request.toBuilder().setPrivateKey(secret).build();
                }
            }
            AddResourceCredentialResponse response = resourceSecretClient.addCertificateCredential(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Error occurred while saving  certificate credential : " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }


    }

    @Override
    public void getSSHCredential(GetResourceCredentialByTokenRequest request, StreamObserver<SSHCredential> responseObserver) {
        LOGGER.debug("Request received to get SSHCredential ");
        try {
            SSHCredential response = resourceSecretClient.getSSHCredential(request);

            if (request.getUseShamirsSecretSharingWithEncryption()) {

                int numberOfShares = response.getNumOfShares();
                int threshold = response.getThreshold();

                String privateKey = response.getPrivateKey();

                if (privateKey != null && privateKey.trim().equals("")) {
                    Map<Integer, byte[]> shares = ShamirSecretHandler.splitSecret(privateKey, numberOfShares, threshold);

                    List<ByteString> byteStringList = shares.values().stream().
                            map(val -> ByteString.copyFromUtf8(new String(val))).collect(Collectors.toList());

                    response = response.toBuilder().addAllPrivateKeyShares(byteStringList)
                            .setPrivateKey("")
                            .build();

                }
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Error occurred while fetching  SSH credentials : " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void getPasswordCredential(GetResourceCredentialByTokenRequest request, StreamObserver<PasswordCredential> responseObserver) {
        LOGGER.debug("Request received to get PasswordCredential " + request.getTenantId());
        try {

            PasswordCredential response = resourceSecretClient.getPasswordCredential(request);
            if (request.getUseShamirsSecretSharingWithEncryption()) {

                int numberOfShares = response.getNumOfShares();
                int threshold = response.getThreshold();

                String privateKey = response.getPassword();

                if (privateKey != null && privateKey.trim().equals("")) {
                    Map<Integer, byte[]> shares = ShamirSecretHandler.splitSecret(privateKey, numberOfShares, threshold);

                    List<ByteString> byteStringList = shares.values().stream().
                            map(val -> ByteString.copyFromUtf8(new String(val))).collect(Collectors.toList());

                    response = response.toBuilder().addAllSecretShares(byteStringList)
                            .setPassword("")
                            .build();

                }
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Error occurred while  fetching password credentials : " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void getCertificateCredential(GetResourceCredentialByTokenRequest request, StreamObserver<CertificateCredential> responseObserver) {
        LOGGER.debug("Request received to get CertificateCredential " + request.getTenantId());
        try {

            CertificateCredential response = resourceSecretClient.getCertificateCredential(request);
            if (request.getUseShamirsSecretSharingWithEncryption()) {

                int numberOfShares = response.getNumOfShares();
                int threshold = response.getThreshold();

                String privateKey = response.getPrivateKey();

                if (privateKey != null && privateKey.trim().equals("")) {
                    Map<Integer, byte[]> shares = ShamirSecretHandler.splitSecret(privateKey, numberOfShares, threshold);

                    List<ByteString> byteStringList = shares.values().stream().
                            map(val -> ByteString.copyFromUtf8(new String(val))).collect(Collectors.toList());

                    response = response.toBuilder().addAllPrivateKeyShares(byteStringList)
                            .setPrivateKey("")
                            .build();

                }
            }
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Error occurred while fetching  certificate credential : " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void deleteSSHCredential(GetResourceCredentialByTokenRequest request, StreamObserver<ResourceCredentialOperationStatus> responseObserver) {
        LOGGER.debug("Request received to delete SSHCredential " + request.getTenantId());
        try {

            ResourceCredentialOperationStatus response = resourceSecretClient.deleteSSHCredential(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred while deleting  SSH credential : " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void deletePWDCredential(GetResourceCredentialByTokenRequest request, StreamObserver<ResourceCredentialOperationStatus> responseObserver) {
        LOGGER.debug("Request received to delete PWDCredential " + request.getTenantId());
        try {

            ResourceCredentialOperationStatus response = resourceSecretClient.deletePWDCredential(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            String msg = "Error occurred while deleting password credential : " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void deleteCertificateCredential(GetResourceCredentialByTokenRequest request, StreamObserver<ResourceCredentialOperationStatus> responseObserver) {
        LOGGER.debug("Request received to delete CertificateCredential " + request.getTenantId());
        try {
            ResourceCredentialOperationStatus response = resourceSecretClient.deleteCertificateCredential(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred while deleting  certificate credential :  " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }


    @Override
    public void getKVCredential(KVCredential request, StreamObserver<KVCredential> responseObserver) {
        LOGGER.debug("Request received to getKVCredential in tenant " + request.getMetadata().getTenantId()
                + " of user " + request.getMetadata().getOwnerId() + "for key " + request.getKey());
        try {

            KVCredential kvCredential = resourceSecretClient.getKVCredential(request);

            responseObserver.onNext(kvCredential);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred while fetching  KV credentials :  " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void addKVCredential(KVCredential request, StreamObserver<ResourceCredentialOperationStatus> responseObserver) {
        LOGGER.debug("Request received to addKVCredential in tenant " + request.getMetadata().getTenantId()
                + " of user " + request.getMetadata().getOwnerId() + "for key " + request.getKey());
        try {

            ResourceCredentialOperationStatus status = resourceSecretClient.setKVCredential(request);
            responseObserver.onNext(status);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred while adding  KV  credentials :  " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void updateKVCredential(KVCredential request, StreamObserver<ResourceCredentialOperationStatus> responseObserver) {
        LOGGER.debug("Request received to updateKVCredential in tenant " + request.getMetadata().getTenantId()
                + " of user " + request.getMetadata().getOwnerId() + "for key " + request.getKey());
        try {
            ResourceCredentialOperationStatus status = resourceSecretClient.updateKVCredential(request);
            responseObserver.onNext(status);
            responseObserver.onCompleted();


        } catch (Exception ex) {
            String msg = "Error occurred while updating  KV credentials :  " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void deleteKVCredential(KVCredential request, StreamObserver<ResourceCredentialOperationStatus> responseObserver) {
        LOGGER.debug("Request received to deleteKVCredential in tenant " + request.getMetadata().getTenantId()
                + " of user " + request.getMetadata().getOwnerId() + "for key " + request.getKey());
        try {
            ResourceCredentialOperationStatus status = resourceSecretClient.deleteKVCredential(request);
            responseObserver.onNext(status);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred while deleting  KV credentials :  " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void getCredentialMap(CredentialMap request, StreamObserver<CredentialMap> responseObserver) {
        LOGGER.debug("Request received to getCredentialMap in tenant " + request.getMetadata().getTenantId()
                + " of user " + request.getMetadata().getOwnerId() + "for key " + request.getMetadata().getToken());
        try {
            CredentialMap status = resourceSecretClient.getCredentialMap(request);
            responseObserver.onNext(status);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred while fetching   Credentials  Map :  " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void addCredentialMap(CredentialMap request, StreamObserver<AddResourceCredentialResponse> responseObserver) {
        LOGGER.debug("Request received to addCredentialMap in tenant " + request.getMetadata().getTenantId()
                + " of user " + request.getMetadata().getOwnerId() + "for key " + request.getMetadata().getToken());
        try {
            AddResourceCredentialResponse status = resourceSecretClient.setCredentialMap(request);
            responseObserver.onNext(status);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred while saving  CredentialMap :  " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void updateCredentialMap(CredentialMap request, StreamObserver<ResourceCredentialOperationStatus> responseObserver) {
        LOGGER.debug("Request received to updateCredentialMap in tenant " + request.getMetadata().getTenantId()
                + " of user " + request.getMetadata().getOwnerId() + "for key " + request.getMetadata().getToken());
        try {
            ResourceCredentialOperationStatus status = resourceSecretClient.updateCredentialMap(request);
            responseObserver.onNext(status);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred while updaintg  Credentials  Map:  " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void deleteCredentialMap(CredentialMap request, StreamObserver<ResourceCredentialOperationStatus> responseObserver) {
        LOGGER.debug("Request received to deleteKVCredential in tenant " + request.getMetadata().getTenantId()
                + " of user " + request.getMetadata().getOwnerId() + "for key " + request.getMetadata().getToken());
        try {
            ResourceCredentialOperationStatus status = resourceSecretClient.deleteCredentialMap(request);
            responseObserver.onNext(status);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred while deleting  Credential Map :  " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }


    @Override
    public void updateCertificateCredential(CertificateCredential request, StreamObserver<ResourceCredentialOperationStatus> responseObserver) {
        LOGGER.debug("Request received to updateCertificateCredential in tenant " + request.getMetadata().getTenantId()
                + " of user " + request.getMetadata().getOwnerId() + "for key " + request.getMetadata().getToken());
        try {
            ResourceCredentialOperationStatus status = resourceSecretClient.updateCertificate(request);
            responseObserver.onNext(status);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred while deleting  Credential Map :  " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }
}
