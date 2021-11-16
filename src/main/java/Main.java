
import models.annotation.*;
import models.enums.*;
import models.exceptions.WormException;
import services.dao;

import java.lang.reflect.Field;
import java.util.List;

public class Main {
	public static void main(String[] args) {

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