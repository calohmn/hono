/**
 * Copyright (c) 2016 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bosch Software Innovations GmbH - initial creation
 */

package org.eclipse.hono.server;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonQoS;
import io.vertx.proton.ProtonSender;
import io.vertx.proton.ProtonSession;

/**
 * A default {@code SenderFactory} for creating {@code ProtonSender} from a given connection.
 */
@Component
public class SenderFactoryImpl implements SenderFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SenderFactoryImpl.class);

    @Override
    public void createSender(
            final ProtonConnection connection,
            final String address,
            final ProtonQoS qos,
            final Handler<ProtonSender> sendQueueDrainHandler,
            final Future<ProtonSender> result) {

        Objects.requireNonNull(connection);
        Objects.requireNonNull(address);
        Objects.requireNonNull(result);

        if (connection.isDisconnected()) {
            result.fail("connection is disconnected");
        } else {
            newSession(connection, remoteOpen -> {
                if (remoteOpen.succeeded()) {
                    newSender(connection, remoteOpen.result(), address, qos, sendQueueDrainHandler, result);
                } else {
                    result.fail(remoteOpen.cause());
                }
            });
        }
    }

    private void newSession(final ProtonConnection con, final Handler<AsyncResult<ProtonSession>> sessionOpenHandler) {
        con.createSession().openHandler(sessionOpenHandler).open();
    }

    private void newSender(
            final ProtonConnection connection,
            final ProtonSession session,
            final String address,
            final ProtonQoS qos,
            final Handler<ProtonSender> sendQueueDrainHandler,
            final Future<ProtonSender> result) {

        ProtonSender sender = session.createSender(address);
        sender.setQoS(qos);
        sender.sendQueueDrainHandler(sendQueueDrainHandler);
        sender.openHandler(openAttempt -> {
            if (openAttempt.succeeded()) {
                LOG.debug(
                        "sender [{}] for container [{}] open",
                        address,
                        connection.getRemoteContainer());
                result.complete(openAttempt.result());
            } else {
                LOG.debug("could not open sender [{}] for container [{}]",
                        address,
                        connection.getRemoteContainer(), openAttempt.cause());
                result.fail(openAttempt.cause());
            }
        });
        sender.closeHandler(closed -> {
            if (closed.succeeded()) {
                LOG.debug("sender [{}] for container [{}] closed", address, connection.getRemoteContainer());
            }
        });
        sender.open();
    }
}
