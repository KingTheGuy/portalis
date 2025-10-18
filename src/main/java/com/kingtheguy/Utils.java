package com.kingtheguy;

public class Utils {

  public int getDistance(int x1, int z1, int x2, int z2) {
    int z = x2 - x1;
    int x = z2 - z1;
    return (int) Math.sqrt(x * x + z * z);
  }

  public static double get3DDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
    int deltaX = x2 - x1;
    int deltaY = y2 - y1;
    int deltaZ = z2 - z1;
    return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
  } 
}
