package com.revature.services;
import com.revature.models.annotation.*;
import com.revature.models.enums.*;
import com.revature.models.exceptions.*;

import java.beans.FeatureDescriptor;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <h3>Holds the generic functions to store any object that has primitive fields and ignores object fields except for String</h3>
 * @author ryanh
 * @version 1
 *
 */
public class dao<T> {
	/**
	 * <p>Create a row on the objects table in the database. Will create a table if none exists</p>
	 * @param tClass Objects Class to insert
	 * @param obj Object to insert
	 * @return obj inserted or null
	 * @throws IllegalAccessException if field on object could not be accessed
	 */
	public T create(Class<T> tClass,T obj,Connection connection) throws IllegalAccessException {
		String TableName;
		if (tClass.isAnnotationPresent(ClassWorm.class)) TableName= tClass.getDeclaredAnnotation(ClassWorm.class).table();
		else TableName = tClass.getSimpleName()+"s";
		TableName = TableName.toLowerCase();
		Field[] fields = tClass.getDeclaredFields();
		StringBuilder cols = new StringBuilder();
		StringBuilder qmks = new StringBuilder();
		StringBuilder tble = new StringBuilder();

		tble.append("CREATE TABLE IF NOT EXISTS ").append(TableName).append(" (\n");
		boolean pk = false;
		for (Field field:fields) {
			FieldWorm a = field.getAnnotation(FieldWorm.class);
			if (JavaTypeToSqlJava(field.getType()).equals("")){continue;}
			String colname = field.getName().toLowerCase();
			if (a != null) colname=a.Name().toLowerCase();

			tble.append(colname).append(" ");
			cols.append(colname);
			tble.append(JavaTypeToSqlJava(field.getType())).append(" ");
			if (a != null) {
				EnumConstraintsWorm[] enumConstraintsWorms= a.constraints();
				for (EnumConstraintsWorm e : enumConstraintsWorms) {
					switch (e) {
						case Unique:
							tble.append("UNIQUE");
							break;
						case NonNull:
							tble.append("NOT NULL");
							break;
						case PrimaryKey:
							if (!pk)
								tble.append("Primary Key");
							pk = true;
							break;
					}

				}
			}
			qmks.append("?");
			cols.append(",");
			qmks.append(",");
			tble.append(",\n");
		}
		tble.deleteCharAt(tble.length()-1);
		tble.deleteCharAt(tble.length()-1);
		qmks.deleteCharAt(qmks.length()-1);
		cols.deleteCharAt(cols.length()-1);
		tble.append("\n);");
		String sql0 = tble.toString();
		String sql1 = "INSERT INTO "+TableName+" ("+cols+") Values ("+qmks+")";
		try (PreparedStatement creator =  connection.prepareStatement(sql0); PreparedStatement inserter =  connection.prepareStatement(sql1);) {
			creator.executeUpdate();
			int loc = 1;
			for (Field field:fields) {
				if (JavaTypeToSqlJava(field.getType()).equals("")){continue;}
				field.setAccessible(true);
				inserter.setObject(loc++,field.get(obj));

			}
			if (inserter.executeUpdate() > 0) return obj;
			else return null;
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * <p>gets an object from its matching table with the matching values</p>
	 * @param tClass Class of object to get.
	 * @param matchValues Array of Objects to look for in table.
	 * @param matchKeys Corresponding column names to match matchValues.
	 * @return List<T> All matching objects in table.
	 * @throws WormException is thrown if matchValues does not correspond to keys
	 */
	public List<T> read(Class<T> tClass,Object[] matchValues,String[] operators ,Field[] matchKeys,Connection connection, boolean and) throws WormException {
		if (matchValues.length != matchKeys.length) throw new WormException();
		else if(matchValues.length == 0) return readAll(tClass,connection);
		List<T> out = new ArrayList<>();
		String TableName;
		if (tClass.isAnnotationPresent(ClassWorm.class)) TableName= tClass.getDeclaredAnnotation(ClassWorm.class).table();
		else TableName = tClass.getSimpleName()+"s";
		TableName = TableName.toLowerCase();
		Field[] fields = tClass.getDeclaredFields();
		StringBuilder keyString = new StringBuilder();
		int i = 0;
		for (Field key:matchKeys) {
			String comparitor = (and)? "AND":"OR";
			if (keyString.length() > 0 ) keyString.append(" ").append(comparitor).append(" ");
			String keyname =  key.getName().toLowerCase();
			if (key.getAnnotation(FieldWorm.class) !=null)keyname = key.getAnnotation(FieldWorm.class).Name();
			keyString.append(keyname).append(" ").append(operators[i++]).append(" ?");
		}

		String sql = "SELECT * FROM "+TableName + " WHERE "+keyString.toString();
		try (PreparedStatement selector =  connection.prepareStatement(sql); ) {
			int loc = 1;
			for (Object o:matchValues) {
				selector.setObject(loc++,o);
			}
			ResultSet resultSet = selector.executeQuery();
			while (resultSet.next()){
				Constructor<?> constructor = Arrays.stream(tClass.getDeclaredConstructors()).filter(x->x.getParameterCount() == 0).findFirst().orElse(null);
				constructor.setAccessible(true);
				T obj= (T) constructor.newInstance();
				loc = 1;
				for (Field field:fields) {
					if (JavaTypeToSqlJava(field.getType()).equals("")){continue;}
					field.setAccessible(true);
					field.set(obj,resultSet.getObject(loc++));
				}
				out.add(obj);
			}
			}catch (SQLException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return out;
	}
	/**
	 * Gets all objects from object table
	 * @param tClass Class of object to get from its table
	 * @return List<T> all objects of type tClass
	 */
	public List<T> readAll(Class<T> tClass,Connection connection){
		List<T> all = new ArrayList<>();
		String TableName;
		if (tClass.isAnnotationPresent(ClassWorm.class)) TableName= tClass.getDeclaredAnnotation(ClassWorm.class).table();
		else TableName = tClass.getSimpleName()+"s";
		TableName = TableName.toLowerCase();
		Field[] fields = tClass.getDeclaredFields();
		String sql = "SELECT * FROM "+TableName;
		try ( PreparedStatement selector =  connection.prepareStatement(sql); ) {
			ResultSet resultSet = selector.executeQuery();
			while (resultSet.next()){
				Constructor<?> constructor = Arrays.stream(tClass.getDeclaredConstructors()).filter(x->x.getParameterCount() == 0).findFirst().orElse(null);
				constructor.setAccessible(true);
				T obj= (T) constructor.newInstance();
				int loc = 1;
				for (Field field:fields) {
					if (JavaTypeToSqlJava(field.getType()).equals("")){continue;}
					field.setAccessible(true);
					field.set(obj,resultSet.getObject(loc++));
				}
				all.add(obj);
			}


		}
		catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return all;
	}
	/**
	 * <p>Updates all rows that match matchValues with T obj</p>
	 * @param tClass Class of object to update
	 * @param matchValues Values to look for in table
	 * @param matchKeys Corresponding column names to match matchValues.
	 * @return int of rows updated
	 * @throws WormException is thrown if matchValues does not correspond to keys
	 */
	public List<T> update(Class<T> tClass,T obj,Object[] matchValues,Field[] matchKeys,Connection connection) throws WormException {
		if (matchValues.length != matchKeys.length) throw new WormException();
		List<T> out = null;
		String TableName;
		if (tClass.isAnnotationPresent(ClassWorm.class)) TableName= tClass.getDeclaredAnnotation(ClassWorm.class).table();
		else TableName = tClass.getSimpleName()+"s";
		TableName = TableName.toLowerCase();
		Field[] fields = tClass.getDeclaredFields();
		StringBuilder keyString = new StringBuilder();
		StringBuilder cols = new StringBuilder();
		StringBuilder qmks = new StringBuilder();
		for (Field key:matchKeys) {
			if (keyString.length() > 0 ) keyString.append(" AND ");
			String keyname =  key.getName().toLowerCase();
			if (key.getAnnotation(FieldWorm.class) !=null)keyname = key.getAnnotation(FieldWorm.class).Name();
			keyString.append(keyname).append(" = ?");
		}
		for (Field field:fields) {
			FieldWorm a = field.getAnnotation(FieldWorm.class);
			if (JavaTypeToSqlJava(field.getType()).equals("")){continue;}
			String colname = field.getName().toLowerCase();
			if (a != null) colname=a.Name().toLowerCase();
			cols.append(colname);
			qmks.append("?");
			cols.append(",");
			qmks.append(",");
		}
		qmks.deleteCharAt(qmks.length()-1);
		cols.deleteCharAt(cols.length()-1);

		String sql = "UPDATE "+TableName + " SET ( "+cols+" )=( "+qmks+" ) WHERE "+keyString.toString();
		try ( PreparedStatement selector =  connection.prepareStatement(sql) ) {
			int loc = 1;
			for (Field o:fields) {
				o.setAccessible(true);
				if (JavaTypeToSqlJava(o.getType()).equals("")){continue;}
				selector.setObject(loc++, o.get(obj));
			}
			for (Object o:matchValues) {
				if (JavaTypeToSqlJava(o.getClass()).equals("")){continue;}
				selector.setObject(loc++,o);
			}

			selector.executeUpdate();
			ResultSet resultSet = selector.getResultSet();
			out = new ArrayList<>();
			while (resultSet.next()) {
				Constructor<?> constructor = Arrays.stream(tClass.getDeclaredConstructors()).filter(x->x.getParameterCount() == 0).findFirst().orElse(null);
				constructor.setAccessible(true);
				T o= (T) constructor.newInstance();
				int i = 1;
				for (Field field: o.getClass().getDeclaredFields()){
					field.set(o,resultSet.getObject(i++));
				}
				out.add(o);

			}
		}catch (SQLException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}




		return out;

	}
	/**
	 * <p>Updates all rows that match matchValues with changeValues</p>
	 * @param tClass Class of object to update
	 * @param changeValues Values to update to
	 * @param changeKeys Corresponding column names to match changeValues.
	 * @param matchValues Values to look for in table
	 * @param matchKeys Corresponding column names to match matchValues.
	 * @return int of updated rows
	 * @throws WormException is thrown if Values does not correspond to keys
	 */
	public List<T> update(Class<T> tClass,Object[] changeValues,Field[] changeKeys,Object[] matchValues,Field[] matchKeys,Connection connection) throws WormException {
		if (matchValues.length != matchKeys.length) throw new WormException();
		if (changeValues.length != changeKeys.length) throw new WormException();
		List<T> out = null;
		String TableName;
		if (tClass.isAnnotationPresent(ClassWorm.class)) TableName= tClass.getDeclaredAnnotation(ClassWorm.class).table();
		else TableName = tClass.getSimpleName()+"s";
		TableName = TableName.toLowerCase();



		StringBuilder keyString = new StringBuilder();
		StringBuilder cols = new StringBuilder();
		StringBuilder qmks = new StringBuilder();
		if (changeKeys.length > 1){
			cols.append('(');
			qmks.append('(');
		}
		for (Field key:matchKeys) {
			if (keyString.length() > 0 ) keyString.append(" AND ");
			String keyname =  key.getName().toLowerCase();
			if (key.getAnnotation(FieldWorm.class) !=null)keyname = key.getAnnotation(FieldWorm.class).Name();
			keyString.append(keyname).append(" = ?");
		}
		for (Field field:changeKeys) {
			FieldWorm a = field.getAnnotation(FieldWorm.class);
			if (JavaTypeToSqlJava(field.getType()).equals("")){continue;}
			String colname = field.getName().toLowerCase();
			if (a != null) colname=a.Name().toLowerCase();
			cols.append(colname);
			qmks.append("?");
			cols.append(",");
			qmks.append(",");
		}
		qmks.deleteCharAt(qmks.length()-1);
		cols.deleteCharAt(cols.length()-1);
		if (changeKeys.length > 1){
			cols.append(')');
			qmks.append(')');
		}
		String sql = "UPDATE "+TableName + " SET "+cols+"="+qmks+" WHERE "+keyString.toString();
		try ( PreparedStatement selector =  connection.prepareStatement(sql); ) {
			int loc = 1;

			for (Object o:changeValues) {
				if (JavaTypeToSqlJava(o.getClass()).equals("")){continue;}
				selector.setObject(loc++, o);
			}
			for (Object o:matchValues) {
				if (JavaTypeToSqlJava(o.getClass()).equals("")){continue;}
				selector.setObject(loc++, o);
			}

			System.out.println(selector);
			selector.executeUpdate();
			ResultSet resultSet = selector.getResultSet();
			out = new ArrayList<>();
			while (resultSet.next()) {
				Constructor<?> constructor = Arrays.stream(tClass.getDeclaredConstructors()).filter(x->x.getParameterCount() == 0).findFirst().orElse(null);
				constructor.setAccessible(true);
				T obj= (T) constructor.newInstance();
				int i = 1;
				for (Field field: obj.getClass().getDeclaredFields()){
					field.set(obj,resultSet.getObject(i++));
				}
				out.add(obj);

			}
		}catch (SQLException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}


		return out;

	}
	/**
	 * <p>Deletes row[s] from object table</p>
	 * @param tClass Class of object to delete
	 * @param matchValues Values to look for in table
	 * @param matchKeys Corresponding column names to match vals.
	 * @return int of rows deleted
	 * @throws WormException is thrown if vals does not correspond to keys
	 **/
	public int delete(Class<T> tClass,Object[] matchValues,Field[] matchKeys,Connection connection) throws WormException {
		if (matchValues.length != matchKeys.length) throw new WormException();
		int out = -1;
		String TableName;
		if (tClass.isAnnotationPresent(ClassWorm.class)) TableName= tClass.getDeclaredAnnotation(ClassWorm.class).table();
		else TableName = tClass.getSimpleName()+"s";
		TableName = TableName.toLowerCase();
		Field[] fields = tClass.getDeclaredFields();
		StringBuilder keyString = new StringBuilder();
		for (Field key:matchKeys) {
			if (keyString.length() > 0 ) keyString.append(" AND ");
			String keyname =  key.getName().toLowerCase();
			if (key.getAnnotation(FieldWorm.class) !=null)keyname = key.getAnnotation(FieldWorm.class).Name();
			keyString.append(keyname).append(" = ?");
		}
		String sql = "DELETE FROM "+TableName + " WHERE "+keyString.toString();
		try ( PreparedStatement selector =  connection.prepareStatement(sql); ) {
			int loc = 1;
			for (Object o:matchValues) {
				if (JavaTypeToSqlJava(o.getClass()).equals("")){continue;}
				selector.setObject(loc++, o);
			}
			out = selector.executeUpdate();
		}catch (SQLException e) {
			e.printStackTrace();
		}


		return out;
	}
	private String JavaTypeToSqlJava(Class<?> type){
		switch (type.getTypeName()){
			case "java.lang.Boolean":
			case "boolean":
				return "bool";
			case "java.lang.Integer":
			case "int":
				return "int";
			case "java.lang.Long":
			case "long":
				return "bigint";
			case "java.lang.Short":
			case "short":
				return "smallint";
			case "java.lang.Byte":
			case "byte":
				return "bytea";
			case "java.lang.Float":
			case "float":
				return "real";
			case "java.lang.Double":
			case "double":
				return "float8";

		}
		if 	(type.getTypeName().equals("java.lang.String"))
			return "varchar";
		return "";
	}
}
