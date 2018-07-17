package com.wu.common.core.mybatis.generator;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

/**
 * 
 * @author Jeff Butler
 * 
 */
public class ExampleGenerator extends AbstractJavaGenerator {

	public ExampleGenerator() {
		super();
	}
	
	private void addSuperType(TopLevelClass topLevelClass){
		String exampleInterface = context.getJavaModelGeneratorConfiguration().getProperty("exampleInterface");
		if(exampleInterface == null){
			return;
		}
		FullyQualifiedJavaType type = new FullyQualifiedJavaType(exampleInterface);
		topLevelClass.setSuperClass(type);
		topLevelClass.addImportedType(type);
	}

	@Override
	public List<CompilationUnit> getCompilationUnits() {
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		progressCallback.startTask(getString("Progress.6", table.toString())); //$NON-NLS-1$
		CommentGenerator commentGenerator = context.getCommentGenerator();

		FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getExampleType());
		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		commentGenerator.addJavaFileComment(topLevelClass);
		commentGenerator.addClassComment(topLevelClass, introspectedTable);
		
		addSuperType(topLevelClass);
		topLevelClass.addImportedType(new FullyQualifiedJavaType("java.util.List"));
		
		// add default constructor
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setConstructor(true);
		method.setName(type.getShortName());
		method.addBodyLine("super();"); //$NON-NLS-1$

		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);

		String entitySuperInterface = context.getJavaModelGeneratorConfiguration().getProperty("entitySuperInterface");
		if(entitySuperInterface != null){
			FullyQualifiedJavaType modelType = new FullyQualifiedJavaType(entitySuperInterface);
			topLevelClass.addImportedType(modelType);
			Method constructorMethod = new Method();
			constructorMethod.setVisibility(JavaVisibility.PUBLIC);
			constructorMethod.setConstructor(true);
			constructorMethod.setName(type.getShortName());
			constructorMethod.addParameter(new Parameter(modelType, "data"));
			constructorMethod.addBodyLine("super(data);");
			commentGenerator.addGeneralMethodComment(constructorMethod, introspectedTable);
			topLevelClass.addMethod(constructorMethod);
		}

		method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName("or"); //$NON-NLS-1$
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
		method.addBodyLine("Criteria criteria = createCriteriaInternal();"); //$NON-NLS-1$
		method.addBodyLine("oredCriteria.add(criteria);"); //$NON-NLS-1$
		method.addBodyLine("return criteria;"); //$NON-NLS-1$
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);

		method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName("createCriteria"); //$NON-NLS-1$
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
		method.addBodyLine("Criteria criteria = createCriteriaInternal();"); //$NON-NLS-1$
		method.addBodyLine("if (oredCriteria.size() == 0) {"); //$NON-NLS-1$
		method.addBodyLine("oredCriteria.add(criteria);"); //$NON-NLS-1$
		method.addBodyLine("}"); //$NON-NLS-1$
		method.addBodyLine("return criteria;"); //$NON-NLS-1$
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);

		method = new Method();
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setName("createCriteriaInternal"); //$NON-NLS-1$
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
		method.addBodyLine("Criteria criteria = new Criteria();"); //$NON-NLS-1$
		method.addBodyLine("return criteria;"); //$NON-NLS-1$
		commentGenerator.addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);

		// now generate the inner class that holds the AND conditions
		topLevelClass.addInnerClass(getGeneratedCriteriaInnerClass(topLevelClass));

		topLevelClass.addInnerClass(getCriteriaInnerClass());

		//topLevelClass.addInnerClass(getCriterionInnerClass());

		List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
		if (context.getPlugins().modelExampleClassGenerated(topLevelClass, introspectedTable)) {
			answer.add(topLevelClass);
		}
		return answer;
	}


	private InnerClass getCriteriaInnerClass() {
		Method method;

		InnerClass answer = new InnerClass(FullyQualifiedJavaType.getCriteriaInstance());

		answer.setVisibility(JavaVisibility.PUBLIC);
		//answer.setStatic(true);
		answer.setSuperClass(FullyQualifiedJavaType.getGeneratedCriteriaInstance());

		context.getCommentGenerator().addClassComment(answer, introspectedTable, true);

		method = new Method();
		method.setVisibility(JavaVisibility.PROTECTED);
		method.setName("Criteria"); //$NON-NLS-1$
		method.setConstructor(true);
		method.addBodyLine("super();"); //$NON-NLS-1$
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
		answer.addMethod(method);
		return answer;
	}
	
	private void addCriteriaSuperClass(InnerClass answer,TopLevelClass topLevelClass){
		String superInterface = context.getJavaModelGeneratorConfiguration().getProperty("criteriaInterface");
		if(superInterface == null){
			return;
		}
		FullyQualifiedJavaType type = new FullyQualifiedJavaType(superInterface);
		answer.setSuperClass(type);
		topLevelClass.addImportedType(type);
	}

	private InnerClass getGeneratedCriteriaInnerClass(TopLevelClass topLevelClass) {
		InnerClass answer = new InnerClass(FullyQualifiedJavaType.getGeneratedCriteriaInstance());

		answer.setVisibility(JavaVisibility.PROTECTED);
		//answer.setStatic(true);
		answer.setAbstract(true);
		context.getCommentGenerator().addClassComment(answer, introspectedTable);
		addCriteriaSuperClass(answer,topLevelClass);

		for (IntrospectedColumn introspectedColumn : introspectedTable.getNonBLOBColumns()) {
			topLevelClass.addImportedType(introspectedColumn.getFullyQualifiedJavaType());

			// here we need to add the individual methods for setting the
			// conditions for a field
			answer.addMethod(getSetNullMethod(introspectedColumn));
			answer.addMethod(getSetNotNullMethod(introspectedColumn));
			answer.addMethod(getSetEqualMethod(introspectedColumn));
			answer.addMethod(getSetNotEqualMethod(introspectedColumn));
			answer.addMethod(getSetGreaterThanMethod(introspectedColumn));
			answer.addMethod(getSetGreaterThenOrEqualMethod(introspectedColumn));
			answer.addMethod(getSetLessThanMethod(introspectedColumn));
			answer.addMethod(getSetLessThanOrEqualMethod(introspectedColumn));

			if (introspectedColumn.isJdbcCharacterColumn()) {
				answer.addMethod(getSetLikeMethod(introspectedColumn));
				answer.addMethod(getSetNotLikeMethod(introspectedColumn));
			}

			answer.addMethod(getSetInOrNotInMethod(introspectedColumn, true));
			answer.addMethod(getSetInOrNotInMethod(introspectedColumn, false));
			answer.addMethod(getSetBetweenOrNotBetweenMethod(introspectedColumn, true));
			answer.addMethod(getSetBetweenOrNotBetweenMethod(introspectedColumn, false));
		}

		return answer;
	}

	private Method getSetNullMethod(IntrospectedColumn introspectedColumn) {
		return getNoValueMethod(introspectedColumn, "IsNull", "is null"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetNotNullMethod(IntrospectedColumn introspectedColumn) {
		return getNoValueMethod(introspectedColumn, "IsNotNull", "is not null"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetEqualMethod(IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn, "EqualTo", "="); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetNotEqualMethod(IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn, "NotEqualTo", "<>"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetGreaterThanMethod(IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn, "GreaterThan", ">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetGreaterThenOrEqualMethod(IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn, "GreaterThanOrEqualTo", ">="); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetLessThanMethod(IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn, "LessThan", "<"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetLessThanOrEqualMethod(IntrospectedColumn introspectedColumn) {
		return getSingleValueMethod(introspectedColumn, "LessThanOrEqualTo", "<="); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetLikeMethod(IntrospectedColumn introspectedColumn) {
		return getLikeValueMethod(introspectedColumn, "Like", "like"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSetNotLikeMethod(IntrospectedColumn introspectedColumn) {
		return getLikeValueMethod(introspectedColumn, "NotLike", "not like"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Method getSingleValueMethod(IntrospectedColumn introspectedColumn, String nameFragment, String operator) {
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), "value")); //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		sb.append(introspectedColumn.getJavaProperty());
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		sb.insert(0, "and"); //$NON-NLS-1$
		sb.append(nameFragment);
		method.setName(sb.toString());
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
		sb.setLength(0);

		if (introspectedColumn.isJDBCDateColumn()) {
			sb.append("addCriterionForJDBCDate(\""); //$NON-NLS-1$
		} else if (introspectedColumn.isJDBCTimeColumn()) {
			sb.append("addCriterionForJDBCTime(\""); //$NON-NLS-1$
		} else if (stringHasValue(introspectedColumn.getTypeHandler())) {
			sb.append("add"); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
			sb.append("Criterion(\""); //$NON-NLS-1$
		} else {
			sb.append("addCriterion(\""); //$NON-NLS-1$
		}

		sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
		sb.append(' ');
		sb.append(operator);
        sb.append("\", "); //$NON-NLS-1$
        sb.append("value"); //$NON-NLS-1$
        sb.append(", \""); //$NON-NLS-1$
		sb.append(introspectedColumn.getJavaProperty());
		sb.append("\");"); //$NON-NLS-1$
		method.addBodyLine(sb.toString());
		method.addBodyLine("return (Criteria) this;"); //$NON-NLS-1$
		CommentGenerator comment = context.getCommentGenerator();
		comment.addGeneralMethodComment(method, introspectedTable);
		return method;
	}
	
	private Method getLikeValueMethod(IntrospectedColumn introspectedColumn, String nameFragment, String operator) {
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.addParameter(new Parameter(introspectedColumn.getFullyQualifiedJavaType(), "value")); //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		sb.append(introspectedColumn.getJavaProperty());
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		sb.insert(0, "and"); //$NON-NLS-1$
		sb.append(nameFragment);
		method.setName(sb.toString());
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
		sb.setLength(0);

		if (introspectedColumn.isJDBCDateColumn()) {
			sb.append("addCriterionForJDBCDate(\""); //$NON-NLS-1$
		} else if (introspectedColumn.isJDBCTimeColumn()) {
			sb.append("addCriterionForJDBCTime(\""); //$NON-NLS-1$
		} else if (stringHasValue(introspectedColumn.getTypeHandler())) {
			sb.append("add"); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
			sb.append("Criterion(\""); //$NON-NLS-1$
		} else {
			sb.append("addCriterion(\""); //$NON-NLS-1$
		}

		sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
		sb.append(' ');
		sb.append(operator);
        sb.append("\", \"%\" + "); //$NON-NLS-1$
        sb.append("value"); //$NON-NLS-1$
        sb.append(" + \"%\", \""); //$NON-NLS-1$
		sb.append(introspectedColumn.getJavaProperty());
		sb.append("\");"); //$NON-NLS-1$
		method.addBodyLine(sb.toString());
		method.addBodyLine("return (Criteria) this;"); //$NON-NLS-1$
		CommentGenerator comment = context.getCommentGenerator();
		comment.addGeneralMethodComment(method, introspectedTable);
		return method;
	}

	/**
	 * Generates methods that set between and not between conditions
	 * 
	 * @param introspectedColumn
	 * @param betweenMethod
	 * @return a generated method for the between or not between method
	 */
	private Method getSetBetweenOrNotBetweenMethod(IntrospectedColumn introspectedColumn, boolean betweenMethod) {
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();

		method.addParameter(new Parameter(type, "value1")); //$NON-NLS-1$
		method.addParameter(new Parameter(type, "value2")); //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		sb.append(introspectedColumn.getJavaProperty());
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		sb.insert(0, "and"); //$NON-NLS-1$
		if (betweenMethod) {
			sb.append("Between"); //$NON-NLS-1$
		} else {
			sb.append("NotBetween"); //$NON-NLS-1$
		}
		method.setName(sb.toString());
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
		sb.setLength(0);

		if (introspectedColumn.isJDBCDateColumn()) {
			sb.append("addCriterionForJDBCDate(\""); //$NON-NLS-1$
		} else if (introspectedColumn.isJDBCTimeColumn()) {
			sb.append("addCriterionForJDBCTime(\""); //$NON-NLS-1$
		} else if (stringHasValue(introspectedColumn.getTypeHandler())) {
			sb.append("add"); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
			sb.append("Criterion(\""); //$NON-NLS-1$
		} else {
			sb.append("addCriterion(\""); //$NON-NLS-1$
		}

		sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
		if (betweenMethod) {
			sb.append(" between"); //$NON-NLS-1$
		} else {
			sb.append(" not between"); //$NON-NLS-1$
		}
		sb.append("\", "); //$NON-NLS-1$
		sb.append("value1, value2"); //$NON-NLS-1$
		sb.append(", \""); //$NON-NLS-1$
		sb.append(introspectedColumn.getJavaProperty());
		sb.append("\");"); //$NON-NLS-1$
		method.addBodyLine(sb.toString());
		method.addBodyLine("return (Criteria) this;"); //$NON-NLS-1$
		CommentGenerator comment = context.getCommentGenerator();
		comment.addGeneralMethodComment(method, introspectedTable);
		return method;
	}

	/**
	 * 
	 * @param introspectedColumn
	 * @param inMethod
	 *            if true generates an "in" method, else generates a "not in"
	 *            method
	 * @return a generated method for the in or not in method
	 */
	private Method getSetInOrNotInMethod(IntrospectedColumn introspectedColumn, boolean inMethod) {
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		FullyQualifiedJavaType type = FullyQualifiedJavaType.getNewListInstance();
		if (introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
			type.addTypeArgument(introspectedColumn.getFullyQualifiedJavaType().getPrimitiveTypeWrapper());
		} else {
			type.addTypeArgument(introspectedColumn.getFullyQualifiedJavaType());
		}

		method.addParameter(new Parameter(type, "values")); //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		sb.append(introspectedColumn.getJavaProperty());
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		sb.insert(0, "and"); //$NON-NLS-1$
		if (inMethod) {
			sb.append("In"); //$NON-NLS-1$
		} else {
			sb.append("NotIn"); //$NON-NLS-1$
		}
		method.setName(sb.toString());
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
		sb.setLength(0);

		if (introspectedColumn.isJDBCDateColumn()) {
			sb.append("addCriterionForJDBCDate(\""); //$NON-NLS-1$
		} else if (introspectedColumn.isJDBCTimeColumn()) {
			sb.append("addCriterionForJDBCTime(\""); //$NON-NLS-1$
		} else if (stringHasValue(introspectedColumn.getTypeHandler())) {
			sb.append("add"); //$NON-NLS-1$
			sb.append(introspectedColumn.getJavaProperty());
			sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
			sb.append("Criterion(\""); //$NON-NLS-1$
		} else {
			sb.append("addCriterion(\""); //$NON-NLS-1$
		}

		sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
		if (inMethod) {
			sb.append(" in"); //$NON-NLS-1$
		} else {
			sb.append(" not in"); //$NON-NLS-1$
		}
		sb.append("\", values, \""); //$NON-NLS-1$
		sb.append(introspectedColumn.getJavaProperty());
		sb.append("\");"); //$NON-NLS-1$
		method.addBodyLine(sb.toString());
		method.addBodyLine("return (Criteria) this;"); //$NON-NLS-1$
		CommentGenerator comment = context.getCommentGenerator();
		comment.addGeneralMethodComment(method, introspectedTable);
		return method;
	}

	private Method getNoValueMethod(IntrospectedColumn introspectedColumn, String nameFragment, String operator) {
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		StringBuilder sb = new StringBuilder();
		sb.append(introspectedColumn.getJavaProperty());
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		sb.insert(0, "and"); //$NON-NLS-1$
		sb.append(nameFragment);
		method.setName(sb.toString());
		method.setReturnType(FullyQualifiedJavaType.getCriteriaInstance());
		sb.setLength(0);
		sb.append("addCriterion(\""); //$NON-NLS-1$
		sb.append(MyBatis3FormattingUtilities.getAliasedActualColumnName(introspectedColumn));
		sb.append(' ');
		sb.append(operator);
		sb.append("\");"); //$NON-NLS-1$
		method.addBodyLine(sb.toString());
		method.addBodyLine("return (Criteria) this;"); //$NON-NLS-1$
		CommentGenerator comment = context.getCommentGenerator();
		comment.addGeneralMethodComment(method, introspectedTable);
		return method;
	}

	/**
     * This method adds all the extra methods and fields required to support a
     * user defined type handler on some column.
     * 
     * @param introspectedColumn
     * @param constructor
     * @param innerClass
     * @return the name of the List added to the class by this method
     */
    private String addtypeHandledObjectsAndMethods(
            IntrospectedColumn introspectedColumn, Method constructor,
            InnerClass innerClass) {
        String answer;
        StringBuilder sb = new StringBuilder();

        // add new private field and public accessor in the class
        sb.setLength(0);
        sb.append(introspectedColumn.getJavaProperty());
        sb.append("Criteria"); //$NON-NLS-1$
        answer = sb.toString();

        Field field = new Field();
        field.setVisibility(JavaVisibility.PROTECTED);
        field.setType(new FullyQualifiedJavaType("java.util.List<Criterion>")); //$NON-NLS-1$
        field.setName(answer);
        innerClass.addField(field);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(field.getType());
        method.setName(getGetterMethodName(field.getName(), field
                .getType()));
        sb.insert(0, "return "); //$NON-NLS-1$
        sb.append(';');
        method.addBodyLine(sb.toString());
        innerClass.addMethod(method);

        // add constructor initialization
        sb.setLength(0);
        sb.append(field.getName());
        sb.append(" = new ArrayList<Criterion>();"); //$NON-NLS-1$;
        constructor.addBodyLine(sb.toString());

        // now add the methods for simplifying the individual field set methods
        method = new Method();
        method.setVisibility(JavaVisibility.PROTECTED);
        sb.setLength(0);
        sb.append("add"); //$NON-NLS-1$
        sb.append(introspectedColumn.getJavaProperty());
        sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
        sb.append("Criterion"); //$NON-NLS-1$

        method.setName(sb.toString());
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getStringInstance(), "condition")); //$NON-NLS-1$
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getObjectInstance(), "value")); //$NON-NLS-1$
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getStringInstance(), "property")); //$NON-NLS-1$
        method.addBodyLine("if (value == null) {"); //$NON-NLS-1$
        method
                .addBodyLine("throw new RuntimeException(\"Value for \" + property + \" cannot be null\");"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$

        method.addBodyLine(
                String.format("%s.add(new Criterion(condition, value, \"%s\"));", //$NON-NLS-1$
                        field.getName(), introspectedColumn.getTypeHandler()));
        method.addBodyLine("allCriteria = null;"); //$NON-NLS-1$
        innerClass.addMethod(method);

        sb.setLength(0);
        sb.append("add"); //$NON-NLS-1$
        sb.append(introspectedColumn.getJavaProperty());
        sb.setCharAt(3, Character.toUpperCase(sb.charAt(3)));
        sb.append("Criterion"); //$NON-NLS-1$

        method = new Method();
        method.setVisibility(JavaVisibility.PROTECTED);
        method.setName(sb.toString());
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getStringInstance(), "condition")); //$NON-NLS-1$
        method.addParameter(new Parameter(introspectedColumn
                .getFullyQualifiedJavaType(), "value1")); //$NON-NLS-1$
        method.addParameter(new Parameter(introspectedColumn
                .getFullyQualifiedJavaType(), "value2")); //$NON-NLS-1$
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getStringInstance(), "property")); //$NON-NLS-1$
        if (!introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
            method.addBodyLine("if (value1 == null || value2 == null) {"); //$NON-NLS-1$
            method
                .addBodyLine("throw new RuntimeException(\"Between values for \" + property + \" cannot be null\");"); //$NON-NLS-1$
            method.addBodyLine("}"); //$NON-NLS-1$
        }

        method.addBodyLine(
                String.format("%s.add(new Criterion(condition, value1, value2, \"%s\"));", //$NON-NLS-1$
                        field.getName(), introspectedColumn.getTypeHandler()));
        
        method.addBodyLine("allCriteria = null;"); //$NON-NLS-1$
        innerClass.addMethod(method);

        return answer;
    }
}