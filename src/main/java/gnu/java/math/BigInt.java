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

import java.util.concurrent.ExecutionException;

/**
 * <p>
 * A class for arbitrary-precision integer arithmetic purely written in Java.
 * </p>
 * <p>
 * This class does what {@link java.math.BigInteger} doesn't.<br />
 * It is <b>faster</b>, and it is <b>mutable</b>!<br />
 * It supports <b>ints</b> and <b>longs</b> as parameters!<br />
 * It has a way faster {@link #toString()} method!<br />
 * It utilizes a faster multiplication algorithm for those nasty big numbers!
 * </p>
 * <p>
 * Get it today! Because performance matters (and we like Java).
 * </p>
 *
 * @author Simon Klein
 * @version 0.7
 */
@SuppressWarnings("javadoc")
public class BigInt extends BigIntBinary implements Comparable<BigInt>, Cloneable {
  private static final long serialVersionUID = -4360183347203631370L;

  // FIXME: Rewrite this javadoc
  /**
   * The value array with the following encoding:<br>
   * <blockquote> <b>{@code val[1]}</b>: <ins>signum</ins>:
   * <code>{-1, 0, 1}</code><br>
   * &nbsp;&nbsp;&nbsp;&nbsp;-1 for negative, 0 for zero, or 1 for positive.<br>
   * <b>{@code val[0]}</b>: <ins>length</ins>:
   * {@code [1, Integer.MAX_VALUE - 2]}<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;The number of base 2^32 digits in the number.<br>
   * <b>{@code val[2,val[0]-1]}</b>: <ins>digits</ins>:
   * {@code [Integer.MIN_VALUE, Integer.MAX_VALUE]}<br>
   * &nbsp;&nbsp;&nbsp;&nbsp;The base 2^32 digits of the number in
   * <i>little-endian</i> order. </blockquote>
   */
  private int[] val;

  public int[] val() {
    return val;
  }

  /**
   * Creates a BigInt from the given parameters. The input-array will be used as
   * is and not be copied.
   *
   * @param val The magnitude of the number, the first position gives the least
   *          significant 32 bits.
   * @complexity O(1)
   */
  public BigInt(final int[] val) {
    assign(val);
  }

  /**
   * Creates a BigInt from the given parameters. The contents of the input-array
   * will be copied.
   *
   * @param sig The sign of the number.
   * @param mag The magnitude of the number, the first position gives the least
   *          significant 8 bits.
   * @param len The (first) number of entries of v that are considered part of
   *          the number.
   * @complexity O(n)
   */
  public BigInt(final int sig, final byte[] mag, int len) {
    this.val = assign(emptyVal, sig, mag, len);
  }

  public BigInt(final int sig, final byte[] mag) {
    this.val = assign(emptyVal, sig, mag, mag.length);
  }

  /**
   * Creates a BigInt from the given parameters. The input-value will be
   * interpreted as unsigned.
   *
   * @param sig The sign of the number.
   * @param mag The magnitude of the number.
   * @complexity O(1)
   */
  public BigInt(final int sig, final int mag) {
    this.val = assign(alloc(2), sig, mag);
  }

  /**
   * Creates a BigInt from the given parameters. The input-value will be
   * interpreted as unsigned.
   *
   * @param sig The sign of the number.
   * @param mag The magnitude of the number.
   * @complexity O(1)
   */
  public BigInt(final int sig, final long mag) {
    this.val = assign(alloc(3), sig, mag);
  }

  /**
   * Creates a BigInt from the given int. The input-value will be interpreted a
   * signed value.
   *
   * @param mag The value of the number.
   * @complexity O(1)
   */
  public BigInt(final int mag) {
    this.val = assign(alloc(2), mag);
  }

  /**
   * Creates a BigInt from the given long. The input-value will be interpreted a
   * signed value.
   *
   * @param mag The value of the number.
   * @complexity O(1)
   */
  public BigInt(final long mag) {
    this.val = assign(alloc(3), mag);
  }

  /**
   * Creates a BigInt from the given string.
   *
   * @param s A string representing the number in decimal.
   * @complexity O(n^2)
   */
  public BigInt(final String s) {
    this.val = assign(emptyVal, s);
  }

  /**
   * Creates a BigInt from the given char-array.
   *
   * @param s A char array representing the number in decimal.
   * @complexity O(n^2)
   */
  public BigInt(final char[] s) {
    this.val = assign(emptyVal, s);
  }

