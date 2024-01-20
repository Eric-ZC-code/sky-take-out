package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    private WorkspaceService workspaceService;

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

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //1.查询数据库，获取营业数据----查询最近30天运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2.通过POI将数据写入到Excel文件中
        //获得这个类对象，获得类加载器，从类路径下读取资源返回一个输入流对象
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel=new XSSFWorkbook(in);
            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间:"+dateBegin+"至"+dateEnd);
            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row= sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());


            //填充明细数据
            for(int i=0;i<30;i++){
                LocalDate date =dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3.通过输出流将Excel下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
