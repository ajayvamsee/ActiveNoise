package com.ajayvamsee.activenoise;

import org.checkerframework.checker.units.qual.C;
import java.lang.Math;

/**
 * Compilation:  javac FFT.java
 * Execution:    java FFT n
 * Dependencies: Complex.java
 * <p>
 * compute the FFT and inverse FFT of a length  n complex sequence using
 * the radix 2 cooley -Tukey algorithm.
 * <p>
 * Bare bonus implementation thats runs in 0(n log n) time and O(n)
 * space. Our goal is to optimize the clarity of the code ,rather than performance.
 * <p>
 * This Implementation uses the primitive root of unity w = e^(-2 pi i / n).
 * Some resources use w = e^(2 pi i / n).
 * <p>
 * Reference: https://www.cs.princeton.edu/~wayne/kleinberg-tardos/pdf/05DivideAndConquerII.pdf
 * <p>
 * Limitations
 * -----------
 * -  assumes n is a power of 2
 * <p>
 * -  not the most memory efficient algorithm (because it uses
 * an object type for representing complex numbers and because
 * it re-allocates memory for the subarray, instead of doing
 * in-place or reusing a single temporary array)
 * <p>
 * For an in-place radix 2 Cooley-Tukey FFT, see
 * https://introcs.cs.princeton.edu/java/97data/InplaceFFT.java.html
 */

public class FFT {

    // compute the FFT of x[] ,assuming its length n is a power of 2

    public static Complex[] fft(Complex[] x) {
        int n = x.length;

        //base case
        if (n == 1) return new Complex[]{x[0]};

        // radix 2 Cooley-Tukey FFT
        if (n % 2 != 0) {
            throw new IllegalArgumentException("n is not a power of 2");
        }

        //compute FFT of even terms
        Complex[] even = new Complex[n / 2];
        for (int k = 0; k < n / 2; k++) {
            even[k] = x[2 * k];
        }
        Complex[] evenFFT = fft(even);

        //compute FFT for odd terms
        Complex[] odd = even; // reuse the array (to avoid n lon n space)
        for (int k = 0; k < n / 2; k++) {
            odd[k] = x[2 * k + 1];
        }
        Complex[] oddFFT = fft(odd);

        // combine
        Complex[] y = new Complex[n];
        for (int k = 0; k < n / 2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk=new Complex(Math.cos(kth),Math.sin(kth));
            y[k] =evenFFT[k].plus(wk.times(oddFFT[k]));
            y[k=n/2]=evenFFT[k].minus(wk.times(oddFFT[k]));
        }
        return y;
    }

    // compute the inverse FFT of x[], assuming its length n is a power of 2
    public static Complex[] ifft(Complex[] x) {
        int n = x.length;
        Complex[] y = new Complex[n];

        // take conjugate
        for (int i = 0; i < n; i++) {
            y[i] = x[i].conjugate();
        }

        // compute forward FFT
        y = fft(y);

        // take conjugate again
        for (int i = 0; i < n; i++) {
            y[i] = y[i].conjugate();
        }

        // divide by n
        for (int i = 0; i < n; i++) {
            y[i] = y[i].scale(1.0 / n);
        }

        return y;

    }

    // compute the inverse FFT of x[], assuming its length n is a power of 2
    public static Complex[] ifft(Complex[] x) {
        int n = x.length;
        Complex[] y = new Complex[n];

        // take conjugate
        for (int i = 0; i < n; i++) {
            y[i] = x[i].conjugate();
        }

        // compute forward FFT
        y = fft(y);

        // take conjugate again
        for (int i = 0; i < n; i++) {
            y[i] = y[i].conjugate();
        }

        // divide by n
        for (int i = 0; i < n; i++) {
            y[i] = y[i].scale(1.0 / n);
        }

        return y;

    }

    // compute the circular convolution of x and y
    public static Complex[] cconvolve(Complex[] x, Complex[] y) {

        // should probably pad x and y with 0s so that they have same length
        // and are powers of 2
        if (x.length != y.length) {
            throw new IllegalArgumentException("Dimensions don't agree");
        }

        int n = x.length;

        // compute FFT of each sequence
        Complex[] a = fft(x);
        Complex[] b = fft(y);

        // point-wise multiply
        Complex[] c = new Complex[n];
        for (int i = 0; i < n; i++) {
            c[i] = a[i].times(b[i]);
        }

        // compute inverse FFT
        return ifft(c);
    }


    // compute the linear convolution of x and y
    public static Complex[] convolve(Complex[] x, Complex[] y) {
        Complex ZERO = new Complex(0, 0);

        Complex[] a = new Complex[2*x.length];
        for (int i = 0;        i <   x.length; i++) a[i] = x[i];
        for (int i = x.length; i < 2*x.length; i++) a[i] = ZERO;

        Complex[] b = new Complex[2*y.length];
        for (int i = 0;        i <   y.length; i++) b[i] = y[i];
        for (int i = y.length; i < 2*y.length; i++) b[i] = ZERO;

        return cconvolve(a, b);
    }

    // compute the DFT of x[] via brute force (n^2 time)
    public static Complex[] dft(Complex[] x) {
        int n = x.length;
        Complex ZERO = new Complex(0, 0);
        Complex[] y = new Complex[n];
        for (int k = 0; k < n; k++) {
            y[k] = ZERO;
            for (int j = 0; j < n; j++) {
                int power = (k * j) % n;
                double kth = -2 * power *  Math.PI / n;
                Complex wkj = new Complex(Math.cos(kth), Math.sin(kth));
                y[k] = y[k].plus(x[j].times(wkj));
            }
        }
        return y;
    }


}
