package com.crm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.BusinessException;
import com.crm.entity.Region;
import com.crm.entity.SysConfig;
import com.crm.mapper.RegionMapper;
import com.crm.mapper.SysConfigMapper;
import com.crm.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysConfigService {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Autowired
    private RegionMapper regionMapper;

    // ==================== 系统参数 ====================

    public List<SysConfig> listConfigs() {
        return sysConfigMapper.selectList(null);
    }

    public SysConfig getByKey(String key) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, key);
        return sysConfigMapper.selectOne(wrapper);
    }

    @Transactional
    public boolean addConfig(SysConfig config) {
        ValidationUtil.requireNotBlank(config.getConfigKey(), "参数键");
        // 去重
        SysConfig existing = getByKey(config.getConfigKey());
        if (existing != null) {
            throw new BusinessException("参数键 '" + config.getConfigKey() + "' 已存在");
        }
        return sysConfigMapper.insert(config) > 0;
    }

    @Transactional
    public boolean updateConfig(SysConfig config) {
        ValidationUtil.requireNotBlank(config.getConfigKey(), "参数键");
        return sysConfigMapper.updateById(config) > 0;
    }

    @Transactional
    public boolean deleteConfig(Integer id) {
        return sysConfigMapper.deleteById(id) > 0;
    }

    // ==================== 区域管理 ====================

    public List<Region> listRegions() {
        return regionMapper.selectList(null);
    }

    /** 构建树形结构 */
    public List<Map<String, Object>> regionTree() {
        List<Region> all = regionMapper.selectList(null);
        Map<Integer, Map<String, Object>> nodeMap = new HashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();

        // 第一遍：创建所有节点
        for (Region r : all) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", r.getId());
            node.put("name", r.getName());
            node.put("level", r.getLevel());
            node.put("parentId", r.getParentId());
            node.put("children", new ArrayList<Map<String, Object>>());
            nodeMap.put(r.getId(), node);
        }

        // 第二遍：建立父子关系
        for (Region r : all) {
            Map<String, Object> node = nodeMap.get(r.getId());
            if (r.getParentId() == null || r.getParentId() == 0) {
                roots.add(node);
            } else {
                Map<String, Object> parent = nodeMap.get(r.getParentId());
                if (parent != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                    children.add(node);
                }
            }
        }
        return roots;
    }

    @Transactional
    public boolean addRegion(Region region) {
        ValidationUtil.requireNotBlank(region.getName(), "区域名称");
        if (region.getLevel() == null) region.setLevel(1);
        if (region.getParentId() == null) region.setParentId(0);
        return regionMapper.insert(region) > 0;
    }

    @Transactional
    public boolean updateRegion(Region region) {
        return regionMapper.updateById(region) > 0;
    }

    @Transactional
    public boolean deleteRegion(Integer id) {
        // 删除前检查是否有子节点
        long children = regionMapper.selectCount(
                new LambdaQueryWrapper<Region>().eq(Region::getParentId, id));
        if (children > 0) {
            throw new BusinessException("该区域下有子区域，请先删除子区域");
        }
        return regionMapper.deleteById(id) > 0;
    }
}
