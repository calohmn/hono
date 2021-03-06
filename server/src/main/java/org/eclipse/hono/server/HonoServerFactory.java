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

/**
 * A factory for creating {@code HonoServer} instances.
 */
public interface HonoServerFactory {

    /**
     * Creates a new Hono server instance.
     * 
     * @return The new instance.
     */
    HonoServer getHonoServer();
}
