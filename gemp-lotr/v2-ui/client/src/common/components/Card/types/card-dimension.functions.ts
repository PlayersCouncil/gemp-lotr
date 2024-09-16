import { Dimensions } from '../../../types/size-and-position';

const WIDTH_TO_HEIGHT = 0.716;
const DEFAULT_WIDTH = 67;
const DEFAULT_HEIGHT = 88;

// this is based off the images I randomly checked.... could be wrong
const MAX_ALLOWED_WIDTH = 515; // it was 375

export const getCardHeightByWidth = (width: number): Dimensions => {
  let actualWidth = width;
  if (actualWidth > MAX_ALLOWED_WIDTH) {
    actualWidth = MAX_ALLOWED_WIDTH;
  }

  return {
    width: actualWidth,
    height: Math.round(actualWidth * WIDTH_TO_HEIGHT),
  };
};

export const getCardWidthByHeight = (height: number): Dimensions => {
  return {
    height,
    width: Math.round(height * WIDTH_TO_HEIGHT),
  };
};

export const getDefaultCardDimensions = (): Dimensions => ({
  width: DEFAULT_WIDTH,
  height: DEFAULT_HEIGHT,
});

// TODO: this could turn into a hook.... oh, i think i might understand react at some point

// TODO: WRITE UTs for this biatch. It's janky and gross
export const determineCardDimensions = (
  isHorizontal: boolean,
  height?: number,
  width?: number
): Dimensions => {
  let dimensions: Dimensions = { height: 0, width: 0 };
  if (height && width) {
    return { height, width };
  }
  if (height) {
    dimensions = getCardWidthByHeight(height);
  }
  if (width) {
    dimensions = getCardHeightByWidth(width);
  }
  if (!height && !width) {
    dimensions = getDefaultCardDimensions();
  }
  if (isHorizontal) {
    dimensions = {
      height: Math.min(dimensions.height, dimensions.width),
      width: Math.max(dimensions.height, dimensions.width),
    };
  } else {
    dimensions = {
      height: Math.max(dimensions.height, dimensions.width),
      width: Math.min(dimensions.height, dimensions.width),
    };
  }
  return dimensions;
};
