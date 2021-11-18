package com.revature;

import java.lang.reflect.Field;

public class Main {
	public static void main(String[] args) {
		Worm.getInstance();
		for (int i = 0; i < 100; i++) {
			Worm.add(new Test(i,(i%3==0),((float) i)/5f));
			System.out.println(i);
		}
		try {
			System.out.println(Worm.get(Test.class,new Integer[]{33,66},new String[]{">=","<="},new Field[]{Test.class.getDeclaredField("anInt"),Test.class.getDeclaredField("anInt")},true));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
}

class Test{
	int anInt;
	boolean aBoolean;
	float aFloat;

	public Test() {
	}

	public Test(int anInt, boolean aBoolean, float aFloat) {
		this.anInt = anInt;
		this.aBoolean = aBoolean;
		this.aFloat = aFloat;
	}

	public int getAnInt() {
		return anInt;
	}

	public void setAnInt(int anInt) {
		this.anInt = anInt;
	}

	public boolean isaBoolean() {
		return aBoolean;
	}

	public void setaBoolean(boolean aBoolean) {
		this.aBoolean = aBoolean;
	}

	public float getaFloat() {
		return aFloat;
	}

	public void setaFloat(float aFloat) {
		this.aFloat = aFloat;
	}

	@Override
	public String toString() {
		return "Test{" +
			"anInt=" + anInt +
			", aBoolean=" + aBoolean +
			", aFloat=" + aFloat +
			"}\n";
	}
}
