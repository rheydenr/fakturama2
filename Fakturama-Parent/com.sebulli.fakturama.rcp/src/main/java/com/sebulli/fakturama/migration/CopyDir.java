package com.sebulli.fakturama.migration;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * This program copies a whole directory (including its sub files and sub
 * directories) to another, using the Java NIO API.
 *
 * @author www.codejava.net
 */
public class CopyDir extends SimpleFileVisitor<Path> {
	private Path sourceDir;
	private Path targetDir;

	public CopyDir(Path sourceDir, Path targetDir) {
		this.sourceDir = sourceDir;
		this.targetDir = targetDir;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {

		try {
			Path targetFile = targetDir.resolve(sourceDir.relativize(file));
			Files.copy(file, targetFile);
		} catch (IOException ex) {
			System.err.println(ex);
		}

		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) {
		try {
			Path newDir = targetDir.resolve(sourceDir.relativize(dir));
			Files.createDirectory(newDir);
		} catch (IOException ex) {
			System.err.println(ex);
		}

		return FileVisitResult.CONTINUE;
	}
}
