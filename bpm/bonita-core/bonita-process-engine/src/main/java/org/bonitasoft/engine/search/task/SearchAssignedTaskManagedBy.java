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
package org.bonitasoft.engine.search.task;

import java.util.List;

import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.AbstractHumanTaskInstanceSearchEntity;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.descriptor.SearchHumanTaskInstanceDescriptor;

/**
 * @author Baptiste Mesta
 */
public class SearchAssignedTaskManagedBy extends AbstractHumanTaskInstanceSearchEntity {

    private final ActivityInstanceService activityInstanceService;

    private final long managerUserId;

    public SearchAssignedTaskManagedBy(final ActivityInstanceService activityInstanceService, final FlowNodeStateManager flowNodeStateManager,
            final SearchHumanTaskInstanceDescriptor searchHumanTaskInstanceDescriptor, final long managerUserId, final SearchOptions options) {
        super(searchHumanTaskInstanceDescriptor, options, flowNodeStateManager);
        this.activityInstanceService = activityInstanceService;
        this.managerUserId = managerUserId;
    }

    @Override
    public long executeCount(final QueryOptions searchOptions) throws SBonitaReadException {
        return activityInstanceService.getNumberOfAssignedTasksManagedBy(managerUserId, searchOptions);
    }

    @Override
    public List<SHumanTaskInstance> executeSearch(final QueryOptions searchOptions) throws SBonitaReadException {
        return activityInstanceService.searchAssignedTasksManagedBy(managerUserId, searchOptions);
    }

}
