package services;
import models.annotation.*;
import models.enums.*;
import models.exceptions.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

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
	 * @return int number of rows edited
	 * @throws IllegalAccessException if field on object could not be accessed
	 */
	public int create(Class<T> tClass,T obj,Connection connection) throws IllegalAccessException {
		int output = -1;
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
				switch (field.getType().getTypeName()) {
					case "boolean":
						inserter.setBoolean(loc++,field.getBoolean(obj));
						break;
					case "int":
						inserter.setInt(loc++,field.getInt(obj));
						break;
					case "long":
						inserter.setLong(loc++,field.getLong(obj));
						break;
					case "short":
						inserter.setShort(loc++,field.getShort(obj));
						break;
					case "byte":
						inserter.setByte(loc++,field.getByte(obj));
						break;
					case "float":
						inserter.setFloat(loc++,field.getFloat(obj));
						break;
					case "double":
						inserter.setDouble(loc++,field.getDouble(obj));
						break;
					default:
						Object o = field.get(obj);
						inserter.setString(loc++,(o != null)? o.toString():"null");
						break;
				}
			}
			output = inserter.executeUpdate();

		}
		catch (SQLException e) {
			e.printStackTrace();
		}


		return output;
	}
	/**
	 * <p>gets an object from its matching table with the matching values</p>
	 * @param tClass Class of object to get.
	 * @param matchValues Array of Objects to look for in table.
	 * @param matchKeys Corresponding column names to match vals.
	 * @return List<T> All matching objects in table.
	 * @throws WormException is thrown if vals does not correspond to keys
	 */
	public List<T> read(Class<T> tClass,Object[] matchValues,Field[] matchKeys,Connection connection) throws WormException {
		if (matchValues.length != matchKeys.length) throw new WormException();
		else if(matchValues.length == 0) return readAll(tClass,connection);
		List<T> out = new ArrayList<>();
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


		String sql = "SELECT * FROM "+TableName + " WHERE "+keyString.toString();
		try (PreparedStatement selector =  connection.prepareStatement(sql); ) {
			int loc = 1;
			for (Object o:matchValues) {
				if (JavaTypeToSqlJava(o.getClass()).equals("")){continue;}
				switch (o.getClass().getTypeName()) {
					case "java.lang.Boolean":
					case "boolean":
						selector.setBoolean(loc++, (Boolean) o);
						break;
					case "java.lang.Integer":
					case "int":
						selector.setInt(loc++, (Integer) o);
						break;
					case "java.lang.Long":
					case "long":
						selector.setLong(loc++, (Long) o);
						break;
					case "java.lang.Short":
					case "short":
						selector.setShort(loc++, (Short) o);
						break;
					case "java.lang.Byte":
					case "byte":
						selector.setByte(loc++, (Byte) o);
						break;
					case "java.lang.Float":
					case "float":
						selector.setFloat(loc++, (Float) o);
						break;
					case "java.lang.Double":
					case "double":
						selector.setDouble(loc++, (Double) o);
						break;
					default:
						selector.setString(loc++, (String) o);
						break;
				}
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
					switch (field.getType().getTypeName()) {
						case "boolean":
							field.setBoolean(obj,resultSet.getBoolean(loc++));
							break;
						case "int":
							field.setInt(obj,resultSet.getInt(loc++));
							break;
						case "long":
							field.setLong(obj,resultSet.getLong(loc++));
							break;
						case "short":
							field.setShort(obj,resultSet.getShort(loc++));
							break;
						case "byte":
							field.setByte(obj,resultSet.getByte(loc++));
							break;
						case "float":
							field.setFloat(obj,resultSet.getFloat(loc++));
							break;
						case "double":
							field.setDouble(obj,resultSet.getDouble(loc++));
							break;
						default:
							field.set(obj, resultSet.getString(loc++));
							break;
					}

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
					switch (field.getType().getTypeName()) {
						case "boolean":
							field.setBoolean(obj,resultSet.getBoolean(loc++));
							break;
						case "int":
							field.setInt(obj,resultSet.getInt(loc++));
							break;
						case "long":
							field.setLong(obj,resultSet.getLong(loc++));
							break;
						case "short":
							field.setShort(obj,resultSet.getShort(loc++));
							break;
						case "byte":
							field.setByte(obj,resultSet.getByte(loc++));
							break;
						case "float":
							field.setFloat(obj,resultSet.getFloat(loc++));
							break;
						case "double":
							field.setDouble(obj,resultSet.getDouble(loc++));
							break;
						default:
							field.set(obj, resultSet.getString(loc++));
							break;
					}

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
	public int update(Class<T> tClass,T obj,Object[] matchValues,Field[] matchKeys,Connection connection) throws WormException {
		if (matchValues.length != matchKeys.length) throw new WormException();
		int out = -1;
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
				switch (o.getType().toString()) {
					case "java.lang.Boolean":
					case "boolean":
						selector.setBoolean(loc++, o.getBoolean(obj));
						break;
					case "java.lang.Integer":
					case "int":
						selector.setInt(loc++, o.getInt(obj));
						break;
					case "java.lang.Long":
					case "long":
						selector.setLong(loc++, o.getLong(obj));
						break;
					case "java.lang.Short":
					case "short":
						selector.setShort(loc++, o.getShort(obj));
						break;
					case "java.lang.Byte":
					case "byte":
						selector.setByte(loc++, o.getByte(obj));
						break;
					case "java.lang.Float":
					case "float":
						selector.setFloat(loc++, o.getFloat(obj));
						break;
					case "java.lang.Double":
					case "double":
						selector.setDouble(loc++, o.getDouble(obj));
						break;
					default:
						selector.setString(loc++, (String) o.get(obj));
						break;
				}
			}
			for (Object o:matchValues) {
				if (JavaTypeToSqlJava(o.getClass()).equals("")){continue;}
				switch (o.getClass().getTypeName()) {
					case "java.lang.Boolean":
					case "boolean":
						selector.setBoolean(loc++, (Boolean) o);
						break;
					case "java.lang.Integer":
					case "int":
						selector.setInt(loc++, (Integer) o);
						break;
					case "java.lang.Long":
					case "long":
						selector.setLong(loc++, (Long) o);
						break;
					case "java.lang.Short":
					case "short":
						selector.setShort(loc++, (Short) o);
						break;
					case "java.lang.Byte":
					case "byte":
						selector.setByte(loc++, (Byte) o);
						break;
					case "java.lang.Float":
					case "float":
						selector.setFloat(loc++, (Float) o);
						break;
					case "java.lang.Double":
					case "double":
						selector.setDouble(loc++, (Double) o);
						break;
					default:
						selector.setString(loc++, (String) o);
						break;
				}
			}

			out= selector.executeUpdate();




		}catch (SQLException | IllegalAccessException e) {
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
	public int update(Class<T> tClass,Object[] changeValues,Field[] changeKeys,Object[] matchValues,Field[] matchKeys,Connection connection) throws WormException {
		if (matchValues.length != matchKeys.length) throw new WormException();
		if (changeValues.length != changeKeys.length) throw new WormException();
		int out = -1;
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
				switch (o.getClass().getTypeName().toString()) {
					case "java.lang.Boolean":
					case "boolean":
						selector.setBoolean(loc++, (Boolean) o);
						break;
					case "java.lang.Integer":
					case "int":
						selector.setInt(loc++, (Integer) o);
						break;
					case "java.lang.Long":
					case "long":
						selector.setLong(loc++, (Long) o);
						break;
					case "java.lang.Short":
					case "short":
						selector.setShort(loc++, (Short) o);
						break;
					case "java.lang.Byte":
					case "byte":
						selector.setByte(loc++, (Byte) o);
						break;
					case "java.lang.Float":
					case "float":
						selector.setFloat(loc++, (Float) o);
						break;
					case "java.lang.Double":
					case "double":
						selector.setDouble(loc++, (Double) o);
						break;
					default:
						selector.setString(loc++, (String) o);
						break;
				}
			}
			for (Object o:matchValues) {
				if (JavaTypeToSqlJava(o.getClass()).equals("")){continue;}
				switch (o.getClass().getTypeName()) {
					case "java.lang.Boolean":
					case "boolean":
						selector.setBoolean(loc++, (Boolean) o);
						break;
					case "java.lang.Integer":
					case "int":
						selector.setInt(loc++, (Integer) o);
						break;
					case "java.lang.Long":
					case "long":
						selector.setLong(loc++, (Long) o);
						break;
					case "java.lang.Short":
					case "short":
						selector.setShort(loc++, (Short) o);
						break;
					case "java.lang.Byte":
					case "byte":
						selector.setByte(loc++, (Byte) o);
						break;
					case "java.lang.Float":
					case "float":
						selector.setFloat(loc++, (Float) o);
						break;
					case "java.lang.Double":
					case "double":
						selector.setDouble(loc++, (Double) o);
						break;
					default:
						selector.setString(loc++, (String) o);
						break;
				}
			}

			System.out.println(selector);
			out= selector.executeUpdate();




		}catch (SQLException e) {
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
				switch (o.getClass().getTypeName()) {
					case "java.lang.Boolean":
					case "boolean":
						selector.setBoolean(loc++, (Boolean) o);
						break;
					case "java.lang.Integer":
					case "int":
						selector.setInt(loc++, (Integer) o);
						break;
					case "java.lang.Long":
					case "long":
						selector.setLong(loc++, (Long) o);
						break;
					case "java.lang.Short":
					case "short":
						selector.setShort(loc++, (Short) o);
						break;
					case "java.lang.Byte":
					case "byte":
						selector.setByte(loc++, (Byte) o);
						break;
					case "java.lang.Float":
					case "float":
						selector.setFloat(loc++, (Float) o);
						break;
					case "java.lang.Double":
					case "double":
						selector.setDouble(loc++, (Double) o);
						break;
					default:
						selector.setString(loc++, (String) o);
						break;
				}
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
