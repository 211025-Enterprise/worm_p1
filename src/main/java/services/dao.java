package services;

import models.annotation.*;
import models.enums.EnumConstraintsWorm;
import sun.reflect.annotation.AnnotationType;


import java.lang.reflect.*;
import java.util.List;
import java.util.Locale;

public class dao<T> {
	public int create(Class<T> tClass,T obj) throws IllegalAccessException {
		//TODO
		//	get all fields in order of declaration
		//	check for annotations
		String TableName;
		if (tClass.isAnnotationPresent(ClassWorm.class)) TableName= tClass.getDeclaredAnnotation(ClassWorm.class).table();
		else TableName = tClass.getSimpleName()+"s";
		TableName = TableName.toLowerCase();
		Field[] fields = tClass.getDeclaredFields();
		StringBuilder cols = new StringBuilder();
		StringBuilder qmks = new StringBuilder();
		StringBuilder tble = new StringBuilder();

		tble.append("CREATE TABLE ").append(TableName).append(" (\n");
		boolean pk = false;
		for (Field field:fields) {

			cols.append(field.getName().toLowerCase());
			tble.append(field.getName().toLowerCase()).append(" ");
			tble.append(JavaTypeToSqlJava(field.getType())).append(" ");
			FieldWorm a = field.getAnnotation(FieldWorm.class);
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



		return -1;
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
