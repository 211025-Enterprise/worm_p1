package services;

import models.annotation.*;

import java.lang.reflect.Field;
import java.util.List;

public class dao<T> {
	public int create(Class<T> tClass,T obj) throws IllegalAccessException {
		//TODO
		//	get all fields in order of declaration
		//	check for annotations
		String TableName;
		if (tClass.isAnnotationPresent(ClassWorm.class)) TableName= tClass.getDeclaredAnnotation(ClassWorm.class).table();
		else TableName = tClass.getSimpleName()+"s";
		Field[] fields = tClass.getDeclaredFields();
		StringBuilder cols = new StringBuilder();
		StringBuilder vals = new StringBuilder();
		StringBuilder Qmks = new StringBuilder();
		for (Field field:fields) {
			field.setAccessible(true);
			cols.append(field.getName());
			vals.append(field.get(obj));


		}



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

}
