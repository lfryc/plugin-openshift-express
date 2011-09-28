/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package com.redhat.openshift.express.core.internal.utils;


/**
 * @author André Dietisheim
 */
public class Assert {

	public static final class AssertionFailedException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public AssertionFailedException() {
			super();
		}
		
	}
	
	public static <V> V assertNotNull(V value) {
		if (value == null) {
			throw new AssertionFailedException();
		}
		return value;
	}
	
}
