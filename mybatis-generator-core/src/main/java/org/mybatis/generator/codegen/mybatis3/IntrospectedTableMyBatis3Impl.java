/**
 *    Copyright ${license.git.copyrightYears} the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.AnnotatedClientGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.JavaMapperGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.MixedClientGenerator;
import org.mybatis.generator.codegen.mybatis3.model.BaseRecordGenerator;
import org.mybatis.generator.codegen.mybatis3.model.ExampleGenerator;
import org.mybatis.generator.codegen.mybatis3.model.PrimaryKeyGenerator;
import org.mybatis.generator.codegen.mybatis3.model.RecordWithBLOBsGenerator;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.ObjectFactory;

import com.wu.common.core.mybatis.generator.DTOGenerator;
import com.wu.common.core.mybatis.generator.ServiceGenerator;
import com.wu.common.core.mybatis.generator.ServiceImplGenerator;
import com.wu.common.core.mybatis.generator.XMLMapperGenerator;

/**
 * The Class IntrospectedTableMyBatis3Impl.
 *
 * @author Jeff Butler
 */
public class IntrospectedTableMyBatis3Impl extends IntrospectedTable {

	/** The java model generators. */
	protected List<AbstractJavaGenerator> javaModelGenerators;

	/** The client generators. */
	protected List<AbstractJavaGenerator> clientGenerators;

	/** The xml mapper generator. */
	protected AbstractXmlGenerator xmlMapperGenerator;

	protected AbstractJavaGenerator dtoGenerator;

	protected List<AbstractJavaGenerator> serviceGeneratorList = new ArrayList<>();

	/**
	 * Instantiates a new introspected table my batis3 impl.
	 */
	public IntrospectedTableMyBatis3Impl() {
		super(TargetRuntime.MYBATIS3);
		javaModelGenerators = new ArrayList<AbstractJavaGenerator>();
		clientGenerators = new ArrayList<AbstractJavaGenerator>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mybatis.generator.api.IntrospectedTable#calculateGenerators(java.util
	 * .List, org.mybatis.generator.api.ProgressCallback)
	 */
	@Override
	public void calculateGenerators(List<String> warnings, ProgressCallback progressCallback) {
		calculateJavaModelGenerators(warnings, progressCallback);

		AbstractJavaClientGenerator javaClientGenerator = calculateClientGenerators(warnings, progressCallback);
		calculateJavaDTOGenerator(warnings, progressCallback);
		calculateJavaServiceGenerator(warnings, progressCallback);
		calculateXmlMapperGenerator(javaClientGenerator, warnings, progressCallback);
	}

	/**
	 * Calculate xml mapper generator.
	 *
	 * @param javaClientGenerator
	 *            the java client generator
	 * @param warnings
	 *            the warnings
	 * @param progressCallback
	 *            the progress callback
	 */
	protected void calculateXmlMapperGenerator(AbstractJavaClientGenerator javaClientGenerator, List<String> warnings,
			ProgressCallback progressCallback) {
		if (javaClientGenerator == null) {
			if (context.getSqlMapGeneratorConfiguration() != null) {
				xmlMapperGenerator = new XMLMapperGenerator();
			}
		} else {
			xmlMapperGenerator = javaClientGenerator.getMatchedXMLGenerator();
		}

		initializeAbstractGenerator(xmlMapperGenerator, warnings, progressCallback);
	}

	/**
	 * Calculate client generators.
	 *
	 * @param warnings
	 *            the warnings
	 * @param progressCallback
	 *            the progress callback
	 * @return true if an XML generator is required
	 */
	protected AbstractJavaClientGenerator calculateClientGenerators(List<String> warnings,
			ProgressCallback progressCallback) {
		if (!rules.generateJavaClient()) {
			return null;
		}

		AbstractJavaClientGenerator javaGenerator = createJavaClientGenerator();
		if (javaGenerator == null) {
			return null;
		}

		initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
		clientGenerators.add(javaGenerator);

		return javaGenerator;
	}

