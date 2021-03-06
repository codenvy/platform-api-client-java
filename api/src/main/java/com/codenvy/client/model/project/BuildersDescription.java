/*******************************************************************************
 * Copyright (c) 2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package com.codenvy.client.model.project;

/**
 * Defines default builder.
 * @author Florent Benoit
 */
public interface BuildersDescription {

    /**
     * @return the default builder selected (if any)
     */
    String defaultBuilder();

}
