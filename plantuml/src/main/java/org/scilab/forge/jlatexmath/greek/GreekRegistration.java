package org.scilab.forge.jlatexmath.greek;

import org.scilab.forge.jlatexmath.AlphabetRegistration;
import org.scilab.forge.jlatexmath.AlphabetRegistrationException;

public class GreekRegistration implements AlphabetRegistration
{
    @Override
    public Character.UnicodeBlock[] getUnicodeBlock()
    {
        return new Character.UnicodeBlock[0];
    }

    @Override
    public Object getPackage() throws AlphabetRegistrationException
    {
        return null;
    }

    @Override
    public String getTeXFontFileName()
    {
        return null;
    }
}
