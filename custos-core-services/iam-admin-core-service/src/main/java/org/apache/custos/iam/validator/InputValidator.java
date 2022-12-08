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

package org.apache.custos.iam.validator;


import org.apache.custos.core.services.commons.Validator;
import org.apache.custos.iam.exceptions.MissingParameterException;
import org.apache.custos.iam.service.*;

import java.util.List;

/**
 * This class validates the  requests
 */
public class InputValidator implements Validator {

    /**
     * Input parameter validater
     *
     * @param methodName
     * @param obj
     * @return
     */
    public void validate(String methodName, Object obj) {

        switch (methodName) {
            case "setUPTenant":
                validateSetUPTenant(obj);
                break;
            case "registerUser":
                validateRegisterUser(obj);
                break;
            case "registerAndEnableUsers":
                validateRegisterUsers(obj);
                break;
            case "enableUser":
            case "isUserEnabled":
            case "isUserExist":
            case "getUser":
            case "deleteUser":
            case "isUsernameAvailable":
            case "grantAdminPrivilege":
                validateUserAccess(obj);
                break;
            case "resetPassword":
                validateResetPassword(obj);
                break;
            case "findUsers":
                validateFindUsers(obj);
                break;
            case "updateUserProfile":
                validateUpdateUserProfile(obj);
                break;
            case "addRoleToUser":
            case "deleteRolesFromUser":
                validateDeleteRolesFromUser(obj);
                break;
            case "configureFederatedIDP":
                validateConfigureFederatedIDP(obj);
                break;
            case "addRolesToTenant":
                validateAddRoleToTenant(obj);
                break;
            case "addProtocolMapper":
                validateAddProtocolMapper(obj);
                break;
            case "addUserAttributes":
                validateAddUserAttributes(obj);
                break;

            case "createGroups":
                validateCreateGroups(obj);
                break;

            case "updateGroup":
                validateUpdateGroup(obj);
                break;
            case "deleteGroup":
                validateDeleteGroup(obj);
                break;

            case "findGroup":
                validateFindGroup(obj);
                break;

            case "getAllGroups":
                validateGetAllGroups(obj);
                break;

            case "validateUserGroupMapping":
                validateUserGroupMapping(obj);
                break;

            case "createAgentClient":
                validateCreateAgentClient(obj);
                break;
            case "configureAgentClient":
                configureAgentClient(obj);
                break;
            case "isAgentNameAvailable":
            case "deleteAgent":
            case "disableAgent":
            case "enableAgent":
                validateUserSearchRequestForAgent(obj);
                break;
            case "registerAndEnableAgent":
                validateRegisterAndEnableAgent(obj);
                break;
            case "addAgentAttributes":
                validateAddAgentAttributes(obj);
                break;
            case "deleteAgentAttributes":
                validateDeleteAgentAttributes(obj);
                break;
            case "addRolesToAgent":
                validateAddRolesToAgent(obj);
                break;
            case "deleteRolesFromAgent":
                validateDeleteRolesFromAgent(obj);
                break;
            case "getAllResources":
                validateGetAllResources(obj);
                break;
            case "deleteExternalIDPLinksOfUsers":
                validateDeleteExternalIDPsLinks(obj);
                break;

            default:

        }

    }

