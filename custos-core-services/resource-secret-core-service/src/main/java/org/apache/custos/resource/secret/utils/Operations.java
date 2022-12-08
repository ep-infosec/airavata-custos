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

package org.apache.custos.resource.secret.utils;

public enum Operations {

    ADD_SSH_CREDENTIAL,
    ADD_CERTIFICATE_CREDENTIAL,
    ADD_PASSWORD_CREDENTIAL,
    DELETE_SSH_CREDENTIAL,
    DELETE_CERTIFICATE_CREDENTIAL,
    DELETE_PASSWORD_CREDENTIAL,
    SAVE_KV_CREDENTIAL,
    UPDATE_KV_CREDENTIAL,
    DELETE_KV_CREDENTIAL,
    SAVE_CREDENTIAL_MAP,
    UPDATE_CREDENTIAL_MAP,
    DELETE_CREDENTIAL_MAP

}
