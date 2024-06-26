/**
 * Given a pair of integers return an array that contains both bounding numbers and every integer between them
 * @param lowerBound First number that should appear in result
 * @param upperBound Last number that should appear in result
 * @returns a sorted array of numbers [{lower}, {upper}]
 */
export const generateRangeOfNumbers = (
  lowerBound: number,
  upperBound: number
): number[] => {
  return new Array(upperBound - lowerBound + 1)
    .fill(1)
    .map((d, i) => i + lowerBound);
};
