import com.revature.Worm;

import java.lang.reflect.Field;
import java.time.LocalDate;

public class Main {
	public static void main(String[] args) throws NoSuchFieldException {
		Worm.getInstance("properties.xml");
		Worm.add(new Test(1,"hi",false,LocalDate.of(1000,10,01)));
		Worm.update(Test.class,new Test(2,"bye",true,LocalDate.now()), new Object[]{false},new Field[]{Test.class.getDeclaredField("aBoolean")});
		Worm.delete(Test.class, new Object[]{true},new Field[]{Test.class.getDeclaredField("aBoolean")});
	}
}
class Test{
	int anInt;
	String aString;
	boolean  aBoolean;
	LocalDate localDate;

	public Test(int anInt, String aString, boolean aBoolean, LocalDate  localDate) {
		this.anInt = anInt;
		this.aString = aString;
		this.aBoolean = aBoolean;
		this.localDate  = localDate;
	}

	public Test() {
	}
}