    private boolean validateSetUPTenant(Object obj) {
        if (obj instanceof SetUpTenantRequest) {
            SetUpTenantRequest request = (SetUpTenantRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getAdminFirstname() == null || request.getAdminFirstname().trim().equals("")) {
                throw new MissingParameterException("Admin firstname should not be null", null);
            }
            if (request.getAdminLastname() == null || request.getAdminLastname().trim().equals("")) {
                throw new MissingParameterException("Admin lastname should not be null", null);
            }
            if (request.getAdminEmail() == null || request.getAdminEmail().trim().equals("")) {
                throw new MissingParameterException("Admin email should not be null", null);
            }
            if (request.getAdminPassword() == null || request.getAdminPassword().trim().equals("")) {
                throw new MissingParameterException("Admin password should not be null", null);
            }
            if (request.getAdminUsername() == null || request.getAdminUsername().trim().equals("")) {
                throw new MissingParameterException("Admin username should not be null", null);
            }
            if (request.getTenantName() == null || request.getTenantName().trim().equals("")) {
                throw new MissingParameterException("Tenant name should not be null", null);
            }
            if (request.getTenantURL() == null || request.getTenantURL().trim().equals("")) {
                throw new MissingParameterException("Tenant URL should not be null", null);
            }
            if (request.getRequesterEmail() == null || request.getRequesterEmail().trim().equals("")) {
                throw new MissingParameterException("Tenant requester email should not be null", null);
            }


        } else {
            throw new RuntimeException("Unexpected input type for method setUPTenant");
        }
        return true;
    }


