package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.entity.ExportHistory;
import com.crm.mapper.ExportHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExportHistoryService {

    @Autowired
    private ExportHistoryMapper exportHistoryMapper;

    @Transactional
    public void save(ExportHistory history) {
        exportHistoryMapper.insert(history);
    }

    @Transactional(readOnly = true)
    public ExportHistory getById(Integer id) {
        return exportHistoryMapper.selectById(id);
    }

    @Transactional(readOnly = true)
    public IPage<ExportHistory> list(Integer page, Integer size) {
        LambdaQueryWrapper<ExportHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ExportHistory::getCreateTime);
        return exportHistoryMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
