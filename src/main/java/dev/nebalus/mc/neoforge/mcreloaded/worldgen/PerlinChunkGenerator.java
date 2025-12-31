package dev.nebalus.mc.neoforge.mcreloaded.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PerlinChunkGenerator extends ChunkGenerator {
    public static final MapCodec<PerlinChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
            Codec.LONG.fieldOf("seed").forGetter(g -> g.seed)).apply(instance, PerlinChunkGenerator::new));

    private final long seed;
    private final PerlinTest perlin;
    private final BiomeSource biomeSource;

    public PerlinChunkGenerator(BiomeSource biomeSource, long seed) {
        super(biomeSource);
        this.biomeSource = Objects.requireNonNull(biomeSource);
        this.seed = seed;
        this.perlin = new PerlinTest(seed);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager,
            StructureManager structureManager, ChunkAccess chunkAccess) {

    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState,
            ChunkAccess chunk) {
        ChunkPos cpos = chunk.getPos();
        int baseX = cpos.getMinBlockX();
        int baseZ = cpos.getMinBlockZ();

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = baseX + localX;
                int worldZ = baseZ + localZ;

                double freq = 0.01;
                double amp = 30.0;
                double value = this.perlin.noise(worldX * freq, 0.0, worldZ * freq);
                int height = 64 + (int) Math.round(value * amp);
                height = Math.max(-64, Math.min(319, height));

                for (int y = -64; y <= height; y++) {
                    net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(worldX, y, worldZ);
                    if (y == height) {
                        chunk.setBlockState(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 0);
                    } else if (y > height - 6) {
                        chunk.setBlockState(pos, Blocks.DIRT.defaultBlockState(), 0);
                    } else {
                        chunk.setBlockState(pos, Blocks.STONE.defaultBlockState(), 0);
                    }
                }
            }
        }
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {

    }

    @Override
    public int getGenDepth() {
        return 384; // Total world height (-64 to 320)
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
            StructureManager structureManager, ChunkAccess chunkAccess) {
        // We handle all terrain generation in buildSurface, so just return completed
        // future
        return CompletableFuture.completedFuture(chunkAccess);
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getMinY() {
        return -64; // Minecraft 1.21.8 world minimum height
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor,
            RandomState randomState) {
        double freq = 0.01;
        double amp = 30.0;
        double value = this.perlin.noise(x * freq, 0.0, z * freq);
        int height = 64 + (int) Math.round(value * amp);
        return Math.max(-64, Math.min(320, height));
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        int height = getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
        int minY = levelHeightAccessor.getMinY();
        int maxY = levelHeightAccessor.getMaxY();

        net.minecraft.world.level.block.state.BlockState[] states = new net.minecraft.world.level.block.state.BlockState[maxY
                - minY];

        for (int y = minY; y < maxY; y++) {
            int index = y - minY;
            if (y > height) {
                states[index] = Blocks.AIR.defaultBlockState();
            } else if (y == height) {
                states[index] = Blocks.GRASS_BLOCK.defaultBlockState();
            } else if (y > height - 6) {
                states[index] = Blocks.DIRT.defaultBlockState();
            } else {
                states[index] = Blocks.STONE.defaultBlockState();
            }
        }

        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {

    }
}