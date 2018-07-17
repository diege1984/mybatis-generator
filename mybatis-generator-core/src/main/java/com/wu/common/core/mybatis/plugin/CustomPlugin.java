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
package com.wu.common.core.mybatis.plugin;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;

import com.wu.common.core.mybatis.support.DBSupport;
import com.wu.common.core.mybatis.support.dbsupprt.MysqlSupport;
import com.wu.common.core.mybatis.support.dbsupprt.OracleSupport;
import com.wu.common.core.mybatis.support.dbsupprt.SqlServerSupport;

/**
 * .支持oracle/mysql/sqlserver数据库分页查询<br/>
 * .支持oracle/mysql/sqlserver数据库插入时自增主键<br/>
 * .支持oracle/mysql/sqlserver数据库批量插入<br/>
 * 
 * @author 吴帅
 * @CreationDate 2015年8月2日
 * @version 1.0
 */
public class CustomPlugin extends PluginAdapter {
	private String dbType;
	private DBSupport dbSupport;

	/**
	 * 修改Model类
	 */
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		addBuilder(topLevelClass, introspectedTable);
		return super.modelBaseRecordClassGenerated(topLevelClass, introspectedTable);
	}

	/**
	 * Model类添加使用Builder设计模式的构造方式
	 * @author 吴帅
	 * @parameter @param topLevelClass
	 * @parameter @param introspectedTable
	 * @createDate 2016年1月15日 上午11:27:58
	 */
	private void addBuilder(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		CommentGenerator commentGenerator = context.getCommentGenerator();
		FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("String");;
		Method toStringMethod = new Method("toString");
		toStringMethod.setVisibility(JavaVisibility.PUBLIC);
		toStringMethod.setReturnType(fqjt);
        List<IntrospectedColumn> introspectedColumns;
        if (includePrimaryKeyColumns(introspectedTable)) {
            if (includeBLOBColumns(introspectedTable)) {
                introspectedColumns = introspectedTable.getAllColumns();
            } else {
                introspectedColumns = introspectedTable.getNonBLOBColumns();
            }
        } else {
            if (includeBLOBColumns(introspectedTable)) {
                introspectedColumns = introspectedTable
                        .getNonPrimaryKeyColumns();
            } else {
                introspectedColumns = introspectedTable.getBaseColumns();
            }
        }
        toStringMethod.addBodyLine("return super.toString()");
        for (int i=0;i<introspectedColumns.size();i++) {
        	if (RootClassInfo.getInstance(getRootClass(introspectedTable), new ArrayList<String>())
                    .containsProperty(introspectedColumns.get(i))) {
                continue;
            }
        	String property = introspectedColumns.get(i).getJavaProperty();
        	toStringMethod.addBodyLine("+\"\\n" + property + ":\" + this." + property + "");
        }
        toStringMethod.addBodyLine(";");
        commentGenerator.addGeneralMethodComment(toStringMethod, introspectedTable);
        topLevelClass.addMethod(toStringMethod);
		//3. add default constructor
//		Method defConstructor = new Method(topLevelClass.getType().getShortName());
//		defConstructor.setConstructor(true);
//		defConstructor.setVisibility(JavaVisibility.PUBLIC);
//		defConstructor.addBodyLine("super();");
//		topLevelClass.addMethod(defConstructor);
	}
	
    public String getRootClass(IntrospectedTable introspectedTable) {
        String rootClass = introspectedTable
                .getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_CLASS);
        if (rootClass == null) {
            Properties properties = context
                    .getJavaModelGeneratorConfiguration().getProperties();
            rootClass = properties.getProperty(PropertyRegistry.ANY_ROOT_CLASS);
        }

        return rootClass;
    }
	
    private boolean includePrimaryKeyColumns(IntrospectedTable introspectedTable) {
        return !introspectedTable.getRules().generatePrimaryKeyClass()
                && introspectedTable.hasPrimaryKeyColumns();
    }

    private boolean includeBLOBColumns(IntrospectedTable introspectedTable) {
        return !introspectedTable.getRules().generateRecordWithBLOBsClass()
                && introspectedTable.hasBLOBColumns();
    }

	/**
	 * 修改Example类
	 */
	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) { 
		addPage(topLevelClass, introspectedTable);
		return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
	}

	/**
	 * 修改Mapper类
	 */
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		// 1.增加批量插入方式签名
//		addBatchInsertMethod(interfaze, introspectedTable);

		// 2.增加数据源名称常量
		//addDataSourceNameField(interfaze, introspectedTable);

		return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
	}

	/**
	 * 修改mapper.xml,支持分页和批量
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		dbSupport.sqlDialect(document, introspectedTable);
		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		XmlElement newElement = dbSupport.adaptSelectByExample(element, introspectedTable);
		return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(newElement, introspectedTable);
	}

	@Override
	public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		dbSupport.adaptInsertSelective(element, introspectedTable);
		return super.sqlMapInsertSelectiveElementGenerated(element, introspectedTable);
	}

	/**
	 * This plugin is always valid - no properties are required
	 */
	@Override
	public boolean validate(List<String> warnings) {

		dbType = properties.getProperty("dbType"); //$NON-NLS-1$

		boolean valid1 = stringHasValue(dbType);
		if (valid1) {
			dbType = dbType.toUpperCase();// 忽略大小写
			if (dbType.equals("ORACLE")) {
				dbSupport = new OracleSupport();
			}
			else if (dbType.equals("MYSQL")) {
				dbSupport = new MysqlSupport();
			}
			else if (dbType.equals("SQLSERVER")) {
				dbSupport = new SqlServerSupport();
			}
			else{// 不支持其他数据库
				valid1 = false;
				warnings.add(getString("RuntimeError.18", "RenameExampleClassPlugin", "searchString"));
			}
			Pattern.compile(dbType);
		}
		else {

			if (!stringHasValue(dbType)) {
				warnings.add(getString("ValidationError.18", "RenameExampleClassPlugin", "searchString")); //$NON-NLS-1$
			}
		}

		return (valid1);
	}

	/**
	 * 在Mapper类中增加批量插入方法声明
	 * 
	 * @author 吴帅
	 * @parameter @param interfaze
	 * @parameter @param introspectedTable
	 * @createDate 2015年9月30日 下午4:43:32
	 */
	private void addBatchInsertMethod(Interface interfaze, IntrospectedTable introspectedTable) {
		// 设置需要导入的类
		Set<FullyQualifiedJavaType> importedTypes = new TreeSet<FullyQualifiedJavaType>();
		importedTypes.add(FullyQualifiedJavaType.getNewListInstance());
		importedTypes.add(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));

		Method ibsmethod = new Method();
		// 1.设置方法可见性
		ibsmethod.setVisibility(JavaVisibility.PUBLIC);
		// 2.设置返回值类型
		FullyQualifiedJavaType ibsreturnType = FullyQualifiedJavaType.getIntInstance();// int型
		ibsmethod.setReturnType(ibsreturnType);
		// 3.设置方法名
		ibsmethod.setName("insertBatch");
		// 4.设置参数列表
		FullyQualifiedJavaType paramType = FullyQualifiedJavaType.getNewListInstance();
		FullyQualifiedJavaType paramListType;
		if (introspectedTable.getRules().generateBaseRecordClass()) {
			paramListType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
		}
		else if (introspectedTable.getRules().generatePrimaryKeyClass()) {
			paramListType = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
		}
		else {
			throw new RuntimeException(getString("RuntimeError.12")); //$NON-NLS-1$  
		}
		paramType.addTypeArgument(paramListType);

		ibsmethod.addParameter(new Parameter(paramType, "records"));

		interfaze.addImportedTypes(importedTypes);
		interfaze.addMethod(ibsmethod);
	}


	/**
	 * 修改Example类,添加分页支持
	 *
	 * @param topLevelClass
	 * @param introspectedTable
	 */
	private void addPage(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        
	}

	/**
	 * 增加数据源名称字段
	 * 
	 * @author 吴帅
	 * @parameter @param interfaze
	 * @parameter @param introspectedTable
	 * @createDate 2015年10月2日 上午10:06:47
	 */
	private void addDataSourceNameField(Interface interfaze, IntrospectedTable introspectedTable) {
		TableConfiguration tableConfiguration = introspectedTable.getTableConfiguration();
		Properties properties = tableConfiguration.getProperties();
		String dataSourceName = properties.getProperty("dataSourceName");
		Field field = new Field();
		field.setVisibility(JavaVisibility.PUBLIC);
		field.setStatic(true);
		field.setFinal(true);
		field.setType(FullyQualifiedJavaType.getStringInstance());
		field.setName("DATA_SOURCE_NAME");
		field.setInitializationString("\"" + dataSourceName + "\"");
		interfaze.addField(field);
	}
}
