package com.xuecheng.manage_cms;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        int[] arr={3,1,5,0};
      new Test().sort(arr);
        System.out.println(Arrays.toString(arr));

    }

    public int[] sort(int[] arr){
        int temp;
        for (int i = 0; i < arr.length-1; i++) {
            for (int j = 0; j < arr.length-1-i; j++) {
                if(arr[j+1]<arr[j]){

                temp=arr[j];
                arr[j]=arr[j+1];
                arr[j+1]=temp;
                }
            }

        }
        return arr;
    }

}
