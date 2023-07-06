package com.github.miho73.ion.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
    public enum PRIVILEGES {
        USER,
        ROOT
    }

    public int privilegeOf(boolean user, boolean root) {
        int priv = 0;
        if(user) priv += 1;
        if(root) priv += 2;
        return priv;
    }

    public boolean isLoggedIn(HttpSession session) {
        if(session == null) return false;
        return (boolean)session.getAttribute("login");
    }

    public boolean checkPrivilege(HttpSession session, int privilege) {
        if(session == null) return false;
        Integer sp = (Integer)session.getAttribute("priv");
        if(sp == null) return false;
        boolean flag = true;

        do {
            boolean i = sp % 2 == 1;
            boolean u = privilege % 2 == 1;
            flag = flag && (i || !u);
            sp /= 2;
            privilege /= 2;
        } while (sp != 0 || privilege != 0);
        return flag;
    }
}
