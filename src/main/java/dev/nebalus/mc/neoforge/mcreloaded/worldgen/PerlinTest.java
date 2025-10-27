package dev.nebalus.mc.neoforge.mcreloaded.worldgen;

import java.util.Random;

public class PerlinTest {
    private final int[] perm = new int[512];

    public PerlinTest(long seed) {
        Random random = new Random(seed);
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;
        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256 - i) + i;
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
            perm[i] = perm[i + 256] = p[i];
        }
    }

    public double noise(double x, double y, double z) {
        int X = (int)Math.floor(x) & 255;
        int Y = (int)Math.floor(y) & 255;
        int Z = (int)Math.floor(z) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);

        double u = fade(x), v = fade(y), w = fade(z);
        int A = perm[X] + Y, AA = perm[A] + Z, AB = perm[A + 1] + Z;
        int B = perm[X + 1] + Y, BA = perm[B] + Z, BB = perm[B + 1] + Z;

        return lerp(w,
                lerp(v,
                        lerp(u, grad(perm[AA], x, y, z),
                                grad(perm[BA], x - 1, y, z)),
                        lerp(u, grad(perm[AB], x, y - 1, z),
                                grad(perm[BB], x - 1, y - 1, z))),
                lerp(v,
                        lerp(u, grad(perm[AA + 1], x, y, z - 1),
                                grad(perm[BA + 1], x - 1, y, z - 1)),
                        lerp(u, grad(perm[AB + 1], x, y - 1, z - 1),
                                grad(perm[BB + 1], x - 1, y - 1, z - 1))));
    }

    private double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private double lerp(double t, double a, double b) { return a + t * (b - a); }
    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
