/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.execution.state;

import static org.bonitasoft.engine.execution.StateBehaviors.AFTER_ON_FINISH;
import static org.bonitasoft.engine.execution.StateBehaviors.BEFORE_ON_ENTER;
import static org.bonitasoft.engine.execution.StateBehaviors.BEFORE_ON_FINISH;
import static org.bonitasoft.engine.execution.StateBehaviors.DURING_ON_ENTER;
import static org.bonitasoft.engine.execution.StateBehaviors.DURING_ON_FINISH;

import java.util.List;

import org.bonitasoft.engine.bar.BEntry;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.StateBehaviors;

/**
 * @author Baptiste Mesta
 */
public abstract class OnEnterAndFinishConnectorState implements FlowNodeState {

    private final StateBehaviors stateBehaviors;

    public OnEnterAndFinishConnectorState(StateBehaviors stateBehaviors) {

        this.stateBehaviors = stateBehaviors;
    }

    @Override
    public StateCode execute(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityStateExecutionException {
        // Retrieve the phase to execute depending on which connectors to execute and when to execute them:
        final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> entry = getConnectorToExecuteAndFlag(processDefinition,
                flowNodeInstance);
        final Integer phase = entry.getKey();
        if ((phase & BEFORE_ON_ENTER) != 0) {
            beforeOnEnter(processDefinition, flowNodeInstance);
        }
        if ((phase & DURING_ON_ENTER) != 0) {
            stateBehaviors.executeConnectorInWork(processDefinition.getId(), flowNodeInstance.getParentProcessInstanceId(),
                    flowNodeInstance.getFlowNodeDefinitionId(), flowNodeInstance.getId(), entry
                            .getValue().getKey(), entry.getValue().getValue());
            return StateCode.EXECUTING;
        }
        if ((phase & BEFORE_ON_FINISH) != 0) {
            onEnterToOnFinish(processDefinition, flowNodeInstance);
        }
        if ((phase & DURING_ON_FINISH) != 0) {
            stateBehaviors.executeConnectorInWork(processDefinition.getId(), flowNodeInstance.getParentProcessInstanceId(),
                    flowNodeInstance.getFlowNodeDefinitionId(), flowNodeInstance.getId(), entry
                            .getValue().getKey(), entry.getValue().getValue());
            return StateCode.EXECUTING;
        }
        if ((phase & AFTER_ON_FINISH) != 0) {
            afterOnFinish(processDefinition, flowNodeInstance);
        }
        return StateCode.DONE;
    }
    /**
     * Return the phases and connectors to execute, as a couple of (phase, couple of (connector instance, connector definition))
     *
     * @param processDefinition the process where the connectors are defined.
     * @param flowNodeInstance the instance of the flow node to execute possible connectors on.
     * @return the phases and connectors to execute
     * @throws SActivityStateExecutionException
     */
    public BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> getConnectorToExecuteAndFlag(final SProcessDefinition processDefinition,
                                                                                                          final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        try {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            final SFlowNodeDefinition flowNodeDefinition = processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
            boolean onEnterExecuted = false;
            final List<SConnectorDefinition> connectorsOnEnter = flowNodeDefinition.getConnectors(ConnectorEvent.ON_ENTER);
            if (connectorsOnEnter.size() > 0) {
                final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorToExecuteOnEnter = getConnectorToExecuteOnEnter(
                        flowNodeInstance, connectorsOnEnter);
                if (connectorToExecuteOnEnter != null) {
                    return connectorToExecuteOnEnter;
                }
                // All connectors ON ENTER have already been executed:
                onEnterExecuted = true;
            }
            // no on enter connector to execute
            final BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> connectorToExecuteOnFinish = getConnectorToExecuteOnFinish(
                    flowNodeDefinition, flowNodeInstance, onEnterExecuted);
            if (connectorToExecuteOnFinish != null) {
                return connectorToExecuteOnFinish;
            }
            // no ON ENTER no ON FINISH active
            if (flowNodeInstance.isStateExecuting()) {
                // there was a connector executed but no more: execute only before and after finish
                return getConnectorWithFlag(null, null, BEFORE_ON_FINISH | AFTER_ON_FINISH);
            }
            // no connector and was just starting
            return getConnectorWithFlag(null, null, BEFORE_ON_ENTER | BEFORE_ON_FINISH | AFTER_ON_FINISH);
        } catch (final SConnectorInstanceReadException e) {
            throw new SActivityStateExecutionException(e);
        }
    }

    private BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> getConnectorToExecuteOnFinish(final SFlowNodeDefinition flowNodeDefinition,
                                                                                                            final SFlowNodeInstance flowNodeInstance, final boolean onEnterExecuted)
            throws SConnectorInstanceReadException,
            SActivityStateExecutionException {
        final List<SConnectorDefinition> connectorsOnFinish = flowNodeDefinition.getConnectors(ConnectorEvent.ON_FINISH);
        if (connectorsOnFinish.size() > 0) {
            final SConnectorInstance nextConnectorInstanceToExecute = stateBehaviors.getNextConnectorInstance(flowNodeInstance, ConnectorEvent.ON_FINISH);
            if (nextConnectorInstanceToExecute != null) {
                if (nextConnectorInstanceToExecute.getState().equals(ConnectorState.TO_BE_EXECUTED.name())
                        && connectorsOnFinish.get(0).getName().equals(nextConnectorInstanceToExecute.getName())) {
                    // first finish connector
                    final SConnectorDefinition connectorDefinition = connectorsOnFinish.get(0);
                    if (onEnterExecuted) {
                        // some connectors were already executed
                        return getConnectorWithFlag(nextConnectorInstanceToExecute, connectorDefinition, BEFORE_ON_FINISH | DURING_ON_FINISH);
                    }
                    // on finish but the first connector
                    return getConnectorWithFlag(nextConnectorInstanceToExecute, connectorDefinition, BEFORE_ON_ENTER | BEFORE_ON_FINISH
                            | DURING_ON_FINISH);
                }
                // no the first, don't execute before
                return getConnectorWithFlagIfIsNextToExecute(flowNodeInstance, connectorsOnFinish, nextConnectorInstanceToExecute, DURING_ON_FINISH);
            }
            // all finish connectors executed
            return getConnectorWithFlag(null, null, AFTER_ON_FINISH);
        }
        return null;
    }

    private BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> getConnectorToExecuteOnEnter(final SFlowNodeInstance flowNodeInstance,
                                                                                                           final List<SConnectorDefinition> connectorsOnEnter) throws SConnectorInstanceReadException, SActivityStateExecutionException {
        final SConnectorInstance nextConnectorInstanceToExecute = stateBehaviors.getNextConnectorInstance(flowNodeInstance, ConnectorEvent.ON_ENTER);
        if (nextConnectorInstanceToExecute != null) {
            // Have we already executed the 'before on enter' phase?
            if (nextConnectorInstanceToExecute.getState().equals(ConnectorState.TO_BE_EXECUTED.name())
                    && connectorsOnEnter.get(0).getName().equals(nextConnectorInstanceToExecute.getName())) {
                // first enter connector:
                return getConnectorWithFlag(nextConnectorInstanceToExecute, connectorsOnEnter.get(0), BEFORE_ON_ENTER | DURING_ON_ENTER);
                // Or do we have to skip the 'before on enter' phase:
            }
            // not the first connector, or first connector not in state TO_BE_EXECUTED => don't execute phase BEFORE_ON_ENTER:
            return getConnectorWithFlagIfIsNextToExecute(flowNodeInstance, connectorsOnEnter, nextConnectorInstanceToExecute, DURING_ON_ENTER);
        }
        return null;
    }

    private BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> getConnectorWithFlagIfIsNextToExecute(final SFlowNodeInstance flowNodeInstance,
                                                                                                                    final List<SConnectorDefinition> sConnectorDefinitions, final SConnectorInstance nextConnectorInstanceToExecute, final int flag)
            throws SActivityStateExecutionException {
        for (final SConnectorDefinition sConnectorDefinition : sConnectorDefinitions) {
            if (sConnectorDefinition.getName().equals(nextConnectorInstanceToExecute.getName())) {
                return getConnectorWithFlag(nextConnectorInstanceToExecute, sConnectorDefinition, flag);
            }
        }
        throw new SActivityStateExecutionException("Connector definition of " + nextConnectorInstanceToExecute + " not found on "
                + flowNodeInstance);
    }

    private BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>> getConnectorWithFlag(final SConnectorInstance nextConnectorInstance,
                                                                                                   final SConnectorDefinition connectorDefinition, final int flag) {
        return new BEntry<Integer, BEntry<SConnectorInstance, SConnectorDefinition>>(flag, new BEntry<SConnectorInstance, SConnectorDefinition>(
                nextConnectorInstance, connectorDefinition));
    }

    protected abstract void beforeOnEnter(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException;

    protected abstract void onEnterToOnFinish(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException;

    protected abstract void afterOnFinish(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException;
}
