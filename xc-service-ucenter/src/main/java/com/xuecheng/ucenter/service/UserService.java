package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    XcUserRepository xcUserRepository;
    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;

    @Autowired
    XcMenuMapper xcMenuMapper;
    public XcUserExt getUserExt(String username){
        XcUserExt xcUserExt = new XcUserExt();

        XcUser xcUser = findXcUser(username);
        BeanUtils.copyProperties(xcUser,xcUserExt);
        String id = xcUser.getId();
        XcCompanyUser xcCompanyUser = findXcCompanyUser(id);
        List<XcMenu> menulist = findMenulistById(id);
        if (xcCompanyUser!=null){
            xcUserExt.setCompanyId(xcCompanyUser.getCompanyId());
        }
        if (menulist!=null && menulist.size()>0){
            xcUserExt.setPermissions(menulist);
        }
        return xcUserExt;
    }
    private XcUser findXcUser(String username){
        XcUser xcUser = xcUserRepository.findXcUserByUsername(username);
        return xcUser;
    }

    private XcCompanyUser findXcCompanyUser(String userId){
        XcCompanyUser xcCompanyUser= xcCompanyUserRepository.findXcCompanyUserByUserId(userId);
        return xcCompanyUser;
    }

    private List<XcMenu> findMenulistById(String id){
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(id);
        return xcMenus;
    }

}
