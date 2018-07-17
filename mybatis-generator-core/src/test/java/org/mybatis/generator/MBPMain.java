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
package org.mybatis.generator;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MBPMain {

	public static final String CFG_FILE_PATH = MBPMain.class.getResource("/generatorConfig.xml").getFile();

	public static void main(String[] args) {
		List<String> warnings = new ArrayList<String>();
		boolean overwrite = true;
		File configFile = new File(CFG_FILE_PATH);
		ConfigurationParser cp = new ConfigurationParser(warnings);
		Configuration config = null;
		try {
			config = cp.parseConfiguration(configFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (XMLParserException e) {
			e.printStackTrace();
		}
		DefaultShellCallback callback = new DefaultShellCallback(overwrite);
		MyBatisGenerator myBatisGenerator = null;
		try {
			myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
		}
		catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		try {
			myBatisGenerator.generate(new ProgressCallback() {
				@Override
				public void startTask(String taskName) {
					System.out.println("startTask(String "+taskName+")");
				}
				@Override
				public void saveStarted(int totalTasks) {
					System.out.println("saveStarted(int "+totalTasks+")");
				}
				@Override
				public void introspectionStarted(int totalTasks) {
					System.out.println("introspectionStarted(int "+totalTasks+")");
				}
				@Override
				public void generationStarted(int totalTasks) {
					System.out.println("generationStarted(int "+totalTasks+")");
				}
				@Override
				public void done() {
					System.out.println("done()");
				}
				@Override
				public void checkCancel() throws InterruptedException {
					System.out.println("checkCancel()");
				}
			});
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
