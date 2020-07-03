/* Copyright (c) 2020 LibJ
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package gnu.java.math;

import java.math.BigInteger;

import org.junit.Test;

public class BigBitwiseTest extends BigIntTest {
  private static long randomBit(final String seed, final int bitCount) {
    return Math.abs(seed.hashCode() % (bitCount + 1));
  }

  @Test
  public void testBitCount() {
    testRange("bitCount()",
      s("BigInteger", this::scaledBigInteger, (BigInteger a, String b) -> a.bitCount(), Integer::valueOf),
      s("BigInt", this::scaledBigInt, (BigInt a, String b) -> a.bitCount(), Integer::valueOf)
    );
  }

  @Test
  public void testTestBit() {
    testRange("testBit(int)",
      s("BigInteger", this::scaledBigInteger, (a, b) -> randomBit(b, a.bitCount()), (BigInteger a, long b) -> a.testBit((int)b), Boolean::valueOf),
      s("BigInt", this::scaledBigInt, (a, b) -> randomBit(b, a.bitCount()), (BigInt a, long b) -> a.testBit((int)b), Boolean::valueOf)
    );
  }

  @Test
  public void testSetBit() {
    testRange("setBit(int)",
      s("BigInteger", this::scaledBigInteger, (a, b) -> randomBit(b, a.bitCount()), (BigInteger a, long b) -> a.setBit((int)b), String::valueOf),
      s("BigInt", this::scaledBigInt, (a, b) -> randomBit(b, a.bitCount()), (BigInt a, long b) -> a.setBit((int)b), String::valueOf)
    );
  }

  @Test
  public void testFlipBit() {
    testRange("flipBit(int)",
      s("BigInteger", this::scaledBigInteger, (a, b) -> randomBit(b, a.bitCount()), (BigInteger a, long b) -> a.flipBit((int)b), String::valueOf),
      s("BigInt", this::scaledBigInt, (a, b) -> randomBit(b, a.bitCount()), (BigInt a, long b) -> a.flipBit((int)b), String::valueOf)
    );
  }

  @Test
  public void testClearBit() {
    testRange("clearBit(int)",
      s("BigInteger", this::scaledBigInteger, (a, b) -> randomBit(b, a.bitCount()), (BigInteger a, long b) -> a.clearBit((int)b), String::valueOf),
      s("BigInt", this::scaledBigInt, (a, b) -> randomBit(b, a.bitCount()), (BigInt a, long b) -> a.clearBit((int)b), String::valueOf)
    );
  }

  @Test
  public void testShiftLeft() {
    testRange("shiftLeft(int)",
      s("BigInteger", this::scaledBigInteger, (a, b) -> randomBit(b, a.bitCount()), (BigInteger a, long b) -> a.shiftLeft((int)b), String::valueOf),
      s("BigInt", this::scaledBigInt, (a, b) -> randomBit(b, a.bitCount()), (BigInt a, long b) -> a.shiftLeft((int)b), String::valueOf)
    );
  }

  @Test
  public void testShiftRight() {
    testRange("shiftRight(int)",
      s("BigInteger", this::scaledBigInteger, (a, b) -> randomBit(b, a.bitCount()), (BigInteger a, long b) -> a.shiftRight((int)b), String::valueOf),
      s("BigInt", this::scaledBigInt, (a, b) -> randomBit(b, a.bitCount()), (BigInt a, long b) -> a.shiftRight((int)b), String::valueOf)
    );
  }
}