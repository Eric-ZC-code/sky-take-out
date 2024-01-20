package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        // 1. 获取日期列表
        List<LocalDate> list = getDateList(begin, end);
        // 2. 查询每日营业额
        List<Double> result = new ArrayList<>();
        Double turnover;
        LocalDateTime dayBegin;
        LocalDateTime dayEnd;
        if (list != null && list.size() > 0) {
            dayBegin = LocalDateTime.of(list.get(0), LocalTime.MIN);  // 知识点2和3
            dayEnd = LocalDateTime.of(list.get(0), LocalTime.MAX);  // 知识点2和3
        } else {
            return new TurnoverReportVO();
        }
        for (LocalDate localDate : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("status", Orders.COMPLETED);
            map.put("begin", dayBegin);
            map.put("end", dayEnd);
            turnover = orderMapper.sumByMap(map);  // 知识点4
            result.add(turnover == null ? 0 : turnover);

            dayBegin = dayBegin.plusDays(1);
            dayEnd = dayEnd.plusDays(1);
        }
        // 3. 返回
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(StringUtils.join(list, ","));
        turnoverReportVO.setTurnoverList(StringUtils.join(result, ","));
        return turnoverReportVO;
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        // 1. 获取日期列表
        List<LocalDate> dateList = getDateList(begin, end);
        // 2. 获取用户数量列表
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        LocalDateTime dayBegin;
        LocalDateTime dayEnd;

        if (dateList != null && dateList.size() > 0) {
            dayBegin = LocalDateTime.of(dateList.get(0), LocalTime.MIN);
            dayEnd = LocalDateTime.of(dateList.get(0), LocalTime.MAX);
        } else {
            return new UserReportVO();
        }
        Integer totalUser;
        Integer newUser;
        for (LocalDate localDate : dateList) {
            Map<String, Object> map = new HashMap<>();
            map.put("end", dayEnd);
            totalUser = userMapper.countByMap(map);
            totalUserList.add(totalUser == null ? 0 : totalUser);
            map.put("begin", dayBegin);
            newUser = userMapper.countByMap(map);
            newUserList.add(newUser == null ? 0 : newUser);

            dayBegin = dayBegin.plusDays(1);
            dayEnd = dayEnd.plusDays(1);
        }
        // 3. 返回
        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(StringUtils.join(dateList, ","));
        userReportVO.setNewUserList(StringUtils.join(newUserList, ","));
        userReportVO.setTotalUserList(StringUtils.join(totalUserList, ","));
        return userReportVO;
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        OrderReportVO orderReportVO = new OrderReportVO();
        // 1. 日期列表
        List<LocalDate> dateList = getDateList(begin, end);
        if (dateList == null) {
            return orderReportVO;
        }
        // 2. 订单数列表
        List<Integer> totalOrderList = new ArrayList<>();
        // 3. 有效订单数列表
        List<Integer> validOrderList = new ArrayList<>();
        // 4. 订单总数
        Integer totalOrderCount = 0;
        // 5. 有效订单总数
        Integer validOrderCount = 0;
        for (LocalDate localDate : dateList) {
            Map map = new HashMap();
            map.put("begin", LocalDateTime.of(localDate, LocalTime.MIN));
            map.put("end", LocalDateTime.of(localDate, LocalTime.MAX));
            Integer total = orderMapper.countByMap(map);
            total = total == null ? 0 : total;
            map.put("status", Orders.COMPLETED);
            Integer valid = orderMapper.countByMap(map);
            valid = valid == null ? 0 : valid;
            totalOrderList.add(total);
            validOrderList.add(valid);
            totalOrderCount += total;
            validOrderCount += valid;
        }
        // 6. 订单完成率
        Double completionR = 0.0;
        if (totalOrderCount != null) {
            completionR = validOrderCount * 1.0 / totalOrderCount;
        }

        orderReportVO.setDateList(StringUtils.join(dateList, ","));
        orderReportVO.setOrderCountList(StringUtils.join(totalOrderList, ","));
        orderReportVO.setValidOrderCountList(StringUtils.join(validOrderList, ","));
        orderReportVO.setTotalOrderCount(totalOrderCount);
        orderReportVO.setValidOrderCount(validOrderCount);
        orderReportVO.setOrderCompletionRate(completionR);

        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        List<GoodsSalesDTO> goodsSalesDTOS = orderMapper.countSaleTop10(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        if (goodsSalesDTOS == null) {
            return new SalesTop10ReportVO();
        }

//        List<String> nameList = new ArrayList<>();
//        List<Integer> numberList = new ArrayList<>();
//        for (GoodsSalesDTO goodsSalesDTO : goodsSalesDTOS) {
//            nameList.add(goodsSalesDTO.getName());
//            numberList.add(goodsSalesDTO.getNumber());
//        }
        // ==========注意这里的写法==========
        List<String> nameList = goodsSalesDTOS.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = goodsSalesDTOS.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        // ==========注意上面的写法==========

        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();
        salesTop10ReportVO.setNameList(StringUtils.join(nameList, ","));
        salesTop10ReportVO.setNumberList(StringUtils.join(numberList, ","));
        return salesTop10ReportVO;
    }

    private List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> list = new ArrayList<>();
        while (begin.compareTo(end) <= 0) {  // 知识点1
            list.add(begin);
            begin = begin.plusDays(1);
        }
        return list;
    }
}
