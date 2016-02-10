/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.profile.builder;

import org.bonitasoft.engine.profile.model.SProfileEntry;

/**
 * @author Matthieu Chaffotte
 */
public interface SProfileEntryBuilderFactory {

    String ID = "id";

    String NAME = "name";

    String DESCRIPTION = "description";

    String PROFILE_ID = "profileId";

    String PARENT_ID = "parentId";

    String PAGE = "page";

    String INDEX = "index";

    String TYPE = "type";

    String CUSTOM = "custom";

    SProfileEntryBuilder createNewInstance(String name, long profileId);

    SProfileEntryBuilder createNewInstance(SProfileEntry profileEntry);

}
