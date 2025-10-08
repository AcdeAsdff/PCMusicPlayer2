package com.linearity.utils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ShuffleUtils {


    public static <T> List<T> getShuffledList(@Nonnull List<T> list){
        List<T> shuffled = new ArrayList<>(list);
        shuffleList(shuffled);
        return shuffled;
    }
    public static <T> void shuffleList(@Nonnull List<T> list){
        Random randomGenerator = ThreadLocalRandom.current();
        int n = list.size();
        // Loop over array.
        for (int i = 0; i < list.size(); i++) {
            // Get a random index of the array past the current index.
            // ... The argument is an exclusive bound.
            //     It will not go past the array send.
            int randomValue = i + randomGenerator.nextInt(n - i);
            // Swap the random element with the present element.
            T randomElement = list.get(randomValue);
            list.set(randomValue, list.get(i));
            list.set(i, randomElement);
//            array[randomValue] = array[i];
//            array[i] = randomElement;
        }
    }
}
