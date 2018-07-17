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
package org.mybatis.generator.internal;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.StringTokenizer;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.exception.ShellException;

/**
 * The Class DefaultShellCallback.
 *
 * @author Jeff Butler
 */
public class DefaultShellCallback implements ShellCallback {

	/** The overwrite. */
	private boolean overwrite;

	private List<String> warnings;

	/**
	 * Instantiates a new default shell callback.
	 *
	 * @param overwrite
	 *            the overwrite
	 */
	public DefaultShellCallback(boolean overwrite) {
		super();
		this.overwrite = overwrite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mybatis.generator.api.ShellCallback#getDirectory(java.lang.String,
	 * java.lang.String)
	 */
	public File getDirectory(String targetProject, String targetPackage) throws ShellException {
		// targetProject is interpreted as a directory that must exist
		//
		// targetPackage is interpreted as a sub directory, but in package
		// format (with dots instead of slashes). The sub directory will be
		// created
		// if it does not already exist

		File project = new File(targetProject);
		if (!project.isDirectory()) {
			throw new ShellException(getString("Warning.9", //$NON-NLS-1$
					targetProject));
		}

		StringBuilder sb = new StringBuilder();
		StringTokenizer st = new StringTokenizer(targetPackage, "."); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			sb.append(st.nextToken());
			sb.append(File.separatorChar);
		}

		File directory = new File(project, sb.toString());
		if (!directory.isDirectory()) {
			boolean rc = directory.mkdirs();
			if (!rc) {
				throw new ShellException(getString("Warning.10", //$NON-NLS-1$
						directory.getAbsolutePath()));
			}
		}

		return directory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mybatis.generator.api.ShellCallback#refreshProject(java.lang.String)
	 */
	public void refreshProject(String project) {
		// nothing to do in the default shell callback
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mybatis.generator.api.ShellCallback#isMergeSupported()
	 */
	public boolean isMergeSupported() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mybatis.generator.api.ShellCallback#isOverwriteEnabled()
	 */
	public boolean isOverwriteEnabled() {
		return overwrite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mybatis.generator.api.ShellCallback#mergeJavaFile(java.lang.String,
	 * java.lang.String, java.lang.String[], java.lang.String)
	 */
	public String mergeJavaFile(String newFileSource, File existingFile, String[] javadocTags, String fileEncoding)
			throws ShellException {
		try (RandomAccessFile raf = new RandomAccessFile(existingFile, "r")){
			byte[] b = new byte[(int) raf.length()];
			raf.readFully(b);

			String existsFileStr = new String(b, "utf-8");

			boolean blobFlag = newFileSource.contains("BlobMapper<");
			boolean existsBlobFlag = existsFileStr.contains("BlobMapper<");

			if (blobFlag != existsBlobFlag) {
				warnings.add(getString("Warning.31", existingFile.getName()));
				return newFileSource;
			}

			boolean primaryFlag = newFileSource.contains("PrimaryMapper<");
			boolean existsPprimaryFlag = existsFileStr.contains("PrimaryMapper<");

			if (primaryFlag != existsPprimaryFlag) {
				warnings.add(getString("Warning.31", existingFile.getName()));
				return newFileSource;
			}
			return existsFileStr;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return newFileSource;
	}

	@Override
	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}

}
