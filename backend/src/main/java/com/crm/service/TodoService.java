package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.entity.Todo;
import com.crm.mapper.TodoMapper;
import com.crm.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TodoService {

    @Autowired
    private TodoMapper todoMapper;

    @Transactional(readOnly = true)
    public IPage<Todo> search(Integer userId, String status, Integer page, Integer size) {
        LambdaQueryWrapper<Todo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Todo::getUserId, userId);
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Todo::getStatus, status);
        }
        wrapper.orderByDesc(Todo::getCreateTime);
        return todoMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Transactional
    public boolean add(Todo todo) {
        ValidationUtil.requireNotBlank(todo.getTitle(), "待办标题");
        if (todo.getPriority() == null || todo.getPriority().trim().isEmpty()) {
            todo.setPriority("中");
        }
        todo.setStatus("pending");
        todo.setCreateTime(LocalDateTime.now());
        return todoMapper.insert(todo) > 0;
    }

    @Transactional
    public boolean update(Todo todo) {
        ValidationUtil.requireNotBlank(todo.getTitle(), "待办标题");
        return todoMapper.updateById(todo) > 0;
    }

    @Transactional
    public boolean markDone(Integer id, Integer userId) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null || !todo.getUserId().equals(userId)) {
            throw new BusinessException("待办不存在或无权操作");
        }
        todo.setStatus("done");
        return todoMapper.updateById(todo) > 0;
    }

    @Transactional
    public boolean delete(Integer id, Integer userId) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null || !todo.getUserId().equals(userId)) {
            throw new BusinessException("待办不存在或无权操作");
        }
        return todoMapper.deleteById(id) > 0;
    }
}
