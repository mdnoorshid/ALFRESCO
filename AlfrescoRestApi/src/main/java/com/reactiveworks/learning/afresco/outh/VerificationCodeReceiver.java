package com.reactiveworks.learning.afresco.outh;

/**
 * Verfication Code Receiver
 * @author Md Noorshid
 *
 */
public interface VerificationCodeReceiver {
     
	/**
	 * Return the redirect URI
	 * @return
	 * @throws Exception 
	 */
	String getRedirectUri() throws Exception;
	
	/**
	 * Waits for verification code
	 */
	String waitForCode();
	
	/**
	 * Release any resource and stops any processes started
	 * @throws Exception 
	 */
	void stop() throws Exception;
	
	
}
