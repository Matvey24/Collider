package com.matvey.perelman.gdxcollider.scheduler.value;

public class Math {
    //returns n! / (n - m)!, top m multipliers of n!, fact(n, n) returns n!
    public static int fact(int n, int m){
        int val = 1;
        int nm = n - m;
        for(int i = n; i > nm; --i)
            val *= i;
        return val;
    }
    public static int CNK(int n, int k){
        return fact(n, k) / fact(k, k);
    }
    public static float qpow(float v, int pow){
        float result = 1;
        while(pow != 0){
            if(pow % 2 == 0){
                v *= v;
                pow /= 2;
            }else{
                result *= v;
                pow -= 1;
            }
        }
        return result;
    }
}
