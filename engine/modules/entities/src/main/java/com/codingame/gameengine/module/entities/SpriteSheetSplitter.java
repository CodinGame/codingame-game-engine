package com.codingame.gameengine.module.entities;

import java.util.stream.IntStream;

import com.google.inject.Inject;

public class SpriteSheetSplitter {
    private String name;
    private String sourceImage;
    private Integer width;
    private Integer height;
    private Integer origRow;
    private Integer origCol;
    private Integer imageCount;
    private int imagesPerRow = 0; // 0 means no wrap

    private final GraphicEntityModule graphicEntityModule;

    @Inject
    public SpriteSheetSplitter(GraphicEntityModule graphicEntityModule) {
        this.graphicEntityModule = graphicEntityModule;
    }

    public String getName() {
        return name;
    }

    public String getSourceImage() {
        return sourceImage;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getOrigRow() {
        return origRow;
    }

    public int getOrigCol() {
        return origCol;
    }

    public int getImageCount() {
        return imageCount;
    }

    public int getImagesPerRow() {
        return imagesPerRow;
    }

    public SpriteSheetSplitter setName(String name) {
        this.name = name;
        return this;
    }

    public SpriteSheetSplitter setSourceImage(String sourceImage) {
        this.sourceImage = sourceImage;
        return this;
    }

    public SpriteSheetSplitter setWidth(int width) {
        this.width = width;
        return this;
    }

    public SpriteSheetSplitter setHeight(int height) {
        this.height = height;
        return this;
    }

    public SpriteSheetSplitter setOrigRow(int origRow) {
        this.origRow = origRow;
        return this;
    }

    public SpriteSheetSplitter setOrigCol(int origCol) {
        this.origCol = origCol;
        return this;
    }

    public SpriteSheetSplitter setImageCount(int imageCount) {
        this.imageCount = imageCount;
        return this;
    }

    public SpriteSheetSplitter setImagesPerRow(int imagesPerRow) {
        this.imagesPerRow = imagesPerRow;
        return this;
    }

    /**
     * Splits up a spritesheet (all fields are required except imagesPerRow). Returns an array of image names that can be used in Sprite or SpriteAnimation.
     * @return an array of image names.
     */
    public String[] split() {
        if (name == null) {
            throw new IllegalStateException("invalid name");
        }
        if (sourceImage == null) {
            throw new IllegalStateException("invalid sourceImage");
        }
        if (width == null || width <= 0) {
            throw new IllegalStateException("invalid width");
        }
        if (height == null || height <= 0) {
            throw new IllegalStateException("invalid height");
        }
        if (imageCount == null || imageCount <= 0) {
            throw new IllegalStateException("invalid imageCount");
        }
        if (origRow == null || origRow < 0) {
            throw new IllegalStateException("invalid origRow");
        }
        if (origCol == null || origCol < 0) {
            throw new IllegalStateException("invalid origCol");
        }

        graphicEntityModule.loadSpriteSheetSplitter(this);
        if (imageCount > 1) {
            return IntStream.range(0, imageCount).mapToObj(i -> name + i).toArray(String[]::new);
        } else {
            return new String[]{ name };
        }
    }
}
