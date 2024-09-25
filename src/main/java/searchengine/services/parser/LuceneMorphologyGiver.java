package searchengine.services.parser;

import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.services.GlobalVariables;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class LuceneMorphologyGiver {

    private static final ConcurrentLinkedQueue<RussianLuceneMorphology> lucenePool = new ConcurrentLinkedQueue<>();

    public static void initPool() {
        int countOfProcessors = GlobalVariables.COUNT_OF_PROCESSORS;
        ExecutorService threadPool = Executors.newFixedThreadPool(countOfProcessors);

        for (int i = 0; i < countOfProcessors; i++) {
            threadPool.submit(() -> {
                try {
                    lucenePool.add(new RussianLuceneMorphology());
                } catch (IOException e) {
                    System.err.println("Ошибка при создании RussianLuceneMorphology: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            });
        }
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, TimeUnit.MINUTES)) {
                System.err.println("Некоторые потоки не завершились за 5 минут");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ожидание завершения потоков было прервано", e);
        }

    }
    public static RussianLuceneMorphology get(){
        if(lucenePool.isEmpty()){
            initPool();
        }
        return lucenePool.poll();
    }

    public static void returnLucene(RussianLuceneMorphology luceneMorphology){
        lucenePool.add(luceneMorphology);
    }

    public static void closePool(){
        lucenePool.clear();
    }

}
