package com.wu.common.core.mybatis.generator;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansField;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansGetter;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansSetter;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.internal.util.StringUtility;

public class DTOGenerator extends AbstractJavaGenerator {

	public DTOGenerator() {
		super();
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString("Progress.20", table.toString())); //$NON-NLS-1$
		Plugin plugins = context.getPlugins();
		CommentGenerator commentGenerator = context.getCommentGenerator();

		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseDTOType());
		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(topLevelClass);

		addSuperType(topLevelClass);

		commentGenerator.addModelClassComment(topLevelClass, introspectedTable);

		List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();

		if (introspectedTable.isConstructorBased()) {
			addParameterizedConstructor(topLevelClass);

			if (!introspectedTable.isImmutable()) {
				addDefaultConstructor(topLevelClass);
			}
		}

		String rootClass = getRootClass();

		Field serialVersionUID = new Field();
		serialVersionUID.setFinal(true);
		serialVersionUID.setStatic(true);
		serialVersionUID.setName("serialVersionUID = " + StringUtility.getLongUUID()+"L");
		serialVersionUID.setVisibility(JavaVisibility.PRIVATE);
		serialVersionUID.setType(new FullyQualifiedJavaType("long"));
		topLevelClass.addField(serialVersionUID);

		for (IntrospectedColumn introspectedColumn : introspectedColumns) {
			if (RootClassInfo.getInstance(rootClass, warnings).containsProperty(introspectedColumn)) {
				continue;
			}

			Field field = getJavaBeansField(introspectedColumn, context, introspectedTable);
			if (plugins.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable,
					Plugin.ModelClassType.BASE_RECORD)) {
				topLevelClass.addField(field);
				topLevelClass.addImportedType(field.getType());
			}

			Method method = getJavaBeansGetter(introspectedColumn, context, introspectedTable);
			if (plugins.modelGetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable,
					Plugin.ModelClassType.BASE_RECORD)) {
				topLevelClass.addMethod(method);
			}

			if (!introspectedTable.isImmutable()) {
				method = getJavaBeansSetter(introspectedColumn, context, introspectedTable);
				if (plugins.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn, introspectedTable,
						Plugin.ModelClassType.BASE_RECORD)) {
					topLevelClass.addMethod(method);
				}
			}
		}

		List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
		if (context.getPlugins().modelBaseRecordClassGenerated(topLevelClass, introspectedTable)) {
			answer.add(topLevelClass);
		}
		return answer;
	}

	private void addSuperType(TopLevelClass topLevelClass) {
		String rootClass = context.getJavaDTOGeneratorConfiguration().getProperty("rootClass");
		if (rootClass == null) {
			return;
		}
		FullyQualifiedJavaType type = new FullyQualifiedJavaType(rootClass);
		topLevelClass.setSuperClass(type);
		topLevelClass.addImportedType(type);

	}

	private void addParameterizedConstructor(TopLevelClass topLevelClass) {
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setConstructor(true);
		method.setName(topLevelClass.getType().getShortName());
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

		List<IntrospectedColumn> constructorColumns = introspectedTable.getAllColumns();

		for (IntrospectedColumn introspectedColumn : constructorColumns) {
			method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(),
					introspectedColumn.getJavaProperty()));
		}

		StringBuilder sb = new StringBuilder();
		if (introspectedTable.getRules().generatePrimaryKeyClass()) {
			boolean comma = false;
			sb.append("super("); //$NON-NLS-1$
			for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
				if (comma) {
					sb.append(", "); //$NON-NLS-1$
				} else {
					comma = true;
				}
				sb.append(introspectedColumn.getJavaProperty());
			}
			sb.append(");"); //$NON-NLS-1$
			method.addBodyLine(sb.toString());
		}

		List<IntrospectedColumn> introspectedColumns = introspectedTable.getAllColumns();

		for (IntrospectedColumn introspectedColumn : introspectedColumns) {
			sb.setLength(0);
			sb.append("this."); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.append(" = "); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.append(';');
			method.addBodyLine(sb.toString());
		}

		topLevelClass.addMethod(method);
	}

}
