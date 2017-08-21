package org.mt.lasertarget;

public class TargetException extends Exception
{

    private static final long serialVersionUID = -3824492020568388008L;

    public TargetException(String message)
    {
        super(message);
    }

    public TargetException()
    {
    }

    public TargetException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}
