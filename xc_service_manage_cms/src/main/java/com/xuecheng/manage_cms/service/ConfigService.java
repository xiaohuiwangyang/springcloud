package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class ConfigService {
    @Autowired
    private CmsConfigRepository cmsConfigRepository;

    /**
     * 根据id查询
     * @param id
     * @return
     */
    public CmsConfig findById(String id){
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if(optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
