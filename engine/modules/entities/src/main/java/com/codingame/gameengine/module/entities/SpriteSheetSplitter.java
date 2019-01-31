package com.codingame.gameengine.module.entities;

import java.util.stream.IntStream;

import com.google.inject.Inject;

/**
 * Utility to load an image containg serveral subimages displayed in sequential rectangles.
 * <p>
 * Will extract each separate image and return generated names that can be used to create a <code>Sprite</code> or <code>SpriteAnimation</code>
 * </p>
 *
 */
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

    /**
     * The constructor expected to be called by Guice. Use <code>GraphicEntityModule.createSpriteSheetSplitter()</code> to instantiate
     * 
     * @param graphicEntityModule
     *            the singleton to be injected
     */
    @Inject
    public SpriteSheetSplitter(GraphicEntityModule graphicEntityModule) {
        this.graphicEntityModule = graphicEntityModule;
    }

    /**
     * Returns the prefix of all subimage names
     * @return the prefix of all subimage names
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the image from which subimages will be extracted
     * @return the image from which subimages will be extracted
     */
    public String getSourceImage() {
        return sourceImage;
    }

    /**
     * Returns the width of subimages
     * @return the width of subimages
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of subimages
     * @return the height of subimages
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns from which row of subimages to start extracting subimages
     * @return from which row of subimages to start extracting subimages
     */
    public int getOrigRow() {
        return origRow;
    }

    /**
     * Returns from which column of subimages to start extracting subimages
     * @return from which column of subimages to start extracting subimages
     */
    public int getOrigCol() {
        return origCol;
    }

    /**
     * Returns number of subimages to extract 
     * @return number of subimages to extract
     */
    public int getImageCount() {
        return imageCount;
    }

    /**
     * Returns number of subimages to extract per row. Extraction will continue at <code>origCol</code> on the next row.
     * @return number of subimages to extract per row. Extraction will continue at <code>origCol</code> on the next row.
     */
    public int getImagesPerRow() {
        return imagesPerRow;
    }

    /**
     * Sets the prefix of all subimage names.
     * @param name the prefix of all subimages
     * @return this <code>SpriteSheetSplitter</code>
     */
    public SpriteSheetSplitter setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the image from which to extract subimages
     * @param sourceImage the image from which to extract subimages
     * @return this <code>SpriteSheetSplitter</code>
     */
    public SpriteSheetSplitter setSourceImage(String sourceImage) {
        this.sourceImage = sourceImage;
        return this;
    }

    /**
     * Sets the width of the subimages to extract
     * @param width the width of the subimages
     * @return this <code>SpriteSheetSplitter</code>
     */
    public SpriteSheetSplitter setWidth(int width) {
        this.width = width;
        return this;
    }
    
    /**
     * Sets the height of the subimages to extract
     * @param height the height of the subimages
     * @return this <code>SpriteSheetSplitter</code>
     */
    public SpriteSheetSplitter setHeight(int height) {
        this.height = height;
        return this;
    }

    
    /**
     * Sets the row of subimages from which subimages start being extracting
     * @param origRow the row of subimages from which subimages start being extracting
     * @return this <code>SpriteSheetSplitter</code>
     */
    public SpriteSheetSplitter setOrigRow(int origRow) {
        this.origRow = origRow;
        return this;
    }

    /**
     * Sets the column of subimages from which subimages start being extracting
     * @param origCol the column of subimages from which subimages start being extracting
     * @return this <code>SpriteSheetSplitter</code>
     */
    public SpriteSheetSplitter setOrigCol(int origCol) {
        this.origCol = origCol;
        return this;
    }

    /**
     * Sets the number of subimages to be extracted
     * @param imageCount the number of subimages to be extracted
     * @return this <code>SpriteSheetSplitter</code>
     */
    public SpriteSheetSplitter setImageCount(int imageCount) {
        this.imageCount = imageCount;
        return this;
    }


    /**
     * Sets the number of subimages to extract per row before advancing to the <code>origCol</code>th column of the next row. 
     * @param imagesPerRow the number of subimages to extract per row before advancing to the <code>origCol</code>th column of the next row.
     * @return this <code>SpriteSheetSplitter</code>
     */
    public SpriteSheetSplitter setImagesPerRow(int imagesPerRow) {
        this.imagesPerRow = imagesPerRow;
        return this;
    }

    /**
     * Splits up a spritesheet (all fields are required except imagesPerRow). Returns an array of image names that can be used in Sprite or
     * SpriteAnimation.
     * 
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
            return new String[] { name };
        }
    }
}
