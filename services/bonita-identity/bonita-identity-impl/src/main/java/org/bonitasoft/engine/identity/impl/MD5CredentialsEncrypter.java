/**
 * Copyright (C) 2013, 2014 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.identity.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author Matthieu Chaffotte
 */
public class MD5CredentialsEncrypter implements CredentialsEncrypter {

    @Override
    public String hash(final String password) {
        final byte[] hash = DigestUtils.md5(password);
        return Base64.encodeBase64String(hash);
    }

    @Override
    public boolean check(final String password, final String hashPassword) {
        System.out.println("Given password: " + password);
        System.out.println("Hashed password: " + hashPassword);

        final String hashedPassword = hash(password);
        return hashedPassword.equals(hashPassword);
    }

}
