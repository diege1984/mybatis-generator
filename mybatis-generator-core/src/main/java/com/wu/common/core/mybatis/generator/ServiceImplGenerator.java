package com.wu.common.core.mybatis.generator;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractJavaGenerator;

public class ServiceImplGenerator extends AbstractJavaGenerator {

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString("Progress.21", table.toString())); //$NON-NLS-1$
		CommentGenerator commentGenerator = context.getCommentGenerator();
		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseServiceImplType());

		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addClassComment(topLevelClass, introspectedTable);

		addSuperType(topLevelClass);
		
		Field mapper = new Field();
		mapper.setName("mapper");
		mapper.setType(getMapper());
		mapper.addAnnotation("@Autowired");
		topLevelClass.addField(mapper);
		topLevelClass.addImportedType("org.springframework.beans.factory.annotation.Autowired");
		topLevelClass.addImportedType(getMapper());
		
		if (introspectedTable.getRules().generateSelectByPrimaryKey()) {
			FullyQualifiedJavaType primaryType = getPrimaryType();
			Method method = new Method();
			method.setVisibility(JavaVisibility.PUBLIC);
			method.setName("selectDTOByPrimary");
			method.setReturnType(new FullyQualifiedJavaType(introspectedTable.getBaseDTOType()));
			method.addParameter(new Parameter(primaryType, "id"));
			method.addAnnotation("@Override");
			
			method.addBodyLine("return super.selectDTOByPrimary(id);");
			commentGenerator.addGeneralMethodComment(method, "根据主键查询");
			topLevelClass.addMethod(method);
			
			Method deleteMethod = new Method();
			deleteMethod.setVisibility(JavaVisibility.PUBLIC);
			deleteMethod.setName("logicDeleteByPrimary");
			deleteMethod.setReturnType(new FullyQualifiedJavaType("int"));
			deleteMethod.addParameter(new Parameter(primaryType, "id"));
			deleteMethod.addBodyLine("return mapper.logicDeleteByPrimaryKey(id);");
			deleteMethod.addAnnotation("@Override");
			commentGenerator.addGeneralMethodComment(deleteMethod, "根据主键逻辑删除");
			topLevelClass.addMethod(deleteMethod);
			
			Method updateMethod = new Method();
			updateMethod.setVisibility(JavaVisibility.PUBLIC);
			updateMethod.setName("updateByPrimary");
			updateMethod.setReturnType(new FullyQualifiedJavaType("int"));
			updateMethod.addParameter(new Parameter(getDTOType(),"dto"));
			updateMethod.addBodyLine(getEntityType().getShortName() + " entity = toEntity(dto);");
			updateMethod.addBodyLine("return mapper.updateByPrimaryKey(entity);");
			commentGenerator.addGeneralMethodComment(updateMethod, "根据主键更新");
			topLevelClass.addMethod(updateMethod);
			
			Method updateSelectiveMethod = new Method();
			updateSelectiveMethod.setVisibility(JavaVisibility.PUBLIC);
			updateSelectiveMethod.setName("updateSelectiveByPrimary");
			updateSelectiveMethod.addParameter(new Parameter(getDTOType(),"dto"));
			updateSelectiveMethod.setReturnType(new FullyQualifiedJavaType("int"));
			updateSelectiveMethod.addBodyLine(getEntityType().getShortName() + " entity = toEntity(dto);");
			updateSelectiveMethod.addBodyLine("return mapper.updateByPrimaryKeySelective(entity);");
			commentGenerator.addGeneralMethodComment(updateSelectiveMethod, "根据主键更新");
			topLevelClass.addMethod(updateSelectiveMethod);
			
		}
		
		Method pageMethod = new Method();
		pageMethod.setVisibility(JavaVisibility.PUBLIC);
		pageMethod.setName("selectByPage");
		FullyQualifiedJavaType pageType = pageType();
		pageMethod.setReturnType(pageType);
		pageMethod.addParameter(new Parameter(getDTOType(),"dto"));
		
		pageMethod.addBodyLine(getExampleType().getShortName() + " example = new " + getExampleType().getShortName() + "();");
		pageMethod.addBodyLine(getExampleType().getShortName() + ".Criteria criteria = example.createCriteria();");
		pageMethod.addBodyLine("return super.selectDTOPageListByExample(example);");
		pageMethod.addAnnotation("@Override");
		commentGenerator.addGeneralMethodComment(pageMethod, "分页查询");
		topLevelClass.addMethod(pageMethod);
		topLevelClass.addImportedType(pageType);
		
		List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
		answer.add(topLevelClass);
		return answer;
	}
	
	private FullyQualifiedJavaType pageType(){
		FullyQualifiedJavaType type = new FullyQualifiedJavaType("com.wu.wuji.common.page.PageBean");
		type.addTypeArgument(getDTOType());
		return type;
	}
	
	private void addSuperType(TopLevelClass topLevelClass) {
		String rootClass = context.getJavaServiceGeneratorConfiguration().getProperty("rootClass");
		if (rootClass == null) {
			return;
		}
		FullyQualifiedJavaType type = new FullyQualifiedJavaType(rootClass);
		
		type.addTypeArgument(getEntityType());
		type.addTypeArgument(getDTOType());
		type.addTypeArgument(getExampleType());
		
		topLevelClass.setSuperClass(type);
		topLevelClass.addImportedType(type);
		topLevelClass.addImportedType(getDTOType());
		topLevelClass.addImportedType(getEntityType());
		topLevelClass.addImportedType(getExampleType());
		
		FullyQualifiedJavaType rootInterface = new FullyQualifiedJavaType(introspectedTable.getBaseServiceType());
		topLevelClass.addSuperInterface(rootInterface);
		topLevelClass.addImportedType(rootInterface);
	}

}
