CREATE TABLE process_definition (
  tenantid BIGINT NOT NULL,
  id BIGINT NOT NULL,
  processId BIGINT NOT NULL,
  name VARCHAR(150) NOT NULL,
  version VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  deploymentDate BIGINT NOT NULL,
  deployedBy BIGINT NOT NULL,
  activationState VARCHAR(30) NOT NULL,
  configurationState VARCHAR(30) NOT NULL,
  migrationDate BIGINT,
  displayName VARCHAR(75),
  displayDescription VARCHAR(255),
  lastUpdateDate BIGINT,
  categoryId BIGINT,
  iconPath VARCHAR(255),
  PRIMARY KEY (tenantid, id),
  UNIQUE (tenantid, name, version)
);