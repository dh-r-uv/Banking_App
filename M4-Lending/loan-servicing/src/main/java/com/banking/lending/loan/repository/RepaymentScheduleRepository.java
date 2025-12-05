package com.banking.lending.loan.repository;

import com.banking.lending.loan.model.RepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByLoanIdAndPaidFalseOrderByDueDateAsc(Long loanId);
    List<RepaymentSchedule> findByLoanId(Long loanId);
}