	protected AbstractJavaGenerator createExampleGenerator() {
		String exampleType = context.getJavaModelGeneratorConfiguration().getProperty("exampleType");
		if (exampleType == null) {
			return new ExampleGenerator();
		}
		return (AbstractJavaGenerator) ObjectFactory.createInternalObject(exampleType);
	}

	/**
	 * Creates the java client generator.
	 *
	 * @return the abstract java client generator
	 */
	protected AbstractJavaClientGenerator createJavaClientGenerator() {
		if (context.getJavaClientGeneratorConfiguration() == null) {
			return null;
		}

		String type = context.getJavaClientGeneratorConfiguration().getConfigurationType();

		AbstractJavaClientGenerator javaGenerator;
		if ("XMLMAPPER".equalsIgnoreCase(type)) { //$NON-NLS-1$
			javaGenerator = new JavaMapperGenerator();
		} else if ("MIXEDMAPPER".equalsIgnoreCase(type)) { //$NON-NLS-1$
			javaGenerator = new MixedClientGenerator();
		} else if ("ANNOTATEDMAPPER".equalsIgnoreCase(type)) { //$NON-NLS-1$
			javaGenerator = new AnnotatedClientGenerator();
		} else if ("MAPPER".equalsIgnoreCase(type)) { //$NON-NLS-1$
			javaGenerator = new JavaMapperGenerator();
		} else {
			javaGenerator = (AbstractJavaClientGenerator) ObjectFactory.createInternalObject(type);
		}

		return javaGenerator;
	}

	/**
	 * Calculate java model generators.
	 *
	 * @param warnings
	 *            the warnings
	 * @param progressCallback
	 *            the progress callback
	 */
	protected void calculateJavaModelGenerators(List<String> warnings, ProgressCallback progressCallback) {
		if (getRules().generateExampleClass()) {
			AbstractJavaGenerator javaGenerator = createExampleGenerator();
			initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
			javaModelGenerators.add(javaGenerator);
		}

		if (getRules().generatePrimaryKeyClass()) {
			AbstractJavaGenerator javaGenerator = new PrimaryKeyGenerator();
			initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
			javaModelGenerators.add(javaGenerator);
		}

		if (getRules().generateBaseRecordClass()) {
			AbstractJavaGenerator javaGenerator = new BaseRecordGenerator();
			initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
			javaModelGenerators.add(javaGenerator);
		}

		if (getRules().generateRecordWithBLOBsClass()) {
			AbstractJavaGenerator javaGenerator = new RecordWithBLOBsGenerator();
			initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
			javaModelGenerators.add(javaGenerator);
		}
	}

	protected void calculateJavaDTOGenerator(List<String> warnings, ProgressCallback progressCallback) {
		if (this.getBaseDTOType() == null) {
			return;
		}
		dtoGenerator = new DTOGenerator();
		initializeAbstractGenerator(dtoGenerator, warnings, progressCallback);
	}

	protected void calculateJavaServiceGenerator(List<String> warnings, ProgressCallback progressCallback) {
		if (this.getBaseServiceType() == null) {
			return;
		}

		AbstractJavaGenerator serviceGenerator = new ServiceGenerator();
		initializeAbstractGenerator(serviceGenerator, warnings, progressCallback);
		serviceGeneratorList.add(serviceGenerator);

		AbstractJavaGenerator serviceImplGenerator = new ServiceImplGenerator();
		initializeAbstractGenerator(serviceImplGenerator, warnings, progressCallback);
		serviceGeneratorList.add(serviceImplGenerator);
	}

