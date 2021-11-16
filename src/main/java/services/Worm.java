package services;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Worm<O> {
	public boolean    add(O object){return  false;}
	public List<O>    get(Class<O> clazz){
		return null;
	}
	public List<O>    get(Class<O> clazz,Object[] matchValues,Field[] matchKeys){return null;}
	public boolean update(Class<O> clazz,O obj,Object[] matchValues,Field[] matchKeys){
		return false;
	}
	public boolean update(Class<O> clazz,Object[] changeValues,Field[] changeKeys,Object[] matchValues,Field[] matchKeys){
		return false;
	}
	public boolean delete(Class<O> clazz,Object[] matchValues,Field[] matchKeys){return false;}
	public boolean delete(Class<O> clazz,Object[] changeValues,Field[] changeKeys,Object[] matchValues,Field[] matchKeys){return false;}

}