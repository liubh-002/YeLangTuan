package com.practice.mealplanner.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "week_bill")
public class WeekBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_cycle", length = 20)
    private String weekCycle;

    @Column(name = "month_salary", precision = 10, scale = 2)
    private BigDecimal monthSalary;

    @Column(name = "week_cost", precision = 10, scale = 2)
    private BigDecimal weekCost;

    @Column(name = "week_budget", precision = 10, scale = 2)
    private BigDecimal weekBudget;

    @Column(name = "over_flag")
    private Boolean overFlag = false;

    @Column(name = "record_date")
    private LocalDate recordDate;
}
