package com.rsasms;

public class RSA
{
	final static String TAG = "RSASMS RSA";
	
    public BigInteger n, e, d;
    public int BIT_SIZE, GROUP_SIZE;
    //private BigInteger d;

    public RSA()
    {
    	configure(1024);
    }
    
    public RSA(int bitsize)
    {
    	configure(bitsize);
    	keyRequest();
    }

    public void configure(int bitsize){
    	BIT_SIZE = bitsize;
    	GROUP_SIZE = 3; // percentage of bit encryption size for max size equation can handle
    }
    
    public void keyRequest(){
    	
        if(BIT_SIZE == 0){return;}
       //Log.w(TAG,BIT_SIZE +" bit RSA Encryption...");
        SecureRandom r = new SecureRandom();
       //Log.w(TAG,"SecureRandom created...");
        BigInteger p = new BigInteger(BIT_SIZE / 2, 100, r);
       //Log.w(TAG,"p created...");
        BigInteger q = new BigInteger(BIT_SIZE / 2, 100, r);
       //Log.w(TAG,"q created...");
        n = p.multiply(q);
       //Log.w(TAG,"n (Shared Key) created...");

        BigInteger m = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        e = new BigInteger("3"); // 3, 17 work (17 is slow), but 5 seems to not work.
        while(m.gcd(e).intValue() > 1) e = e.add(new BigInteger("2"));
       //Log.w(TAG,"e (Public Key) created...");

        d = e.modInverse(m);
       //Log.w(TAG,"d (Private Key) created...");
    }

    public BigInteger encrypt(BigInteger message)
    {
            return message.modPow(e, n);
            // message^e mod n
    }
    
    public BigInteger encryptStr(String message)
   {
    	
            String numericString = "";
            String tempStr = "";
            int encLength = message.length();
            char ch;
            int intedch;
            for(int i = 0;i < encLength;i++){
                    ch = message.charAt(i);
                    intedch = ch;
                    tempStr = "";
                    tempStr += intedch;
                    while(tempStr.length() < 3){tempStr = "0" + tempStr;}
                    numericString += tempStr;
            }
           //Log.w(TAG,"Message("+message.length()+") Ready To be Encrypted String(" +numericString.length() +"): " + numericString);
            BigInteger encryptedBIG = new BigInteger(numericString);
            return encryptedBIG.modPow(e, n);
    }
    
    public String encryptSafe(String message)
    {
    	// Not adjustable at the moment.
    	int pSize = BIT_SIZE * GROUP_SIZE;
    	pSize = pSize/100;
     //Log.w(TAG,"Original bitSize("+BIT_SIZE+") groupSize("+GROUP_SIZE+") pSize("+pSize+")");
     	if(pSize < 10){pSize = 10;}
     	
     	String eData = "";
    	for(int a = 0; a < message.length(); a += pSize){
    		int t = a+pSize;
    		if( t > message.length() ){
    			t = message.length(); 
    		}
    		
    		String sS = message.substring(a, t);
    		
    		eData += encryptStr(sS) + "\n";
    	}
    	return eData;
     }

    public BigInteger decrypt(BigInteger message)
    {
            return message.modPow(d, n);
    }
    public String decryptToStr(BigInteger Emessage)
    {
    	//Log.w(TAG,"decryptToStr length(" +Emessage.toString().length() + ") " );
    		
            BigInteger Dmessage = Emessage.modPow(d, n);

            String numberString = Dmessage.toString();
           //Log.w(TAG,"Number String length(" + numberString.length() + ") /3(" + (numberString.length()/3) + ") %3(" + (3-(numberString.length()%3)) +")" );
           //Log.w(TAG,"Number String: " + numberString);
            
            String tempStr = "", decryptedStr = "";
            char ch,chedint;
            int tempInt;
            BigInteger tempBIG;
            for(int i = (3-(numberString.length()%3)); i > 0 && i < 3; i--){
            //Log.w(TAG,"Appending 0 to numberString");
            	numberString = "0" + "" + numberString;
            }
            
            for(int i = 0;i+2 < numberString.length();i += 3){
            	
            	tempStr = numberString.substring(i, i+3);
            	tempBIG = new BigInteger(tempStr);
            	//tempBIG = new BigInteger(new String(numberString.substring(i, i+2)) );
                //tempInt = tempBIG.intValue();
                //chedint = (char) tempInt;
                decryptedStr += (char) tempBIG.intValue();
               //Log.w(TAG,"num("+tempStr+") char("+(char) tempBIG.intValue() +") int("+tempBIG.intValue()+")");

                //tempStr = "";
            }
            return decryptedStr;
    }
    
    public String decryptSafe(String eData) {
    //Log.w(TAG,"decryptSafe("+eData+")");
    	int nxtpos = 0;
    	String dData = "";
    	for(int b = 0; b < eData.length(); b=(eData.indexOf(10, b+1)) ){
    		nxtpos = eData.indexOf(10,b+1);
    		if( nxtpos < 0 ){
    			nxtpos = eData.length();
    		}
    		String c = eData.substring(b, nxtpos);
    		
    		c = c.trim();
    		
    		if( c.length() > 0 ){
    		//Log.w(TAG,"eData b("+b+") nxtpos("+nxtpos+") String(" + c +")");
    			try {
    				BigInteger bI = new BigInteger(c);
    				dData += decryptToStr(bI);
    			}
    			catch(NumberFormatException e){
    			//Log.w(TAG,"Caught NumberFormatException (Possibly user entered non-number data) " + e.getLocalizedMessage());
    				break;
    			}
    		}else{
    			break;
    		}
    		
    		if(nxtpos == eData.length() ){break;}
    	}
    	dData = dData.trim();
    	return dData;
    }
}


