package com.wu.common.core.mybatis.generator;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.codegen.AbstractJavaGenerator;

public class ServiceGenerator extends AbstractJavaGenerator {

	public ServiceGenerator() {
		super();
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {

		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString("Progress.21", table.toString())); //$NON-NLS-1$
		CommentGenerator commentGenerator = context.getCommentGenerator();
		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseServiceType());

		Interface interfaze = new Interface(type);
		interfaze.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addInterfaceComment(interfaze, introspectedTable);

		addSuperType(interfaze);

		if (introspectedTable.getRules().generateSelectByPrimaryKey()) {
			FullyQualifiedJavaType primaryType = getPrimaryType();

			Method method = new Method();
			method.setVisibility(JavaVisibility.PUBLIC);
			method.setName("selectDTOByPrimary");
			method.setReturnType(new FullyQualifiedJavaType(introspectedTable.getBaseDTOType()));
			method.addParameter(new Parameter(primaryType, "id"));
			commentGenerator.addGeneralMethodComment(method, "根据主键查询");
			interfaze.addMethod(method);
			
			Method deleteMethod = new Method();
			deleteMethod.setVisibility(JavaVisibility.PUBLIC);
			deleteMethod.setName("logicDeleteByPrimary");
			deleteMethod.setReturnType(new FullyQualifiedJavaType("int"));
			deleteMethod.addParameter(new Parameter(primaryType, "id"));
			commentGenerator.addGeneralMethodComment(deleteMethod, "根据主键逻辑删除");
			interfaze.addMethod(deleteMethod);
			
			Method updateMethod = new Method();
			updateMethod.setVisibility(JavaVisibility.PUBLIC);
			updateMethod.setName("updateByPrimary");
			updateMethod.addParameter(new Parameter(getDTOType(),"dto"));
			updateMethod.setReturnType(new FullyQualifiedJavaType("int"));
			commentGenerator.addGeneralMethodComment(updateMethod, "根据主键更新");
			interfaze.addMethod(updateMethod);
			
			Method updateSelectiveMethod = new Method();
			updateSelectiveMethod.setVisibility(JavaVisibility.PUBLIC);
			updateSelectiveMethod.setName("updateSelectiveByPrimary");
			updateSelectiveMethod.addParameter(new Parameter(getDTOType(),"dto"));
			updateSelectiveMethod.setReturnType(new FullyQualifiedJavaType("int"));
			commentGenerator.addGeneralMethodComment(updateSelectiveMethod, "根据主键更新");
			interfaze.addMethod(updateSelectiveMethod);
		}
		
		Method pageMethod = new Method();
		pageMethod.setVisibility(JavaVisibility.PUBLIC);
		pageMethod.setName("selectByPage");
		FullyQualifiedJavaType pageType = pageType();
		pageMethod.setReturnType(pageType);
		pageMethod.addParameter(new Parameter(getDTOType(),"dto"));
		commentGenerator.addGeneralMethodComment(pageMethod, "分页查询");
		interfaze.addMethod(pageMethod);
		interfaze.addImportedType(pageType);

		List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
		answer.add(interfaze);
		return answer;
	}
	
	private FullyQualifiedJavaType pageType(){
		FullyQualifiedJavaType type = new FullyQualifiedJavaType("com.wu.wuji.common.page.PageBean");
		type.addTypeArgument(getDTOType());
		return type;
	}

	private void addSuperType(Interface interfaze) {
		String rootClass = context.getJavaServiceGeneratorConfiguration().getProperty("rootInterface");
		if (rootClass == null) {
			return;
		}
		FullyQualifiedJavaType type = new FullyQualifiedJavaType(rootClass);

		type.addTypeArgument(getDTOType());

		interfaze.addSuperInterface(type);
		interfaze.addImportedType(type);
		interfaze.addImportedType(getDTOType());
	}
	
}
