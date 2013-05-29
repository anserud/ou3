
package ou3;


import java.util.HashSet;
import java.util.Stack;


// TODO: Auto-generated Javadoc
/**
 * The Class QueryMessage.
 */
public class QueryMessage extends Message
{
    
    /** The life length. */
    private static int  lifeLength;
    
    /** The expected. */
    private static int  expected;
    
    /** The first attempt. */
    private boolean     firstAttempt;
    
    /** The attempt time. */
    private int         attemptTime;
    
    /** The path. */
    private Stack<Node> path;
    
    /** The status. */
    private State       status;
    
    /** The target. */
    private EventID     target;
    
    /** The pay load. */
    private Event       payLoad;
    
    /** The success. */
    private static int success = 0;
    
    /** The total. */
    private static int total = 0;
    
    /**
     * Gets the succes.
     *
     * @return the succes
     */
    public static int getSucces()
    {
        return QueryMessage.success;
    }
    
    /**
     * Gets the total.
     *
     * @return the total
     */
    public static int getTotal()
    {
        return QueryMessage.total;
    }
    
    /**
     * The Enum State.
     */
    private enum State
    {
        
        /** The routing. */
        ROUTING, 
 /** The searching. */
 SEARCHING, 
 /** The backtracking. */
 BACKTRACKING;
    }
    
    /**
     * Instantiates a new query message.
     *
     * @param target the target
     * @param position the position
     */
    public QueryMessage( EventID target, Node position )
    {
        super( position );
        this.timeToLive = QueryMessage.lifeLength;
        this.path = new Stack<Node>();
        this.status = State.SEARCHING;
        this.target = target;
        this.firstAttempt = true;
        this.attemptTime = 0;
        
        QueryMessage.total++;
    }
    
    /* (non-Javadoc)
     * @see ou3.Message#choseTarget()
     */
    @Override
    protected Node choseTarget()
    {
        switch ( this.status ) {
        case SEARCHING:
            return this.findNewPath();
        case ROUTING:
            return this.currentPosition.getRoute( this.target ).getDirection();
        case BACKTRACKING:
            return this.path.peek();
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see ou3.Message#processMessage()
     */
    @Override
    public void processMessage()
    {
        switch ( this.status ) {
        case SEARCHING:
            if ( this.currentPosition.getRoutingTable().containsKey(
                    this.target ) )
            {
                this.status = State.ROUTING;
            }
            break;
        case ROUTING:
            Route r = this.currentPosition.getRoute( this.target );
            if ( r.getDistance() == 0 )
            {
                this.payLoad = this.currentPosition.getEvent( this.target );
                this.status = State.BACKTRACKING;
            }
            break;
        case BACKTRACKING:
            if ( this.path.empty() )
            {
                System.out.println( this.payLoad );
                QueryMessage.success++;
                this.timeToLive = 0;
                this.firstAttempt = false;
            }
            break;
        
        }
    }
    
    /* (non-Javadoc)
     * @see ou3.Message#stepUpdate()
     */
    @Override
    public boolean stepUpdate()
    {
        this.attemptTime++;
        if ( this.attemptTime >= QueryMessage.expected )
        {
            if ( this.firstAttempt )
            {
                this.restartQuery();
                this.firstAttempt = false;
                // System.out.print( " Restarting Query to: "+this.target+" 1\n"
                // );
                return true;
            } else
            {
                // System.out.print( " Destroying Query to: "+this.target+" 1\n"
                // );
                return false;
            }
        }
        if ( this.status == State.SEARCHING ) this.timeToLive--;
        if ( this.timeToLive <= 0 )
        {
            if ( this.firstAttempt )
            {
                this.restartQuery();
                this.firstAttempt = false;
                // System.out.print( " Restarting Query to: "+this.target+" 2\n"
                // );
            } else
            {
                // System.out.print( " Destroying Query to: "+this.target+" 2\n"
                // );
                return false;
            }
        }
        return true;
    }
    
    /**
     * Sets the life length.
     *
     * @param lifeLength the new life length
     */
    public static void setLifeLength( int lifeLength )
    {
        QueryMessage.lifeLength = lifeLength;
        
    }
    
    /* (non-Javadoc)
     * @see ou3.Message#sendingUpdate()
     */
    @Override
    protected void sendingUpdate()
    {
        switch ( this.status ) {
        case SEARCHING:
        case ROUTING:
            this.path.add( currentPosition );
            break;
        case BACKTRACKING:
            this.path.pop();
            break;
        }
        
    }
    
    /* (non-Javadoc)
     * @see ou3.Message#toString()
     */
    @Override
    public String toString()
    {
        return super.toString() + " / QueryMessage [ status=" + this.status
                + ", target=" + this.target + ", payLoad=" + this.payLoad
                + "] / ";
    }
    
    /**
     * Sets the expected.
     *
     * @param i the new expected
     */
    public static void setExpected( int i )
    {
        QueryMessage.expected = i;
        
    }
    
    /**
     * Restart query.
     */
    private void restartQuery()
    {
        this.attemptTime = 0;
        this.timeToLive = QueryMessage.lifeLength;
        this.currentPosition = this.path.firstElement();
        this.path = new Stack<Node>();
        this.status = State.SEARCHING;
        this.past = new HashSet<Node>(120);
        this.payLoad = null;
    }
    
}
