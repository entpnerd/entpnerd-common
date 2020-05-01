package com.entpnerd.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

public class FinalReferenceTest {

  @Test
  public void singleThread() {
    FinalReference<String> finalReferenceUnderTest = new FinalReference<>();
    finalReferenceUnderTest.set("abc");
    finalReferenceUnderTest.set("def");
    assertEquals("abc", finalReferenceUnderTest.get());
  }

  @Test
  public void overwriteWithEqualButNotSameFails() {
    Integer ONE = new Integer(1);
    Integer ONE_DUP = new Integer(1);
    FinalReference<Integer> finalReferenceUnderTest = new FinalReference<>();
    finalReferenceUnderTest.set(ONE);
    finalReferenceUnderTest.set(ONE_DUP);
    assertSame(ONE, finalReferenceUnderTest.get());
  }

  @Test
  public void overwriteWithNullFails() {
    FinalReference<String> finalReferenceUnderTest = new FinalReference<>();
    finalReferenceUnderTest.set("abc");
    finalReferenceUnderTest.set(null);
    assertEquals("abc", finalReferenceUnderTest.get());
  }

  @Test
  public void multipleThreadsModifyingFail() throws InterruptedException {
    int numThreads = 10;
    FinalReference<Integer> finalReferenceUnderTest = new FinalReference<>();
    finalReferenceUnderTest.set(-1);

    List<Callable<Void>> raceThreads = new ArrayList<>(numThreads);
    int[] results = new int[numThreads];
    for (int i = 0; i < numThreads; ++i) {
      final int immutableIndex = i;
      Callable<Void> threadCode = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          finalReferenceUnderTest.set(immutableIndex + 1);
          int result = finalReferenceUnderTest.get();
          results[immutableIndex] = result;
          return null;
        }
      };
      raceThreads.add(i, threadCode);
    }

    ExecutorService threadRunner = Executors.newFixedThreadPool(numThreads);
    List<Future<Void>> raceResults = threadRunner.invokeAll(raceThreads);
    for(int i = 0; i< numThreads; ++i) {
      assertTrue(raceResults.get(i).isDone());
      assertFalse(raceResults.get(i).isCancelled());
      assertEquals(-1, results[i]);
    }
  }

  @Test
  public void onlyOneThreadWins() throws InterruptedException {
    int numThreads = 10;
    FinalReference<String> finalReferenceUnderTest = new FinalReference<>();

    List<Callable<Void>> raceThreads = new ArrayList<>(numThreads);
    String[] results = new String[numThreads];
    for (int i = 0; i < numThreads; ++i) {
      final int immutableIndex = i;
      Callable<Void> threadCode = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          long msTimestamp = System.currentTimeMillis();
          long nsTimestamp = System.nanoTime();
          String fullTimestamp = Long.toString(msTimestamp) + Long.toString(nsTimestamp);
          finalReferenceUnderTest.set(fullTimestamp);
          String strTimestamp = finalReferenceUnderTest.get();
          results[immutableIndex] = strTimestamp;
          return null;
        }
      };
      raceThreads.add(i, threadCode);
    }

    ExecutorService threadRunner = Executors.newFixedThreadPool(numThreads);
    threadRunner.invokeAll(raceThreads);
    for(int i = 1; i< numThreads; ++i) {
      assertEquals(results[0], results[i]);
    }
  }
}
