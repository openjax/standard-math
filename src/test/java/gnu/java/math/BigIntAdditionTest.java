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

public class BigIntAdditionTest extends BigIntTest {
  @Test
  public void testAddInt() {
    testRange("add(int)",
      i("BigInteger", this::scaledBigInteger, BigInteger::valueOf, (BigInteger a, BigInteger b) -> a.add(b), String::valueOf),
      i("BigInt", this::scaledBigInt, (BigInt a, int b) -> a.add(b), String::valueOf)
    );
  }

  @Test
  public void testAddLong() {
    testRange("add(long)",
      l("BigInteger", this::scaledBigInteger, BigInteger::valueOf, (BigInteger a, BigInteger b) -> a.add(b), String::valueOf),
      l("BigInt", this::scaledBigInt, (BigInt a, long b) -> a.add(b), String::valueOf)
    );
  }

  @Test
  public void testAddBig() {
    testRange("add(String)",
      s("BigInteger", this::scaledBigInteger, BigInteger::new, (BigInteger a, BigInteger b) -> a.add(b), String::valueOf),
      s("BigInt", this::scaledBigInt, BigInt::new, (BigInt a, BigInt b) -> a.add(b), String::valueOf)
    );
  }

  @Test
  public void testSubInt() {
    testRange("sub(int)",
      i("BigInteger", this::scaledBigInteger, BigInteger::valueOf, (BigInteger a, BigInteger b) -> a.subtract(b), String::valueOf),
      i("BigInt", this::scaledBigInt, (BigInt a, int b) -> a.sub(b), String::valueOf)
    );
  }

  @Test
  public void testSubLong() {
    testRange("sub(long)",
      l("BigInteger", this::scaledBigInteger, BigInteger::valueOf, (BigInteger a, BigInteger b) -> a.subtract(b), String::valueOf),
      l("BigInt", this::scaledBigInt, (BigInt a, long b) -> a.sub(b), String::valueOf)
    );
  }

  @Test
  public void testSubBig() {
    testRange("sub(T)",
      s("BigInteger", this::scaledBigInteger, BigInteger::new, (BigInteger a, BigInteger b) -> a.subtract(b), String::valueOf),
      s("BigInt", this::scaledBigInt, BigInt::new, (BigInt a, BigInt b) -> a.sub(b), String::valueOf)
    );
  }
}