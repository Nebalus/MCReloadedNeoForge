package dev.nebalus.mc.neoforge.mcreloaded.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PerlinChunkGenerator extends ChunkGenerator {
    public static final Codec<PerlinChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
                    Codec.LONG.fieldOf("seed").forGetter(g -> g.seed)
            ).apply(instance, PerlinChunkGenerator::new)
    );

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
        return (MapCodec<? extends ChunkGenerator>) CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess) {

    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
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
                int height = 64 + (int)Math.round(value * amp);
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
        return 100;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        return null;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(int i, int i1, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int i, int i1, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return null;
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {

    }
}