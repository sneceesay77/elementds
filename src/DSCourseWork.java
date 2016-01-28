import java.sql.Statement;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class DSCourseWork {

	ArrayList<String> nilFields = new ArrayList<String>();

	ArrayList<String> keys = null;
	ArrayList<String> schemaList = null;

	public static void main(String args[]) {

		DSCourseWork cw = new DSCourseWork();
		String database = "protocol.db";
		Connection c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + database);
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		printHeader();
		printLog("Success", "Created and Connected to Database " + database
				+ " Successfully");
		cw.setupDBandTables("protocol2.txt", c);
		printLog(
				"Info",
				"===============================================\nInsertion of Messages started");
		cw.insertMessages("message-instance2.txt", c);
	}

	public static void printLog(String logType, String log) {
		System.out.println(logType + " : " + log + "\n");
	}

	public static String removeLastChar(StringBuilder str) {
		String processed = "";
		if (str.toString() != "") {
			processed = str.toString().substring(0, str.length() - 1);
		}
		return processed;
	}

	public static String errorHandling(String defaultSQLError) {
		String error = "";
		String last = defaultSQLError.split(" ")[defaultSQLError.split(" ").length - 1];
		if (defaultSQLError.contains("no such table")) {
			error = "No message schema with name " + last;
		} else if (defaultSQLError.contains("has no column named")) {
			error = "Parameter " + last
					+ " does not exists in message protocol "
					+ defaultSQLError.split(" ")[1] + " definition";
		} else if (defaultSQLError.contains("UNIQUE constraint failed")) {
			error = "This entry for " + last + " already existed";
		} else if (defaultSQLError.contains("NOT NULL constraint failed")) {
			error = "FAILS because " + last
					+ ", which a not a nil parameter, is not bound.";
		}
		// check for nil
		// check for duplicate
		return error;
	}

	public void setupDBandTables(String fileName, Connection cn) {
		Scanner in;
		Statement st = null;
		keys = new ArrayList<String>();
		schemaList = new ArrayList<String>();

		try {
			in = new Scanner(new File(fileName));
			int itrCount = 0;
			int linecount = 0;
			int cnt = 0;
			while (in.hasNextLine()) {

				String line = in.nextLine();
				String sql = "";

				String tokens[] = line.split(" ");
				// for(int i=0; i<=tokens.length; i++){

				// Store the keys
				if (tokens[0].equalsIgnoreCase("key")) {
					for (int i = 1; i < tokens.length; i++) {
						keys.add(tokens[i]);
					}
				}

				// In protocol.txt file, all declarations starting with Schema
				// definition
				// starts at the third line
				if (linecount > 1) {
					schemaList.add(tokens[0]);
					// printLog("Debug", keys.size()+"");
					// Create Table with columns using tokens[0]
					String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
							+ tokens[0];
					StringBuilder createTable = new StringBuilder();
					// Setup up keys int the SQL statement first
					// for(int i = 0; i<keys.size(); i++){
					if (keys.size() == 1 || itrCount == 0) {
						// System.out.println("Here 0.0");
						createTable.append(keys.get(0)
								+ " TEXT PRIMARY KEY NOT NULL,");
						itrCount++;
					} else if (keys.size() > 1 && itrCount > 0) {
						// System.out.println("Here 1");
						// Second iteration get first and second
						createTable.append(keys.get(1) + " TEXT NOT NULL,")
								.append(keys.get(0) + " TEXT  NOT NULL,");
						// increment to get the id for next id of next table.
						// Order of id list and Message declaration has to be
						// the same.
					}
					// }
					for (int i = 1; i < tokens.length; i++) {

						if (i % 2 != 0) {// odd positions have the fields
							// If the field is not a key and not a NIL mapping
							// then append it to create
							// table string.
							if ((!keys.contains(tokens[i]))
									&& !tokens[i + 1].equals("nil")) {
								createTable.append(tokens[i]
										+ " TEXT  NOT NULL,\n");
							} else if (tokens[i + 1].equals("nil")) {// populate
																		// the
																		// nill
																		// list
								// System.out.println(tokens[0]+""+tokens[i]);
								nilFields.add(tokens[0] + "" + tokens[i]);
							}
						}
					}

					try {

						if (cnt == 0 || keys.size() == 1) {
							sql = CREATE_TABLE
									+ " ( "
									+ createTable.toString().substring(0,
											createTable.length() - 2) + ");";
							cnt++;
						} else if (cnt > 0 && keys.size() > 1) {
							createTable.append("PRIMARY KEY (" + keys.get(1)
									+ "," + keys.get(0) + "),");
							// System.out.println("Coool"+createTable);
							sql = CREATE_TABLE
									+ " ( "
									+ createTable.toString().substring(0,
											createTable.length() - 2) + "));";
							// System.out.println(sql);

						}

						st = cn.createStatement();
						st.executeUpdate(sql);
						printLog("SQL", sql.replace("\n", ""));
						printLog("Success", tokens[0] + " Created Successfully");
					} catch (Exception e) {
						System.err.println(e.getClass().getName() + ": "
								+ e.getMessage());
						System.exit(0);
					}

				}
				linecount++;

			} // end while
				// st.close();
				// cn.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void insertMessages(String fileName, Connection ct) {
		Scanner in = null;
		Statement st = null;
		boolean isNil = false;
		String nilField = "";
		String currId = "";
		String currItem = "";
		String integrityMsg = "";

		try {
			in = new Scanner(new File(fileName));
			while (in.hasNextLine()) {
				try {
					String line = in.nextLine();
					String tokens[] = line.split(" ");
					printLog("Reading ", line);
					StringBuilder fields = new StringBuilder();
					StringBuilder values = new StringBuilder();
					for (int i = 1; i < tokens.length; i++) {

						if (i % 2 != 0) {
							fields.append(tokens[i] + ",");
							// If quote get the value of id and item
							if (tokens[0].equalsIgnoreCase("Quote")
									|| tokens[0].equalsIgnoreCase("RFQ")) {
								if (tokens[i].equalsIgnoreCase("rid")
										|| tokens[i].equalsIgnoreCase("qid")) {
									currId = tokens[i + 1];
								}
								if (tokens[i].equalsIgnoreCase("item")) {
									currItem = tokens[i + 1];
								}
							}
						} else {
							values.append("\'" + tokens[i] + "\',");
						}
						if (nilFields.contains(tokens[0] + "" + tokens[i])) {
							nilField = tokens[i];
							isNil = true;
							// i++;//we need to avoid adding the value part to
							// the value list.
							// use pen and paper to understand more.
						}

					}
					String sql = "INSERT INTO " + tokens[0] + " ("
							+ removeLastChar(fields) + ") " + "VALUES ("
							+ removeLastChar(values) + ");";
					/**
					 * If line or message is a quote, then query the RFQ table
					 * to verify its mapping
					 * */
					if (tokens[0].equalsIgnoreCase("Quote")) {
						String sqlQuery = "SELECT item FROM RFQ WHERE rid = '"
								+ currId + "'";
						st = ct.createStatement();
						ResultSet rs = st.executeQuery(sqlQuery);
						while (rs.next()) {
							String item = rs.getString("item");
							if (!item.equalsIgnoreCase(currItem)) {
								integrityMsg = "FAILS integrity, the key rid with value "
										+ currId
										+ " is bound to "
										+ item
										+ " as an item.";
							}
						}

					}

					/**
					 * If line or message is RFQ, then query the Quote table to
					 * verify its mapping
					 * */
					if (tokens[0].equalsIgnoreCase("RFQ")) {
						String sqlQuery = "SELECT item FROM Quote WHERE rid = '"
								+ currId + "'";
						st = ct.createStatement();
						ResultSet rs = st.executeQuery(sqlQuery);
						while (rs.next()) {
							String item = rs.getString("item");
							if (!item.equalsIgnoreCase(currItem)) {
								integrityMsg = "FAILS integrity, the key qid with value "
										+ currId
										+ " is bound to "
										+ item
										+ " as an item.";
							}
						}

					}
					printLog("Executing", sql);
					if (isNil) {
						printLog("Error", "NIL parameter " + nilField
								+ " should not be bounded");
					}
					if (!integrityMsg.equals("")) {
						printLog("Error", integrityMsg);
					}
					// In addition to other checks, only execute SQL queries if
					// there is not nil or integrity issues
					if (integrityMsg.equals("")) {
						st = ct.createStatement();
						st.executeUpdate(sql);
					}
					// Insert message
				} catch (SQLException e) {
					if (!isNil) {// Print this for only non nil column messages.
						// Without the above condition both nil and no parameter
						// message will be printed
						printLog("Error", errorHandling(e.getMessage()));
					}
					// printLog("Error", e.getMessage());
				}
				printLog("=====", "==================END================");
				// Reset all checkign variables
				isNil = false;
				integrityMsg = "";
				currId = "";
				currItem = "";
			}
			st.close();
			ct.close();
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void printHeader() {
		printLog(
				"",
				"<<<<<====================================================================>>>>>");
		printLog(
				"",
				"\t\tElements of Distributed Systems Coursework\n\t\t\t@Authous : sheriffo, allen");
		printLog(
				"",
				"<<<<<====================================================================>>>>>");
	}
}
