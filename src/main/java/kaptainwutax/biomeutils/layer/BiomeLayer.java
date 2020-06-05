package kaptainwutax.biomeutils.layer;

import kaptainwutax.seedutils.mc.MCVersion;
import kaptainwutax.seedutils.prng.SeedMixer;

import java.util.HashMap;
import java.util.Map;

public abstract class BiomeLayer {
    private final MCVersion version;
    private final BiomeLayer[] parents;
    private final long layerSeed;
    private long localSeed;

    protected int scale = -1;
    protected int layerId;

    private Map<Long, Integer> cache = new HashMap<>();

    public BiomeLayer(MCVersion version, long worldSeed, long salt, BiomeLayer... parents) {
        this.version = version;
        this.layerSeed = getLayerSeed(worldSeed, salt);
        this.parents = parents;
    }

    public BiomeLayer(MCVersion version, long worldSeed, long salt) {
        this(version, worldSeed, salt, (BiomeLayer)null);
    }

    public MCVersion getVersion() {
        return this.version;
    }

    public int getLayerId() {
        return this.layerId;
    }

    public int getScale() {
        return this.scale == -1 ? this.scale = this.getParent().getScale() : this.scale;
    }

    public boolean hasParent() {
        return this.parents.length > 0;
    }

    public BiomeLayer getParent() {
        return this.getParent(0);
    }

    public BiomeLayer getParent(int id) {
        return this.parents[id];
    }

    public boolean isMergingLayer() {
        return this.parents.length > 1;
    }

    public BiomeLayer[] getParents() {
        return this.parents;
    }

    public int get(int x, int z) {
        long v = x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
        Integer r = this.cache.get(v);

        if (r == null) {
            r = this.sample(x, z);
            this.cache.put(v, r);
            return r;
        }

        return r;
    }

    public abstract int sample(int x, int z);

    public static long getLayerSeed(long worldSeed, long salt) {
        long midSalt = SeedMixer.mixSeed(salt, salt);
        midSalt = SeedMixer.mixSeed(midSalt, salt);
        midSalt = SeedMixer.mixSeed(midSalt, salt);
        long layerSeed = SeedMixer.mixSeed(worldSeed, midSalt);
        layerSeed = SeedMixer.mixSeed(layerSeed, midSalt);
        layerSeed = SeedMixer.mixSeed(layerSeed, midSalt);
        return layerSeed;
    }

    public static long getLocalSeed(long layerSeed, int x, int z) {
        layerSeed = SeedMixer.mixSeed(layerSeed, x);
        layerSeed = SeedMixer.mixSeed(layerSeed, z);
        layerSeed = SeedMixer.mixSeed(layerSeed, x);
        layerSeed = SeedMixer.mixSeed(layerSeed, z);
        return layerSeed;
    }

    public static long getLocalSeed(long worldSeed, long salt, int x, int z) {
        return getLocalSeed(getLayerSeed(worldSeed, salt), x, z);
    }

    public void setSeed(int x, int z) {
        this.localSeed = BiomeLayer.getLocalSeed(this.layerSeed, x, z);
    }

    public int nextInt(int bound) {
        int i = (int) Math.floorMod(this.localSeed >> 24, bound);
        this.localSeed = SeedMixer.mixSeed(this.localSeed, this.layerSeed);
        return i;
    }

    public int choose(int a, int b) {
        return this.nextInt(2) == 0 ? a : b;
    }

    public int choose(int a, int b, int c, int d) {
        int i = this.nextInt(4);
        return i == 0 ? a : i == 1 ? b : i == 2 ? c : d;
    }

}