  /**
   * Assigns the given number to this BigInt object.
   *
   * @param The BigInt to copy/assign to this BigInt.
   * @complexity O(n)
   */
  // FIXME: Javadoc?!: This is not a clone!
  public BigInt assign(final BigInt b) {
    return assign(b.val);
  }

  /**
   * Assigns the given BigInt parameter to this number. The input magnitude
   * array will be used as is and not copied.
   *
   * @param sign The sign of the number.
   * @param val The magnitude of the number.
   * @param len The length of the magnitude array to be used.
   * @complexity O(1)
   */
  public BigInt assign(final int[] val) {
    this.val = val;
    _debugLenSig(val);
    return this;
  }

  /**
   * Assigns the given BigInt parameter to this number. Assumes no leading
   * zeroes of the input-array, i.e. that v[vlen-1]!=0, except for the case when
   * vlen==1.
   *
   * @param signum The sign of the number.
   * @param v The magnitude of the number.
   * @param len The length of the magnitude array to be used.
   * @complexity O(n)
   */
  public BigInt assign(final int signum, final byte[] v, final int len) {
    this.val = assign(val, signum, v, len);
    return this;
  }

  /**
   * Assigns the given number to this BigInt object.
   *
   * @param s A char array representing the number in decimal.
   * @complexity O(n^2)
   */
  public BigInt assign(final char[] s) {
    val = assign(val, s);
    return this;
  }

  /**
   * Assigns the given number to this BigInt object.
   *
   * @param s A string representing the number in decimal.
   * @complexity O(n^2)
   */
  public BigInt assign(final String s) {
    val = assign(val, s);
    return this;
  }

  /**
   * Assigns the given number to this BigInt object.
   *
   * @param sig The sign of the number.
   * @param mag The magnitude of the number (will be interpreted as unsigned).
   * @complexity O(1)
   */
  public BigInt assign(final int sig, final int mag) {
    val = assign(val, sig, mag);
    return this;
  }

  /**
   * Assigns the given number to this BigInt object.
   *
   * @param sig The sign of the number.
   * @param mag The magnitude of the number (will be interpreted as unsigned).
   * @complexity O(1)
   */
  public BigInt assign(final int sig, final long mag) {
    val = assign(val, sig, mag);
    return this;
  }

  /**
   * Assigns the given number to this BigInt object.
   *
   * @param mag The number to be assigned.
   * @complexity O(1)
   */
  public BigInt assign(final int mag) {
    this.val = assign(val, mag);
    return this;
  }

  /**
   * Assigns the given number to this BigInt object.
   *
   * @param mag The number to be assigned.
   * @complexity O(1)
   */
  public BigInt assign(final long mag) {
    this.val = assign(val, mag);
    return this;
  }

  /**
   * Sets to this {@link BigInt} the absolute value of its magnitude.
   *
   * <pre>
   * {@code v = | v |}
   * </pre>
   *
   * @return This {@link BigInt}.
   */
  public BigInt abs() {
    abs(val);
    return this;
  }

  /**
   * Returns the maximum of {@code this} and {@code v}.
   *
   * @param v Value with which the maximum is to be computed.
   * @return The {@link BigInt} whose value is the greater of {@code this} and
   *         {@code v}. If they are equal, {@code v} will be returned.
   */
  public BigInt max(final BigInt v) {
    return compareTo(v) > 0 ? this : v;
  }

  /**
   * Returns the minimum of {@code this} and {@code v}.
   *
   * @param v Value with which the minimum is to be computed.
   * @return The {@link BigInt} whose value is the greater of {@code this} and
   *         {@code v}. If they are equal, {@code v} will be returned.
   */
  public BigInt min(final BigInt v) {
    return compareTo(v) < 0 ? this : v;
  }

  public int signum() {
    return signum(val);
  }

  public boolean isZero() {
    return isZero(val);
  }

  /**
   * Sets this number to zero.
   *
   * @complexity O(1)
   */
  public BigInt setToZero() {
    setToZero(val);
    return this;
  }

  /**
   * Adds an unsigned {@code int} to this number.
   *
   * @param a The amount to add (unsigned).
   * @complexity O(n)
   * @amortized O(1)
   */
  public BigInt uadd(final int a) {
    return a == 0 ? this : isZero() ? assign(1, a) : uadd0(a);
  }

