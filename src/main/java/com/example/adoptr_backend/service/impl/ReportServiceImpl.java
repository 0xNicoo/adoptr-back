package com.example.adoptr_backend.service.impl;

import com.example.adoptr_backend.model.ReportModelType;
import com.example.adoptr_backend.service.ReportService;
import com.example.adoptr_backend.service.dto.request.ReportDTOin;
import com.example.adoptr_backend.service.impl.report.ReportStrategy;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private List<ReportStrategy> reportStrategies;

    private Map<ReportModelType, ReportStrategy> reportStrategyMap;

    public ReportServiceImpl(List<ReportStrategy> reportStrategies){
        this.reportStrategies = reportStrategies;
    }

    @PostConstruct
    public void setStrategyMap(){
        this.reportStrategyMap = new HashMap<>();
        reportStrategies.forEach(reportStrategy -> reportStrategyMap.put(reportStrategy.getType(), reportStrategy));
    }

    @Override
    public <T> List<T> getReports(ReportModelType reportModelType) {
        return null;
    }

    @Override
    public void report(ReportModelType reportModelType, ReportDTOin dto) {
        reportStrategyMap.get(reportModelType).report(dto);
    }
}
