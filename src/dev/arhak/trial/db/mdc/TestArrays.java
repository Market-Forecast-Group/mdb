/*
 * (C) Copyright 2011 - MFG <http://www.marketforecastgroup.com/>
 * All rights reserved. This program and the accompanying materials
 * are proprietary to Giulio Rugarli.
 * 
 * @author <a href="mailto:boniatillo@gmail.com">Arian Fornaris</a>, MFG
 * 
 * @version $Revision$: $Date$:
 * $Id$:
 */
package dev.arhak.trial.db.mdc;

import static java.lang.System.out;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import dev.arhak.trial.db.mdc.mdb.TableWithArrayMDB;
import dev.arhak.trial.db.mdc.mdb.TableWithArrayMDB.Appender;
import dev.arhak.trial.db.mdc.mdb.TableWithArrayMDB.Cursor;
import dev.arhak.trial.db.mdc.mdb.TestMDBSession;

/**
 * @author arian
 * 
 */
public class TestArrays {
	public static void main(String[] args) throws IOException {
		File root = new File("array_mdb");
		root.mkdirs();
		File file = new File(root, "table.mdb");
		File arrayFile = new File(root, "table.txt");

		file.delete();
		arrayFile.delete();

		if (file.exists() || arrayFile.exists()) {
			throw new IllegalArgumentException("File exixts! Remove it!");
		}

		Random rand = new Random();

		TestMDBSession session = new TestMDBSession("test", root);
		TableWithArrayMDB mdb = session.connectTo_TableWithArrayMDB(file,
				arrayFile);

		Appender a = mdb.appender();

		double res = 0;

		int numOfRecords = 10000;
		int minNumOfArraySize = 1000;
		int maxRandomPlusOfArraySize = 100;

		for (int i = 0; i < numOfRecords; i++) {
			a.string = "Sting!";

			a.boolArray = new boolean[maxRandomPlusOfArraySize];
			for (int j = 0; j < a.boolArray.length; j++) {
				a.boolArray[j] = rand.nextBoolean();
			}

			a.byteArray = new byte[minNumOfArraySize
					+ rand.nextInt(maxRandomPlusOfArraySize)];
			for (int j = 0; j < a.byteArray.length; j++) {
				a.byteArray[j] = (byte) rand.nextInt();
			}

			a.doubleArray = new double[minNumOfArraySize
					+ rand.nextInt(maxRandomPlusOfArraySize)];
			for (int j = 0; j < a.doubleArray.length; j++) {
				a.doubleArray[j] = rand.nextDouble() * 100;
			}

			a.floatArray = new float[minNumOfArraySize
					+ rand.nextInt(maxRandomPlusOfArraySize)];
			for (int j = 0; j < a.floatArray.length; j++) {
				a.floatArray[j] = rand.nextFloat() * 100;
			}

			a.intArray = new int[minNumOfArraySize
					+ rand.nextInt(maxRandomPlusOfArraySize)];
			for (int j = 0; j < a.intArray.length; j++) {
				a.intArray[j] = rand.nextInt();
			}

			a.longArray = new long[minNumOfArraySize
					+ rand.nextInt(maxRandomPlusOfArraySize)];
			for (int j = 0; j < a.longArray.length; j++) {
				a.longArray[j] = rand.nextLong();
			}

			a.shortArray = new short[minNumOfArraySize
					+ rand.nextInt(maxRandomPlusOfArraySize)];
			for (int j = 0; j < a.shortArray.length; j++) {
				a.shortArray[j] = (short) rand.nextInt();
			}

			res = formula(res, a);
			a.sum = res;
			a.append();
			a.flush();
		}

		out.println(res);
		res = 0;
		Cursor c = mdb.cursor();
		int i = 0;
		while (c.next()) {
			res = formula(res, c);
			if (c.sum != res) {
				out.println(i + ": " + c.sum + " != " + res);
			}
			i++;
		}
		c.close();

		out.println(res);

		session.closeAndDelete();

		if (file.exists() || arrayFile.exists()) {
			throw new IllegalArgumentException(
					"File exixts! There is a bug or a problem closing and deleting them!");
		}
	}

	private static double formula(double lastResult,
			TableWithArrayMDB.Record rec) {
		for (int i = 0; i < rec.boolArray.length; i++) {
			lastResult += rec.boolArray[i] ? 1 : 0;
		}

		for (int i = 0; i < rec.byteArray.length; i++) {
			lastResult += rec.byteArray[i];
		}

		for (int i = 0; i < rec.doubleArray.length; i++) {
			if (Double.isNaN(rec.doubleArray[i])) {
				out.println("NaN here!");
			}
		}

		for (int i = 0; i < rec.floatArray.length; i++) {
			lastResult += rec.floatArray[i];
		}

		for (int i = 0; i < rec.intArray.length; i++) {
			lastResult += rec.intArray[i];
		}

		for (int i = 0; i < rec.longArray.length; i++) {
			lastResult += rec.longArray[i];
		}

		for (int i = 0; i < rec.shortArray.length; i++) {
			lastResult += rec.shortArray[i];
		}

		return lastResult;
	}
}
