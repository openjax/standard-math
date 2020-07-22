/* Copyright (c) 2020 Seva Safris, LibJ
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

#include "bigint.h"

const jlong LONG_MASK = 0xFFFFFFFFL;

JNIEXPORT void JNICALL Java_org_libj_math_BigIntMultiplication_nativeMulQuad(JNIEnv *env, jobject obj, jintArray xarr, jint xlen, jintArray yarr, jint ylen, jintArray zarr) {
  jint *x = (*env)->GetPrimitiveArrayCritical(env, xarr, NULL);
  jint *y = (*env)->GetPrimitiveArrayCritical(env, yarr, NULL);
  jint *z = (*env)->GetPrimitiveArrayCritical(env, zarr, NULL);

  int i, j, k;

  unsigned long carry = 0;
  jlong x0 = x[1] & LONG_MASK;

  for (j = 1; j <= ylen; ++j) {
    z[j] = (jint)(carry += x0 * (y[j] & LONG_MASK));
    carry >>= 32;
  }

  z[j] = (jint)carry;
  for (i = 2; i <= xlen; ++i) {
    x0 = x[i] & LONG_MASK;
    for (carry = 0, j = 1, k = i; j <= ylen; ++j, ++k) {
      z[k] = (jint)(carry += x0 * (y[j] & LONG_MASK) + (z[k] & LONG_MASK));
      carry >>= 32;
    }

    z[k] = (jint)carry;
  }

  (*env)->ReleasePrimitiveArrayCritical(env, xarr, x, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, yarr, y, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, zarr, z, 0);
}

JNIEXPORT void JNICALL Java_org_libj_math_BigIntMultiplication_nativeMulQuadInline(JNIEnv *env, jobject obj, jintArray xarr, jint xlen, jintArray yarr, jint ylen, jint zlen) {
  jint *x = (*env)->GetPrimitiveArrayCritical(env, xarr, NULL);
  jint *y = (*env)->GetPrimitiveArrayCritical(env, yarr, NULL);

  int i, j, k, l;

  unsigned long carry = 0;
  jlong x0 = x[1] & LONG_MASK;
  zlen += 1;

  for (j = 1, k = zlen; j <= ylen; ++j, ++k) {
    y[j] = (jint)(carry += x0 * ((y[k] = y[j]) & LONG_MASK));
    carry >>= 32;
  }

  y[k] = y[j];
  y[j] = (jint)carry;
  for (i = 2; i <= xlen; ++i) {
    x0 = x[i] & LONG_MASK;
    for (carry = 0, j = 1, k = i, l = zlen; j <= ylen; ++j, ++k, ++l) {
      y[k] = (jint)(carry += x0 * (y[l] & LONG_MASK) + (y[k] & LONG_MASK));
      carry >>= 32;
    }

    y[k] = (jint)carry;
  }

  (*env)->ReleasePrimitiveArrayCritical(env, xarr, x, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, yarr, y, 0);
}

JNIEXPORT void JNICALL Java_org_libj_math_BigIntMultiplication_nativeKaratsuba(JNIEnv *env, jobject obj, jintArray xarr, jint xoff, jintArray yarr, jint yoff, jintArray zarr, jint zoff, jint zlen, jint zlength, jint off, jint len, jint parallel) {
  jint *x = (*env)->GetPrimitiveArrayCritical(env, xarr, NULL);
  jint *y = (*env)->GetPrimitiveArrayCritical(env, yarr, NULL);
  jint *z = (*env)->GetPrimitiveArrayCritical(env, zarr, NULL);

  karatsuba(x, xoff, y, yoff, z, zoff, zlen, zlength, off, len, parallel);

  (*env)->ReleasePrimitiveArrayCritical(env, xarr, x, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, yarr, y, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, zarr, z, 0);
}

typedef struct KaratsubaArgs {
  jint *x, xoff, *y, yoff, *z, zoff, zlen, zlength, off, len, parallel;
} KaratsubaArgs;

void* karatsubaThread(void *args) {
  KaratsubaArgs *ka = (KaratsubaArgs *)args;
  jint *x = ka->x, xoff = ka->xoff, *y = ka->y, yoff = ka->yoff, *z = ka->z, zoff = ka->zoff, zlen = ka->zlen, zlength = ka->zlength, off = ka->off, len = ka->len, parallel = ka->parallel;
  free(args);
  karatsuba(x, xoff, y, yoff, z, zoff, zlen, zlength, off, len, parallel);
  return NULL;
}

void karatsuba(jint *x, jint xoff, jint *y, jint yoff, jint *z, jint zoff, jint zlen, jint zlength, jint off, jint len, jint parallel) {
  jint i, j, k, l, m;
  jlong x0;

  unsigned long carry = 0;
  const jint xoffoff = xoff + off, yoffoff = yoff + off;

  if (len <= 32) {
    const jint yoffoffl = yoffoff + len, zoffl = zoff + len, xoffoffl = xoffoff + len;

    jlong x0 = x[xoffoff] & LONG_MASK;
    for (k = yoffoff, j = zoff; j < zoffl; ++j, ++k) {
      z[j] = (jint)(carry += x0 * (y[k] & LONG_MASK));
      carry >>= 32;
    }

    z[j] = (jint)carry;
    for (i = xoffoff + 1, l = zoffl + 1, m = zoff + 1; i < xoffoffl; ++i, ++l, ++m) {
      carry = 0;
      x0 = x[i] & LONG_MASK;
      for (j = yoffoff, k = m; j < yoffoffl; ++j, ++k) {
        z[k] = (jint)(carry += x0 * (y[j] & LONG_MASK) + (z[k] & LONG_MASK));
        carry >>= 32;
      }

      z[l] = (jint)carry;
    }
  }
  else {
    const jint b = len >> 1, b2 = b * 2, ll = len * 2, l_b = len - b, l_b2 = l_b * 2;
    jint tmpoff, x2offl_b2, y2offl_b2;
    jint *tmp;
    bool allocated;

    j = ll + l_b2 + 2; // length needed for (x2) computation
    k = j + l_b2;      // length needed for (y2) computation
    if (parallel == 0 && zlength >= (i = zoff + zlen) + k + 1) {
      tmpoff = i;
      x2offl_b2 = j + i;
      y2offl_b2 = k + i;
      tmp = z;
      allocated = false;
    }
    else {
      tmpoff = 0;
      x2offl_b2 = j;
      y2offl_b2 = k;
      zlength = y2offl_b2 + 2;
      tmp = calloc(zlength, sizeof(jint));
      allocated = true;
    }

    const jint x2offl_b2b = x2offl_b2 + b, y2offl_b = x2offl_b2 + l_b, y2offl_b1 = y2offl_b + 1, y2offl_b1b = y2offl_b1 + b;

    for (i = x2offl_b2, j = xoffoff, k = xoffoff + b; i < x2offl_b2b; ++i, ++j, ++k) {
      tmp[i] = (jint)(carry += (x[j] & LONG_MASK) + (x[k] & LONG_MASK));
      carry >>= 32;
    }

    if ((len & 1) != 0)
      tmp[x2offl_b2b] = x[xoffoff + b2];

    if (carry != 0 && ++tmp[x2offl_b2b] == 0)
      ++tmp[x2offl_b2b + 1];

    carry = 0;
    for (i = y2offl_b1, j = yoffoff, k = yoffoff + b; i < y2offl_b1b; ++i, ++j, ++k) {
      tmp[i] = (jint)(carry += (y[j] & LONG_MASK) + (y[k] & LONG_MASK));
      carry >>= 32;
    }

    if ((len & 1) != 0)
      tmp[y2offl_b1b] = y[yoffoff + b2];

    if (carry != 0 && ++tmp[y2offl_b1b] == 0)
      ++tmp[y2offl_b1b + 1];

    const jint tmpoffl_b2 = tmpoff + l_b2;
    const jint tmplen = tmpoffl_b2 + l_b2 + 3;
    const jint r = l_b + (tmp[y2offl_b] != 0 || tmp[y2offl_b2] != 0 ? 1 : 0);
    const jint tmpoffrr = tmpoff + r * 2, tmpoffbb = tmpoff + b2, tmpoffrrbb = tmpoffrr + b2;
    if (parallel == 0) {
      karatsuba(tmp, x2offl_b2, tmp, y2offl_b1, tmp, tmpoff, tmplen, zlength, 0, r, 0);
      karatsuba(x, xoff, y, yoff, tmp, tmpoffrr, tmplen, zlength, off, b, 0);
      karatsuba(x, xoff, y, yoff, tmp, tmpoffrrbb, tmplen, zlength, off + b, l_b, 0);
    }
    else {
      KaratsubaArgs *args1 = malloc(sizeof(KaratsubaArgs));
      *args1 = (KaratsubaArgs){tmp, x2offl_b2, tmp, y2offl_b1, tmp, tmpoff, tmplen, zlength, 0, r, parallel - 1};

      KaratsubaArgs *args2 = malloc(sizeof(KaratsubaArgs));
      *args2 = (KaratsubaArgs){x, xoff, y, yoff, tmp, tmpoffrr, tmplen, zlength, off, b, parallel - 1};

      KaratsubaArgs *args3 = malloc(sizeof(KaratsubaArgs));
      *args3 = (KaratsubaArgs){x, xoff, y, yoff, tmp, tmpoffrrbb, tmplen, zlength, off + b, l_b, parallel - 1};

      pthread_t t1;
      int s1 = pthread_create(&t1, NULL, karatsubaThread, args1);

      pthread_t t2;
      int s2 = pthread_create(&t2, NULL, karatsubaThread, args2);

      pthread_t t3;
      int s3 = pthread_create(&t3, NULL, karatsubaThread, args3);

      pthread_join(t1, NULL);
      pthread_join(t2, NULL);
      pthread_join(t3, NULL);
    }

    memcpy(z + zoff, tmp + tmpoffrr, ll * sizeof(jint));

    x0 = 0;
    for (i = tmpoff, j = zoff + b, k = tmpoffrrbb, l = tmpoffrr, m = tmpoffbb; i < m; ++i, ++j, ++k, ++l) {
      z[j] = (jint)(x0 += (z[j] & LONG_MASK) + (tmp[i] & LONG_MASK) - (tmp[k] & LONG_MASK) - (tmp[l] & LONG_MASK));
      x0 >>= 32;
    }

    for (; i < tmpoffl_b2; ++i, ++j, ++k) {
      z[j] = (jint)(x0 += (z[j] & LONG_MASK) + (tmp[i] & LONG_MASK) - (tmp[k] & LONG_MASK));
      x0 >>= 32;
    }

    for (m = tmpoffrr - 1; i < m; ++i, ++j) {
      z[j] = (jint)(x0 += (z[j] & LONG_MASK) + (tmp[i] & LONG_MASK));
      x0 >>= 32;
    }

    if (allocated)
      free(tmp);

    if (x0 != 0)
      while (++z[j++] == 0);
  }
}


const jint OFF = 1;


JNIEXPORT void JNICALL Java_org_libj_math_BigIntMultiplication_nativeSquareKaratsuba(JNIEnv *env, jobject obj, jintArray xarr, jint len, jintArray zarr, jint zlen, jint zlength, jint parallel, jboolean yCopy) {
  jint *x = (*env)->GetPrimitiveArrayCritical(env, xarr, NULL);
  jint *z = (*env)->GetPrimitiveArrayCritical(env, zarr, NULL);

  if (yCopy) {
    // Inline computation for (mag) requires a copy for (y), otherwise we're
    // reading and writing from the same array for (x) (y) and (z)
    jint *y = calloc(len + OFF, sizeof(jint));
    memcpy(y, x, (len + OFF) * sizeof(jint));
    karatsuba(x, OFF, y, OFF, z, OFF, zlen, zlength, 0, len, parallel);
    free(y);
  }
  else {
    karatsuba(x, OFF, x, OFF, z, OFF, zlen, zlength, 0, len, parallel);
  }

  (*env)->ReleasePrimitiveArrayCritical(env, xarr, x, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, zarr, z, 0);
}

JNIEXPORT void JNICALL Java_org_libj_math_BigIntMultiplication_nativeSquareToLen(JNIEnv *env, jobject obj, jintArray xarr, jint xoff, jint xlen, jintArray zarr, jint zoff, jint zlen) {
  jint *x = (*env)->GetPrimitiveArrayCritical(env, xarr, NULL);
  jint *z = (*env)->GetPrimitiveArrayCritical(env, zarr, NULL);

  squareToLen(x, xoff, xlen, z, zoff, zlen);

  (*env)->ReleasePrimitiveArrayCritical(env, xarr, x, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, zarr, z, 0);
}

/**
 * The algorithm used here is adapted from Colin Plumb's C library.
 */