  private BigInt uadd0(final int a) {
    val = BigIntAddition.uadd(val, a);
    return this;
  }

  /**
   * Adds an unsigned {@code long} to this number.
   *
   * @param a The amount to add (unsigned).
   * @complexity O(n)
   * @amortized O(1)
   */
  public BigInt uadd(final long a) {
    if (a == 0)
      return this;

    if (isZero())
      return assign(1, a);

    final long ah = a >>> 32;
    return ah == 0 ? uadd0((int)a) : uadd0(a, ah);
  }

  private BigInt uadd0(final long a, final long ah) {
    int signum = 1, len = val[0]; if (len < 0) { len = -len; signum = -1; }
    final long al = a & LONG_INT_MASK;
    // FIXME: This will fail for 0 value
    final long val0l = val[1] & LONG_INT_MASK;
    final long val0h = len <= 1 ? 0 : val[2] & LONG_INT_MASK;
    val = BigIntAddition.uadd(val, val0l, val0h, al, ah, len, signum, true);
    return this;
  }

  /**
   * Subtracts an unsigned {@code int} from this number.
   *
   * @param s The amount to subtract (unsigned).
   * @complexity O(n)
   * @amortized O(1)
   */
  public BigInt usub(final int s) {
    return s == 0 ? this : isZero() ? assign(-1, s) : usub0(s);
  }

  private BigInt usub0(final int s) {
    val = BigIntAddition.usub(val, s);
    return this;
  }

  /**
   * Subtracts an unsigned {@code long} from this number.
   *
   * @param s The amount to subtract (unsigned).
   * @complexity O(n)
   * @amortized O(1)
   */
  public BigInt usub(final long s) {
    if (s == 0)
      return this;

    if (isZero())
      return assign(-1, s);

    final long sh = s >>> 32;
    return sh == 0 ? usub0((int)s) : usub0(s, sh);
  }

  private BigInt usub0(final long s, final long sh) {
    final long sl = s & LONG_INT_MASK;
    val = BigIntAddition.uadd(val, sl, sh, false);
    return this;
  }

  /**
   * Adds an {@code int} to this number.
   *
   * @param a The amount to add.
   * @complexity O(n)
   */
  // Has not amortized O(1) due to the risk of
  // alternating +1 -1 on continuous sequence of
  // 1-set bits.
  public BigInt add(final int a) {
    if (isZero())
      return a == 0 ? this : assign(a);

    if (a > 0)
      uadd0(a);
    else if (a < 0)
      usub0(-a);

    return this;
  }

  /**
   * Adds a {@code long} to this number.
   *
   * @param a The amount to add.
   * @complexity O(n)
   */
  public BigInt add(long a) {
    if (isZero())
      return a == 0 ? this : assign(a);

    if (a > 0) {
      final long ah = a >>> 32;
      return ah == 0 ? uadd0((int)a) : uadd0(a, ah);
    }

    if (a < 0) {
      // FIXME: What about Long.MIN_VALUE?!
      a = -a;
      final long ah = a >>> 32;
      return ah == 0 ? usub0((int)a) : usub0(a, ah);
    }

    return this;
  }

  /**
   * Adds a {@link BigInt} to this number.
   *
   * @param a The number to add.
   * @complexity O(n)
   */
  public BigInt add(final BigInt a) {
    if (isZero())
      return a.isZero() ? this : assign(a);

    if (a.isZero())
      return this;

    val = BigIntAddition.add(val, a.val, true);
    return this;
  }

  /**
   * Subtracts an {@code int} from this number.
   *
   * @param s The amount to subtract.
   * @complexity O(n)
   */
  public BigInt sub(final int s) {
    if (isZero())
      return s == 0 ? this : s == Integer.MIN_VALUE ? assign(1, s) : assign(-s);

    if (s > 0)
      usub0(s);
    else if (s == Integer.MIN_VALUE)
      uadd0(-(long)s, 0);
    else if (s < 0)
      uadd0(-s);

    return this;
  }

