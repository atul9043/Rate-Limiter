package atul.bucket.dto;

public class RateLimitResult {

    private boolean allowed;
    private long tokens;

    public RateLimitResult(boolean allowed, long tokens){

        this.allowed = allowed;
        this.tokens = tokens;
    }

    public boolean isAllowed(){
        return allowed;
    }

    public long tokensLeft(){
        return tokens;
    }

}
