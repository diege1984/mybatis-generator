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
package com.wu.common.core.mybatis.support;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;

public interface DBSupport {
    /**
     * 向&lt;mapper&gt;中添加子节点
     *
     * @author 吴帅
     * @parameter @param document
     * @parameter @param introspectedTable
     * @createDate 2015年9月29日 上午10:20:11
     */
    public void sqlDialect(Document document, IntrospectedTable introspectedTable);

    /**
     * 增加批量插入的xml配置
     *
     * @author 吴帅
     * @parameter @param document
     * @parameter @param introspectedTable
     * @createDate 2015年8月9日 下午6:57:43
     */
    public void addBatchInsertXml(Document document, IntrospectedTable introspectedTable);

    /**
     * 条件查询sql适配
     *
     * @return
     * @author 吴帅
     * @parameter @param element
     * @parameter @param preFixId
     * @parameter @param sufFixId
     * @createDate 2015年9月29日 上午11:59:06
     */
    public XmlElement adaptSelectByExample(XmlElement element, IntrospectedTable introspectedTable);

    /**
     * 插入sql适配
     *
     * @author 吴帅
     * @parameter @param element
     * @parameter @param introspectedTable
     * @createDate 2015年9月29日 下午12:00:37
     */
    public void adaptInsertSelective(XmlElement element, IntrospectedTable introspectedTable);
}