  /**
   * Subtracts a {@code long} from this number.
   *
   * @param s The amount to subtract.
   * @complexity O(n)
   */
  public BigInt sub(long s) {
    if (isZero())
      return s == 0 ? this : s == Long.MIN_VALUE ? assign(1, s) : assign(-s);

    if (s > 0) {
      final long ah = s >>> 32;
      return ah == 0 ? usub0((int)s) : usub0(s, ah);
    }

    if (s < 0) {
      final boolean isMinInt = s == Integer.MIN_VALUE;
      s = -s;
      final long ah = s >>> 32;
      return ah == 0 && !isMinInt ? uadd0((int)s) : uadd0(s, ah);
    }

    return this;
  }

  /**
   * Subtracts a {@link BigInt} from this number.
   *
   * @param s The number to subtract.
   * @complexity O(n)
   */
  public BigInt sub(final BigInt s) {
    if (isZero()) {
      if (s.isZero())
        return this;

      this.val = copy(this.val, s.val);
      val[0] = -val[0];
      return this;
    }

    if (s.isZero())
      return this;

    val = BigIntAddition.add(val, s.val, false);
    return this;
  }

  /**
   * Multiplies this number with an unsigned {@code int}.
   *
   * @param m The amount by which to multiply (unsigned).
   * @complexity O(n)
   */
  public BigInt umul(final int m) {
    return isZero() ? this : m == 0 ? setToZero() : umul0(m);
  }

  private BigInt umul0(final int m) {
    val = umul0(val, m);
    return this;
  }

  private static int[] umul0(int[] val, final int m) {
    int signum = 1, len = val[0]; if (len < 0) { len = -len; signum = -1; }
    if (len + 1 >= val.length)
      val = realloc(val);

    val[0] = BigIntMultiplication.umul(val, 1, len + 1, m) - 1;
    if (signum < 0)
      val[0] = -val[0];

    _debugLenSig(val);
    return val;
  }

  /**
   * Multiplies this number with an unsigned {@code long}.
   *
   * @param m The amount by which to multiply (unsigned).
   * @complexity O(n)
   */
  public BigInt umul(final long m) {
    return isZero() ? this : m == 0 ? setToZero() : umul0(m);
  }

  private BigInt umul0(final long m) {
    final long mh = m >>> 32;
    return mh == 0 ? umul0((int)m) : umul0(m & LONG_INT_MASK, mh);
  }

  private BigInt umul0(final long ml, final long mh) {
    val = umul0(val, ml, mh);
    return this;
  }

  public static int[] umul0(int[] val, final long ml, final long mh) {
    boolean signum = true; int len = val[0]; if (len < 0) { len = -len; signum = false; }
    ++len;
    if (len + 1 >= val.length)
      val = realloc(val, 2 * len);

    val[0] = BigIntMultiplication.umul(val, 1, len, ml, mh) - 1;
    if (!signum)
      val[0] = -val[0];

    _debugLenSig(val);
    return val;
  }

  /**
   * Multiplies this number by the given {@link BigInt} using the Karatsuba
   * algorithm.
   * <p>
   * NOTE: Size of mag1 and mag2 must be the same!
   *
   * @param m The amount to multiply.
   * @param p Whether to attempt to use the parallel algorithm.
   */
  BigInt karatsuba(final BigInt m, final boolean p) throws ExecutionException, InterruptedException {
    val = karatsuba(val, m.val, p);
    return this;
  }

  /**
   * Divides this number with an unsigned {@code int} and returns the unsigned
   * remainder.
   *
   * @param div The amount by which to divide (unsigned).
   * @return The absolute value of the remainder as an unsigned int.
   * @complexity O(n)
   */
  public long divRem(final int sig, final int div) {
    return BigIntDivision.divRem(val, sig, div);
  }

  public long divRem(final int div) {
    return BigIntDivision.divRem(val, div);
  }

  /**
   * Divides this number with an unsigned {@code long} and returns the
   * remainder.
   *
   * @param div The amount by which to divide (unsigned).
   * @return The absolute value of the remainder as an unsigned long.
   * @complexity O(n)
   */
  public long divRem(final int sig, final long div) {
    return BigIntDivision.divRem(val, sig, div);
  }

  public long divRem(final long div) {
    return BigIntDivision.divRem(val, div);
  }

  /**
   * Multiplies this number by an {@code int}.
   *
   * @param m The amount by which to multiply.
   * @complexity O(n)
   */
  public BigInt mul(final int m) {
    if (m == 0)
      return setToZero();

    if (isZero())
      return this;

    if (m > 0)
      return umul0(m);

    val[0] = -val[0];
    if (m != Integer.MIN_VALUE)
      return umul0(-m);

    final long l = -(long)m;
    return umul0(l & LONG_INT_MASK, l >>> 32);
  }

