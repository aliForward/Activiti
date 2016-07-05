/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.form.engine.impl.repository;

import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.activiti.editor.form.converter.FormJsonConverter;
import org.activiti.form.engine.ActivitiFormException;
import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.FormRepositoryServiceImpl;
import org.activiti.form.engine.impl.context.Context;
import org.activiti.form.engine.impl.persistence.entity.FormDeploymentEntity;
import org.activiti.form.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.form.engine.impl.persistence.entity.ResourceEntityManager;
import org.activiti.form.engine.repository.FormDeployment;
import org.activiti.form.engine.repository.FormDeploymentBuilder;
import org.activiti.form.model.FormDefinition;
import org.apache.commons.io.IOUtils;

/**
 * @author Tijs Rademakers
 */
public class FormDeploymentBuilderImpl implements FormDeploymentBuilder, Serializable {

  private static final long serialVersionUID = 1L;
  protected static final String DEFAULT_ENCODING = "UTF-8";

  protected transient FormRepositoryServiceImpl repositoryService;
  protected transient ResourceEntityManager resourceEntityManager;
  
  protected FormDeploymentEntity deployment;
  protected boolean isDuplicateFilterEnabled;

  public FormDeploymentBuilderImpl() {
    FormEngineConfiguration formEngineConfiguration = Context.getFormEngineConfiguration();
    this.repositoryService = formEngineConfiguration.getFormRepositoryService();
    this.deployment = formEngineConfiguration.getDeploymentEntityManager().create();
    this.resourceEntityManager = formEngineConfiguration.getResourceEntityManager();
  }

  public FormDeploymentBuilder addInputStream(String resourceName, InputStream inputStream) {
    if (inputStream == null) {
      throw new ActivitiFormException("inputStream for resource '" + resourceName + "' is null");
    }

    byte[] bytes = null;
    try {
      bytes = IOUtils.toByteArray(inputStream);
    } catch (Exception e) {
      throw new ActivitiFormException("could not get byte array from resource '" + resourceName + "'");
    }
    
    if (bytes == null) {
      throw new ActivitiFormException("byte array for resource '" + resourceName + "' is null");
    }
    
    ResourceEntity resource = resourceEntityManager.create();
    resource.setName(resourceName);
    resource.setBytes(bytes);
    deployment.addResource(resource);
    return this;
  }

  public FormDeploymentBuilder addClasspathResource(String resource) {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(resource);
    if (inputStream == null) {
      throw new ActivitiFormException("resource '" + resource + "' not found");
    }
    return addInputStream(resource, inputStream);
  }

  public FormDeploymentBuilder addString(String resourceName, String text) {
    if (text == null) {
      throw new ActivitiFormException("text is null");
    }

    ResourceEntity resource = resourceEntityManager.create();
    resource.setName(resourceName);
    try {
      resource.setBytes(text.getBytes(DEFAULT_ENCODING));
    } catch (UnsupportedEncodingException e) {
      throw new ActivitiFormException("Unable to get process bytes.", e);
    }
    deployment.addResource(resource);
    return this;
  }

  public FormDeploymentBuilder addFormModel(String resourceName, FormDefinition formDefinition) {
    FormJsonConverter formLConverter = new FormJsonConverter();
    String dmn20Xml = formLConverter.convertToJson(formDefinition);
    addString(resourceName, dmn20Xml);
    
    return this;
  }

  public FormDeploymentBuilder name(String name) {
      deployment.setName(name);
      return this;
  }

  public FormDeploymentBuilder category(String category) {
      deployment.setCategory(category);
      return this;
  }

  public FormDeploymentBuilder tenantId(String tenantId) {
      deployment.setTenantId(tenantId);
      return this;
  }

  public FormDeployment deploy() {
      return repositoryService.deploy(this);
  }

  // getters and setters
  // //////////////////////////////////////////////////////

  public FormDeploymentEntity getDeployment() {
      return deployment;
  }

  public boolean isDuplicateFilterEnabled() {
    return isDuplicateFilterEnabled;
  }
}
