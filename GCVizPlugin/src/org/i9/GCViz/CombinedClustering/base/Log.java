package org.i9.GCViz.CombinedClustering.base;

import java.io.File;
import java.io.RandomAccessFile;

final public class Log {

	public String m_LogFilename;

	public boolean m_LogConsole = false;

	
	public boolean m_LogFile = true;

	
	protected RandomAccessFile m_File;

	
	
	public final void log(String logString) {
		if (m_LogConsole)
			System.out.print(logString);
		if (m_LogFile) {
			try {
				m_File.writeBytes(logString);
			} catch (Exception e) {
				System.out.println("Error: " + e.toString());
			}
		}
	}
	
	public Log(String filename, boolean file, boolean console, boolean deleteExisting) {
		m_LogConsole = console;
		m_LogFile = file;
		m_LogFilename = filename;
		if (file)
			try {
				if(deleteExisting) { 
					File tmp = new File(filename);
					tmp.delete();
					tmp = null;
				}
				m_File = new RandomAccessFile(filename, "rw");
			} catch (Exception e) {
				System.out
						.println("Error: Cannot create RandomAccessFile from '"
								+ filename + "'.");
				System.out.println(e.toString());
				m_LogFile = false;
			}
		else
			assert filename == null;
	}

		
	public Log(String filename, boolean file, boolean console) {
		m_LogConsole = console;
		m_LogFile = file;
		m_LogFilename = filename;
		if (file)
			try {
				m_File = new RandomAccessFile(filename, "rw");
			} catch (Exception e) {
				System.out
						.println("Error: Cannot create RandomAccessFile from '"
								+ filename + "'.");
				System.out.println(e.toString());
				m_LogFile = false;
			}
		else
			assert filename == null;
	}

	public void close() throws Exception {
		m_File.close();
	}
}