  /**
   * Multiplies this number by a {@code long}.
   *
   * @param m The amount by which to multiply.
   * @complexity O(n)
   */
  public BigInt mul(long m) {
    val = mul(val, m);
    return this;
  }

  public static int[] mul(final int[] val, long m) {
    if (m == 0)
      return setToZero(val);

    if (isZero(val))
      return val;

    final long mh = m >>> 32;
    if (mh == 0)
      return umul0(val, (int)m);

    final long ml = m & LONG_INT_MASK;
    if (m > 0)
      return umul0(val, ml, mh);

    val[0] = -val[0];
    if (m == Long.MIN_VALUE)
      return umul0(val, ml, mh); // FIXME: Is this actually happening?

    m = -m;
    return umul0(val, m & LONG_INT_MASK, m >>> 32);
  }

  /**
   * Multiplies this number by a {@link BigInt}.
   * <p>
   * Chooses the appropriate algorithm with regards to the size of the numbers.
   *
   * @param m The amount by which to multiply.
   * @complexity O(n^2) - O(n log n)
   */
  public BigInt mul(final BigInt m) {
    final int[] val1 = val;
    final int[] val2 = m.val;
    if (isZero(val2))
      return setToZero();

    if (isZero(val1))
      return this;

    val = BigIntMultiplication.mul(val, m.val);
    return this;
  }

  /**
   * Divides this number by an unsigned {@code int}.
   * <p>
   * Division by zero is undefined.
   *
   * @param div The number by which to divide.
   * @complexity O(n^2)
   */
  public BigInt div(final int sig, final int div) {
    BigIntDivision.divRem(val, sig, div);
    return this;
  }

  /**
   * Divides this number by an unsigned {@code long}.
   * <p>
   * Division by zero is undefined.
   *
   * @param div The number by which to divide.
   * @complexity O(n^2)
   */
  public BigInt div(final int sig, final long div) {
    BigIntDivision.divRem(val, sig, div);
    return this;
  }

  /**
   * Divides this number by an {@code int}.
   * <p>
   * Division by zero is undefined.
   *
   * @param div The number by which to divide.
   * @complexity O(n^2)
   */
  public BigInt div(final int div) {
    BigIntDivision.divRem(val, div);
    return this;
  }

  /**
   * Divides this number by a {@code long}.
   * <p>
   * Division by zero is undefined.
   *
   * @param div The number by which to divide.
   * @complexity O(n^2)
   */
  public BigInt div(final long div) {
    BigIntDivision.divRem(val, div);
    return this;
  }

  /**
   * Divides this number by a {@link BigInt}.
   * <p>
   * Division by zero is undefined.
   *
   * @param div The number by which to divide.
   * @complexity O(n^2)
   */
  public BigInt div(final BigInt div) {
    val = BigIntDivision.div(val, div.val);
    return this;
  }

  /**
   * Sets this number to the remainder r satisfying q*div + r = this, where q =
   * floor(this/div).
   *
   * @param div The number to use in the division causing the remainder.
   * @complexity O(n^2)
   */
  public BigInt rem(final int div) {
    BigIntDivision.rem(val, div);
    return this;
  }

  /**
   * Sets this number to the remainder r satisfying q*div + r = this, where q =
   * floor(this/div).
   *
   * @param div The number to use in the division causing the remainder.
   * @complexity O(n^2)
   */
  public BigInt rem(final long div) {
    BigIntDivision.rem(val, div);
    return this;
  }

  /**
   * Applies the modulus of this number by an unsigned {@code int} (i.e.
   * {@code this = (this % div)}).
   *
   * @param div The amount by which to modulo (unsigned).
   * @complexity O(n)
   */
  public BigInt rem(final int sig, final int div) {
    BigIntDivision.rem(val, sig, div);
    return this;
  }

  /**
   * Applies the modulus of this number by an unsigned {@code long} (i.e.
   * {@code this = (this % mod)}).
   *
   * @param div The amount by which to modulo (unsigned).
   * @complexity O(n)
   */
  public BigInt rem(final int sig, final long div) {
    BigIntDivision.rem(val, sig, div);
    return this;
  }

