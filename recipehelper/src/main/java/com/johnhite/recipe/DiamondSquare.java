package com.johnhite.recipe;

import java.util.Arrays;
import java.util.Queue;
import java.util.Random;

import com.google.common.collect.Lists;

public class DiamondSquare {

	public static class Diamond {
		public int step;
		public int x;
		public int z;
		public int mag;
		public Diamond(int step, int x, int z, int mag) {
			super();
			this.step = step;
			this.x = x;
			this.z = z;
			this.mag = mag;
		}
		@Override
		public String toString() {
			return "Diamond [step=" + step + ", x=" + x + ", z=" + z + "]";
		}
		
	}
	
	public static void main(String...strings) {
		
		int[][] heightMap = diamondSquare(9);
		printMatrix(heightMap);
	}
	public static void printMatrix(int[][] mat) {
		for (int i = mat.length - 1; i >= 0 ; i--) {
			System.out.println(Arrays.toString(mat[i]));
		}
	}
	
	private static Random rand = new Random();
	public static int[][] diamondSquare(int size) {
		int[][] heightMap = new int[size][size];
		
		heightMap[0][0] = rand.nextInt(16);
		heightMap[size-1][0] = rand.nextInt(16);
		heightMap[0][size -1] = rand.nextInt(16);
		heightMap[size -1 ][size - 1] = rand.nextInt(16);
		
		int step = size;
		int mag = 16;
		int numSteps = (int)(Math.log(mag) / Math.log(2));
		Queue<Diamond> queue = Lists.newLinkedList();
		queue.add(new Diamond(step, 0, 0, mag));
		
		while(!queue.isEmpty()) {
			Diamond d = queue.remove();
			diamond(d.step, d.x, d.z, size, heightMap, d.mag);
			square(d.step, d.x, d.z, size, heightMap, d.mag);
			/*System.out.println(d);
			printMatrix(heightMap);
			System.out.println();*/
			int next = d.step / 2;
			int nextMag = d.mag - (mag / numSteps) + 1;
			if (next > 1) {
				queue.add(new Diamond(next + 1, d.x, d.z, nextMag));
				queue.add(new Diamond(next + 1, d.x + next, d.z, nextMag));
				queue.add(new Diamond(next + 1, d.x, d.z + next, nextMag));
				queue.add(new Diamond(next + 1, d.x + next, d.z + next, nextMag));
			}
		}
		return heightMap;
	}
	
	public static void diamond(int step, int x, int z, int size, int[][] heightMap, int mag) {
		//diamond step
		int mid = step / 2;
		int corner = step - 1;
		heightMap[x + mid][z + mid] = avg(
				heightMap[x][z],
				heightMap[x][z + corner],
				heightMap[x + corner][z],
				heightMap[x + corner][z + corner],
				rand.nextInt(mag));
	}
	
	public static void square(int step, int x, int z, int size, int[][] heightMap, int mag) {
		int mid = step / 2;
		int corner = step - 1;

		//square step
		int wrap = z - mid < 0 ? z - mid + size -1 : z - mid;
		heightMap[x + mid][z] = avg(
				heightMap[x + mid][z + mid],
				heightMap[x][z],
				heightMap[x + corner][z],
				heightMap[x + mid][wrap],
				rand.nextInt(mag)
				);
		
		wrap = (z + corner + mid) % size;
		heightMap[x + mid][z + corner] = avg(
				heightMap[x + mid][z + mid],
				heightMap[x][z + corner],
				heightMap[x + corner][z + corner],
				heightMap[x + mid][wrap],
				rand.nextInt(mag));
		
		wrap = x - mid < 0 ? x - mid + size -1 : x - mid;
		heightMap[x][z + mid] = avg(
				heightMap[x + mid][z + mid],
				heightMap[x][z],
				heightMap[x][z + corner],
				heightMap[wrap][z + mid],
				rand.nextInt(mag));
		
		wrap = (x + corner + mid) % size;
		heightMap[x + corner][z + mid] = avg(
				heightMap[x + mid][z + mid],
				heightMap[x + corner][z],
				heightMap[x + corner][z + corner],
				heightMap[wrap][z + mid],
				rand.nextInt(mag));
	}
	
	public static int avg(int... ints) {
		int sum = 0;
		for (int i : ints) {
			sum += i;
		}
		return (int)Math.round(((double)sum) / ((double)ints.length));
	}
}
