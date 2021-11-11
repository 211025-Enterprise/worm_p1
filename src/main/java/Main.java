
import models.annotation.*;
import models.enums.*;
import services.dao;

public class Main {
	public static void main(String[] args) {
		try {
			new dao<TEST>().create(TEST.class,new TEST("hello",1,false));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}
}

@ClassWorm(table = "testtable1")
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
}