  /**
   * Sets this number to the remainder r satisfying q*div + r = this, where q =
   * floor(this/div).
   *
   * @param div The number to use in the division causing the remainder.
   * @complexity O(n^2)
   */
  public BigInt rem(final BigInt div) {
    val = BigIntDivision.rem(val, div.val);
    return this;
  }

  /**
   * Divides this number by a {@link BigInt} and returns the remainder.
   * <p>
   * Division by zero is undefined.
   *
   * @param div The amount by which to divide.
   * @return The remainder.
   * @complexity O(n^2)
   */
  // FIXME: I think we can just align div to return the remainder instead of this.
  public BigInt divRem(final BigInt div) {
    return new BigInt(divRem(val, div.val));
  }

  /**
   * Sets this number to {@code (this mod m}). This method differs from
   * {@link BigInt#rem(BigInt)} in that it always computes <i>non-negative</i>
   * result.
   *
   * @param div The number to use in the division causing the remainder.
   * @see #rem
   */
  public BigInt mod(final BigInt div) {
    val = BigIntDivision.mod(val, div.val);
    return this;
  }

  // Negative numbers are imagined in their two's complement form with infinite
  // sign extension.
  // This has no effect on bit shifts, but makes implementation of other bit
  // operations a bit
  // tricky if one wants them to be as efficient as possible.

  /**
   * Returns the number of bits in the two's complement representation of this
   * {@link BigInt} that differ from its sign bit. This method is useful when
   * implementing bit-vector style sets atop {@link BigInt}.
   *
   * @return The number of bits in the two's complement representation of this
   *         {@link BigInt} that differ from its sign bit.
   */
  public int bitCount() {
    return bitCount(val);
  }

  /**
   * Shifts this number left by the given amount.
   *
   * @param s The amount to shift.
   * @complexity O(n)
   */
  public BigInt shiftLeft(final int s) {
    val = BigIntBinary.shiftLeft(val, s);
    return this;
  }

  /**
   * Shifts this number right by the given amount.
   *
   * @param s The amount to shift.
   * @complexity O(n)
   */
  public BigInt shiftRight(final int s) {
    val = BigIntBinary.shiftRight(val, s);
    return this;
  }

  /**
   * Tests if the bit at the given index is set.
   *
   * @param i The index of the bit to test.
   * @return {@code true} if the bit at the given index is {@code 1}, and
   *         {@code false} if {@code 0}.
   * @complexity O(n)
   */
  public boolean testBit(final int i) {
    if (i < 0)
      throw new IllegalArgumentException("index (" + i + ") must be a positive integer");

    return BigIntBinary.testBit(val, i);
  }

  /**
   * Sets the given bit in the number.
   *
   * @param i The index of the bit to set.
   * @complexity O(n)
   */
  public BigInt setBit(final int i) {
    if (i < 0)
      throw new IllegalArgumentException("index (" + i + ") must be a positive integer");

    val = BigIntBinary.setBit(val, i);
    return this;
  }

  /**
   * Clears the given bit in the number.
   *
   * @param i The index of the bit to clear.
   * @complexity O(n)
   */
  public BigInt clearBit(final int i) {
    if (i < 0)
      throw new IllegalArgumentException("index (" + i + ") must be a positive integer");

    val = BigIntBinary.clearBit(val, i);
    return this;
  }

  /**
   * Flips the given bit in the number.
   *
   * @param i The index of the bit to flip.
   * @complexity O(n)
   */
  public BigInt flipBit(final int i) {
    if (i < 0)
      throw new IllegalArgumentException("index (" + i + ") must be a positive integer");

    val = BigIntBinary.flipBit(val, i);
    return this;
  }

  /**
   * Bitwise-ands this number with the given number, i.e. this &= mask.
   *
   * @param mask The mask with which to bitwise-"and".
   * @complexity O(n)
   */
  public BigInt and(final BigInt mask) {
    if (mask.isZero())
      return setToZero();

    val = BigIntBinary.and(val, mask.val);
    return this;
  }

  /**
   * Bitwise-ors this number with the given number, i.e. this |= mask.
   *
   * @param mask The mask with which to bitwise-"or".
   * @complexity O(n)
   */
  public BigInt or(final BigInt mask) {
    if (mask.isZero())
      return this;

    if (isZero())
      return assign(mask);

    val = BigIntBinary.or(val, mask.val);
    return this;
  }

