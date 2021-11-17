
import models.annotation.*;
import models.enums.*;
import models.exceptions.WormException;
import services.Worm;
import services.dao;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {
	public static void main(String[] args) throws SQLException {
		Worm.getInstance();
		Worm.add(new TEST("NEW",1235134561,false));
		for (Object o : Worm.get(TEST.class)) System.out.println(o);
		try {System.out.println(Worm.get(TEST.class, new String[]{"NEW"}, new Field[]{TEST.class.getField("x")}));} catch (NoSuchFieldException e) {e.printStackTrace();}
		try {System.out.println(Worm.update(TEST.class, new TEST("NEW1",123123,true),new String[]{"NEW"}, new Field[]{TEST.class.getField("x")}));} catch (NoSuchFieldException e) {e.printStackTrace();}
		for (Object o : Worm.get(TEST.class)) System.out.println(o);
		try {System.out.println(Worm.update(TEST.class,new Boolean[]{true}, new Field[]{TEST.class.getField("z")} ,new String[]{"hello"}, new Field[]{TEST.class.getField("x")}));} catch (NoSuchFieldException e) {e.printStackTrace();}
		for (Object o : Worm.get(TEST.class)) System.out.println(o);
		try {System.out.println(Worm.delete(TEST.class,new Boolean[]{false}, new Field[]{TEST.class.getField("z")}));} catch (NoSuchFieldException e) {e.printStackTrace();}
		for (Object o : Worm.get(TEST.class)) System.out.println(o);
		Worm.closeInstance();

	}
}

@ClassWorm(table = "testtable12")
class TEST{
	public String x;
	@FieldWorm(Name = "testid", constraints = {EnumConstraintsWorm.PrimaryKey})
	public  int y;
	public  boolean z;
	public Object object;
	public TEST(String x, int y, boolean z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	TEST(){};

	@Override
	public String toString() {
		return "TEST{" +
			"x='" + x + '\'' +
			", y=" + y +
			", z=" + z +
			", object=" + object +
			'}';
	}
}