    private boolean validateRegisterUser(Object obj) {
        if (obj instanceof RegisterUserRequest) {
            RegisterUserRequest request = (RegisterUserRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getAccessToken() == null || request.getAccessToken().trim().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }

            if (request.getUser() == null || request.getUser().getUsername() == null ||
                    request.getUser().getUsername().trim().equals("")) {
                throw new MissingParameterException("Username should not be null", null);
            }

            if (request.getUser() == null || request.getUser().getFirstName() == null ||
                    request.getUser().getFirstName().trim().equals("")) {
                throw new MissingParameterException("Firstname should not be null", null);
            }
            if (request.getUser() == null || request.getUser().getLastName() == null ||
                    request.getUser().getLastName().trim().equals("")) {
                throw new MissingParameterException("Lastname should not be null", null);
            }
            if (request.getUser() == null || request.getUser().getEmail() == null ||
                    request.getUser().getEmail().trim().equals("")) {
                throw new MissingParameterException("Email should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method registerUser");
        }
        return true;
    }

    private boolean validateRegisterUsers(Object obj) {
        if (obj instanceof RegisterUsersRequest) {
            RegisterUsersRequest usersRequest = (RegisterUsersRequest) obj;

            if (usersRequest.getUsersList().isEmpty()) {
                throw new MissingParameterException("You need to have at least one User", null);
            }

            if (usersRequest.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (usersRequest.getAccessToken() == null) {
                throw new MissingParameterException("Access Token  should not be null", null);
            }

            if (usersRequest.getClientId() == null) {
                throw new MissingParameterException("Client Id  should not be null", null);
            }

            List<UserRepresentation> userRepresentationList = usersRequest.getUsersList();

            for (UserRepresentation user : userRepresentationList) {
                if (user.getUsername() == null ||
                        user.getUsername().trim().equals("")) {
                    throw new MissingParameterException("Username should not be null", null);
                }

                if (user.getFirstName() == null ||
                        user.getFirstName().trim().equals("")) {
                    throw new MissingParameterException("Firstname should not be null", null);
                }
                if (user.getLastName() == null ||
                        user.getLastName().trim().equals("")) {
                    throw new MissingParameterException("Lastname should not be null", null);
                }
                if (user.getEmail() == null ||
                        user.getEmail().trim().equals("")) {
                    throw new MissingParameterException("Email should not be null", null);
                }

            }

        } else {
            throw new RuntimeException("Unexpected input type for method registerUsers");
        }
        return true;
    }

    private boolean validateUserAccess(Object obj) {
        if (obj instanceof UserSearchRequest) {
            UserSearchRequest request = (UserSearchRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getAccessToken() == null || request.getAccessToken().trim().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }

            if (request.getUser().getUsername() == null || request.getUser().getUsername().trim().equals("")) {
                throw new MissingParameterException("Username should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method userAccess");
        }
        return true;
    }


    private boolean validateResetPassword(Object obj) {
        if (obj instanceof ResetUserPassword) {
            ResetUserPassword request = (ResetUserPassword) obj;

            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getAccessToken() == null || request.getAccessToken().trim().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }

            if (request.getUsername() == null || request.getUsername().trim().equals("")) {
                throw new MissingParameterException("Username should not be null", null);
            }

            if (request.getPassword() == null || request.getPassword().trim().equals("")) {
                throw new MissingParameterException("Password should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method resetPassword");
        }
        return true;
    }

    private boolean validateFindUsers(Object obj) {
        if (obj instanceof FindUsersRequest) {
            FindUsersRequest request = (FindUsersRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getAccessToken() == null || request.getAccessToken().trim().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }

            if (request.getUser() == null) {
                throw new MissingParameterException("Atleast one user search string is required", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method findUsers");
        }
        return true;
    }

    private boolean validateUpdateUserProfile(Object obj) {
        if (obj instanceof UpdateUserProfileRequest) {
            UpdateUserProfileRequest re = (UpdateUserProfileRequest) obj;
            UserRepresentation request = re.getUser();
            if (re.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (re.getAccessToken() == null || re.getAccessToken().trim().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }

            if (request.getUsername() == null || request.getUsername().trim().equals("")) {
                throw new MissingParameterException("Username should not be null", null);
            }

            if (request.getFirstName() == null || request.getFirstName().trim().equals("")) {
                throw new MissingParameterException("Firstname should not be null", null);
            }
            if (request.getLastName() == null || request.getLastName().trim().equals("")) {
                throw new MissingParameterException("Lastname should not be null", null);
            }
            if (request.getEmail() == null || request.getEmail().trim().equals("")) {
                throw new MissingParameterException("Email should not be null", null);
            }


        } else {
            throw new RuntimeException("Unexpected input type for method updateUserProfile");
        }
        return true;
    }


    private boolean validateDeleteRolesFromUser(Object obj) {
        if (obj instanceof DeleteUserRolesRequest) {
            DeleteUserRolesRequest request = (DeleteUserRolesRequest) obj;

            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getUsername() == null || request.getUsername().trim().equals("")) {
                throw new MissingParameterException("Username should not be null", null);
            }

            if (request.getClientId() == null || request.getClientId().trim().equals("")) {
                throw new MissingParameterException("Client Id should not be null", null);
            }

            if (request.getAccessToken() == null || request.getAccessToken().trim().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }

            if (request.getClientRolesList().isEmpty() && request.getRolesList().isEmpty()) {
                throw new MissingParameterException("At least client roles or realm roles should not be null", null);
            }

            if (request.getPerformedBy() == null || request.getPerformedBy().equals("")) {
                throw new MissingParameterException("Performed By should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method roleOperationsRequest");
        }
        return true;
    }

    private boolean validateConfigureFederatedIDP(Object obj) {
        if (obj instanceof ConfigureFederateIDPRequest) {
            ConfigureFederateIDPRequest request = (ConfigureFederateIDPRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getRequesterEmail() == null || request.getRequesterEmail().trim().equals("")) {
                throw new MissingParameterException("Requester email should not be null", null);
            }

            if (request.getClientID() == null || request.getClientID().trim().equals("")) {
                throw new MissingParameterException("Client Id should not be null", null);
            }

            if (request.getClientSec() == null || request.getClientSec().trim().equals("")) {
                throw new MissingParameterException("Client secret should not be null", null);
            }
            if (request.getType() == null) {
                throw new MissingParameterException("Type should not be null", null);
            }


        } else {
            throw new RuntimeException("Unexpected input type for method configureFederatedIDP");
        }
        return true;
    }

    private boolean validateAddRoleToTenant(Object obj) {
        if (obj instanceof AddRolesRequest) {
            AddRolesRequest request = (AddRolesRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getClientId() == null || request.getClientId().trim().equals("")) {
                throw new MissingParameterException("Client Id should not be null", null);
            }

            if (request.getRolesCount() == 0) {
                throw new MissingParameterException("There should be at least one role", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method validateAddRoleToTenant");
        }
        return true;
    }


    private boolean validateAddProtocolMapper(Object obj) {
        if (obj instanceof AddProtocolMapperRequest) {
            AddProtocolMapperRequest request = (AddProtocolMapperRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getClientId() == null || request.getClientId().trim().equals("")) {
                throw new MissingParameterException("Client Id should not be null", null);
            }

            if (request.getClaimName() == null || request.getClaimName().trim().equals("")) {
                throw new MissingParameterException("Claim name should not be null", null);
            }
            if (request.getMapperType() == null) {
                throw new MissingParameterException("Mapper Type should not be null", null);
            }

            if (request.getMapperType().equals(MapperTypes.USER_ATTRIBUTE)) {
                if (request.getAttributeName() == null || request.getAttributeName().trim().equals("")) {
                    throw new MissingParameterException("Attribute name should not be null", null);
                }
            }


            if (request.getName() == null || request.getName().trim().equals("")) {
                throw new MissingParameterException("Name should not be null", null);
            }


            if (request.getClaimType() == null) {
                throw new MissingParameterException("Claim Type should not be null", null);
            }


        } else {
            throw new RuntimeException("Unexpected input type for method validateAddRoleToTenant");
        }
        return true;
    }

    private boolean validateAddUserAttributes(Object obj) {
        if (obj instanceof AddUserAttributesRequest) {
            AddUserAttributesRequest request = (AddUserAttributesRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getClientId() == null || request.getClientId().trim().equals("")) {
                throw new MissingParameterException("Client Id should not be null", null);
            }

            if (request.getAttributesList().isEmpty()) {
                throw new MissingParameterException("Attributes should not be null", null);
            }

            if (request.getUsersList().isEmpty()) {
                throw new MissingParameterException("Users should not be null", null);
            }


            for (UserAttribute attribute : request.getAttributesList()) {
                if (attribute.getKey() == null || attribute.getKey().equals("")) {
                    throw new MissingParameterException("Attribute Key should not be null", null);
                }

                if (attribute.getValuesList().isEmpty()) {
                    throw new MissingParameterException("Attribute value should not be null", null);
                }
            }

        } else {
            throw new RuntimeException("Unexpected input type for method validateAddRoleToTenant");
        }
        return true;
    }


    private boolean validateCreateGroups(Object obj) {
        if (obj instanceof GroupsRequest) {
            GroupsRequest request = (GroupsRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getClientId() == null || request.getClientId().trim().equals("")) {
                throw new MissingParameterException("Client Id should not be null", null);
            }

            if (request.getGroupsList().isEmpty()) {
                throw new MissingParameterException("There should be at least one group representation", null);
            }

            for (GroupRepresentation groupRepresentation : request.getGroupsList()) {
                if (groupRepresentation.getName() == null || groupRepresentation.getName().trim().equals("")) {
                    throw new MissingParameterException("Name should not be null", null);
                }
            }

        } else {
            throw new RuntimeException("Unexpected input type for method validateCreateGroups");
        }
        return true;

    }


    private boolean validateUpdateGroup(Object obj) {
        if (obj instanceof GroupRequest) {
            GroupRequest request = (GroupRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getClientId() == null || request.getClientId().trim().equals("")) {
                throw new MissingParameterException("Client Id should not be null", null);
            }

            if (request.getGroup() == null || request.getGroup().getId() == null ||
                    request.getGroup().getName().trim().equals("")) {
                throw new MissingParameterException("Group id and name should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method validateAddRoleToTenant");
        }
        return true;
    }


    private boolean validateDeleteGroup(Object obj) {
        if (obj instanceof GroupRequest) {
            GroupRequest request = (GroupRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getClientId() == null || request.getClientId().trim().equals("")) {
                throw new MissingParameterException("Client Id should not be null", null);
            }

            if (request.getGroup() == null || request.getGroup().getId() == null) {
                throw new MissingParameterException("Group id should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method validateAddRoleToTenant");
        }
        return true;
    }


    private boolean validateFindGroup(Object obj) {
        if (obj instanceof GroupRequest) {
            GroupRequest request = (GroupRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getClientId() == null || request.getClientId().trim().equals("")) {
                throw new MissingParameterException("Client Id should not be null", null);
            }

            if (request.getGroup() == null || !(request.getGroup().getId().trim().equals("") ||
                    request.getGroup().getName().trim().equals(""))) {
                throw new MissingParameterException("Group id or name should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method validateAddRoleToTenant");
        }
        return true;

    }


    private boolean validateGetAllGroups(Object obj) {
        if (obj instanceof GroupRequest) {
            GroupRequest request = (GroupRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method validateAddRoleToTenant");
        }
        return true;

    }

    private boolean validateUserGroupMapping(Object obj) {
        if (obj instanceof UserGroupMappingRequest) {
            UserGroupMappingRequest request = (UserGroupMappingRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getAccessToken() == null || request.getAccessToken().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }
            if (request.getGroupId() == null || request.getGroupId().equals("")) {
                throw new MissingParameterException("Group Id should not be null", null);
            }
            if (request.getUsername() == null || request.getUsername().equals("")) {
                throw new MissingParameterException("Username should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method validateUserGroupMapping");
        }
        return true;

    }

    private boolean validateCreateAgentClient(Object obj) {
        if (obj instanceof AgentClientMetadata) {
            AgentClientMetadata request = (AgentClientMetadata) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }
            if (request.getAccessToken() == null || request.getAccessToken().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }
            if (request.getRedirectURIsCount() == 0) {
                throw new MissingParameterException("Atleast one redirect URI should be there", null);
            }
            if (request.getClientName() == null || request.getClientName().equals("")) {
                throw new MissingParameterException("Client name should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method validateUserGroupMapping");
        }
        return true;
    }

    private boolean configureAgentClient(Object obj) {
        if (obj instanceof AgentClientMetadata) {
            AgentClientMetadata request = (AgentClientMetadata) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getClientName() == null || request.getClientName().equals("")) {
                throw new MissingParameterException("Client name should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method validateUserGroupMapping");
        }
        return true;
    }


    private boolean validateUserSearchRequestForAgent(Object obj) {
        if (obj instanceof UserSearchRequest) {
            UserSearchRequest request = (UserSearchRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getAccessToken() == null || request.getAccessToken().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }
            if (request.getUser() == null) {
                throw new MissingParameterException("Agent  should not be null", null);
            }
            if (request.getUser().getId() == null || request.getUser().getId().equals("")) {
                throw new MissingParameterException("Agent Id should not be null", null);
            }
        } else {
            throw new RuntimeException("Unexpected input type for request UserSearchRequest");
        }
        return true;
    }

    private boolean validateRegisterAndEnableAgent(Object obj) {
        if (obj instanceof RegisterUserRequest) {
            RegisterUserRequest request = (RegisterUserRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }
            if (request.getAccessToken() == null || request.getAccessToken().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }
            if (request.getUser() == null) {
                throw new MissingParameterException(" Agent should not be null", null);
            }
            if (request.getUser().getId() == null || request.getUser().getId().equals("")) {
                throw new MissingParameterException("Agent Id should not be null", null);
            }

            if (request.getUser().getPassword() == null || request.getUser().getPassword().equals("")) {
                throw new MissingParameterException("Agent password should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method validateRegisterAndEnableAgent");
        }
        return true;
    }


    private boolean validateDeleteAgentAttributes(Object obj) {
        if (obj instanceof DeleteUserAttributeRequest) {
            DeleteUserAttributeRequest request = (DeleteUserAttributeRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getAccessToken() == null || request.getAccessToken().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }
            if (request.getAttributesList().isEmpty()) {
                throw new MissingParameterException("Attributes should not be null", null);
            }

            if (request.getAgentsList().isEmpty()) {
                throw new MissingParameterException("Agents should not be null", null);
            }


            for (UserAttribute attribute : request.getAttributesList()) {
                if (attribute.getKey() == null || attribute.getKey().equals("")) {
                    throw new MissingParameterException("Attribute Key should not be null", null);
                }

                if (attribute.getValuesList().isEmpty()) {
                    throw new MissingParameterException("Attribute value should not be null", null);
                }
            }
        } else {
            throw new RuntimeException("Unexpected input type for method validateUserGroupMapping");
        }
        return true;
    }

    private boolean validateAddAgentAttributes(Object obj) {
        if (obj instanceof AddUserAttributesRequest) {
            AddUserAttributesRequest request = (AddUserAttributesRequest) obj;
            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getAccessToken() == null || request.getAccessToken().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }
            if (request.getAttributesList().isEmpty()) {
                throw new MissingParameterException("Attributes should not be null", null);
            }

            if (request.getAgentsList().isEmpty()) {
                throw new MissingParameterException("Agents should not be null", null);
            }


            for (UserAttribute attribute : request.getAttributesList()) {
                if (attribute.getKey() == null || attribute.getKey().equals("")) {
                    throw new MissingParameterException("Attribute Key should not be null", null);
                }

                if (attribute.getValuesList().isEmpty()) {
                    throw new MissingParameterException("Attribute value should not be null", null);
                }
            }

        } else {
            throw new RuntimeException("Unexpected input type for method validateUserGroupMapping");
        }
        return true;
    }


    private boolean validateAddRolesToAgent(Object obj) {
        if (obj instanceof AddUserRolesRequest) {
            AddUserRolesRequest request = (AddUserRolesRequest) obj;

            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getAgentsCount() == 0) {
                throw new MissingParameterException("At least one agent id should present", null);
            }


            if (request.getAccessToken() == null || request.getAccessToken().trim().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }

            if (request.getRolesList().isEmpty() && request.getRolesList().isEmpty()) {
                throw new MissingParameterException("At least client roles or realm roles should not be null", null);
            }

            if (request.getPerformedBy() == null || request.getPerformedBy().equals("")) {
                throw new MissingParameterException("Performed By should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method roleOperationsRequest");
        }
        return true;

    }

    private boolean validateDeleteRolesFromAgent(Object obj) {
        if (obj instanceof DeleteUserRolesRequest) {
            DeleteUserRolesRequest request = (DeleteUserRolesRequest) obj;

            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getId() == null || request.getId().trim().equals("")) {
                throw new MissingParameterException("Agent Id should not be null", null);
            }


            if (request.getAccessToken() == null || request.getAccessToken().trim().equals("")) {
                throw new MissingParameterException("Access token should not be null", null);
            }

            if (request.getClientRolesList().isEmpty() && request.getRolesList().isEmpty()) {
                throw new MissingParameterException("At least client roles or realm roles should not be null", null);
            }

            if (request.getPerformedBy() == null || request.getPerformedBy().equals("")) {
                throw new MissingParameterException("Performed By should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method roleOperationsRequest");
        }
        return true;
    }


    private boolean validateGetAllResources(Object obj) {
        if (obj instanceof GetAllResources) {
            GetAllResources request = (GetAllResources) obj;

            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

            if (request.getClientId() == null || request.getClientId().trim().equals("")) {
                throw new MissingParameterException("Client Id should not be null", null);
            }

            if (request.getResourceType() == null) {
                throw new MissingParameterException("Resource  type should not be null", null);
            }


        } else {
            throw new RuntimeException("Unexpected input type for method getAllResources");
        }
        return true;
    }

    private boolean validateDeleteExternalIDPsLinks(Object obj) {
        if (obj instanceof DeleteExternalIDPsRequest) {
            DeleteExternalIDPsRequest request = (DeleteExternalIDPsRequest) obj;

            if (request.getTenantId() == 0) {
                throw new MissingParameterException("Tenant Id should not be null", null);
            }

        } else {
            throw new RuntimeException("Unexpected input type for method deleteExternalIDPLinks");
        }
        return true;
    }
}