	/**
	 * Initialize abstract generator.
	 *
	 * @param abstractGenerator
	 *            the abstract generator
	 * @param warnings
	 *            the warnings
	 * @param progressCallback
	 *            the progress callback
	 */
	protected void initializeAbstractGenerator(AbstractGenerator abstractGenerator, List<String> warnings,
			ProgressCallback progressCallback) {
		if (abstractGenerator == null) {
			return;
		}

		abstractGenerator.setContext(context);
		abstractGenerator.setIntrospectedTable(this);
		abstractGenerator.setProgressCallback(progressCallback);
		abstractGenerator.setWarnings(warnings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mybatis.generator.api.IntrospectedTable#getGeneratedJavaFiles()
	 */
	@Override
	public List<GeneratedJavaFile> getGeneratedJavaFiles() {
		List<GeneratedJavaFile> answer = new ArrayList<GeneratedJavaFile>();

		for (AbstractJavaGenerator javaGenerator : javaModelGenerators) {
			List<CompilationUnit> compilationUnits = javaGenerator.getCompilationUnits();
			for (CompilationUnit compilationUnit : compilationUnits) {
				GeneratedJavaFile gjf = new GeneratedJavaFile(compilationUnit,
						context.getJavaModelGeneratorConfiguration().getTargetProject(),
						context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
				gjf.setOverwriteEnabled(true);
				answer.add(gjf);
			}
		}

		for (AbstractJavaGenerator javaGenerator : clientGenerators) {
			List<CompilationUnit> compilationUnits = javaGenerator.getCompilationUnits();
			for (CompilationUnit compilationUnit : compilationUnits) {
				GeneratedJavaFile gjf = new GeneratedJavaFile(compilationUnit,
						context.getJavaClientGeneratorConfiguration().getTargetProject(),
						context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
				// gjf.setOverwriteEnabled(true);
				gjf.setMergeable(true);
				answer.add(gjf);
			}
		}

		if (dtoGenerator != null) {
			List<CompilationUnit> compilationUnits = dtoGenerator.getCompilationUnits();
			for (CompilationUnit compilationUnit : compilationUnits) {
				GeneratedJavaFile gjf = new GeneratedJavaFile(compilationUnit,
						context.getJavaClientGeneratorConfiguration().getTargetProject(),
						context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
				answer.add(gjf);
			}
		}

		for (AbstractJavaGenerator serviceGenerator : serviceGeneratorList) {
			List<CompilationUnit> compilationUnits = serviceGenerator.getCompilationUnits();
			for (CompilationUnit compilationUnit : compilationUnits) {
				GeneratedJavaFile gjf = new GeneratedJavaFile(compilationUnit,
						context.getJavaClientGeneratorConfiguration().getTargetProject(),
						context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING), context.getJavaFormatter());
				answer.add(gjf);
			}
		}

		return answer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mybatis.generator.api.IntrospectedTable#getGeneratedXmlFiles()
	 */
	@Override
	public List<GeneratedXmlFile> getGeneratedXmlFiles() {
		List<GeneratedXmlFile> answer = new ArrayList<GeneratedXmlFile>();

		if (xmlMapperGenerator != null) {
			Document document = xmlMapperGenerator.getDocument();
			GeneratedXmlFile gxf = new GeneratedXmlFile(document, getMyBatis3XmlMapperFileName(),
					getMyBatis3XmlMapperPackage(), context.getSqlMapGeneratorConfiguration().getTargetProject(), true,
					context.getXmlFormatter());
			if (context.getPlugins().sqlMapGenerated(gxf, this)) {
				answer.add(gxf);
			}
		}

		return answer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mybatis.generator.api.IntrospectedTable#getGenerationSteps()
	 */
	@Override
	public int getGenerationSteps() {
		return javaModelGenerators.size() + clientGenerators.size() + (xmlMapperGenerator == null ? 0 : 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mybatis.generator.api.IntrospectedTable#isJava5Targeted()
	 */
	@Override
	public boolean isJava5Targeted() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mybatis.generator.api.IntrospectedTable#requiresXMLGenerator()
	 */
	@Override
	public boolean requiresXMLGenerator() {
		AbstractJavaClientGenerator javaClientGenerator = createJavaClientGenerator();

		if (javaClientGenerator == null) {
			return false;
		} else {
			return javaClientGenerator.requiresXMLGenerator();
		}
	}
}
