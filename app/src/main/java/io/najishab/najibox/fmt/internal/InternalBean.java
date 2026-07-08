package io.najishab.najibox.fmt.internal;

import io.najishab.najibox.fmt.AbstractBean;

public abstract class InternalBean extends AbstractBean {

    @Override
    public String displayAddress() {
        return "";
    }

    @Override
    public boolean canICMPing() {
        return false;
    }

    @Override
    public boolean canTCPing() {
        return false;
    }

    @Override
    public boolean canMapping() {
        return false;
    }
}