void squareToLen(jint *x, jint xoff, jint xlen, jint *z, jint zoff, jint zlen) {
  jint i, j, k, off;
  long x0 = 0;

  xlen += xoff;
  zlen += zoff;

  // Store the squares, right shifted one bit (i.e., divided by 2)
  for (i = xlen - 1, j = zlen; i >= xoff; --i) {
    k = (jint)x0 << 31;
    x0 = x[i] & LONG_MASK;
    x0 *= x0;
    z[--j] = k | (jint)((unsigned long)x0 >> 33);
    z[--j] = (jint)(x0 >> 1);
  }

  // Add in off-diagonal sums
  for (i = xoff, j = xlen - xoff, off = zoff; i < xlen; --j, off += 2) {
    k = x[i];
    k = mulAdd(x, ++i, xlen, k, z, off + 1);
    addOne(z, off, zlen, j, k);
  }

  // Shift back up and set low bit
  primitiveLeftShift(z, zoff, zlen, 1);
  z[zoff] |= x[xoff] & 1;
}

// shifts a up to len left n bits assumes no leading zeros, 0<=n<32
void primitiveLeftShift(jint *a, jint start, jint end, jint n) {
  if (end <= start || n == 0)
    return;

  jint i, n2 = 32 - n;
  jint c = a[--end];
  while (end > start) {
    i = c << n;
    c = a[end - 1];
    a[end--] = i | ((unsigned int)c >> n2);
  }

  a[start] <<= n;
}

/**
 * Multiply an array by one word k and add to result, return the carry
 */
jint mulAdd(jint *x, jint from, jint to, jint mul, jint *z, jint zoff) {
  const jlong tLong = mul & LONG_MASK;
  unsigned long carry = 0;

  while (from < to) {
    carry += (x[from++] & LONG_MASK) * tLong + (z[zoff] & LONG_MASK);
    z[zoff++] = (jint)carry;
    carry >>= 32;
  }

  return (jint)carry;
}

/**
 * Add one word to the number a mlen words into a. Return the resulting carry.
 */
jint addOne(jint *x, jint xoff, jint xlen, jint mlen, jint carry) {
  xoff += mlen;
  const unsigned long t = (x[xoff] & LONG_MASK) + (carry & LONG_MASK);

  x[xoff] = (int)t;
  if (((t) >> 32) == 0)
    return 0;

  while (--mlen >= 0) {
    if (++xoff == xlen) // Carry out of number
      return 1;

    ++x[xoff];
    if (x[xoff] != 0)
      return 0;
  }

  return 1;
}