package com.revature;
import com.revature.services.SQLConnector;
import com.revature.services.dao;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;
import java.util.concurrent.*;


/**
 * <p1>Standard ORM that adds,gets,updates, and delete objects from an sql database</p1>
 * @author ryanh
 * @version 1
 **/
public class Worm {
	private static final Connection[] connections = new Connection[10];
	private static final Boolean[] connectionsInUse = new Boolean[10];
	private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
	public static Worm instance = null;
	private Worm()  {
	}
	private int getFreeConnection() {
		for (int i = 0; i < connectionsInUse.length; i++) {
			if (!connectionsInUse[i]) {
				connectionsInUse[i] = true;
				try {
					if (connections[i] == null || connections[i].isClosed())
						connections[i] = SQLConnector.getConnection();
					return i;
				} catch (SQLException e) {e.printStackTrace();}
			}

		}
		return -1;
	}
	private void freeConnection(int conID){
		synchronized (connectionsInUse){
			connectionsInUse[conID] = false;
			connectionsInUse.notify();
		}

	}


	/**
	 * Starts the threadpool and all connections. Must be called before anything else
	 * @return the static instance of Worm
	 */
	public static Worm getInstance() {
		if (instance == null)
		{
			instance = new Worm();
			for (int i = 0; i < 10; i++) {
				try {
					connections[i] = SQLConnector.getConnection();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				connectionsInUse[i] = false;
			}
		}

		return instance;
	}
	/** Closes the Instance of Worm and shuts down the thread pool and all connections
	 *
	 **/
	public static void closeInstance(){
		Worm.threadPool.shutdown();
		for (Connection c: connections) {
			try {
				c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Adds object to the SQL database
	 * @param object The object to be added to the SQL database
	 * @return 1 on success and 0 or -1 on failure
	 */
	static public int    add(Object object){
		Future<Integer> fb= threadPool.submit(()-> {
			int i = -1;
			int out = 0;
			while ((i = getInstance().getFreeConnection())==-1) {System.out.println("Waiting");
				synchronized (connectionsInUse){
					connectionsInUse.wait();
				}

				}
			try {
				out = new dao().create(object.getClass(),object,connections[i]);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			getInstance().freeConnection(i);
			return out;
		});
		try {
			return fb.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		} finally {

		}
		return -1;
	}

	/**
	 * Gets all objects of Class clazz
	 * @param clazz the class of object to get from the database
	 * @return List<?> of type clazz
	 */
	static public List<?>    get(Class<?> clazz){
		Future<List<?>> fb= threadPool.submit(()-> {
			int i = -1;
			List<?> list = null;
			while ((i = getInstance().getFreeConnection())==-1) {System.out.println("Waiting");
				synchronized (connectionsInUse){
					connectionsInUse.wait();
				}

			}
			list = new dao().readAll(clazz,connections[i]);
			getInstance().freeConnection(i);
			return list;
		});
		try {
			return fb.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * <p>gets an object from its matching table with the matching values</p>
	 * @param clazz Class of object to get.
	 * @param matchValues Array of Objects to look for in table.
	 * @param matchKeys Corresponding column names to match vals.
	 * @return List<T> All matching objects in table.
	 */
	static public List<?>    get(Class<?> clazz,Object[] matchValues,Field[] matchKeys){
		Future<List<?>> fb= threadPool.submit(()-> {
			int i = -1;
			List<?> list = null;
			while ((i = getInstance().getFreeConnection())==-1) {System.out.println("Waiting");
				synchronized (connectionsInUse){
					connectionsInUse.wait();
				}

			}
			String[] chars = new String[matchKeys.length];
			for (int j = 0; j < chars.length; j++) {
				chars[j] = "=";
			}
			list = new dao().read(clazz,matchValues,chars,matchKeys,connections[i],true);
			getInstance().freeConnection(i);
			return list;
		});
		try {
			return fb.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * <p>gets an object from its matching table with the matching values</p>
	 * @param clazz Class of object to get.
	 * @param matchValues Array of Objects to look for in table.
	 * @param matchKeys Corresponding column names to match vals.
	 * @return List<T> All matching objects in table.
	 */
	static public List<?>    get(Class<?> clazz,Object[] matchValues,String[] comperators,Field[] matchKeys,boolean useAnd){
		Future<List<?>> fb= threadPool.submit(()-> {
			List<?> list = null;
			int i = -1;
			while ((i = getInstance().getFreeConnection())==-1) {System.out.println("Waiting");
				synchronized (connectionsInUse){
					connectionsInUse.wait();
				}

			}
			list = new dao().read(clazz,matchValues,comperators,matchKeys,connections[i],useAnd);
			getInstance().freeConnection(i);
			return list;
		});
		try {
			return fb.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * <p>Updates all rows that match matchValues with T obj</p>
	 * @param clazz Class of object to update
	 * @param matchValues Values to look for in table
	 * @param matchKeys Corresponding column names to match matchValues.
	 * @return int of rows updated
	 */
	static public Integer update(Class<?> clazz,Object obj,Object[] matchValues,Field[] matchKeys){
		Future<Integer> fb= threadPool.submit(()-> {
			int i = -1;
			int out = -1;
			while ((i = getInstance().getFreeConnection())==-1) {System.out.println("Waiting");
				synchronized (connectionsInUse){
					connectionsInUse.wait();
				}

			}
			out = new dao().update(clazz,obj,matchValues,matchKeys,connections[i]);
			getInstance().freeConnection(i);
			return out;
		});
		try {
			return fb.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * <p>Updates all rows that match matchValues with changeValues</p>
	 * @param clazz Class of object to update
	 * @param changeValues Values to update to
	 * @param changeKeys Corresponding column names to match changeValues.
	 * @param matchValues Values to look for in table
	 * @param matchKeys Corresponding column names to match matchValues.
	 * @return int of updated rows
	 */
	static public Integer update(Class<?> clazz,Object[] changeValues,Field[] changeKeys,Object[] matchValues,Field[] matchKeys){
		Future<Integer> fb= threadPool.submit(()-> {
			int i = -1;
			int out = 0;
			while ((i = getInstance().getFreeConnection())==-1) {System.out.println("Waiting");
				synchronized (connectionsInUse){
					connectionsInUse.wait();
				}

			}
			out = new dao().update(clazz,changeValues,changeKeys,matchValues,matchKeys,connections[i]);
			getInstance().freeConnection(i);
			return out;
		});
		try {
			return fb.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * <p>Deletes row[s] from object table</p>
	 * @param clazz Class of object to delete
	 * @param matchValues Values to look for in table
	 * @param matchKeys Corresponding column names to match vals.
	 * @return int of rows deleted
	 **/
	static public int delete(Class<?> clazz,Object[] matchValues,Field[] matchKeys){
		Future<Integer> fb= threadPool.submit(()-> {
		int i = -1;
		int out = -1;
		while ((i = getInstance().getFreeConnection())==-1) {System.out.println("Waiting");
				synchronized (connectionsInUse){
					connectionsInUse.wait();
				}

			}

		out = new dao().delete(clazz,matchValues,matchKeys,connections[i]);
		getInstance().freeConnection(i);
		return out;
	});
		try {
			return fb.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return -1;
	}
}