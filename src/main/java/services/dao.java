package services;

import models.annotation.*;
import models.enums.EnumConstraintsWorm;
import sun.reflect.annotation.AnnotationType;


import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class dao<T> {
	public int create(Class<T> tClass,T obj) throws IllegalAccessException {
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
		int fieldcount = 0;
		for (Field field:fields) {
			FieldWorm a = field.getAnnotation(FieldWorm.class);
			if (JavaTypeToSqlJava(field.getType()).equals("")){continue;}
			fieldcount++;
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
		System.out.println(tble.toString());
		System.out.println("INSERT INTO "+TableName+" ("+cols+") Values ("+qmks+")");
		String sql0 = tble.toString();
		String sql1 = "INSERT INTO "+TableName+" ("+cols+") Values ("+qmks+")";
		try (Connection connection= SQLConnector.getConnection(); PreparedStatement creator =  connection.prepareStatement(sql0); PreparedStatement inserter =  connection.prepareStatement(sql1);) {
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
	public T read(Class<T> tClass,T obj){
		return null;
	}
	public List<T> readAll(Class<T> tClass,T obj){
		return null;
	}
	public int update(Class<T> tClass,T obj){
		return -1;
	}
	public int delete(Class<T> tClass,T obj){
		return -1;
	}

	private String JavaTypeToSqlJava(Class<?> type){
		if (type.isPrimitive()){
			switch (type.getTypeName()){
				case "boolean":
					return "bool";
				case "int":
					return "int";
				case "long":
					return "bigint";
				case "short":
					return "smallint";
				case "byte":
					return "bytea";
				case "float":
					return "real";
				case "double":
					return "float8";

			}
		}
		if 	(type.getTypeName().equals("java.lang.String"))
			return "varchar";
		return "";
	}
}
