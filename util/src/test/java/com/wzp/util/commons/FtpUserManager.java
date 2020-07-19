package com.wzp.util.commons;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.*;

import java.util.ArrayList;
import java.util.List;

public class FtpUserManager extends AbstractUserManager {

    public FtpUserManager(String adminName, PasswordEncryptor passwordEncryptor) {
        super(adminName, passwordEncryptor);
    }

    @Override
    public User getUserByName(String username) {
        BaseUser user = new BaseUser();
        user.setName(username);
        user.setEnabled(true);
        user.setHomeDirectory(System.getProperty("java.io.tmpdir"));
        user.setPassword(username);
        user.setMaxIdleTime(0);

        List<Authority> authorities = new ArrayList<>();
        authorities.add(new WritePermission());
        authorities.add(new ConcurrentLoginPermission(0, 0));
        authorities.add(new TransferRatePermission(0, 0));
        user.setAuthorities(authorities);
        return user;
    }

    @Override
    public String[] getAllUserNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(String username) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean doesExist(String username) {
        return false;
    }

    @Override
    public User authenticate(Authentication authentication) {
        if (authentication instanceof UsernamePasswordAuthentication) {
            UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;
            return getUserByName(upauth.getUsername());
        } else if (authentication instanceof AnonymousAuthentication) {
            return getUserByName("anonymous");
        } else {
            throw new IllegalArgumentException(
                    "Authentication not supported by this user manager");
        }
    }
}