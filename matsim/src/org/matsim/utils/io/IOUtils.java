/* *********************************************************************** *
 * project: org.matsim.*
 * IOUtils.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.utils.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** A class with some static utility functions for file-I/O. */
public class IOUtils {


	/**
	 * Tries to open the specified file for reading and returns a BufferedReader for it.
	 * Supports gzip-compressed files, such files are automatically decompressed.
	 * If the file is not found, a gzip-compressed version of the file with the
	 * added ending ".gz" will be searched for and used if found.
	 *
	 * @param filename The file to read, may contain the ending ".gz" to force reading a compressed file.
	 * @return BufferedReader for the specified file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 *
	 * @author mrieser
	 */
	public static BufferedReader getBufferedReader(final String filename) throws FileNotFoundException, IOException {
		BufferedReader infile = null;
		if (new File(filename).exists()) {
			if (filename.endsWith(".gz")) {
				infile = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filename))));
			} else {
				infile = new BufferedReader(new FileReader(filename));
			}
		} else if (new File(filename + ".gz").exists()) {
			infile = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filename  + ".gz"))));
		}

		if (infile == null) {
			throw new FileNotFoundException(filename);
		}
		return infile;
	}


	/**
	 * Tries to open the specified file for writing and returns a BufferedWriter for it.
	 * Supports gzip-compression of the written data. The filename may contain the
	 * ending ".gz". If no compression is to be used, the ending will be removed
	 * from the filename. If compression is to be used and the filename does not yet
	 * have the ending ".gz", the ending will be added to it.
	 *
	 * @param filename The filename where to write the data.
	 * @param useCompression whether the file should be gzip-compressed or not.
	 * @return BufferedWriter for the specified file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static BufferedWriter getBufferedWriter(final String filename, boolean useCompression) throws FileNotFoundException, IOException {
		if (useCompression && !filename.endsWith(".gz")) {
			return getBufferedWriter(filename + ".gz");
		} else if (!useCompression && filename.endsWith(".gz")) {
			return getBufferedWriter(filename.substring(0, filename.length() - 3));
		} else {
			return getBufferedWriter(filename);
		}
	}


	/**
	 * Tries to open the specified file for writing and returns a BufferedWriter for it.
	 * If the filename ends with ".gz", data will be automatically gzip-compressed.
	 *
	 * @param filename The filename where to write the data.
	 * @return BufferedWriter for the specified file.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static BufferedWriter getBufferedWriter(final String filename) throws FileNotFoundException, IOException {
		if (filename.endsWith(".gz")) {
			return new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filename))));
		}
		return new BufferedWriter( new FileWriter (filename));
	}

	/**
	 * Attempts to rename a file. The built-in method File.renameTo() often fails
	 * for different reasons (e.g. if the file should be moved across different
	 * file systems). This method first tries the method File.renameTo(),
	 * but tries other possibilities if the first one fails.
	 *
	 * @param fromFilename The file name of an existing file
	 * @param toFilename The new file name
	 * @return <code>true</code> if the file was successfully renamed, <code>false</code> otherwise
	 *
	 * @author mrieser
	 */
	public static boolean renameFile(final String fromFilename, final String toFilename) {
		return renameFile(new File(fromFilename), new File(toFilename));
	}

	/**
	 * Attempts to rename a file. The built-in method File.renameTo() often fails
	 * for different reasons (e.g. if the file should be moved across different
	 * file systems). This method first tries the method File.renameTo(),
	 * but tries other possibilities if the first one fails.
	 *
	 * @param fromFile The existing file.
	 * @param toFile A file object pointing to the new location of the file.
	 * @return <code>true</code> if the file was successfully renamed, <code>false</code> otherwise
	 *
	 * @author mrieser
	 */
	public static boolean renameFile(final File fromFile, final File toFile) {
		File toFile2 = toFile;

		// the first, obvious approach
		if (fromFile.renameTo(toFile)) {
			return true;
		}

		// if it failed, make some tests to decide if we should try any other approach
		if (!fromFile.exists()) {
			// there's no use renaming an inexistant file
			return false;
		}

		if (!fromFile.canRead()) {
			// there's no use to proceed further if we cannot read the file
			return false;
		}

		if (toFile.isDirectory()) {
			toFile2 = new File(toFile, fromFile.getName());
		}

		if (toFile2.exists()) {
			// we do not overwrite existing files
			return false;
		}

		String parent = toFile2.getParent();
    if (parent == null)
      parent = System.getProperty("user.dir");
    File dir = new File(parent);

    if (!dir.exists()) {
    	// we cannot move a file to an inexistent directory
    	return false;
    }

    if (!dir.canWrite()) {
    	// we cannot write into the directory
    	return false;
    }

    try {
    	copyFile(fromFile, toFile2);
    } catch (FileNotFoundException e) {
    	if (toFile2.exists()) toFile2.delete();
    	return false;
    } catch (IOException e) {
    	if (toFile2.exists()) toFile2.delete();
    	return false;
    }

    // okay, at this place we can assume that we successfully copied the data, so remove the old file
    fromFile.delete();

		return true;
	}

	/**
	 * Copies the file content from one file to the other file.
	 *
	 * @param fromFile The file containing the data to be copied
	 * @param toFile The file the data should be written to
	 * @throws FileNotFoundException
	 * @throws IOException
	 *
	 * @author mrieser
	 */
	public static void copyFile(final File fromFile, final File toFile) throws FileNotFoundException, IOException {
		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = from.read(buffer)) != -1) {
				to.write(buffer, 0, bytesRead);
			}
		} finally {
			if (from != null) {
				try {
					from.close();
				} catch (IOException ignored) {}
			}
			if (to != null) {
				try {
					to.close();
				} catch (IOException ignored) {}
			}
		}
	}

	/**
	 * This method recursively deletes the directory and all its contents. This
	 * method will not follow symbolic links, i.e. if the directory or one of the
	 * sub-directories contains a symbolic link, it will not delete this link. Thus
	 * the directory cannot be deleted, too.
	 *
	 * @param dir
	 *          File which must represent a directory, files or symbolic links are
	 *          not permitted as arguments
	 *
	 * @author dgrether
	 */
	public static void deleteDirectory(final File dir) {
		if (dir.isFile()) {
			throw new IllegalArgumentException("Directory " + dir.getName()
					+ " must not be a file!");
		}
		else if (isLink(dir)) {
			throw new IllegalArgumentException("Directory " + dir.getName()
					+ " doesn't exist or is a symbolic link!");
		}
		if (dir.exists()) {
			IOUtils.deleteDir(dir);
		}
		else {
			throw new IllegalArgumentException("Directory " + dir.getName()
					+ " doesn't exist!");
		}
	}

	/**
	 * Recursive helper for {@link #deleteDirectory(File)}. Deletes a directory
	 * recursive. If the directory or one of its sub-directories is a symbolic
	 * neither the link nor its parent directories are deleted.
	 *
	 * @param dir The directory to be recursively deleted.
	 *
	 * @author dgrether
	 */
	private static void deleteDir(final File dir) {
		File[] outDirContents = dir.listFiles();
		for (int i = 0; i < outDirContents.length; i++) {
			if (isLink(outDirContents[i])) {
				continue;
			}
			if (outDirContents[i].isDirectory()) {
				deleteDir(outDirContents[i]);
			}
			outDirContents[i].delete();
		}
		dir.delete();
	}

	/**
	 * Checks if the given File Object may be a symbolic link.<br />
	 *
	 * For a link that actually points to something (either a file or a
	 * directory), the absolute path is the path through the link, whereas the
	 * canonical path is the path the link references.<br />
	 *
	 * Dangling links appear as files of size zero, and generate a
	 * FileNotFoundException when you try to open them. Unfortunately non-existent
	 * files have the same behavior (size zero - why did't file.length() throw an
	 * exception if the file doesn't exist, rather than returning length zero?).<br />
	 *
	 * The routine below appears to detect Unix/Linux symbolic links, but it also
	 * returns true for non-existent files. Note that if a directory is a link,
	 * all the contents of this directory will appear as symbolic links as well.<br />
	 *
	 * Note that this method has problems if the file is specified with a relative
	 * path like "./" or "../".<br />
	 *
	 * @see <a href="http://www.idiom.com/~zilla/Xfiles/javasymlinks.html">Javasymlinks on www.idiom.com</a>
	 *
	 * @param file
	 * @return true if the file is a symbolic link or does not even exist. false if not
	 *
	 * @author dgrether
	 */
	public static boolean isLink(final File file) {
		try {
			if (!file.exists()) {
				return true;
			}
			String cnnpath = file.getCanonicalPath();
			String abspath = file.getAbsolutePath();
			return !abspath.equals(cnnpath);
		} catch (IOException ex) {
			System.err.println(ex);
			return true;
		}
	}

}
