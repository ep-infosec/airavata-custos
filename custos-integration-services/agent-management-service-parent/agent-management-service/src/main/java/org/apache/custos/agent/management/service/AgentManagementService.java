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

package org.apache.custos.agent.management.service;

import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.apache.custos.agent.profile.client.AgentProfileClient;
import org.apache.custos.agent.profile.service.Agent;
import org.apache.custos.agent.profile.service.AgentAttribute;
import org.apache.custos.agent.profile.service.AgentRequest;
import org.apache.custos.agent.profile.service.AgentStatus;
import org.apache.custos.credential.store.client.CredentialStoreServiceClient;
import org.apache.custos.credential.store.service.CredentialMetadata;
import org.apache.custos.credential.store.service.GetCredentialRequest;
import org.apache.custos.credential.store.service.Type;
import org.apache.custos.iam.admin.client.IamAdminServiceClient;
import org.apache.custos.iam.service.*;
import org.apache.custos.identity.client.IdentityClient;
import org.apache.custos.tenant.profile.client.async.TenantProfileClient;
import org.apache.custos.tenant.profile.service.GetTenantRequest;
import org.apache.custos.tenant.profile.service.GetTenantResponse;
import org.apache.custos.tenant.profile.service.Tenant;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@GRpcService
public class AgentManagementService extends org.apache.custos.agent.management.service.AgentManagementServiceGrpc.AgentManagementServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentManagementService.class);

    @Autowired
    private AgentProfileClient agentProfileClient;

    @Autowired
    private IamAdminServiceClient iamAdminServiceClient;

    @Autowired
    private IdentityClient identityClient;

    @Autowired
    private TenantProfileClient tenantProfileClient;

    @Autowired
    private CredentialStoreServiceClient credentialStoreServiceClient;


    private static final String AGENT_CLIENT = "agent-client";

    private static final String CUSTOS_REALM_AGENT = "custos-realm-agent";

    public static final String AGENT_ID = "agent-id";
    public static final String AGENT_PARENT_ID = "agent-parent-id";


    @Override
    public void enableAgents(AgentClientMetadata request, StreamObserver<OperationStatus> responseObserver) {
        try {

            LOGGER.debug("Request received to enable agent " + request.getTenantId());


            GetTenantRequest tenantRequest = GetTenantRequest
                    .newBuilder()
                    .setTenantId(request.getTenantId()).build();

            GetTenantResponse response = tenantProfileClient.getTenant(tenantRequest);

            Tenant tenant = response.getTenant();

            if (tenant == null || tenant.getTenantId() == 0) {
                String msg = "Tenant not found  for " + request.getTenantId();
                LOGGER.error(msg);
                responseObserver.onError(Status.NOT_FOUND.withDescription(msg).asRuntimeException());
                return;
            }

            List<String> redirectURIs = new ArrayList<>();
            String redirectURI = tenant.getClientUri() + "/agent/callback";
            redirectURIs.add(redirectURI);

            request = request.toBuilder()
                    .setTenantURL(tenant.getClientUri())
                    .addAllRedirectURIs(redirectURIs)
                    .setClientName(AGENT_CLIENT)
                    .build();

            GetCredentialRequest credentialRequest = GetCredentialRequest
                    .newBuilder()
                    .setType(Type.AGENT_CLIENT)
                    .setOwnerId(request.getTenantId())
                    .build();

            CredentialMetadata credentialMetadata = this.credentialStoreServiceClient.getCredential(credentialRequest);
            if (credentialMetadata != null && !credentialMetadata.getId().equals("")) {
                OperationStatus operationStatus = OperationStatus.newBuilder().setStatus(true).build();
                responseObserver.onNext(operationStatus);
                responseObserver.onCompleted();
                return;
            }


            SetUpTenantResponse setUpTenantResponse = iamAdminServiceClient.createAgentClient(request);


            if (setUpTenantResponse == null || setUpTenantResponse.getClientId().equals("") ||
                    setUpTenantResponse.getClientSecret().equals("")) {
                String msg = "Agent activation failed for tenant " + request.getTenantId();
                LOGGER.error(msg);
                responseObserver.onError(Status.NOT_FOUND.withDescription(msg).asRuntimeException());
                return;
            }


            AddProtocolMapperRequest addProtocolMapperRequest = AddProtocolMapperRequest
                    .newBuilder()
                    .setAddToAccessToken(true)
                    .setAddToIdToken(true)
                    .setMapperType(MapperTypes.USER_ATTRIBUTE)
                    .setClaimName(AGENT_ID)
                    .setName(AGENT_ID)
                    .setAttributeName(AGENT_ID)
                    .setTenantId(request.getTenantId())
                    .setClientId(setUpTenantResponse.getClientId())
                    .build();

            iamAdminServiceClient.addProtocolMapper(addProtocolMapperRequest);

            addProtocolMapperRequest = addProtocolMapperRequest.toBuilder()
                    .setClaimName(AGENT_PARENT_ID).setName(AGENT_PARENT_ID)
                    .setAttributeName(AGENT_PARENT_ID).build();

            iamAdminServiceClient.addProtocolMapper(addProtocolMapperRequest);

            CredentialMetadata metadata = CredentialMetadata.newBuilder().
                    setId(setUpTenantResponse.getClientId())
                    .setSecret(setUpTenantResponse.getClientSecret())
                    .setOwnerId(request.getTenantId())
                    .setType(Type.AGENT_CLIENT)
                    .build();
            org.apache.custos.credential.store.service.OperationStatus status =
                    credentialStoreServiceClient.putCredential(metadata);

            OperationStatus operationStatus = OperationStatus.newBuilder().setStatus(status.getState()).build();
            responseObserver.onNext(operationStatus);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred at enableAgents " + ex.getMessage();
            LOGGER.error(msg, ex);
            if (ex.getMessage().contains("UNAUTHENTICATED")) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(msg).asRuntimeException());
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
        }
    }


    @Override
    public void registerAndEnableAgent(RegisterUserRequest request,
                                       StreamObserver<org.apache.custos.agent.management.service.AgentRegistrationResponse> responseObserver) {
        try {

            LOGGER.debug("Request received to registerAndEnableAgent for tenant " + request.getTenantId() +
                    " for agent " + request.getUser().getId());

            UserSearchMetadata metadata = UserSearchMetadata.newBuilder().setId(request.getUser().getId()).build();


            UserSearchRequest userSearchRequest = UserSearchRequest.
                    newBuilder().setUser(metadata).
                    setTenantId(request.getTenantId())
                    .setAccessToken(request.getAccessToken()).build();

            OperationStatus status = iamAdminServiceClient.isAgentNameAvailable(userSearchRequest);


            if (status.getStatus()) {


                CredentialMetadata credentialMetadata = CredentialMetadata.newBuilder()
                        .setOwnerId(request.getTenantId())
                        .setId(request.getUser().getId())
                        .build();

                CredentialMetadata agentCredential = credentialStoreServiceClient.createAgentCredential(credentialMetadata);

                UserRepresentation representation = request.getUser();
                representation = representation.toBuilder().setPassword(agentCredential.getInternalSec()).build();


                List<UserAttribute> attributeList = request.toBuilder().getUser().getAttributesList();
                List<UserAttribute> newAtrList = new ArrayList<>();
                if (attributeList != null && !attributeList.isEmpty()) {
                    newAtrList.addAll(attributeList);

                }

                UserAttribute attribute = UserAttribute.newBuilder()
                        .setKey(CUSTOS_REALM_AGENT).addValues("true").build();
                UserAttribute agentId = UserAttribute.newBuilder()
                        .setKey(AGENT_ID).addValues(request.getUser().getId()).build();
                UserAttribute agentParentId = UserAttribute.newBuilder()
                        .setKey(AGENT_PARENT_ID).addValues(request.getClientId()).build();
                newAtrList.add(agentId);
                newAtrList.add(attribute);
                newAtrList.add(agentParentId);
                representation = representation.toBuilder().addAllAttributes(newAtrList).build();

                request = request.toBuilder().setUser(representation).setClientId(AGENT_CLIENT).build();

                RegisterUserResponse response = iamAdminServiceClient.registerAndEnableAgent(request);

                if (response.getIsRegistered()) {

                    Agent agent = Agent.newBuilder()
                            .setId(representation.getId().toLowerCase())
                            .setStatus(AgentStatus.ENABLED)
                            .build();

                    if (representation.getRealmRolesList() != null && !representation.getRealmRolesList().isEmpty()) {
                        agent = agent.toBuilder().addAllRoles(representation.getRealmRolesList()).build();

                    }

                    if (representation.getClientRolesList() != null && !representation.getClientRolesList().isEmpty()) {
                        agent = agent.toBuilder().addAllAgentClientRoles(representation.getClientRolesList()).build();
                    }

                    if (representation.getAttributesList() != null && !representation.getAttributesList().isEmpty()) {
                        List<AgentAttribute> agentAttributes = new ArrayList<>();
                        representation.getAttributesList().forEach(atr -> {
                            AgentAttribute agentAttribute = AgentAttribute
                                    .newBuilder()
                                    .setKey(atr.getKey())
                                    .addAllValue(atr.getValuesList())
                                    .build();
                            agentAttributes.add(agentAttribute);

                        });

                        agent = agent.toBuilder().addAllAttributes(agentAttributes).build();
                    }


                    AgentRequest agentRequest = AgentRequest.newBuilder()
                            .setTenantId(request.getTenantId())
                            .setAgent(agent)
                            .build();
                    agentProfileClient.createAgent(agentRequest);

                    org.apache.custos.agent.management.service.AgentRegistrationResponse registrationResponse =
                            org.apache.custos.agent.management.service.AgentRegistrationResponse
                                    .newBuilder()
                                    .setId(request.getUser().getId())
                                    .setSecret(agentCredential.getSecret())
                                    .build();

                    responseObserver.onNext(registrationResponse);
                    responseObserver.onCompleted();

                } else {
                    String msg = "Agent name not registered ";
                    LOGGER.error(msg);
                    responseObserver.onError(Status.INTERNAL.
                            withDescription(msg).asRuntimeException());
                }

            } else {
                String msg = "Agent name is not valid ";
                LOGGER.error(msg);
                responseObserver.onError(Status.ALREADY_EXISTS.
                        withDescription(msg).asRuntimeException());
            }

        } catch (Exception ex) {
            String msg = "Error occurred at registerAndEnableAgent " + ex.getMessage();
            LOGGER.error(msg, ex);
            if (ex.getMessage().contains("UNAUTHENTICATED")) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(msg).asRuntimeException());
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
        }
    }


    @Override
    public void configureAgentClient(AgentClientMetadata request, StreamObserver<OperationStatus> responseObserver) {
        try {

            LOGGER.debug("Request received to configure agent client" + request.getTenantId());

            GetTenantRequest tenantRequest = GetTenantRequest
                    .newBuilder()
                    .setTenantId(request.getTenantId()).build();

            GetTenantResponse response = tenantProfileClient.getTenant(tenantRequest);

            Tenant tenant = response.getTenant();

            if (tenant == null || tenant.getTenantId() == 0) {
                String msg = "Tenant not found  for " + request.getTenantId();
                LOGGER.error(msg);
                responseObserver.onError(Status.NOT_FOUND.withDescription(msg).asRuntimeException());
                return;
            }

            request = request.toBuilder().setClientName(AGENT_CLIENT).build();

            OperationStatus status = iamAdminServiceClient.configureAgentClient(request);

            OperationStatus operationStatus = OperationStatus.newBuilder().setStatus(status.getStatus()).build();
            responseObserver.onNext(operationStatus);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred at configureAgentClient " + ex.getMessage();
            LOGGER.error(msg, ex);
            if (ex.getMessage().contains("UNAUTHENTICATED")) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(msg).asRuntimeException());
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
        }
    }


    @Override
    public void addRolesToClient(AddRolesRequest request, StreamObserver<OperationStatus> responseObserver) {
        try {

            LOGGER.debug("Request received to add agent client roles" + request.getTenantId());

            request = request.toBuilder()
                    .setClientId(AGENT_CLIENT)
                    .setClientLevel(true)
                    .build();

            iamAdminServiceClient.addRolesToTenant(request);

            OperationStatus operationStatus = OperationStatus.newBuilder().setStatus(true).build();
            responseObserver.onNext(operationStatus);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred at addRolesToClient " + ex.getMessage();
            LOGGER.error(msg, ex);
            if (ex.getMessage().contains("UNAUTHENTICATED")) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(msg).asRuntimeException());
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
        }
    }

    @Override
    public void getAgent(AgentSearchRequest request, StreamObserver<org.apache.custos.iam.service.Agent> responseObserver) {
        try {
            LOGGER.debug("Request received to getAgent " + request.getTenantId());

            UserSearchMetadata metadata = UserSearchMetadata.newBuilder().setId(request.getId()).build();

            UserSearchRequest searchRequest = UserSearchRequest
                    .newBuilder()
                    .setUser(metadata)
                    .setTenantId(request.getTenantId())
                    .setAccessToken(request.getAccessToken())
                    .build();

            org.apache.custos.iam.service.Agent agent = iamAdminServiceClient.getAgent(searchRequest);
            List<UserAttribute> attributeList = agent.getAttributesList();
            List<UserAttribute> userAttributes = new ArrayList<>();
            for (UserAttribute attribute : attributeList) {
                if (!attribute.getKey().trim().equals(CUSTOS_REALM_AGENT)) {
                    userAttributes.add(attribute);
                }
            }
            agent = agent.toBuilder().clearAttributes().addAllAttributes(userAttributes).build();

            responseObserver.onNext(agent);
            responseObserver.onCompleted();


        } catch (Exception ex) {
            String msg = "Error occurred at getAgent " + ex.getMessage();
            LOGGER.error(msg, ex);
            if (ex.getMessage().contains("UNAUTHENTICATED")) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(msg).asRuntimeException());
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
        }
    }


    @Override
    public void deleteAgent(AgentSearchRequest request, StreamObserver<OperationStatus> responseObserver) {
        try {

            LOGGER.debug("Request received to deleteAgent " + request.getTenantId());

            UserSearchMetadata metadata = UserSearchMetadata.newBuilder().setId(request.getId()).build();

            UserSearchRequest searchRequest = UserSearchRequest
                    .newBuilder()
                    .setUser(metadata)
                    .setTenantId(request.getTenantId())
                    .setAccessToken(request.getAccessToken())
                    .build();

            OperationStatus status = iamAdminServiceClient.deleteAgent(searchRequest);

            if (status.getStatus()) {

                CredentialMetadata credentialMetadata = CredentialMetadata.newBuilder()
                        .setOwnerId(request.getTenantId())
                        .setId(request.getId())
                        .build();

                credentialStoreServiceClient.deleteAgentCredential(credentialMetadata);

                Agent agent = Agent.newBuilder().setId(request.getId().toLowerCase()).build();

                AgentRequest agentRequest = AgentRequest.newBuilder().setTenantId(request.getTenantId())
                        .setAgent(agent).build();
                agentProfileClient.deleteAgent(agentRequest);
                responseObserver.onNext(status);
                responseObserver.onCompleted();


            } else {
                String msg = "Error occurred at delete Agent at IAM Server ";
                LOGGER.error(msg);
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }

        } catch (Exception ex) {
            String msg = "Error occurred at getAgent " + ex.getMessage();
            LOGGER.error(msg, ex);
            if (ex.getMessage().contains("UNAUTHENTICATED")) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(msg).asRuntimeException());
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
        }
    }


    @Override
    public void disableAgent(AgentSearchRequest request, StreamObserver<OperationStatus> responseObserver) {
        try {

            LOGGER.debug("Request received to disableAgent " + request.getTenantId());

            UserSearchMetadata metadata = UserSearchMetadata.newBuilder().setId(request.getId()).build();

            UserSearchRequest searchRequest = UserSearchRequest
                    .newBuilder()
                    .setUser(metadata)
                    .setTenantId(request.getTenantId())
                    .setAccessToken(request.getAccessToken())
                    .build();

            OperationStatus status = iamAdminServiceClient.disableAgent(searchRequest);

            if (status.getStatus()) {

                org.apache.custos.iam.service.Agent agent = iamAdminServiceClient.getAgent(searchRequest);


                Agent agentProfile = getAgentProfile(agent);

                AgentRequest agentRequest = AgentRequest.newBuilder().setTenantId(request.getTenantId())
                        .setAgent(agentProfile).build();
                agentProfileClient.updateAgent(agentRequest);
                responseObserver.onNext(status);
                responseObserver.onCompleted();

            } else {
                String msg = "Error occurred at disable Agent at IAM Server ";
                LOGGER.error(msg);
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }

        } catch (Exception ex) {
            String msg = "Error occurred at disableAgent " + ex.getMessage();
            LOGGER.error(msg, ex);
            if (ex.getMessage().contains("UNAUTHENTICATED")) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(msg).asRuntimeException());
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
        }
    }


    @Override
    public void addAgentAttributes(AddUserAttributesRequest request, StreamObserver<OperationStatus> responseObserver) {
        try {

            LOGGER.debug("Request received to addAgentAttributes " + request.getTenantId());

            OperationStatus status = iamAdminServiceClient.addAgentAttributes(request);

            if (status.getStatus()) {

                for (String agent : request.getAgentsList()) {

                    UserSearchMetadata metadata = UserSearchMetadata.newBuilder().setId(agent).build();

                    UserSearchRequest searchRequest = UserSearchRequest
                            .newBuilder()
                            .setUser(metadata)
                            .setTenantId(request.getTenantId())
                            .setAccessToken(request.getAccessToken())
                            .build();

                    org.apache.custos.iam.service.Agent iamAgent = iamAdminServiceClient.getAgent(searchRequest);

                    Agent agentProfile = getAgentProfile(iamAgent);
                    AgentRequest agentRequest = AgentRequest.newBuilder().setTenantId(request.getTenantId())
                            .setAgent(agentProfile).build();
                    agentProfileClient.updateAgent(agentRequest);
                }
                responseObserver.onNext(status);
                responseObserver.onCompleted();

            } else {
                String msg = "Error occurred at addAgentAttributes at IAM Server ";
                LOGGER.error(msg);
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }

        } catch (Exception ex) {
            String msg = "Error occurred at addAgentAttributes " + ex.getMessage();
            LOGGER.error(msg, ex);
            if (ex.getMessage().contains("UNAUTHENTICATED")) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(msg).asRuntimeException());
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
        }

    }


    @Override
    public void deleteAgentAttributes(DeleteUserAttributeRequest request, StreamObserver<OperationStatus> responseObserver) {
        try {
            LOGGER.debug("Request received to deleteAgentAttributes " + request.getTenantId());

            OperationStatus status = iamAdminServiceClient.deleteAgentAttributes(request);

            if (status.getStatus()) {

                for (String agent : request.getAgentsList()) {

                    UserSearchMetadata metadata = UserSearchMetadata.newBuilder().setId(agent).build();

                    UserSearchRequest searchRequest = UserSearchRequest
                            .newBuilder()
                            .setUser(metadata)
                            .setTenantId(request.getTenantId())
                            .setAccessToken(request.getAccessToken())
                            .build();

                    org.apache.custos.iam.service.Agent iamAgent = iamAdminServiceClient.getAgent(searchRequest);
                    Agent agentProfile = getAgentProfile(iamAgent);

                    AgentRequest agentRequest = AgentRequest.newBuilder().setTenantId(request.getTenantId())
                            .setAgent(agentProfile).build();
                    agentProfileClient.updateAgent(agentRequest);
                }
                responseObserver.onNext(status);
                responseObserver.onCompleted();

            } else {
                String msg = "Error occurred at deleteAgentAttributes at IAM Server ";
                LOGGER.error(msg);
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }

        } catch (Exception ex) {
            String msg = "Error occurred at deleteAgentAttributes " + ex.getMessage();
            LOGGER.error(msg, ex);
            if (ex.getMessage().contains("UNAUTHENTICATED")) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(msg).asRuntimeException());
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
        }
    }


    @Override
    public void addRolesToAgent(AddUserRolesRequest request, StreamObserver<OperationStatus> responseObserver) {
        try {
            LOGGER.debug("Request received to deleteAgentAttributes " + request.getTenantId());
            request = request.toBuilder().setClientId(AGENT_CLIENT).build();

            OperationStatus status = iamAdminServiceClient.addRolesToAgent(request);

            if (status.getStatus()) {

                for (String agent : request.getAgentsList()) {

                    UserSearchMetadata metadata = UserSearchMetadata.newBuilder().setId(agent).build();

                    UserSearchRequest searchRequest = UserSearchRequest
                            .newBuilder()
                            .setUser(metadata)
                            .setTenantId(request.getTenantId())
                            .setAccessToken(request.getAccessToken())
                            .build();

                    org.apache.custos.iam.service.Agent iamAgent = iamAdminServiceClient.getAgent(searchRequest);

                    Agent agentProfile = getAgentProfile(iamAgent);

                    AgentRequest agentRequest = AgentRequest.newBuilder().setTenantId(request.getTenantId())
                            .setAgent(agentProfile).build();
                    agentProfileClient.updateAgent(agentRequest);
                }
                responseObserver.onNext(status);
                responseObserver.onCompleted();

            } else {
                String msg = "Error occurred at addRolesToAgent at IAM Server ";
                LOGGER.error(msg);
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }

        } catch (Exception ex) {
            String msg = "Error occurred at addRolesToAgent " + ex.getMessage();
            LOGGER.error(msg, ex);
            if (ex.getMessage().contains("UNAUTHENTICATED")) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(msg).asRuntimeException());
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
        }
    }

    @Override
    public void deleteRolesFromAgent(DeleteUserRolesRequest request, StreamObserver<OperationStatus> responseObserver) {
        try {
            LOGGER.debug("Request received to deleteAgentAttributes " + request.getTenantId());

            request = request.toBuilder().setClientId(AGENT_CLIENT).build();


            OperationStatus status = iamAdminServiceClient.deleteAgentRoles(request);

            if (status.getStatus()) {


                UserSearchMetadata metadata = UserSearchMetadata.newBuilder().setId(request.getId()).build();

                UserSearchRequest searchRequest = UserSearchRequest
                        .newBuilder()
                        .setUser(metadata)
                        .setTenantId(request.getTenantId())
                        .setAccessToken(request.getAccessToken())
                        .build();

                org.apache.custos.iam.service.Agent iamAgent = iamAdminServiceClient.getAgent(searchRequest);

                Agent agentProfile = getAgentProfile(iamAgent);

                AgentRequest agentRequest = AgentRequest.newBuilder().setTenantId(request.getTenantId())
                        .setAgent(agentProfile).build();
                agentProfileClient.updateAgent(agentRequest);
                responseObserver.onNext(status);
                responseObserver.onCompleted();

            } else {
                String msg = "Error occurred at deleteRolesFromAgent at IAM Server ";
                LOGGER.error(msg);
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }

        } catch (Exception ex) {
            String msg = "Error occurred at deleteRolesFromAgent " + ex.getMessage();
            LOGGER.error(msg, ex);
            if (ex.getMessage().contains("UNAUTHENTICATED")) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(msg).asRuntimeException());
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
        }
    }

    @Override
    public void enableAgent(AgentSearchRequest request, StreamObserver<OperationStatus> responseObserver) {
        try {
            LOGGER.debug("Request received to enableAgent " + request.getTenantId());

            UserSearchMetadata metadata = UserSearchMetadata.newBuilder().setId(request.getId()).build();

            UserSearchRequest searchRequest = UserSearchRequest
                    .newBuilder()
                    .setUser(metadata)
                    .setTenantId(request.getTenantId())
                    .setAccessToken(request.getAccessToken())
                    .build();

            OperationStatus status = iamAdminServiceClient.enableAgent(searchRequest);

            if (status.getStatus()) {

                org.apache.custos.iam.service.Agent agent = iamAdminServiceClient.getAgent(searchRequest);


                Agent agentProfile = getAgentProfile(agent);

                AgentRequest agentRequest = AgentRequest.newBuilder().setTenantId(request.getTenantId())
                        .setAgent(agentProfile).build();
                agentProfileClient.updateAgent(agentRequest);
                responseObserver.onNext(status);
                responseObserver.onCompleted();

            } else {
                String msg = "Error occurred at disable Agent at IAM Server ";
                LOGGER.error(msg);
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }

        } catch (Exception ex) {
            String msg = "Error occurred at disableAgent " + ex.getMessage();
            LOGGER.error(msg, ex);
            if (ex.getMessage().contains("UNAUTHENTICATED")) {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription(msg).asRuntimeException());
            } else {
                responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
            }
        }
    }


    @Override
    public void addProtocolMapper(AddProtocolMapperRequest request, StreamObserver<OperationStatus> responseObserver) {
        try {
            OperationStatus allRoles = iamAdminServiceClient.addProtocolMapper(request);

            responseObserver.onNext(allRoles);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred at addProtocolMapper " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }


    @Override
    public void getAllAgents(GetAllResources request, StreamObserver<GetAllResourcesResponse> responseObserver) {
        try {
            GetAllResources resources = request.toBuilder().setResourceType(ResourceTypes.AGENT).build();
            GetAllResourcesResponse allRoles = iamAdminServiceClient.getAllResources(resources);

            responseObserver.onNext(allRoles);
            responseObserver.onCompleted();

        } catch (Exception ex) {
            String msg = "Error occurred at getAllAgents in tenant " + request.getTenantId() + "reason :" + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    @Override
    public void synchronizeAgentDBs(SynchronizeAgentDBRequest request, StreamObserver<OperationStatus> responseObserver) {
        try {
            Context ctx = Context.current().fork();
            ctx.run(() -> {
                GetAllResources resources = GetAllResources
                        .newBuilder()
                        .setClientId(request.getClientId())
                        .setTenantId(request.getTenantId())
                        .setResourceType(ResourceTypes.AGENT)
                        .build();
                GetAllResourcesResponse response = iamAdminServiceClient.getAllResources(resources);

                if (response != null && response.getAgentsList() != null && !response.getAgentsList().isEmpty()) {

                    for (org.apache.custos.iam.service.Agent agent : response.getAgentsList()) {

                        Agent profile = getAgentProfile(agent);

                        AgentRequest agentRequest = AgentRequest.newBuilder().setTenantId(request.getTenantId())
                                .setAgent(profile).build();
                        Agent exAgent = agentProfileClient.getAgent(agentRequest);


                        if (exAgent != null && exAgent.getId() != null && !exAgent.getId().equals("")) {
                            agentProfileClient.updateAgent(agentRequest);
                        } else {
                            agentProfileClient.createAgent(agentRequest);
                        }
                    }
                }

                OperationStatus status = OperationStatus.newBuilder().setStatus(true).build();

                responseObserver.onNext(status);
                responseObserver.onCompleted();
            });

        } catch (Exception ex) {
            String msg = "Error occurred at synchronizeAgentDBs " + ex.getMessage();
            LOGGER.error(msg, ex);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
        }
    }

    private Agent getAgentProfile(org.apache.custos.iam.service.Agent iamAgent) {
        Agent.Builder agentProfile = Agent.newBuilder()
                .setId(iamAgent.getId().toLowerCase())
                .setStatus(iamAgent.getIsEnabled() ? AgentStatus.ENABLED : AgentStatus.DISABLED);

        if (!iamAgent.getRealmRolesList().isEmpty()) {
            agentProfile = agentProfile.addAllRoles(iamAgent.getRealmRolesList());
        }

        if (!iamAgent.getClientRolesList().isEmpty()) {
            agentProfile = agentProfile.addAllAgentClientRoles(iamAgent.getClientRolesList());
        }


        for (UserAttribute atr : iamAgent.getAttributesList()) {

            AgentAttribute agentAttribute = AgentAttribute.newBuilder()
                    .setKey(atr.getKey())
                    .addAllValue(atr.getValuesList())
                    .build();

            agentProfile = agentProfile.addAttributes(agentAttribute);

        }
        return agentProfile.build();

    }


}
