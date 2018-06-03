package com.johnhite.recipe;

public class TestArray {

	
	public static void main(String...strings) {
		int width = 5;
		int height = 3;
		int length = 7;
		int[] world = new int[width * length * height];
		for (int z =0; z < length; z++) {
			for (int x = 0; x < width; x++) {
				for (int y=0; y < height; y++) {
					System.out.println("index: " + (x + y*height + z* length) + " (" + x + ", " + y +", " + z + ")");
					world[(x + y*height + z* length)] = y;
				}
			}
		}
	}
}
