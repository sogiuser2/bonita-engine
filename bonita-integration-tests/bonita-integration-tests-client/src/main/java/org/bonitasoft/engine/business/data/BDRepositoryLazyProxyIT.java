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
package org.bonitasoft.engine.business.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BDRepositoryLazyProxyIT extends CommonAPIIT {

    private User matti;

    @Before
    public void setUp() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        matti = createUser("matti", "bpm");

        assertThat(getTenantAdministrationAPI().isPaused()).as("should not have tenant is paused mode").isFalse();

        installBusinessDataModel();

        assertThat(getTenantAdministrationAPI().isPaused()).as("should have resume tenant after installing Business Object Model").isFalse();
    }

    @After
    public void tearDown() throws Exception {
        uninstallBusinessObjectModel();
        deleteUser(matti);
        logoutOnTenant();
    }

    private void uninstallBusinessObjectModel() throws UpdateException, BusinessDataRepositoryDeploymentException {
        if (!getTenantAdministrationAPI().isPaused()) {
            getTenantAdministrationAPI().pause();
            getTenantAdministrationAPI().cleanAndUninstallBusinessDataModel();
            getTenantAdministrationAPI().resume();
        }
    }

    private void installBusinessDataModel() throws Exception {
        getTenantAdministrationAPI().pause();
        getTenantAdministrationAPI().cleanAndUninstallBusinessDataModel();

        final BusinessObject patte = new BusinessObject();
        patte.setQualifiedName("com.company.model.Patte");
        SimpleField taille = new SimpleField();
        taille.setName("taille");
        taille.setType(FieldType.INTEGER);
        patte.addField(taille);

        RelationField patteRelation = new RelationField();
        patteRelation.setType(RelationField.Type.AGGREGATION);
        patteRelation.setFetchType(RelationField.FetchType.LAZY);
        patteRelation.setName("patteAvantDroite");
        patteRelation.setNullable(true);
        patteRelation.setCollection(false);
        patteRelation.setReference(patte);


        BusinessObjectModel model = new BusinessObjectModel();
        BusinessObject cochon = new BusinessObject();
        cochon.setQualifiedName("com.company.model.Cochon");
        cochon.addField(patteRelation);
        model.addBusinessObject(patte);
        model.addBusinessObject(cochon);


        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        final byte[] zip = converter.zip(model);
        final String businessDataModelVersion = getTenantAdministrationAPI()
                .installBusinessDataModel(zip);
        getTenantAdministrationAPI().resume();
        assertThat(businessDataModelVersion).as("should have deployed BDM").isNotNull();
    }

    @Test
    public void should_bdm_work() throws Exception {
        //Create cochons + get the id of the first Patte
        ProcessDefinitionBuilder rempliDesCocohonsBuilder = new ProcessDefinitionBuilder().createNewInstance("RempliDesCocohons", "1.0");
        rempliDesCocohonsBuilder.addBusinessData("initCochons", "com.company.model.Cochon", new ExpressionBuilder().createGroovyScriptExpression("initCochons",
                "import com.company.model.Patte;\n" +
                        "import com.company.model.Cochon;\n" +
                        "\n" +
                        "Patte p = new Patte();\n" +
                        "p.setTaille(1);\n" +
                        "Cochon c = new Cochon();\n" +
                        "c.setPatteAvantDroite(p);\n" +
                        "return c;",
                "com.company.model.Cochon")).setMultiple(false);
        rempliDesCocohonsBuilder.addStartEvent("Démarrer2");
        rempliDesCocohonsBuilder.addUserTask("FillStep","Acteur1").addData("patteId",Long.class.getName(),new ExpressionBuilder().createGroovyScriptExpression(
                "getPatteId",
                "initCochons.patteAvantDroite.persistenceId",
                Long.class.getName(),
                new ExpressionBuilder().createBusinessDataExpression("initCochons", "com.company.model.Cochon")
        ));
        rempliDesCocohonsBuilder.addEndEvent("Fin1");
        rempliDesCocohonsBuilder.addTransition("Démarrer2", "Fin1");
        rempliDesCocohonsBuilder.addActor("Acteur1");
        rempliDesCocohonsBuilder.setActorInitiator("Acteur1");
        ProcessDefinition rempliProcess = deployAndEnableProcessWithActor(rempliDesCocohonsBuilder.done(), "Acteur1", matti);
        getProcessAPI().startProcess(rempliProcess.getId());
        long fillStep = waitForUserTask("FillStep");
        Long patteId = (Long) getProcessAPI().getActivityDataInstance("patteId", fillStep).getValue();


        //launch process that associate the patte to a cochon via operation
        ProcessDefinitionBuilder gestionDeCochonsBuilder = new ProcessDefinitionBuilder().createNewInstance("GestionDeCochons", "1.0");
        gestionDeCochonsBuilder.addBusinessData("cochon", "com.company.model.Cochon", new ExpressionBuilder().createQueryBusinessDataExpression(
                "Cochon.findByPersistenceId", "Cochon.findByPersistenceId", "com.company.model.Cochon",
                new ExpressionBuilder().createConstantLongExpression("persistenceId", 1)));
        gestionDeCochonsBuilder.addAutomaticTask("setPatteAvantDroite")
                .addOperation(new OperationBuilder().createBusinessDataSetAttributeOperation("cochon", "setPatteAvantDroite","com.company.model.Patte",
                        new ExpressionBuilder().createQueryBusinessDataExpression("Patte.findByPersistenceId","Patte.findByPersistenceId","com.company.model.Patte",
                                new ExpressionBuilder().createConstantLongExpression("persistenceId",patteId))));
        gestionDeCochonsBuilder.addUserTask("Étape1","Employee actor");
        gestionDeCochonsBuilder.addStartEvent("Démarrer1");
        gestionDeCochonsBuilder.addTransition("Démarrer1","setPatteAvantDroite");
        gestionDeCochonsBuilder.addTransition("setPatteAvantDroite","Étape1");
        gestionDeCochonsBuilder.addActor("Employee actor");
        gestionDeCochonsBuilder.setActorInitiator("Employee actor");

        ProcessDefinition gereProcess = deployAndEnableProcessWithActor(gestionDeCochonsBuilder.done(), "Employee actor", matti);
        getProcessAPI().startProcess(gereProcess.getId());

        waitForUserTask("Étape1");

        //then
        disableAndDeleteProcess(gereProcess,rempliProcess);

    }

}
