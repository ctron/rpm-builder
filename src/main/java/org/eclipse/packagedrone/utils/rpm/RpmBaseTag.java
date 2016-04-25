/*******************************************************************************
 * Copyright (c) 2015 IBH SYSTEMS GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.packagedrone.utils.rpm;

public interface RpmBaseTag
{
    /**
     * Get the key value of the tag
     * <p>
     * An Integer object is used since the main use case of the key value is to
     * be used in maps.
     * </p>
     *
     * @return the key value
     */
    public Integer getValue ();
}