  /**
   * Bitwise-xors this number with the given number, i.e. this ^= mask.
   *
   * @param mask The mask with which to bitwise-"xor".
   * @complexity O(n)
   */
  public BigInt xor(final BigInt mask) {
    if (mask.isZero())
      return this;

    if (isZero())
      return assign(mask);

    val = BigIntBinary.xor(val, mask.val);
    return this;
  }

  /**
   * Bitwise-and-nots this number with the given number, i.e. this &= ~mask.
   *
   * @param mask The mask with which to bitwise-"and-not".
   * @complexity O(n)
   */
  public BigInt andNot(final BigInt mask) {
    val = BigIntBinary.andNot(val, mask.val);
    return this;
  }

  /**
   * Inverts sign and all bits of this number, i.e. this = ~this. The identity
   * -this = ~this + 1 holds.
   *
   * @complexity O(n)
   */
  public BigInt not() {
    val = BigIntBinary.not(val);
    return this;
  }

  /**
   * {@inheritDoc} Returns this BigInt as a {@code byte}.
   *
   * @return {@code sign * (this & 0x7F)}
   */
  @Override
  public byte byteValue() {
    return byteValue(val);
  }

  /**
   * {@inheritDoc} Returns this BigInt as a {@code short}.
   *
   * @return {@code sign * (this & 0x7FFF)}
   */
  @Override
  public short shortValue() {
    return shortValue(val);
  }

  /**
   * {@inheritDoc} Returns this BigInt as an {@code int}.
   *
   * @return {@code sign * (this & 0x7FFFFFFF)}
   */
  @Override
  public int intValue() {
    return intValue(val);
  }

  /**
   * {@inheritDoc} Returns this BigInt as a {@code long}.
   *
   * @return {@code sign * (this & 0x7FFFFFFFFFFFFFFF)}
   */
  @Override
  public long longValue() {
    return longValue(val);
  }

  public long longValueUnsigned() {
    return longValueUnsigned(val);
  }

  /**
   * {@inheritDoc} Returns this BigInt as a {@code float}.
   *
   * @return the most significant 24 bits in the mantissa (the highest order bit
   *         obviously being implicit), the exponent value which will be
   *         consistent for {@code BigInt}s up to 128 bits (should it not fit
   *         it'll be calculated modulo 256), and the sign bit set if this
   *         number is negative.
   */
  @Override
  public float floatValue() {
    return floatValue(val);
  }

  /**
   * {@inheritDoc} Returns this BigInt as a {@code double}.
   *
   * @return the most significant 53 bits in the mantissa (the highest order bit
   *         obviously being implicit), the exponent value which will be
   *         consistent for {@code BigInt}s up to 1024 bits (should it not fit
   *         it'll be calculated modulo 2048), and the sign bit set if this
   *         number is negative.
   */
  @Override
  public double doubleValue() {
    return doubleValue(val);
  }

  /**
   * Compares the absolute value of this and the given number.
   *
   * @param o The number to be compared with.
   * @return -1 if the absolute value of this number is less, 0 if it's equal, 1
   *         if it's greater.
   * @complexity O(n)
   */
  public int compareToAbs(final BigInt o) {
    return compareToAbs(val, o.val);
  }

  /**
   * Compares the value of this and the given number.
   *
   * @param o The number to be compared with.
   * @return -1 if the value of this number is less, 0 if it's equal, 1 if it's
   *         greater.
   * @complexity O(n)
   */
  @Override
  public int compareTo(final BigInt o) {
    return compareTo(val, o.val);
  }

  /**
   * Tests equality of this number and the given one.
   *
   * @param n The number to be compared with.
   * @return true if the two numbers are equal, false otherwise.
   * @complexity O(n)
   */
  public boolean equals(final BigInt n) {
    return equals(val, n.val);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof BigInt && equals((BigInt)obj);
  }

  @Override
  public int hashCode() {
    return hashCode(val);
  }

  /**
   * Converts this number into a string of radix 10.
   *
   * @return The string representation of this number in decimal.
   * @complexity O(n^2)
   */
  @Override
  public String toString() {
    return toString(val);
  }

  /**
   * Creates a copy of this number.
   *
   * @return The BigInt copy.
   * @complexity O(n)
   */
  @Override
  public BigInt clone() {
    return new BigInt(val.clone());
  }
}