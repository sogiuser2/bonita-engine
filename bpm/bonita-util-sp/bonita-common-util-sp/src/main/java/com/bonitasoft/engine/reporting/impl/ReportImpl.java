/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.reporting.impl;

import java.util.Date;

import com.bonitasoft.engine.reporting.Report;

/**
 * @author Matthieu Chaffotte
 */
public class ReportImpl implements Report {

    private static final long serialVersionUID = 5445403438892593799L;

    private final long id;

    private final String name;

    private boolean provided;

    private String description;

    private final Date installationDate;

    private final long installedBy;

    private Date lastModificationDate;

    private byte[] screenshot;

    public ReportImpl(final long id, final String name, final long installationDate, final long installedBy) {
        this.id = id;
        this.name = name;
        this.installationDate = new Date(installationDate);
        this.installedBy = installedBy;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isProvided() {
        return provided;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Date getInstallationDate() {
        return installationDate;
    }

    @Override
    public long getInstalledBy() {
        return installedBy;
    }

    @Override
    public Date getLastModificationDate() {
        return lastModificationDate;
    }

    @Override
    public byte[] getScreenshot() {
        return screenshot;
    }

    public void setProvided(final boolean provided) {
        this.provided = provided;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setLastModificationDate(final Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public void setScreenshot(final byte[] screenshot) {
        this.screenshot = screenshot;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((installationDate == null) ? 0 : installationDate.hashCode());
        result = prime * result + (int) (installedBy ^ (installedBy >>> 32));
        result = prime * result + ((lastModificationDate == null) ? 0 : lastModificationDate.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (provided ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReportImpl other = (ReportImpl) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id != other.id)
            return false;
        if (installationDate == null) {
            if (other.installationDate != null)
                return false;
        } else if (!installationDate.equals(other.installationDate))
            return false;
        if (installedBy != other.installedBy)
            return false;
        if (lastModificationDate == null) {
            if (other.lastModificationDate != null)
                return false;
        } else if (!lastModificationDate.equals(other.lastModificationDate))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (provided != other.provided)
            return false;
        return true;
    }

}
