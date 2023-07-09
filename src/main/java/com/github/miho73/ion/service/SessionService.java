package com.github.miho73.ion.service;

import com.github.miho73.ion.exceptions.IonException;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SessionService {
    public static int USER_PRIVILEGE = 1;
    public static int ROOT_PRIVILEGE = 3;

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
        Object login = session.getAttribute("login");
        if(login == null) return false;
        return (boolean)session.getAttribute("login");
    }

    /**
     * check if user has sufficient privilege
     * @param session session of user to check
     * @param privilege privilege that expect to have
     * @return true when user has sufficient privilege
     */
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

    public int getUid(HttpSession session) throws IonException {
        if(session == null) throw new IonException();
        Object uid = session.getAttribute("uid");
        if(uid == null) throw new IonException();
        return (int)uid;
    }

    public String getName(HttpSession session) {
        return session.getAttribute("name").toString();
    }

    public Object getId(HttpSession session) {
        return session.getAttribute("id").toString();
    }
}
