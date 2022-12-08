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

package org.apache.custos.integration.services.commons.utils;

import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import org.apache.custos.messaging.client.MessagingClient;
import org.apache.custos.messaging.service.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class EventPublisher {

    @Autowired
    private MessagingClient messagingClient;


    public void publishMessage(String clientId, long tenantId, String serviceName, String eventType, Map<String, String> properties) {
        Message message = Message
                .newBuilder()
                .setMessageId(UUID.randomUUID().toString())
                .setClientId(clientId)
                .setTenantId(tenantId)
                .setServiceName(serviceName)
                .setEventType(eventType)
                .putAllProperties(properties)
                .build();
        Context ctx = Context.current().fork();
        ctx.run(()->{
            messagingClient.publishAsync(message, new OutputStreamObserver());
        });
    }

}
