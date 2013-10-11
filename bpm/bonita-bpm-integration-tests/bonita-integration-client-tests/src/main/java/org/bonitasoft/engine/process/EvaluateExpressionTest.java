/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ActivityDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.TransitionDefinitionBuilder;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Zhao Na
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class EvaluateExpressionTest extends CommonAPITest {

    private static final String STEP2_NAME = "Approval";

    private static final String STEP1_NAME = "Request";

    protected static final String USERNAME = "dwight";

    protected static final String PASSWORD = "Schrute";

    private ProcessDefinition processDefinition = null;

    private ProcessInstance processInstance = null;

    private Map<Expression, Map<String, Serializable>> expressions;

    private User user = null;

    @Before
    public void before() throws Exception {
        login();
        user = createUser(USERNAME, PASSWORD);
        logout();
        loginWith(USERNAME, PASSWORD);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilder.addData("stringData", String.class.getName(), new ExpressionBuilder().createConstantStringExpression("Word"));
        processDefinitionBuilder.addDateData("dateData", new ExpressionBuilder().createConstantDateExpression("2013-07-18T14:49:26.86+02:00"));
        processDefinitionBuilder.addData("doubleData", Double.class.getName(), new ExpressionBuilder().createConstantDoubleExpression(2D));
        processDefinitionBuilder.addData("longData", Long.class.getName(), new ExpressionBuilder().createConstantLongExpression(1L));
        processDefinitionBuilder.addData("booleanData", Boolean.class.getName(), new ExpressionBuilder().createConstantBooleanExpression(true));
        processDefinitionBuilder.addData("floatData", Float.class.getName(), new ExpressionBuilder().createConstantFloatExpression(100F));
        processDefinitionBuilder.addData("integerData", Integer.class.getName(), new ExpressionBuilder().createConstantIntegerExpression(4));
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask(STEP1_NAME, ACTOR_NAME);
        processDefinitionBuilder.addUserTask(STEP2_NAME, ACTOR_NAME);
        processDefinitionBuilder.addTransition(STEP1_NAME, STEP2_NAME);
        processDefinition = deployAndEnableWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
        processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final List<Expression> stringDependencies = new ArrayList<Expression>();
        stringDependencies.add(new ExpressionBuilder().createDataExpression("stringData", String.class.getName()));
        stringDependencies.add(new ExpressionBuilder().createInputExpression("field_string", String.class.getName()));
        final Expression stringExpression = new ExpressionBuilder().createGroovyScriptExpression("StringScript", "stringData + \"-\" + field_string",
                String.class.getName(), stringDependencies);
        final Map<String, Serializable> fieldValues = new HashMap<String, Serializable>();
        fieldValues.put("field_string", "Excel");

        final List<Expression> dateDependencies = Collections.singletonList(new ExpressionBuilder().createDataExpression("dateData", Date.class.getName()));
        final Expression dateExpression = new ExpressionBuilder().createGroovyScriptExpression("DateScript", "dateData.toString() + \"plop\"",
                String.class.getName(), dateDependencies);

        final List<Expression> longDependencies = Collections.singletonList(new ExpressionBuilder().createDataExpression("longData", Long.class.getName()));
        final Expression longExpression = new ExpressionBuilder().createGroovyScriptExpression("LongScript", "String.valueOf(longData)+ \"plop\"",
                String.class.getName(), longDependencies);

        final List<Expression> doubleDependencies = Collections
                .singletonList(new ExpressionBuilder().createDataExpression("doubleData", Double.class.getName()));
        final Expression doubleExpression = new ExpressionBuilder().createGroovyScriptExpression("DoubleScript", "String.valueOf(doubleData) + \"plop\"",
                String.class.getName(), doubleDependencies);

        final List<Expression> booleanDependencies = Collections.singletonList(new ExpressionBuilder().createDataExpression("booleanData",
                Boolean.class.getName()));
        final Expression booleanExpression = new ExpressionBuilder().createGroovyScriptExpression("BooleanScript", "booleanData && false",
                Boolean.class.getName(), booleanDependencies);

        final List<Expression> floatDependencies = Collections.singletonList(new ExpressionBuilder().createDataExpression("floatData", Float.class.getName()));
        final Expression floatExpression = new ExpressionBuilder().createGroovyScriptExpression("FloatScript", "String.valueOf(floatData) + \"plop\"",
                String.class.getName(), floatDependencies);

        final List<Expression> integerDependencies = Collections.singletonList(new ExpressionBuilder().createDataExpression("integerData",
                Integer.class.getName()));
        final Expression integerExpression = new ExpressionBuilder().createGroovyScriptExpression("IntegerScript", "String.valueOf(integerData) + \"plop\"",
                String.class.getName(), integerDependencies);

        expressions = new HashMap<Expression, Map<String, Serializable>>();
        expressions.put(stringExpression, fieldValues);
        expressions.put(dateExpression, new HashMap<String, Serializable>());
        expressions.put(longExpression, new HashMap<String, Serializable>());
        expressions.put(doubleExpression, new HashMap<String, Serializable>());
        expressions.put(booleanExpression, new HashMap<String, Serializable>());
        expressions.put(floatExpression, new HashMap<String, Serializable>());
        expressions.put(integerExpression, new HashMap<String, Serializable>());
    }

    @After
    public void after() throws BonitaException, BonitaHomeNotSetException {
        // Clean up
        deleteUser(user);
        disableAndDeleteProcess(processDefinition);
        logout();
    }

    private static ProcessDefinitionBuilder createProcessDefinitionBuilderWithHumanAndAutomaticSteps(final String processName, final String processVersion,
            final List<String> stepNames, final List<Boolean> isHuman) {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, processVersion);
        processBuilder.addActor("Actor1");
        ActivityDefinitionBuilder activityDefinitionBuilder = null;
        for (int i = 0; i < stepNames.size(); i++) {
            final String stepName = stepNames.get(i);
            if (isHuman.get(i)) {
                if (activityDefinitionBuilder != null) {
                    activityDefinitionBuilder = activityDefinitionBuilder.addUserTask(stepName, "Actor1");
                } else {
                    activityDefinitionBuilder = processBuilder.addUserTask(stepName, "Actor1");
                }
            } else {
                if (activityDefinitionBuilder != null) {
                    activityDefinitionBuilder = activityDefinitionBuilder.addAutomaticTask(stepName);
                } else {
                    activityDefinitionBuilder = processBuilder.addAutomaticTask(stepName);
                }
            }
        }
        TransitionDefinitionBuilder transitionDefinitionBuilder = null;
        for (int i = 0; i < stepNames.size() - 1; i++) {
            if (transitionDefinitionBuilder != null) {
                transitionDefinitionBuilder = transitionDefinitionBuilder.addTransition(stepNames.get(i), stepNames.get(i + 1));
            } else {
                transitionDefinitionBuilder = activityDefinitionBuilder.addTransition(stepNames.get(i), stepNames.get(i + 1));
            }
        }
        return processBuilder;
    }

    private void cleanup(final long userId, final long processDefinitionId) throws BonitaException {
        deleteUser(userId);
        disableAndDeleteProcess(processDefinitionId);
    }

    private Map<Expression, Map<String, Serializable>> createExpression(final Expression expression) {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>();
        expressions.put(expression, null);
        return expressions;
    }

    private ProcessDefinition createAndDeployProcessDefinitionAndInstance(final String dataName, final int value, final boolean isHuman, final User user)
            throws Exception {
        // create data expression
        final Expression dataDefaultexp = new ExpressionBuilder().createConstantIntegerExpression(value);

        // create a processDefinition with data and parameter...
        final DesignProcessDefinition processDef = createProcessDefinitionBuilderWithHumanAndAutomaticSteps("My_Process", "1.0", Arrays.asList("step1"),
                Arrays.asList(isHuman)).addIntegerData(dataName, dataDefaultexp).addDescription("Delivery all day and night long").getProcess();

        return deployAndEnableWithActor(processDef, "Actor1", user);
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Process instantiation" }, story = "Evaluate an expression at process instantiation.", jira = "ENGINE-1160")
    @Test
    public void evaluateExpressionsAtProcessInstantiation() throws Exception {
        waitForPendingTasks(getSession().getUserId(), 1);

        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsAtProcessInstanciation(processInstance.getId(), expressions);
        Assert.assertEquals("Word-Excel", result.get("StringScript"));
        Assert.assertEquals("Thu Jul 18 14:49:26 CEST 2013plop", result.get("DateScript"));
        Assert.assertEquals("1plop", result.get("LongScript"));
        Assert.assertEquals("2.0plop", result.get("DoubleScript"));
        Assert.assertEquals(false, result.get("BooleanScript"));
        Assert.assertEquals("100.0plop", result.get("FloatScript"));
        Assert.assertEquals("4plop", result.get("IntegerScript"));
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Wrong process instance id" }, jira = "ENGINE-1160")
    @Test(expected = ExpressionEvaluationException.class)
    public void evaluateExpressionsAtProcessInstanciationWithWrongProcessInstanceId() throws Exception {
        getProcessAPI().evaluateExpressionsAtProcessInstanciation(36, expressions);
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Completed activity" }, story = "Evaluate an expression on completed activity instance.", jira = "")
    @Test
    public void evaluateExpressionsOnCompletedActivityInstance() throws Exception {
        final HumanTaskInstance userTaskInstance = waitForUserTaskAndExecuteIt(STEP1_NAME, processInstance.getId(), user);
        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnCompletedActivityInstance(userTaskInstance.getId(), expressions);
        Assert.assertEquals("Word-Excel", result.get("StringScript"));
        Assert.assertEquals("Thu Jul 18 14:49:26 CEST 2013plop", result.get("DateScript"));
        Assert.assertEquals("1plop", result.get("LongScript"));
        Assert.assertEquals("2.0plop", result.get("DoubleScript"));
        Assert.assertEquals(false, result.get("BooleanScript"));
        Assert.assertEquals("100.0plop", result.get("FloatScript"));
        Assert.assertEquals("4plop", result.get("IntegerScript"));
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Evaluate", "Wrong activity instance id" }, story = "Execute form expression command with wrong activity instance id", jira = "ENGINE-1160")
    @Test(expected = ExpressionEvaluationException.class)
    public void evaluateExpressionsOnCompletedActivityInstanceWithWrongActivityInstanceId() throws Exception {
        getProcessAPI().evaluateExpressionsOnCompletedActivityInstance(36, expressions);
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Completed process instance" }, story = "Evaluate an expression on completed process instance.", jira = "ENGINE-1160")
    @Test
    public void evaluateExpressionsOnCompletedProcessInstance() throws Exception {
        waitForUserTaskAndExecuteIt(STEP1_NAME, processInstance.getId(), user);
        waitForUserTaskAndExecuteIt(STEP2_NAME, processInstance.getId(), user);
        waitForProcessToFinish(processInstance);

        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionOnCompletedProcessInstance(processInstance.getId(), expressions);
        Assert.assertEquals("Word-Excel", result.get("StringScript"));
        Assert.assertEquals("Thu Jul 18 14:49:26 CEST 2013plop", result.get("DateScript"));
        Assert.assertEquals("1plop", result.get("LongScript"));
        Assert.assertEquals("2.0plop", result.get("DoubleScript"));
        Assert.assertEquals(false, result.get("BooleanScript"));
        Assert.assertEquals("100.0plop", result.get("FloatScript"));
        Assert.assertEquals("4plop", result.get("IntegerScript"));
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Completed process instance", "Updated data" }, story = "Evaluate an expression on completed process instance with variable update.", jira = "ENGINE-1160")
    @Test
    public void evaluateExpressionsOnCompletedProcessInstanceAfterVariableUpdate() throws Exception {
        waitForUserTaskAndExecuteIt(STEP1_NAME, processInstance.getId(), user);
        getProcessAPI().updateProcessDataInstance("stringData", processInstance.getId(), "Plop");
        waitForUserTaskAndExecuteIt(STEP2_NAME, processInstance.getId(), user);
        waitForProcessToFinish(processInstance);

        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionOnCompletedProcessInstance(processInstance.getId(), expressions);
        Assert.assertEquals(
                "if Word-Excel is returned, it means the values of the variable used are the latest ones whereas it should be the ones of when the activity was submited",
                "Plop-Excel", result.get("StringScript"));
        Assert.assertEquals("Thu Jul 18 14:49:26 CEST 2013plop", result.get("DateScript"));
        Assert.assertEquals("1plop", result.get("LongScript"));
        Assert.assertEquals("2.0plop", result.get("DoubleScript"));
        Assert.assertEquals(false, result.get("BooleanScript"));
        Assert.assertEquals("100.0plop", result.get("FloatScript"));
        Assert.assertEquals("4plop", result.get("IntegerScript"));
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Activity instance" }, story = "Evaluate expression on activity instance.", jira = "ENGINE-1160")
    @Test
    public void evaluateExpressionsOnActivityInstance() throws Exception {
        final HumanTaskInstance userTaskInstance = waitForUserTask(STEP1_NAME, processInstance);
        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnActivityInstance(userTaskInstance.getId(), expressions);
        Assert.assertEquals("Word-Excel", result.get("StringScript"));
        Assert.assertEquals("Thu Jul 18 14:49:26 CEST 2013plop", result.get("DateScript"));
        Assert.assertEquals("1plop", result.get("LongScript"));
        Assert.assertEquals("2.0plop", result.get("DoubleScript"));
        Assert.assertEquals(false, result.get("BooleanScript"));
        Assert.assertEquals("100.0plop", result.get("FloatScript"));
        Assert.assertEquals("4plop", result.get("IntegerScript"));
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Wrong activity instance id" }, story = "Execute form expression command with wrong activity instance id", jira = "ENGINE-1160")
    @Test(expected = ExpressionEvaluationException.class)
    public void evaluateExpressionsOnActivityInstanceWithWrongActivityInstanceId() throws Exception {
        getProcessAPI().evaluateExpressionsOnActivityInstance(36, expressions);
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "taskAssigneeId" }, story = "Evaluate engine constant expression taskAssigneeID.", jira = "ENGINE-1256")
    @Test
    public void evaluateAssigneeId() throws Exception {
        final HumanTaskInstance userTaskInstance = waitForUserTaskAndExecuteIt(STEP1_NAME, processInstance.getId(), user);

        final Expression taskAssigneeExpr = new ExpressionBuilder().createEngineConstant(ExpressionConstants.TASK_ASSIGNEE_ID);
        final Expression engineExecContextExpr = new ExpressionBuilder().createEngineConstant(ExpressionConstants.ENGINE_EXECUTION_CONTEXT);
        final Map<Expression, Map<String, Serializable>> engineExpresssions = new HashMap<Expression, Map<String, Serializable>>();
        engineExpresssions.put(taskAssigneeExpr, Collections.<String, Serializable> emptyMap());
        engineExpresssions.put(engineExecContextExpr, Collections.<String, Serializable> emptyMap());

        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnActivityInstance(userTaskInstance.getId(), engineExpresssions);
        Assert.assertEquals(user.getId(), result.get(taskAssigneeExpr.getContent()));
        Assert.assertEquals(user.getId(), ((EngineExecutionContext) result.get(engineExecContextExpr.getContent())).getTaskAssigneeId());
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Process instance", "Initial value" }, story = "Evalute an expression on process intance with initial values.", jira = "ENGINE-1160")
    @Test
    public void evaluateExpressionsOnProcessInstanceWithInitialValues() throws Exception {
        waitForUserTaskAndExecuteIt(STEP1_NAME, processInstance.getId(), user);
        getProcessAPI().updateProcessDataInstance("stringData", processInstance.getId(), "Excel");

        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstance.getId(), expressions);
        Assert.assertFalse("Result should not be empty", result.isEmpty());
        Assert.assertEquals("Excel-Excel", result.get("StringScript"));
        Assert.assertEquals("Thu Jul 18 14:49:26 CEST 2013plop", result.get("DateScript"));
        Assert.assertEquals("1plop", result.get("LongScript"));
        Assert.assertEquals("2.0plop", result.get("DoubleScript"));
        Assert.assertEquals(false, result.get("BooleanScript"));
        Assert.assertEquals("100.0plop", result.get("FloatScript"));
        Assert.assertEquals("4plop", result.get("IntegerScript"));
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Process instance", "Current value" }, story = "Evalute an expression on process intance with current values.", jira = "ENGINE-1160")
    @Test
    public void evaluateExpressionsOnProcessInstanceWithCurrentValues() throws Exception {
        waitForUserTaskAndExecuteIt(STEP1_NAME, processInstance.getId(), user);
        getProcessAPI().updateProcessDataInstance("stringData", processInstance.getId(), "Excel");

        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstance.getId(), expressions);
        Assert.assertEquals("Excel-Excel", result.get("StringScript"));
        Assert.assertEquals("Thu Jul 18 14:49:26 CEST 2013plop", result.get("DateScript"));
        Assert.assertEquals("1plop", result.get("LongScript"));
        Assert.assertEquals("2.0plop", result.get("DoubleScript"));
        Assert.assertEquals(false, result.get("BooleanScript"));
        Assert.assertEquals("100.0plop", result.get("FloatScript"));
        Assert.assertEquals("4plop", result.get("IntegerScript"));
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Wrong process instance id" }, jira = "ENGINE-1160")
    @Test(expected = ExpressionEvaluationException.class)
    public void evaluateExpressionsOnProcessInstanceWithWrongProcessInstanceId() throws Exception {
        getProcessAPI().evaluateExpressionsOnProcessInstance(36, expressions);
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "No parameter" }, story = "Test expression command with no parameter.", jira = "ENGINE-548")
    @Test(expected = ExpressionEvaluationException.class)
    public void evaluateExpressionsOnProcessInstanceWithNoExpressions() throws Exception {
        getProcessAPI().evaluateExpressionsOnProcessInstance(1, null);
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Process definition" }, story = "Evaluate expressions on process definition.", jira = "ENGINE-1160")
    @Test(expected = ExpressionEvaluationException.class)
    public void evaluateExpressionsOnProcessDefinition() throws Exception {
        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessDefinition(processDefinition.getId(), expressions);
        Assert.assertFalse("Result should not be empty", result.isEmpty());
        Assert.assertEquals("Word", result.get("StringScript"));
        Assert.assertEquals("Thu Jul 18 14:49:26 CEST 2013plop", result.get("DateScript"));
        Assert.assertEquals("1plop", result.get("LongScript"));
        Assert.assertEquals("2.0plop", result.get("DoubleScript"));
        Assert.assertEquals(false, result.get("BooleanScript"));
        Assert.assertEquals("100.0plop", result.get("FloatScript"));
        Assert.assertEquals("4plop", result.get("IntegerScript"));
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Wrong process definition id" }, jira = "ENGINE-1160")
    @Test(expected = ExpressionEvaluationException.class)
    public void evaluateExpressionsOnProcessDefinitionWithWrongProcessDefinitionId() throws Exception {
        getProcessAPI().evaluateExpressionsOnProcessDefinition(36, expressions);
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Complex expression" }, story = "Evaluate a complex expression.", jira = "")
    @Ignore("not yet supported")
    @Test
    public void evaluateComplexExpression() throws Exception {
        final String userName = "Manu";
        final User manu = createUser(userName, "bpm");
        loginWith(userName, "bpm");
        final String a = "a";
        final String b = "b";
        final String c = "c";
        final int a_value = 1;
        final Expression aExpr = new ExpressionBuilder().createDataExpression(a, Integer.class.getName());
        final Expression bExpr = new ExpressionBuilder().createDataExpression(b, Integer.class.getName());
        final Expression cExpr = new ExpressionBuilder().createDataExpression(c, Integer.class.getName());

        // get processInstance
        final Expression s1 = new ExpressionBuilder().createGroovyScriptExpression("script1", "a+2", Integer.class.getName(), aExpr);
        final Expression s2 = new ExpressionBuilder().createGroovyScriptExpression("g", "a+b", Integer.class.getName(), aExpr, bExpr);
        final Expression s3 = new ExpressionBuilder().createGroovyScriptExpression("script3", "a+b+g", Integer.class.getName(), aExpr, bExpr, s2);
        final Expression s4 = new ExpressionBuilder().createGroovyScriptExpression("theScript", "a+b+c", Integer.class.getName(), aExpr, bExpr, cExpr);

        final Expression defaultA = new ExpressionBuilder().createConstantIntegerExpression(a_value);
        final Expression defaultB = s1;
        final Expression defaultC = s3;
        // create a processDefinition with data and parameter...
        final DesignProcessDefinition processDef = createProcessDefinitionBuilderWithHumanAndAutomaticSteps("My_Process", "1.0", Arrays.asList("step1"),
                Arrays.asList(true)).addIntegerData(a, defaultA).addIntegerData(b, defaultB).addIntegerData(c, defaultC)
                .addDescription("Delivery all day and night long").getProcess();

        final ProcessDefinition processDefinition = deployAndEnableWithActor(processDef, "Actor1", manu);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // on processInstance
        final Map<Expression, Map<String, Serializable>> expressions = createExpression(s4);
        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessDefinition(processDefinition.getId(), expressions);
        assertEquals(Integer.valueOf(12), result.get("theScript"));

        cleanup(manu.getId(), processDefinition.getId());
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Complex expression", "Process instance" }, story = "Evaluate a complex expression on process instance.", jira = "")
    @Ignore("not yet supported")
    @Test
    public void evaluateComplexExpressionOnProcessInstance() throws Exception {
        final String userName = "Manu";
        final User manu = createUser(userName, "bpm");
        loginWith(userName, "bpm");
        final String a = "a";
        final int a_value = 1;
        final Expression aExpr = new ExpressionBuilder().createDataExpression(a, Integer.class.getName());

        // get processInstance
        final Expression ga = new ExpressionBuilder().createGroovyScriptExpression("ga", "a+3", Integer.class.getName(), aExpr);
        final Expression gb = new ExpressionBuilder().createGroovyScriptExpression("gb", "4", Integer.class.getName());
        final Expression gc = new ExpressionBuilder().createGroovyScriptExpression("gc", "ga+gb", Integer.class.getName(), ga, gb);
        final Expression g = new ExpressionBuilder().createGroovyScriptExpression("g", "ga+gb+gc", Integer.class.getName(), ga, gb, gc);
        final Expression script = new ExpressionBuilder().createGroovyScriptExpression("theScript", "1+g", Integer.class.getName(), g);

        final Expression defaultA = new ExpressionBuilder().createConstantIntegerExpression(a_value);
        // create a processDefinition with data and parameter...
        final DesignProcessDefinition processDef = createProcessDefinitionBuilderWithHumanAndAutomaticSteps("My_Process", "1.0", Arrays.asList("step1"),
                Arrays.asList(true)).addIntegerData(a, defaultA).addDescription("Delivery all day and night long").getProcess();

        final ProcessDefinition processDefinition = deployAndEnableWithActor(processDef, "Actor1", manu);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        // on processInstance
        final Map<Expression, Map<String, Serializable>> expressions = createExpression(script);
        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstance.getId(), expressions);
        assertEquals(Integer.valueOf(17), result.get("theScript"));

        cleanup(manu.getId(), processDefinition.getId());
    }

    @Cover(classes = ProcessAPI.class, concept = BPMNConcept.EXPRESSIONS, keywords = { "Expression", "Evaluate", "Pattern expression" }, story = "Evaluate a pattern expression.", jira = "")
    @Test
    public void evaluatePatternExpression() throws Exception {
        final String userName = "Manu";
        final User manu = createUser(userName, "poele");
        loginWith(userName, "poele");
        final String dataName = "birthYear";
        // get processInstance
        final ProcessDefinition processDefinition = createAndDeployProcessDefinitionAndInstance(dataName, 1977, true, manu);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        final Expression expData = new ExpressionBuilder().createDataExpression(dataName, Integer.class.getName());
        final Expression expConstantExpression = new ExpressionBuilder().createConstantStringExpression("year");

        final String messagePattern = "My birth ${year} is ${birthYear}";
        final Expression expPattern = new ExpressionBuilder().createPatternExpression("TestEvaluatePatternExpression", messagePattern, expData,
                expConstantExpression);

        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>();
        expressions.put(expPattern, null);
        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstance.getId(), expressions);
        assertEquals("My birth year is 1977", result.get(messagePattern));

        cleanup(manu.getId(), processDefinition.getId());
    }

}
