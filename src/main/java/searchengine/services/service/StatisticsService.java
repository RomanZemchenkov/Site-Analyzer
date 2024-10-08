package searchengine.services.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dao.model.Site;
import searchengine.dao.model.Statistic;
import searchengine.dao.repository.site.SiteRepository;
import searchengine.dao.repository.statistic.StatisticRepository;
import searchengine.services.dto.statistics.DetailedStatisticsItem;
import searchengine.services.dto.statistics.StatisticsData;
import searchengine.services.dto.statistics.StatisticsResponse;
import searchengine.services.dto.statistics.TotalStatistics;
import searchengine.services.mapper.StatisticMapper;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static searchengine.services.GlobalVariables.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final StatisticRepository statisticRepository;
    private final SiteRepository siteRepository;
    private final StatisticMapper statisticMapper;

    public StatisticsResponse getTotalStatistic() {
        TotalStatistics totalStatistics = readTotalStatistics();
        List<DetailedStatisticsItem> detailedStatisticsItems = readDetailStatistic();

        AtomicBoolean result = new AtomicBoolean(true);
        detailedStatisticsItems.stream().filter(Objects::isNull)
                .findAny().ifPresent(item -> result.set(false));

        StatisticsData statisticsData = new StatisticsData(totalStatistics, detailedStatisticsItems);
        return new StatisticsResponse(result.get(), statisticsData);
    }

    @Transactional
    public TotalStatistics readTotalStatistics() {
        if (INDEXING_STARTED.get() || LEMMA_CREATING_STARTED.get() || INDEX_CREATING_STARTED.get()) {
            return ifWorking();
        } else {
            return ifFinished();
        }
    }

    public List<DetailedStatisticsItem> readDetailStatistic() {
        List<Site> all = siteRepository.findAll();
        return all.stream()
                .map(statisticRepository::readDetailInformation)
                .map(statisticMapper::mapToStatisticItem)
                .toList();
    }


    private TotalStatistics ifFinished() {
        List<Statistic> allStatistics = statisticRepository.findAll();
        return createTotalStatistic(allStatistics);
    }

    private TotalStatistics ifWorking() {
        List<Site> siteList = siteRepository.findAll();

        List<Statistic> allStatistics = siteList.stream()
                .map(statisticRepository::readStatisticBySiteId)
                .toList();
        return createTotalStatistic(allStatistics);
    }

    private TotalStatistics createTotalStatistic(List<Statistic> allStatistics) {
        long sitesCount = 0;
        long pagesCount = 0;
        long lemmasCount = 0;

        for (Statistic stat : allStatistics) {
            sitesCount++;
            pagesCount += stat.getCountOfPages();
            lemmasCount += stat.getCountOfLemmas();
        }
        lemmasCount += COUNT_OF_LEMMAS.get();

        return new TotalStatistics(sitesCount, pagesCount, lemmasCount, INDEXING_STARTED.get());
    }


}
